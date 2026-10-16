# 聆序（LX Music Compose）

洛雪音乐 Compose 移植版。所有功能对齐原版 lx-music-mobile v1.9.1（React Native 版，包名 cn.toside.music.mobile）。

- **应用名**：聆序；`applicationId` / `namespace` = `com.lingxu.music`；源码目录 `app/src/main/java/com/lingxu/music/`。
- 改任何 .kt 后**必须先跑 `python3 scripts/selfcheck.py`**，它专门拦「只有云端编译才会暴露」的低级错误（import 粘连、MiuixIcons.X 未 import、Spacer/Column/Row/Box 未 import、括号不平衡、包名笔误、赋值写成函数调用、Card 的 onClick 与 combinedClickable 冲突）。

## 构建要求（重要）

- **本地容器没有 Java，禁止本地编译**。所有构建必须通过 GitHub Actions 云端执行（推送到 jty657/lx-music-compose 的 master 分支自动触发）。
- 构建栈：AGP 9.4.1 + Gradle 9.7.1 + Kotlin 2.4.21（AGP 内置 Kotlin，不应用 org.jetbrains.kotlin.android）+ compileSdk 37.0（DSL：`compileSdk { version = release(37) { minorApiLevel = 0 } }`）。
- Miuix 0.9.3 硬性要求 compileSdk 37（AAR metadata minCompileSdk=37），AGP 8.x 装不上。
- CI 里 SDK 包名是 `platforms;android-37.0`（不是 android-37），build-tools 37.0.0。
- KSP 2.4 未发布（Maven Central 最新 2.3.12），**禁止用 KSP/kapt**；持久化用 JSON 文件（filesDir）实现。
- miuix-blur 要求 minSdk 33，**不要引入**；模糊层用 Backdrop 实现。
- material3 用 `org.jetbrains.compose.material3:material3:1.9.0`（1.11.x 未发布 stable）。

## 关键依赖

- `wang.harlon.quickjs:wrapper-android:2.4.0` + `wrapper-java:2.4.0`（与原版相同的 QuickJS 引擎）
- `top.yukonga.miuix.kmp:miuix-ui/preference/icons:0.9.3`
- `io.github.kyant0:backdrop:2.0.1`（Liquid Glass，API 31 以下自动降级）。**不要换 Haze**：Haze 2.x 需要 Compose CMP 1.12.1，与 Miuix 0.9.3 钉的 1.11.x 冲突。
- backdrop 的 `blur` 需 API 31+、`lens` 需 API 33+，低于则静默无效果；UI 上必须按 `Build.VERSION.SDK_INT` 门控降级，否则低版本拿到「半透明色块」而不是玻璃。

## 音源脚本兼容性协议（与原版 1:1）

- assets/scripts/user-api-preload.js 是从原版 APK 提取的**原封不动**的脚本，不要修改。
- 脚本协议：`lx_setup(key,...)` 初始化 → `lx.on(EVENT_NAMES.request, handler)` 注册（musicUrl/lyric/pic 三个 action）→ `lx.send(EVENT_NAMES.inited, {sources})` 宣告就绪。
- native 桥：`__lx_native_call__(key, action, data)` + 6 个 utils 函数（str2b64/b642buf/str2md5/aes_encrypt/rsa_encrypt/set_timeout）。
- 内置 SDK（kw/kg/tx/wy/mg）来自原版 src/utils/musicSdk 的 esbuild 打包产物（assets/engine/music-sdk.bundle.js），重新打包用 /tmp/sdk-bundle 的 esbuild.config.mjs。bundle 内含各源 musicSearch/leaderboard/songList/hotSearch/tipSearch（`getList(sortId,tagId,page)` / `getListDetail(id,page)` / `getTags()` / `search(text,page,limit)`）。
- **搜索/榜单/歌单只走内置 SDK bundle；用户音源只提供 `musicUrl`（播放取 URL）**。这是原版的既定架构，不要试图让用户音源提供搜索。
- **SDK 的 fetch 桥必须让 `resp.text()/blob()/json()` 返回 thenable，且提供 `resp.headers.map`**，否则 SDK 的 `resp.text().then(JSON.parse)` 链直接崩（表现为「搜索无效」）。
- 预置音源放 `assets/scripts/presets/*.js`，首次启动由 `PresetScriptRepository` 自动导入。
- 已用 quickjs-emscripten（Node）做过端到端协议测试，全部通过。

## 云端编译与验证

- 仓库：https://github.com/jty657/lx-music-compose（令牌在 ~/workspace/.aicode/tmp/gh_token，勿提交）
- 构建后下载 artifact：`curl -L -H "Authorization: Bearer $TOKEN" <artifact download url>`
- 验证 APK 用 androguard（~/.venvs/apk 虚拟环境）
