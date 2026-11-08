package com.mcking.stonespeaker.client;

import com.mcking.stonespeaker.UsbScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/** 客户端专用入口，避免服务端加载 Screen 类。 */
public final class ClientHooks {
    private ClientHooks() {
    }

    public static void openUsbScreen(ItemStack stack) {
        Minecraft.getInstance().setScreen(new UsbScreen(stack));
    }
}
