package com.mcking.stonespeaker.client;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import com.mcking.stonespeaker.PlaylistCommandPayload;
import com.mcking.stonespeaker.UsbLibrary;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * 客户端播放：按方块坐标维护歌单与播放实例，支持列表循环 / 单曲循环 / 随机、
 * 暂停恢复、切歌与距离衰减。
 */
public final class ClientPlayback {
    private static final Map<BlockPos, Config> CONFIGS = new ConcurrentHashMap<>();
    private static final Map<BlockPos, Playing> PLAYING = new ConcurrentHashMap<>();
    private static final double RANGE = 64.0;
    private static final Random RANDOM = new Random();

    private ClientPlayback() {
    }

    public record Config(List<String> tracks, String playMode) {
    }

    public static void sync(BlockPos pos, String rawTracks, String playMode) {
        BlockPos key = pos.immutable();
        Config config = new Config(parseTracks(rawTracks), playMode);
        CONFIGS.put(key, config);
        Playing playing = PLAYING.get(key);
        if (playing != null) {
            playing.updateConfig(config);
        }
    }

    public static void command(BlockPos pos, int command) {
        BlockPos key = pos.immutable();
        switch (command) {
            case PlaylistCommandPayload.PLAY -> {
                Config config = CONFIGS.get(key);
                if (config == null || config.tracks().isEmpty()) {
                    return;
                }
                Playing playing = PLAYING.get(key);
                if (playing == null) {
                    playing = new Playing(key, config);
                    PLAYING.put(key, playing);
                }
                playing.play();
            }
            case PlaylistCommandPayload.STOP -> {
                Playing playing = PLAYING.remove(key);
                if (playing != null) {
                    playing.close();
                }
            }
            case PlaylistCommandPayload.TOGGLE_PAUSE -> {
                Playing playing = PLAYING.get(key);
                if (playing != null) {
                    playing.togglePause();
                }
            }
            case PlaylistCommandPayload.NEXT -> {
                Playing playing = PLAYING.get(key);
                if (playing != null) {
                    playing.next();
                }
            }
            default -> {
            }
        }
    }

    public static void tick() {
        for (Map.Entry<BlockPos, Playing> entry : List.copyOf(PLAYING.entrySet())) {
            Playing playing = entry.getValue();
            playing.updateGain();
            if (playing.isFinished()) {
                playing.onEnd();
            }
        }
    }

    private static List<String> parseTracks(String raw) {
        List<String> tracks = new ArrayList<>();
        if (raw != null && !raw.isEmpty()) {
            for (String part : raw.split("\n")) {
                if (!part.isBlank()) {
                    tracks.add(part.trim());
                }
            }
        }
        return tracks;
    }

