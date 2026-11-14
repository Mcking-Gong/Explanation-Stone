package com.mcking.stonespeaker;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 讲解石保存插入的 U 盘歌单与模式。 */
public class SpeakerBlockEntity extends BlockEntity {
    private List<String> tracks = List.of();
    private String playMode = "list";
    private String actionMode = "toggle_switch";
    private String redstoneMode = "toggle_switch";
    private int range = 16;
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

    public int getRange() {
        return range;
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setSettings(List<String> newTracks, String playMode, String actionMode, String redstoneMode,
                            int range) {
        this.tracks = List.copyOf(newTracks);
        this.playMode = playMode;
        this.actionMode = actionMode;
        this.redstoneMode = redstoneMode;
        this.range = range;
        this.playing = false;
        this.paused = false;
        this.setChanged();
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Tracks", String.join("\n", this.tracks));
        tag.putString("PlayMode", this.playMode);
        tag.putString("ActionMode", this.actionMode);
        tag.putString("RedstoneMode", this.redstoneMode);
        tag.putInt("Range", this.range);
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
        this.range = tag.contains("Range") ? tag.getInt("Range") : 16;
    }
}
