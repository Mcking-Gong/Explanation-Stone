package com.mcking.stonespeaker;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** U 盘歌单编辑界面：列出 config/explainationstone/music 里的 .ogg，点击加入/移出歌单。 */
public class UsbScreen extends Screen {
    private static final int MAX_ROWS = 10;

    private final ItemStack stack;
    private final Set<String> selected = new LinkedHashSet<>();
    private final List<String> available;

    public UsbScreen(ItemStack stack) {
        super(Component.translatable("screen.explainationstone.usb"));
        this.stack = stack;
        this.selected.addAll(ModComponents.getPlaylist(stack));
        this.available = UsbLibrary.listTracks();
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 120;
        int top = 45;
        int rows = Math.min(this.available.size(), MAX_ROWS);
        for (int i = 0; i < rows; i++) {
            String name = this.available.get(i);
            this.addRenderableWidget(Button.builder(label(name), button -> {
                toggle(name);
                button.setMessage(label(name));
            }).bounds(left, top + i * 22, 240, 20).build());
        }
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.explainationstone.usb.folder"),
                button -> Util.getPlatform().openFile(UsbLibrary.musicDir().toFile()))
                .bounds(this.width / 2 - 50, this.height - 60, 100, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 35, 100, 20).build());
    }

    private Component label(String name) {
        return Component.literal((this.selected.contains(name) ? "[x] " : "[ ] ") + name);
    }

    private void toggle(String name) {
        if (!this.selected.remove(name)) {
            this.selected.add(name);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 18, 0xFFFFFF);
        if (this.available.isEmpty()) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("screen.explainationstone.usb.empty"),
                    this.width / 2, 60, 0xFF8080);
        } else if (this.available.size() > MAX_ROWS) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("screen.explainationstone.usb.more", this.available.size() - MAX_ROWS),
                    this.width / 2, this.height - 55, 0xAAAAAA);
        }
    }

    @Override
    public void onClose() {
        PacketDistributor.sendToServer(new SetPlaylistPayload(String.join("\n", this.selected)));
        super.onClose();
    }
}
