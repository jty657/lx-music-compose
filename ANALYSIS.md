# 聆序（LX Music Compose）全量源码分析报告

> 本报告基于对仓库全部 Kotlin 源码、资源、构建配置与文档的逐模块复审，覆盖架构、正确性、性能、稳定性、安全、UI/UX、可维护性、可访问性八个维度。
>
> **项目定位**：聆序是一款**同时具备「在线」与「本地」双功能的音乐播放器**。联网时聚合搜索并播放酷我 / 酷狗 / QQ 音乐 / 网易云 / 咪咕的在线音乐（歌单、榜单、歌词、封面、换源）；离线时完整管理本地歌单、收藏、播放队列与数据备份恢复。两种能力共用同一套播放内核与液态玻璃界面，切换音源不影响本地数据。

---

## 一、架构总览

```
ui/                     表现层（Jetpack Compose + Miuix + Backdrop 液态玻璃）
  ├─ screens/           10 个功能页 + 对应 ViewModel
  ├─ components/        Glass（玻璃）/ TrackCover（封面）/ ScrollReporter
  ├─ navigation/        轻量 backStack 导航器
  └─ theme/             主题与全局字体缩放
core/                   领域层
  ├─ player/            ExoPlayer 播放仓库 + LRC 歌词解析（含翻译/罗马音）
  ├─ engine/            QuickJS 引擎 + 内置 SDK 桥 + 脚本管理
  ├─ repository/        音源/音乐/歌单/收藏/备份/更新/不喜欢/设置 仓库
  ├─ db/                JSON 文件持久化（原子写 + 损坏隔离）
  ├─ request/           OkHttp 封装（超时 client 复用 + 磁盘缓存）
  ├─ crypto/ system/    加解密、电池优化、设备信息
LxApp.kt                依赖注入根（单例 + 加载器回调注入）
```

**总体评价**：分层清晰，core 层不反向依赖 UI（通过「加载器 / 回调」注入解耦，如 `lyricLoader` / `coverLoader` / `playInfoLoader`），这一设计值得肯定。ViewModel 与页面一一对应，状态驱动合理。项目已经过七轮系统优化，数据健壮性、播放竞态、引擎资源治理、歌词时序、重组性能等方面都已相当扎实。

---

## 二、分维度评估与建议

### 2.1 正确性 / 并发
| 项 | 现状 | 建议 / 状态 |
|---|---|---|
| 搜索结果竞态 | `requestSeq` 版本锁，仅最新批次可写状态 | 已完善（v1.3.5） |
| **清空后队列复活** | 播放解析是挂起操作，clear/release 后旧协程仍回填队列、操作播放器 | **本轮已修复**：`playGeneration` 代号 + `playQueueJob` 取消，过期解析直接丢弃 |
| **歌词翻译/罗马音错位** | 翻译/罗马音各自按自身 offset 解析；多数音源只给主歌词带 `[offset:]`，导致时间轴错位、翻译静默不显示 | **本轮已修复**：翻译/罗马音继承主歌词 offset（`LrcParser.parseSingle(offsetOverride)`） |
| 播放队列去重 | `addToQueue`/`playQueue` 按 id 去重 | 已完善 |
| 数据加载守卫 | 收藏/不喜欢/我的列表均有 `Mutex` + `loaded` 守卫 | 已完善 |
| 备份导入 | decode 前 JSON 结构校验 + 空数据拒绝 + id 白名单 | 已完善 |
| 定时停止恢复 | `timeoutExitMinutes` 已持久化，冷启动未恢复倒计时 | 建议（需产品决策：重启后是否续算） |