    private static double distanceTo(BlockPos pos) {
        Vec3 listener = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        return Math.sqrt(listener.distanceToSqr(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    private static final class Playing {
        private final BlockPos pos;
        private List<String> tracks;
        private String playMode;
        private int index;
        private int generation;
        private Clip clip;
        private boolean paused;
        private boolean closed;

        private Playing(BlockPos pos, Config config) {
            this.pos = pos;
            this.tracks = config.tracks();
            this.playMode = config.playMode();
        }

        private void updateConfig(Config config) {
            this.tracks = config.tracks();
            this.playMode = config.playMode();
            if (this.index >= this.tracks.size()) {
                this.index = 0;
            }
        }

        private void play() {
            if (this.closed || this.tracks.isEmpty()) {
                return;
            }
            if (this.clip != null) {
                if (this.paused) {
                    this.clip.start();
                    this.paused = false;
                }
                return;
            }
            this.index = Math.min(this.index, this.tracks.size() - 1);
            startIndex(this.index);
        }

        private void togglePause() {
            if (this.clip == null) {
                play();
                return;
            }
            if (this.clip.isRunning()) {
                this.clip.stop();
                this.paused = true;
            } else {
                this.clip.start();
                this.paused = false;
            }
        }

        private void next() {
            if (this.tracks.isEmpty()) {
                return;
            }
            startIndex(nextIndex());
        }

        private void onEnd() {
            if ("single".equals(this.playMode) && this.tracks.size() > 1) {
                startIndex(this.index);
            } else {
                startIndex(nextIndex());
            }
        }

        private int nextIndex() {
            if (this.tracks.size() <= 1) {
                return 0;
            }
            if ("shuffle".equals(this.playMode)) {
                int candidate = RANDOM.nextInt(this.tracks.size() - 1);
                return candidate >= this.index ? candidate + 1 : candidate;
            }
            return (this.index + 1) % this.tracks.size();
        }

        private void startIndex(int newIndex) {
            if (this.closed || this.tracks.isEmpty()) {
                return;
            }
            this.index = Math.max(0, Math.min(newIndex, this.tracks.size() - 1));
            this.generation++;
            int token = this.generation;
            closeClip();
            this.paused = false;
            Path path = UsbLibrary.trackPath(this.tracks.get(this.index));
            CompletableFuture.supplyAsync(() -> decode(path)).thenAccept(audio -> {
                if (audio != null) {
                    Minecraft.getInstance().execute(() -> startAudio(token, audio));
                }
            });
        }

        private void startAudio(int token, AudioData data) {
            if (this.closed || token != this.generation || PLAYING.get(this.pos) != this) {
                return;
            }
            try {
                Clip newClip = AudioSystem.getClip();
                newClip.open(data.format(), data.pcm(), 0, data.pcm().length);
                newClip.start();
                this.clip = newClip;
                updateGain();
            } catch (Throwable t) {
                System.out.println("[ES] clip failed: " + t);
            }
        }

        private boolean isFinished() {
            Clip current = this.clip;
            if (current == null || this.closed || this.paused) {
                return false;
            }
            return !current.isRunning()
                    && current.getMicrosecondPosition() >= current.getMicrosecondLength() - 10_000L;
        }

        private void updateGain() {
            Clip current = this.clip;
            if (current == null || !current.isOpen()) {
                return;
            }
            try {
                FloatControl control = (FloatControl) current.getControl(FloatControl.Type.MASTER_GAIN);
                double volume = Math.max(0.0, 1.0 - distanceTo(this.pos) / RANGE);
                if (volume <= 0.0) {
                    control.setValue(control.getMinimum());
                    return;
                }
                float db = (float) (20.0 * Math.log10(volume));
                db = Math.max(control.getMinimum(), Math.min(control.getMaximum(), db));
                control.setValue(db);
            } catch (Throwable ignored) {
            }
        }

        private void closeClip() {
            Clip current = this.clip;
            this.clip = null;
            if (current != null) {
                try {
                    current.stop();
                    current.close();
                } catch (Throwable ignored) {
                }
            }
        }

        private void close() {
            this.closed = true;
            closeClip();
        }
    }

    private record AudioData(AudioFormat format, byte[] pcm) {
    }

    private static AudioData decode(Path path) {
        try {
            // 直接实例化 provider，避免依赖 ServiceLoader（MOD 类加载器下不一定能扫描到 SPI）
            com.github.trilarion.spi.vorbis.sampled.file.VorbisAudioFileReader reader =
                    new com.github.trilarion.spi.vorbis.sampled.file.VorbisAudioFileReader();
            try (AudioInputStream encoded = reader.getAudioInputStream(path.toFile())) {
                AudioFormat base = encoded.getFormat();
                AudioFormat target = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        base.getSampleRate(), 16, base.getChannels(),
                        base.getChannels() * 2, base.getSampleRate(), false);
                com.github.trilarion.spi.vorbis.sampled.convert.VorbisFormatConversionProvider provider =
                        new com.github.trilarion.spi.vorbis.sampled.convert.VorbisFormatConversionProvider();
                try (AudioInputStream pcm = provider.getAudioInputStream(target, encoded)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream(1 << 20);
                    pcm.transferTo(out);
                    return new AudioData(target, out.toByteArray());
                }
            }
        } catch (Throwable t) {
            System.out.println("[ES] decode failed " + path + " : " + t);
            return null;
        }
    }
}
