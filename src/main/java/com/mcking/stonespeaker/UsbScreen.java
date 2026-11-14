package com.mcking.stonespeaker;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** U 盘界面：勾选/排序曲目，设置播放模式、传播距离、右键/红石模式。 */
public class UsbScreen extends Screen {
    private static final String[] PLAY_MODES = {"list", "single", "shuffle"};
    private static final String[] ACTION_MODES = {"toggle_switch", "toggle_play", "next", "disabled"};
    private static final int[] RANGES = {8, 16, 24, 32, 48, 64};

    private final ItemStack stack;
    private final List<String> rows = new ArrayList<>();
    private final Set<String> selected = new LinkedHashSet<>();
    private String playMode;
    private String actionMode;
    private String redstoneMode;
    private int range;
    private int scroll;

    public UsbScreen(ItemStack stack) {
        super(Component.translatable("screen.explainationstone.usb"));
        this.stack = stack;
        this.rows.addAll(UsbLibrary.listTracks());
        this.selected.addAll(ModComponents.getPlaylist(stack));
        this.playMode = ModComponents.getPlayMode(stack);
        this.actionMode = ModComponents.getActionMode(stack);
        this.redstoneMode = ModComponents.getRedstoneMode(stack);
        this.range = ModComponents.getRange(stack);
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        this.clearWidgets();
        int left = this.width / 2 - 121;
        int top = 28;
        int rowHeight = 18;
        int maxRows = Math.max(3, Math.min(8, (this.height - 120 - top) / rowHeight));
        int end = Math.min(this.rows.size(), this.scroll + maxRows);
        for (int i = this.scroll; i < end; i++) {
            int y = top + (i - this.scroll) * rowHeight;
            String name = this.rows.get(i);
            final int index = i;
            this.addRenderableWidget(Button.builder(rowLabel(name), button -> {
                if (!this.selected.remove(name)) {
                    this.selected.add(name);
                }
                rebuild();
            }).bounds(left, y, 190, 18).build());
            this.addRenderableWidget(Button.builder(Component.literal("↑"), button -> move(index, -1))
                    .bounds(left + 194, y, 18, 18).build());
            this.addRenderableWidget(Button.builder(Component.literal("↓"), button -> move(index, 1))
                    .bounds(left + 214, y, 18, 18).build());
        }

        int row1 = this.height - 64;
        int row2 = this.height - 42;
        int row3 = this.height - 20;
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.explainationstone.usb.play_mode", mode("play." + this.playMode)),
                button -> {
                    this.playMode = cycle(PLAY_MODES, this.playMode);
                    rebuild();
                }).bounds(left, row1, 118, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.explainationstone.usb.range", this.range),
                button -> {
                    this.range = cycleRange(this.range);
                    rebuild();
                }).bounds(left + 124, row1, 118, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.explainationstone.usb.action_mode", mode("action." + this.actionMode)),
                button -> {
                    this.actionMode = cycle(ACTION_MODES, this.actionMode);
                    rebuild();
                }).bounds(left, row2, 118, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.explainationstone.usb.redstone_mode", mode("action." + this.redstoneMode)),
                button -> {
                    this.redstoneMode = cycle(ACTION_MODES, this.redstoneMode);
                    rebuild();
                }).bounds(left + 124, row2, 118, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.explainationstone.usb.folder"),
                button -> Util.getPlatform().openFile(UsbLibrary.musicDir().toFile()))
                .bounds(left, row3, 118, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.explainationstone.usb.eject"), button -> this.onClose())
                .bounds(left + 124, row3, 118, 20).build());
    }

    private void move(int index, int delta) {
        int target = index + delta;
        if (target < 0 || target >= this.rows.size()) {
            return;
        }
        String value = this.rows.remove(index);
        this.rows.add(target, value);
        rebuild();
    }

    private static String cycle(String[] values, String current) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                return values[(i + 1) % values.length];
            }
        }
        return values[0];
    }

    private static int cycleRange(int current) {
        for (int i = 0; i < RANGES.length; i++) {
            if (RANGES[i] == current) {
                return RANGES[(i + 1) % RANGES.length];
            }
        }
        return RANGES[1];
    }

    private static Component mode(String key) {
        return Component.translatable("screen.explainationstone.usb.mode." + key);
    }

    private Component rowLabel(String name) {
        return Component.literal((this.selected.contains(name) ? "[x] " : "[ ] ") + name);
    }

    private List<String> playlist() {
        List<String> out = new ArrayList<>();
        for (String name : this.rows) {
            if (this.selected.contains(name)) {
                out.add(name);
            }
        }
        return out;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, this.rows.size() - Math.max(3, Math.min(8, (this.height - 120 - 28) / 18)));
        int next = Math.max(0, Math.min(maxScroll, this.scroll - (int) Math.signum(scrollY)));
        if (next != this.scroll) {
            this.scroll = next;
            rebuild();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        if (this.rows.isEmpty()) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("screen.explainationstone.usb.empty"),
                    this.width / 2, 40, 0xFF8080);
        }
    }

    @Override
    public void onClose() {
        PacketDistributor.sendToServer(new SetPlaylistPayload(String.join("\n", playlist()),
                this.playMode, this.actionMode, this.redstoneMode, this.range));
        super.onClose();
    }
}