### 2.2 性能
| 项 | 现状 | 建议 / 状态 |
|---|---|---|
| **设置页重组** | 原订阅整个 `PlayState`，positionMs 每 500ms 变化触发整页 ~40 个组件重组 | **本轮已修复**：改为 `state.map { it.timeoutExitRemaining }` 只订阅所需字段 |
| **歌单同步写盘** | 原 `persist` 逐条 `upsertPlaylist`（每次全量重写），`overwriteOnlineMusics` 又调两次，约 2N 次写盘 | **本轮已修复**：新增 `replacePlaylists` 一次落盘，合并两次字段更新 |
| 歌词高亮定位 | 升序二分查找 O(log n) | 已完善（v1.3.5） |
| 封面加载 | 内存 LRU + 失败冷却 + 并发上限 + 请求去重 + 降采样 | 已完善 |
| 聚合搜索 | 多源并发，最坏耗时降至单源超时 | 已完善 |
| 换源取词/取链 | 5 源串行，最坏约 100s；有 `lyricJob` 可取消兜底 | 建议（改 async 并发取首个有效结果，本轮未纳入以控风险） |

### 2.3 稳定性 / 资源治理
- QuickJS Promise 一次性守卫 + 取消清理、定时器安全取值、SDK 销毁期间迟到回调沉默——均已到位。
- **本轮修复**：自定义超时 client 的 `size>=8` 检查与 `getOrPut` 原本非原子，并发下可瞬时超上限；现用 `synchronized` 包住检查与插入。
- **本轮修复**：原子写临时文件名加 UUID，避免并发写同一文件时 `.tmp` 互相覆盖产生残缺 JSON（残缺 JSON 会被 `readJsonOrNull` 判损坏隔离，造成数据丢失观感）。

### 2.4 安全
- 备份导入 id 白名单防路径穿越；备份刻意不含自定义音源脚本，避免凭据外泄——到位。
- 不内置、不分发任何音源脚本与受版权内容——合规。
- QuickJS 沙箱仅运行用户主动导入的可信脚本——当前设计满足。

### 2.5 UI / UX / 美化
- 液态玻璃采样边界处理正确（layerBackdrop 只套主内容，底栏/迷你条为兄弟节点），规避自采样递归闪退——关键风险已规避。
- **本轮美化**：
  - `GlassSurface` 点击从 `indication=null` 改为 `LocalIndication.current`，全 App 卡片获得按压涟漪反馈（`.clip(shape)` 裁剪边界）。
  - 新增 `values-night/themes.xml` + AppRoot 按运行时主题同步状态栏图标极性，深色模式启动不再闪白、深色图标不再压在深色内容上。
  - 歌单搜索无结果由误显「没有找到相关音乐」改为专属空态。
  - 播放页错误提示由单行截断改为两行并附「下一首」恢复出口。
  - 无效的「加入播放队列」按钮（对正在播放的歌入队是空操作）改为「复制歌曲信息」。
  - 设置页「字体大小」改用分区标题样式，系统能力项补「系统」分区标题，层级更清晰。
- **建议**：底栏/迷你条图标热区可统一到 48dp（现 28~36dp），进一步符合触控目标规范（本轮未纳入以控改动面）。

### 2.6 可维护性
- `scripts/selfcheck.py` 静态拦截 import 粘连、图标漏 import、括号不平衡、字段笔误、玻璃自采样等——**本轮已接入 CI 前置步骤**，云端构建前先跑一遍，降低往返成本。
- `AGENTS.md` 详尽记录构建栈约束、音源协议、玻璃采样边界等踩坑经验——交接友好。

### 2.7 可访问性
- 播放队列上下移图标热区 32dp、搜索历史「清空」补 padding——已改善。
- 本轮卡片按压反馈、深色模式状态栏图标对比度已改善。
- 建议：图标按钮热区统一 48dp（见 2.5）。

---

## 三、本轮已执行的优化与美化（第七轮 · v1.3.6）

