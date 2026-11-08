package com.mcking.stonespeaker.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import com.mcking.stonespeaker.UsbLibrary;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * 客户端播放：按方块坐标维护播放实例，曲目从 config/stonespeaker/music 读取，
 * 用 stb_vorbis 解码成 PCM，再由 Java Sound 播放；音量按听者距离衰减。
 */
public final class ClientPlayback {
    private static final Map<BlockPos, Playing> PLAYING = new ConcurrentHashMap<>();
    private static final double RANGE = 64.0;

    private ClientPlayback() {
    }

    public static void play(BlockPos pos, String rawTracks) {
        stop(pos);
        List<String> tracks = new ArrayList<>();
        if (rawTracks != null && !rawTracks.isEmpty()) {
            for (String part : rawTracks.split("\n")) {
                if (!part.isBlank()) {
                    tracks.add(part.trim());
                }
            }
        }
        if (tracks.isEmpty()) {
            return;
        }
        Playing playing = new Playing(pos.immutable(), tracks);
        PLAYING.put(pos.immutable(), playing);
        playing.next();
    }

    public static void stop(BlockPos pos) {
        Playing playing = PLAYING.remove(pos);
        if (playing != null) {
            playing.close();
        }
    }

    public static void tick() {
        for (Map.Entry<BlockPos, Playing> entry : List.copyOf(PLAYING.entrySet())) {
            Playing playing = entry.getValue();
            playing.updateGain();
            if (playing.isFinished()) {
                playing.next();
            }
        }
    }

    private static double distanceTo(BlockPos pos) {
        Vec3 listener = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        return Math.sqrt(listener.distanceToSqr(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    private static final class Playing {
        private final BlockPos pos;
        private final List<String> tracks;
        private int index;
        private Clip clip;
        private boolean closed;

        private Playing(BlockPos pos, List<String> tracks) {
            this.pos = pos;
            this.tracks = tracks;
        }

        private void next() {
            closeClip();
            if (this.closed) {
                return;
            }
            if (this.index >= this.tracks.size()) {
                PLAYING.remove(this.pos, this);
                return;
            }
            String name = this.tracks.get(this.index++);
            Path path = UsbLibrary.trackPath(name);
            CompletableFuture.supplyAsync(() -> decode(path)).thenAccept(audio -> {
                if (audio != null) {
                    Minecraft.getInstance().execute(() -> start(audio));
                }
            });
        }

        private void start(AudioData data) {
            if (this.closed || PLAYING.get(this.pos) != this) {
                return;
            }
            try {
                Clip newClip = AudioSystem.getClip();
                newClip.open(data.format(), data.pcm(), 0, data.pcm().length);
                newClip.start();
                this.clip = newClip;
                updateGain();
            } catch (Throwable ignored) {
                // 没有可用音频设备时静默跳过
            }
        }

        private boolean isFinished() {
            Clip current = this.clip;
            if (current == null || this.closed) {
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
        try (AudioInputStream encoded = AudioSystem.getAudioInputStream(path.toFile())) {
            AudioFormat base = encoded.getFormat();
            // Vorbis SPI 先给出 VORBISENC 流，需要再转成 16bit PCM 才能给 Clip 播放
            AudioFormat target = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    base.getSampleRate(), 16, base.getChannels(),
                    base.getChannels() * 2, base.getSampleRate(), false);
            try (AudioInputStream pcm = AudioSystem.getAudioInputStream(target, encoded)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream(1 << 20);
                pcm.transferTo(out);
                return new AudioData(target, out.toByteArray());
            }
        } catch (Throwable ignored) {
            return null;
        }
    }
}
