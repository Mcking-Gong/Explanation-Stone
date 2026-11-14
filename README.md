# Explanation Stone（讲解石）
![Static Badge](https://img.shields.io/badge/Build-Passing-green?logo=github)
![Static Badge](https://img.shields.io/badge/License-MIT-blue)
![Static Badge](https://img.shields.io/badge/Minecraft-1.21.1-green)
![Static Badge](https://img.shields.io/badge/Neoforge-1.21.1%2B-orange)

![Explanation Stone](screenshots/items.png)

NeoForge 1.21.1 模组：一块会放音乐的石头，一个存歌单的 U 盘，还有一套原石铠。

## 内容

- **讲解石**：播放音乐的石头，使用音符盒、原石和红石粉合成
- **U 盘**：铁锭 + 红石粉合成；右键空气编辑歌单（勾选、排序、播放模式、右键/红石模式），右键讲解石插入；曲库放 `config/explainationstone/music/*.ogg`
- **原石铠**：圆石合成


|物品|配方|
|-|-|
|讲解石|![Recipie](/screenshots/speaker_recipie.png)|
|U盘|![Recipie](/screenshots/usb_recipie.png)|
|原石头盔|![Recipie](/screenshots/helmet_recipie.png)|
|原石铠|![Recipie](/screenshots/chestplate_recipie.png)|
|原石护腿|![Recipie](/screenshots/leggings_recipie.png)|
|原石靴子|![Recipie](/screenshots/boots_recipie.png)|

## 构建

```bash
gradlew build      # 产物 build/libs/explainationstone-0.1.0.jar
gradlew runClient  # 开发客户端
```

需要 JDK 21、NeoForge 21.1.209。

## 许可证

MIT
音频解码使用 [vorbis-support](https://github.com/Trilarion/vorbis-support)（LGPL-2.1，已打包进 jar）。
