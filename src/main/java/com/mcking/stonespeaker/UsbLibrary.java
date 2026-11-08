package com.mcking.stonespeaker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import net.neoforged.fml.loading.FMLPaths;

/** 扫描 config/stonespeaker/music/*.ogg 曲库。 */
public final class UsbLibrary {
    private UsbLibrary() {
    }

    public static Path musicDir() {
        return FMLPaths.CONFIGDIR.get().resolve("stonespeaker").resolve("music");
    }

    /** 返回曲库里的文件名（含 .ogg），按名字排序。 */
    public static List<String> listTracks() {
        Path dir = musicDir();
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {
        }
        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(name -> name.toLowerCase().endsWith(".ogg"))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    public static Path trackPath(String name) {
        return musicDir().resolve(name);
    }
}
