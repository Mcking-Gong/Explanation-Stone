package com.mcking.stonespeaker;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 服务端 → 客户端：停止某个坐标讲解石的播放。 */
public record StopPlaylistPayload(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StopPlaylistPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(StoneSpeaker.MODID, "stop_playlist"));

    public static final StreamCodec<FriendlyByteBuf, StopPlaylistPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, StopPlaylistPayload::pos,
                    StopPlaylistPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
