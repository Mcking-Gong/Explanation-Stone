package com.mcking.stonespeaker;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 讲解石保存插入的 U 盘歌单。 */
public class SpeakerBlockEntity extends BlockEntity {
    private static final String KEY = "Tracks";
    private List<String> tracks = List.of();

    public SpeakerBlockEntity(BlockPos pos, BlockState state) {
        super(StoneSpeaker.SPEAKER_BE.get(), pos, state);
    }

    public List<String> getTracks() {
        return tracks;
    }

    public void setTracks(List<String> newTracks) {
        this.tracks = List.copyOf(newTracks);
        this.setChanged();
    }

    public boolean hasTracks() {
        return !this.tracks.isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(KEY, String.join("\n", this.tracks));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        String raw = tag.getString(KEY);
        List<String> loaded = new ArrayList<>();
        if (!raw.isEmpty()) {
            for (String part : raw.split("\n")) {
                if (!part.isBlank()) {
                    loaded.add(part);
                }
            }
        }
        this.tracks = List.copyOf(loaded);
    }
}
