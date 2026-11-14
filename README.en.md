# Explanation Stone
![Static Badge](https://img.shields.io/badge/Build-Passing-green?logo=github)
![Static Badge](https://img.shields.io/badge/License-MIT-blue)
![Static Badge](https://img.shields.io/badge/Minecraft-1.21.1-green)
![Static Badge](https://img.shields.io/badge/Neoforge-1.21.1%2B-orange)

![Explanation Stone](screenshots/items.png)

A NeoForge 1.21.1 mod: a stone that plays music, a USB drive that stores playlists, and a cobblestone armor set.

## Contents

- **Explanation Stone**: a music-playing stone, crafted from a note block, cobblestone and redstone dust
- **USB Drive**: crafted from an iron ingot + redstone dust; right-click air to edit the playlist (select, reorder, play mode, right-click/redstone mode), right-click the stone to insert it; drop `.ogg` files into `config/explainationstone/music/`
- **Raw Stone Armor**: crafted from cobblestone


|Item|Recipe|
|-|-|
|Explanation Stone|![Recipe](/screenshots/speaker_recipie.png)|
|USB Drive|![Recipe](/screenshots/usb_recipie.png)|
|Raw Stone Helmet|![Recipe](/screenshots/helmet_recipie.png)|
|Raw Stone Armor|![Recipe](/screenshots/chestplate_recipie.png)|
|Raw Stone Leggings|![Recipe](/screenshots/leggings_recipie.png)|
|Raw Stone Boots|![Recipe](/screenshots/boots_recipie.png)|

## Build

```bash
gradlew build      # output: build/libs/explainationstone-0.1.0.jar
gradlew runClient  # dev client
```

Requires JDK 21 and NeoForge 21.1.209.

## License

MIT
Audio decoding uses [vorbis-support](https://github.com/Trilarion/vorbis-support) (LGPL-2.1, bundled into the jar).
