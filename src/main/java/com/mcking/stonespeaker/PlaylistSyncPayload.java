package com.mcking.stonespeaker;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 服务端 → 客户端：同步某个讲解石的歌单与播放模式（不开始播放）。 */
public record PlaylistSyncPayload(BlockPos pos, String tracks, String playMode)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlaylistSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(StoneSpeaker.MODID, "playlist_sync"));

    public static final StreamCodec<FriendlyByteBuf, PlaylistSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PlaylistSyncPayload::pos,
                    ByteBufCodecs.STRING_UTF8, PlaylistSyncPayload::tracks,
                    ByteBufCodecs.STRING_UTF8, PlaylistSyncPayload::playMode,
                    PlaylistSyncPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
