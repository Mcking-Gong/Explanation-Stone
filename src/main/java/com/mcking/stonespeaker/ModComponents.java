package com.mcking.stonespeaker;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** U 盘上存的数据：歌单（用 \n 连接的文件名列表）。 */
public final class ModComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(StoneSpeaker.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PLAYLIST =
            DATA_COMPONENTS.registerComponentType("playlist",
                    builder -> builder.persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8));

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
}
