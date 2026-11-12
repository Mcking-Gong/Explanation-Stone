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

/** U 盘歌单界面：勾选曲目、上下调整顺序、播放模式 / 右键模式 / 红石模式、打开文件夹、弹出保存。 */
public class UsbScreen extends Screen {
    private static final int VISIBLE_ROWS = 7;
    private static final String[] PLAY_MODES = {"list", "single", "shuffle"};
    private static final String[] ACTION_MODES = {"toggle_switch", "toggle_play", "next", "disabled"};

    private final ItemStack stack;
    private final List<String> rows = new ArrayList<>();
    private final Set<String> selected = new LinkedHashSet<>();
    private String playMode;
    private String actionMode;
    private String redstoneMode;
    private int scroll;

    public UsbScreen(ItemStack stack) {
        super(Component.translatable("screen.explainationstone.usb"));
        this.stack = stack;
        this.rows.addAll(UsbLibrary.listTracks());
        this.selected.addAll(ModComponents.getPlaylist(stack));
        this.playMode = ModComponents.getPlayMode(stack);
        this.actionMode = ModComponents.getActionMode(stack);
        this.redstoneMode = ModComponents.getRedstoneMode(stack);
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        this.clearWidgets();
        int left = this.width / 2 - 120;
        int top = 34;
        int end = Math.min(this.rows.size(), this.scroll + VISIBLE_ROWS);
        for (int i = this.scroll; i < end; i++) {
            int y = top + (i - this.scroll) * 20;
            String name = this.rows.get(i);
            final int index = i;
            this.addRenderableWidget(Button.builder(rowLabel(name), button -> {
                if (!this.selected.remove(name)) {
                    this.selected.add(name);
                }
                rebuild();
            }).bounds(left, y, 190, 18).build());
            this.addRenderableWidget(Button.builder(Component.literal("↑"), button -> move(index, -1))
                    .bounds(left + 196, y, 18, 18).build());
            this.addRenderableWidget(Button.builder(Component.literal("↓"), button -> move(index, 1))
                    .bounds(left + 218, y, 18, 18).build());
        }

        int bottom = this.height - 62;
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.explainationstone.usb.play_mode", modeLabel("play." + this.playMode)),
                button -> {
                    this.playMode = cycle(PLAY_MODES, this.playMode);
                    rebuild();
                }).bounds(left, bottom - 44, 118, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.explainationstone.usb.action_mode", modeLabel("action." + this.actionMode)),
                button -> {
                    this.actionMode = cycle(ACTION_MODES, this.actionMode);
                    rebuild();
                }).bounds(left + 124, bottom - 44, 118, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.explainationstone.usb.redstone_mode", modeLabel("action." + this.redstoneMode)),
                button -> {
                    this.redstoneMode = cycle(ACTION_MODES, this.redstoneMode);
                    rebuild();
                }).bounds(left, bottom - 20, 160, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.explainationstone.usb.folder"),
                button -> Util.getPlatform().openFile(UsbLibrary.musicDir().toFile()))
                .bounds(left + 166, bottom - 20, 72, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.explainationstone.usb.eject"), button -> this.onClose())
                .bounds(this.width / 2 - 40, bottom + 4, 80, 20).build());
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

    private static Component modeLabel(String key) {
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
        int maxScroll = Math.max(0, this.rows.size() - VISIBLE_ROWS);
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
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        if (this.rows.isEmpty()) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("screen.explainationstone.usb.empty"),
                    this.width / 2, 60, 0xFF8080);
        } else if (this.rows.size() > VISIBLE_ROWS) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("screen.explainationstone.usb.more", this.rows.size() - VISIBLE_ROWS),
                    this.width / 2, this.height - 86, 0xAAAAAA);
        }
    }

    @Override
    public void onClose() {
        PacketDistributor.sendToServer(new SetPlaylistPayload(String.join("\n", playlist()),
                this.playMode, this.actionMode, this.redstoneMode));
        super.onClose();
    }
}
