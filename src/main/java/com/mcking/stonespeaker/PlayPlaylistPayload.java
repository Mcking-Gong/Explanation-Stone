package com.mcking.stonespeaker;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 服务端 → 客户端：开始播放某个坐标讲解石的歌单。 */
public record PlayPlaylistPayload(BlockPos pos, String tracks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayPlaylistPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(StoneSpeaker.MODID, "play_playlist"));

    public static final StreamCodec<FriendlyByteBuf, PlayPlaylistPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PlayPlaylistPayload::pos,
                    ByteBufCodecs.STRING_UTF8, PlayPlaylistPayload::tracks,
                    PlayPlaylistPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
