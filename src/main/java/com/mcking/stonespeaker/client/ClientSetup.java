package com.mcking.stonespeaker.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mcking.stonespeaker.StoneSpeaker;
import com.mcking.stonespeaker.UsbLibrary;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** 首次启动时把 mod 内置的音乐复制到 config/explainationstone/music/，让默认曲目也能被 U 盘系统播放。 */
@EventBusSubscriber(modid = StoneSpeaker.MODID, value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Path dir = UsbLibrary.musicDir();
            Path target = dir.resolve(StoneSpeaker.DEFAULT_TRACK);
            if (Files.exists(target)) {
                return;
            }
            try (InputStream in = StoneSpeaker.class.getResourceAsStream(
                    "/assets/explainationstone/sounds/music.ogg")) {
                if (in != null) {
                    Files.createDirectories(dir);
                    Files.copy(in, target);
                }
            } catch (IOException ignored) {
            }
        });
    }
}
