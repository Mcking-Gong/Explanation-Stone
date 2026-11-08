# Explanation Stone

![Explanation Stone](screenshots/header.png)

A NeoForge 1.21.1 mod: a stone speaker that plays music, a USB drive that stores playlists, and a cobblestone armor set.

## Features

- **Explanation Stone**: right-click or redstone signal to play; stops when the signal ends or the block is broken
- **USB Drive**: right-click air to edit the playlist, right-click the stone to insert it. Drop `.ogg` files into `config/stonespeaker/music/`
- **Raw Stone Armor**: crafted from cobblestone; jukebox + any armor piece = Explanation Stone

## Build

```bash
gradlew build      # output: build/libs/stonespeaker-0.1.0.jar
gradlew runClient  # dev client
```

Requires JDK 21 and NeoForge 21.1.209.

## License

MIT. Audio decoding uses [vorbis-support](https://github.com/Trilarion/vorbis-support) (LGPL-2.1, bundled into the jar).

Models and textures by Mcking (Blockbench).