1. **【正确性】歌词 offset 时序修复**（`core/player/LrcParser.kt`）：翻译/罗马音继承主歌词 `[offset:]`，修复时间轴错位导致的翻译/罗马音静默不显示。
2. **【正确性】播放竞态收口**（`core/player/PlayerRepository.kt`）：新增 `playGeneration` 代号，playQueue/clear/release 递增并取消在途解析；clear/release 额外清 `resolvingIndexes`，杜绝清空后队列复活与操作已释放播放器。
3. **【性能】设置页重组隔离**（`ui/screens/setting/SettingScreen.kt`）：只订阅 `timeoutExitRemaining` 字段，避免播放进度驱动整页重组。
4. **【性能】歌单同步写盘**（`core/db/AppDatabase.kt` + `core/repository/MyListsRepository.kt`）：新增 `replacePlaylists` 一次落盘，`overwriteOnlineMusics` 合并两次 persist，写盘从 O(N²) 降为 O(N)。
5. **【稳定性】原子写 UUID 临时名**（`core/db/AppDatabase.kt`）：并发写不再共用 `.tmp` 互相覆盖。
6. **【稳定性】HTTP client 容量原子判断**（`core/request/HttpClient.kt`）：check-then-put 纳入 `synchronized`。
7. **【美化】卡片按压反馈**（`ui/components/Glass.kt`）：`LocalIndication.current` 全局启用涟漪。
8. **【美化】深色模式启动过渡**（`res/values-night/themes.xml` + `res/values/themes.xml` + `ui/AppRoot.kt`）：暗色启动窗口 + 状态栏图标极性跟随主题。
9. **【美化】歌单搜索空态 / 播放页错误恢复 / 无效按钮替换**（`SearchScreen.kt` / `PlayerScreen.kt`）。
10. **【美化】设置页分区层级**（`SettingScreen.kt`）。
11. **【工程】CI 接入静态自检**（`.github/workflows/build.yml`）。
12. **【版本】** `versionCode` 9 → 10，`versionName` 1.3.5 → 1.3.6，同步 README。

**质量保证**：所有 `.kt` 改动均通过 `scripts/selfcheck.py` 静态自检；云端构建由 GitHub Actions 验证（并已把自检纳入流水线）。

---

## 四、在线 + 本地双模能力说明

| 维度 | 🌐 在线 | 💾 本地 |
|---|---|---|
| 音源 | 酷我/酷狗/QQ/网易云/咪咕聚合搜索、歌单、榜单、热搜 | 用户音源 `local` 分支解析本地歌曲地址 |
| 内容 | 在线歌词（翻译/罗马音）、封面、换源 fallback | 本地歌单 CRUD、收藏、播放队列、数据备份恢复 |
| 设置 | 音源脚本导入、音质选择 | 主题/字体/模糊/歌词样式本地持久化 |

两种能力共用同一播放内核与界面，切换音源不影响本地数据。本项目不内置任何音源脚本，在线播放需用户自行导入合规第三方音源；本地数据完全存于设备。

---

## 五、版本与时间线

- v1.3.4（第五轮）：数据安全补漏 + 资源治理 + 图标语义化 + 交互细节
- v1.3.5（第六轮）：搜索竞态修复 + 歌词二分查找 + 热搜视觉层级
- **v1.3.6（本轮 · 第七轮）**：歌词 offset 时序 + 播放竞态收口 + 设置页/歌单同步性能 + 原子写与 HTTP 并发 + 深色模式与按压反馈等美化 + CI 自检

---

## 六、后续建议路线图（未纳入本轮）

1. 换源取词/取链改 `async` 并发，取首个有效结果即取消其余，降低最坏等待。
2. 定时停止播放冷启动恢复（明确「重启后续算 / 不续算」语义后实现）。
3. 底栏/迷你条图标统一 48dp 触控热区。
4. `restoreLastPlay` 对过期的在线签名 URL 先异步重解析再灌入播放器，避免冷启动首播失败一次（需谨慎处理时序，本轮因无法本地编译未纳入）。
5. AppDatabase 所有「读改写」统一加 DB 级 `Mutex`，彻底消除并发覆盖（本轮仅做了临时文件名隔离这一低风险子集）。

> 本报告随源码一同维护。详细变更记录见 [CHANGELOG.md](CHANGELOG.md)。
