package com.mcking.stonespeaker;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 服务端 → 客户端：对某个讲解石执行播放控制。 */
public record PlaylistCommandPayload(BlockPos pos, int command) implements CustomPacketPayload {
    public static final int PLAY = 0;
    public static final int STOP = 1;
    public static final int TOGGLE_PAUSE = 2;
    public static final int NEXT = 3;

    public static final CustomPacketPayload.Type<PlaylistCommandPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(StoneSpeaker.MODID, "playlist_command"));

    public static final StreamCodec<FriendlyByteBuf, PlaylistCommandPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PlaylistCommandPayload::pos,
                    ByteBufCodecs.VAR_INT, PlaylistCommandPayload::command,
                    PlaylistCommandPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
