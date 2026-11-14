package com.mcking.stonespeaker;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** U 盘上存的数据：歌单、播放模式、右键模式、红石模式。 */
public final class ModComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(StoneSpeaker.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PLAYLIST =
            DATA_COMPONENTS.registerComponentType("playlist",
                    builder -> builder.persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PLAY_MODE =
            DATA_COMPONENTS.registerComponentType("play_mode",
                    builder -> builder.persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> ACTION_MODE =
            DATA_COMPONENTS.registerComponentType("action_mode",
                    builder -> builder.persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> REDSTONE_MODE =
            DATA_COMPONENTS.registerComponentType("redstone_mode",
                    builder -> builder.persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** 声音传播距离（格），越大传得越远。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RANGE =
            DATA_COMPONENTS.registerComponentType("range",
                    builder -> builder.persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final int DEFAULT_RANGE = 16;

    private ModComponents() {
    }

    public static List<String> getPlaylist(ItemStack stack) {
        String raw = stack.get(PLAYLIST);
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String part : raw.split("\n")) {
            if (!part.isBlank()) {
                out.add(part);
            }
        }
        return out;
    }

    public static void setPlaylist(ItemStack stack, List<String> tracks) {
        stack.set(PLAYLIST, String.join("\n", tracks));
    }

    public static String getPlayMode(ItemStack stack) {
        String value = stack.get(PLAY_MODE);
        return value == null || value.isEmpty() ? "list" : value;
    }

    public static String getActionMode(ItemStack stack) {
        String value = stack.get(ACTION_MODE);
        return value == null || value.isEmpty() ? "toggle_switch" : value;
    }

    public static String getRedstoneMode(ItemStack stack) {
        String value = stack.get(REDSTONE_MODE);
        return value == null || value.isEmpty() ? "toggle_switch" : value;
    }

    public static int getRange(ItemStack stack) {
        Integer value = stack.get(RANGE);
        return value == null || value <= 0 ? DEFAULT_RANGE : value;
    }

    public static void setSettings(ItemStack stack, String playMode, String actionMode, String redstoneMode, int range) {
        stack.set(PLAY_MODE, playMode);
        stack.set(ACTION_MODE, actionMode);
        stack.set(REDSTONE_MODE, redstoneMode);
        stack.set(RANGE, range);
    }
}
