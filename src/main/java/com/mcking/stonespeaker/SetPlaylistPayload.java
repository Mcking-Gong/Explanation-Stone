package com.mcking.stonespeaker;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 客户端 → 服务端：保存 U 盘歌单。 */
public record SetPlaylistPayload(String tracks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetPlaylistPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(StoneSpeaker.MODID, "set_playlist"));

    public static final StreamCodec<FriendlyByteBuf, SetPlaylistPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, SetPlaylistPayload::tracks,
                    SetPlaylistPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
