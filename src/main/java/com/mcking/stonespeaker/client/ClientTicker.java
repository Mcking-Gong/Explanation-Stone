package com.mcking.stonespeaker.client;

import com.mcking.stonespeaker.StoneSpeaker;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** 每 tick 更新播放实例的音量与切歌。 */
@EventBusSubscriber(modid = StoneSpeaker.MODID, value = Dist.CLIENT)
public final class ClientTicker {
    private ClientTicker() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientPlayback.tick();
    }
}
