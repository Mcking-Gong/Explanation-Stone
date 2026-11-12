package com.mcking.stonespeaker;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 讲解石保存插入的 U 盘歌单与模式。 */
public class SpeakerBlockEntity extends BlockEntity {
    private List<String> tracks = List.of();
    private String playMode = "list";
    private String actionMode = "toggle_switch";
    private String redstoneMode = "toggle_switch";
    private boolean playing = false;
    private boolean paused = false;

    public SpeakerBlockEntity(BlockPos pos, BlockState state) {
        super(StoneSpeaker.SPEAKER_BE.get(), pos, state);
    }

    public List<String> getTracks() {
        return tracks;
    }

    public String getPlayMode() {
        return playMode;
    }

    public String getActionMode() {
        return actionMode;
    }

    public String getRedstoneMode() {
        return redstoneMode;
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setSettings(List<String> newTracks, String playMode, String actionMode, String redstoneMode) {
        this.tracks = List.copyOf(newTracks);
        this.playMode = playMode;
        this.actionMode = actionMode;
        this.redstoneMode = redstoneMode;
        this.playing = false;
        this.paused = false;
        this.setChanged();
        sync();
    }

    public void setPlaying(boolean value) {
        this.playing = value;
        if (!value) {
            this.paused = false;
        }
    }

    public void setPaused(boolean value) {
        this.paused = value;
    }

    public boolean hasTracks() {
        return !this.tracks.isEmpty();
    }

    private void sync() {
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Tracks", String.join("\n", this.tracks));
        tag.putString("PlayMode", this.playMode);
        tag.putString("ActionMode", this.actionMode);
        tag.putString("RedstoneMode", this.redstoneMode);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        String raw = tag.getString("Tracks");
        List<String> loaded = new ArrayList<>();
        if (!raw.isEmpty()) {
            for (String part : raw.split("\n")) {
                if (!part.isBlank()) {
                    loaded.add(part);
                }
            }
        }
        this.tracks = List.copyOf(loaded);
        this.playMode = tag.contains("PlayMode") ? tag.getString("PlayMode") : "list";
        this.actionMode = tag.contains("ActionMode") ? tag.getString("ActionMode") : "toggle_switch";
        this.redstoneMode = tag.contains("RedstoneMode") ? tag.getString("RedstoneMode") : "toggle_switch";
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
