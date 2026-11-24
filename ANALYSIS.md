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

**总体评价**：分层清晰，core 层不反向依赖 UI（通过「加载器 / 回调」注入解耦，如 `lyricLoader` / `coverLoader` / `playInfoLoader`），这一设计值得肯定。ViewModel 与页面一一对应，状态驱动合理。项目已经过十轮系统优化，数据健壮性、播放竞态、引擎资源治理、歌词时序、重组性能、可访问性与视觉一致性等方面都已相当扎实。

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
| 定时停止恢复 | `timeoutExitMinutes` 已持久化，冷启动未恢复倒计时 | **本轮已修复**：启动时按设定分钟数恢复倒计时（语义：重启后重新计时，不跨重启续算绝对时间）；倒计时改按绝对到期时间计算，抗后台冻结漂移 |

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
- **建议**：底栏/迷你条图标热区可统一到 48dp（现 28~36dp）——**已修复**（迷你条播放/队列按钮、播放页控制图标热区均提至 ≥48dp，播放模式标签命中区加大）。
- **第十轮美化**：返回键抽为 `BackIconButton` 统一 6 个二级页热区；收藏页/我的列表空态改为居中 + 占位图标；榜单详情补搜索空态与加载提示。
- **第十轮可访问性**：删除键/收藏键/清空/下一首恢复等操作热区扩至 48dp 左右；播放页收藏 `contentDescription` 随状态切换。

### 2.6 可维护性
- `scripts/selfcheck.py` 静态拦截 import 粘连、图标漏 import、括号不平衡、字段笔误、玻璃自采样等——**本轮已接入 CI 前置步骤**，云端构建前先跑一遍，降低往返成本。
- `AGENTS.md` 详尽记录构建栈约束、音源协议、玻璃采样边界等踩坑经验——交接友好。

### 2.7 可访问性
- 播放队列上下移图标热区 32dp、搜索历史「清空」补 padding——已改善。
- 卡片按压反馈、深色模式状态栏图标对比度已改善。
- 第八轮：迷你条播放/队列按钮热区 36/28dp → 48dp，播放页全部控制图标热区 ≥48dp、播放模式标签命中区加大。
- 第十轮：返回键（6 页）、删除键（4 处）、搜索收藏键、队列「清空」、播放页「下一首」恢复出口热区统一 ≥44~48dp；播放页收藏按钮无障碍标签随状态切换。

---

## 三、本轮已执行的优化与美化（第二十七轮 · v1.7.1）

> 本轮延续「功能差异 + UI/动效」主线，对照 `lyswhut/lx-music-mobile` v1.9.2 做低风险补齐。三批改动
> 全部通过 `scripts/selfcheck.py` 与本地 `assembleRelease`（JDK 20 + SDK 37）。零新增依赖、不改路由/
> 状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

1. **播放队列副标题接入列表显示设置**（`ui/screens/playqueue/PlayQueueScreen.kt`）
   - 原版 `list.isShowSource / isShowInterval / isShowAlbumName` 三项开关此前已在收藏页、搜索页、
     歌单/榜单详情、我的列表生效，唯独播放队列固定「歌手 · 来源」、完全无视设置。
   - 按收藏页 `FavoriteCard` 同一套 `buildList` 逻辑接入：歌手恒显示，专辑/来源/时长按开关拼接，
     空片段过滤掉悬空「 · 」。全站列表页显示设置至此一致。

### UI / 视觉层级

2. **搜索页音质角标去重**（`ui/screens/search/SearchScreen.kt`）：删掉副标题下方重复的一行纯文字音质标签，
   只保留歌名右侧的 `QualityBadge` 角标（全站其它列表页本就只有角标）。去掉多出的一行后卡片不再被撑高，
   长列表可见条数增加。
3. **音质角标换源刷新**（`ui/components/QualityBadge.kt`）：`remember` key 从仅 `music.id` 扩为
   `music.id + source + _qualitys + qualitys`，修复同一首歌换源后可用音质变化、角标却停留在旧档位的问题。

### 可访问性 / 响应式

4. **三处长按读屏语义化**（`SourceScreen.kt` / `SongListScreen.kt` / `MyListsScreen.kt`）：`GlassSurface`
   的 `onLongClickLabel` 默认「打开歌曲操作」，但音源页长按是删除、歌单广场是收藏、我的列表是重命名，
   TalkBack 误播。分别传准确标签。
5. **音源导入按钮响应式**（`SourceScreen.kt`）：三个导入按钮固定 `Row` 改 `LazyRow + spacedBy(8.dp)`，
   大字号/窄屏下不再把「剪贴板导入」挤出屏幕。

### 已知差异 / 待确认（本轮）

- 音源「更新提示」开关仅存储状态、未接入原版 `syncSourceList` 的实际更新检测：需在数据库保存脚本来源 URL
  并新增网络检测/冲突提示，改动面偏大，未纳入本轮。

---

## 三、上一轮已执行的优化与美化（第二十六轮 · v1.7.0）

> 上一轮以「功能差异」为主线：对照 `lyswhut/lx-music-mobile` v1.9.2，补上移植版**完全缺失**的三项能力
> ——播放缓存、歌词/链接元数据缓存与清理入口、列表音质角标。零新增依赖、不改路由/状态管理/播放状态机、
> 不触碰 QuickJS 与音源协议；全部 `.kt` 通过 `scripts/selfcheck.py`，并本地 `assembleRelease` 验证通过。

### 功能（原版缺失项）

1. **播放缓存**（`core/player/PlaybackCache.kt`，对齐 `player.cacheSize`）
   - 数据源链 `cache -> http`：`SimpleCache` + LRU 淘汰 + `CacheDataSource`（`FLAG_IGNORE_CACHE_ON_ERROR`，缓存异常自动回源）。
   - 默认 1024 KB（同原版），档位 关闭/1/32/64/128/256/512 MB；目录 `<cacheDir>/player`，与封面缓存分离。
   - **不重建播放器**：media3 1.8.0 的 `ExoPlayer` 实例无 `setMediaSourceFactory`，改为 ExoPlayer 持有稳定的
     `SwitchableDataSourceFactory`，内部按当前缓存实例切换——改容量不打断播放。
2. **歌词与播放链接缓存**（`core/repository/MetaCache.kt`，对齐 `utils/data.ts` 的 lyric / musicUrl）
   - 键为「源+id」；歌词 300 条、链接 200 条 LRU；链接 10 分钟 TTL（避开签名过期）。
   - 收益：切回上一首/换首再回来不再重复取词，也不再触发「五源换源」的多次请求。
   - **已知差异**：原版落盘并跨版本迁移，本轮为内存 LRU（落盘需新增序列化与损坏隔离，风险偏高）。
3. **列表音质角标**（`ui/components/QualityBadge.kt`，对齐 `components/common/Badge`）
   - 判定降序 `flac24bit > flac/ape > 320k/192k`；接入搜索/收藏/歌单详情/我的列表/榜单详情 5 个列表页。
   - 歌名 `weight(1f, fill = false)` 优先收缩；无可用音质（本地文件等）不渲染。

### 设置项

4. 「播放设置 → 播放缓存」：占用/上限展示、档位切换、清空。
5. 「其他 → 歌词与链接缓存」：分别显示条数与单独清空（对齐原版 MetaCache）。

### 仍未对齐（明确记录，本轮不做）

| 原版能力 | 现状 | 原因 |
|---|---|---|
| 同步（WebDAV / 自定义源同步） | 无 | 需新增网络协议栈与冲突合并策略，属大工程 |
| 歌曲评论 | 无 | 依赖各平台令牌与反爬参数，且非播放核心 |
| 逐字歌词 | 无 | 依赖 KRC/QRC 解析与逐字渲染，改动面大 |
| 元数据缓存落盘 | 仅内存 LRU | 落盘需新增序列化文件、并发写与损坏隔离，本项目已在该处踩过坑 |
| 歌曲信息编辑（原版 `MetadataEditModal`） | 无 | 需改 `MusicInfo` 持久化结构，风险偏高 |
| 随机播放的 `playedList` 语义 | 用 ExoPlayer shuffle | 语义差异已记录 |
| `list.isClickPlayList` | 恒等价于「以当前列表为队列」 | 做成开关会改变既有手感 |
| 主题色板数量 | 6 档主色 | 原版每套皮肤几十个色值，直接移植会与 Miuix 语义色冲突 |

---

## 三、上一轮已执行的优化与美化（第二十五轮 · v1.6.0）

> 本轮以「功能差异」为主线：拿 `lyswhut/lx-music-mobile`（v1.9.2）源码逐项对照，补上移植版**完全缺失**的两项能力，并对齐一项设置、打磨一处动效。不改路由/状态管理/播放内核，不触碰 QuickJS 与音源协议，零新增依赖。

### 功能（原版缺失项）

1. **桌面歌词悬浮窗**（新增 `core/system/DesktopLyric.kt`，对齐原版 `desktopLyric.*`）
   - 实现：`WindowManager` + 原生 `TextView`/`LinearLayout`，`TYPE_APPLICATION_OVERLAY`（API 26+，以下回退 `TYPE_PHONE`）；`FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCH_MODAL` 保证不抢焦点、窗口外触摸照常下传。
   - 内容：单行当前歌词 + 翻译副行；「♪」用主题主色（由 `AppRoot` 在主题变化时推送 `Color.toArgb()`）；暂停时整窗 alpha 0.55。
   - 交互：上下拖动（松手写 `SharedPreferences` 记住位置）、轻点 `playerRepository.togglePlay()`；`ValueAnimator.areAnimatorsEnabled()` 经 API 26 门控，minSdk 24 安全。
   - 权限：`SYSTEM_ALERT_WINDOW`；设置页未授权时拉起 `ACTION_MANAGE_OVERLAY_PERMISSION`，用 `ViewTreeObserver.OnWindowFocusChangeListener` 在返回时复检，避免引入 lifecycle-compose。
   - 边界：所有窗口操作 `runCatching`；进程被杀即消失，冷启动按开关恢复；窗口宽度按启动时屏幕宽度固定，横竖屏切换后需重启开关才会重算（记录为待确认项）。

2. **主题主色**（`ui/theme/Theme.kt`，对齐原版 `theme.id` 皮肤）
   - 用 Miuix `ThemeController(keyColor = …)` 由单一主色推导整套调色板（明暗两套 + 语义色），取代「照搬原版多份色板」的方案——零新增文件、零依赖，对比度交由库保证。
   - 6 档：默认（不覆盖，保持升级前外观）/ 青绿 / 靛蓝 / 黛紫 / 暖橙 / 绯粉；`monet` 模式下不覆盖（与原版取色语义一致）。

### 设置对齐

3. **歌词字体阴影**（原版 `theme.fontShadow`，默认 `false`）：`TextStyle(shadow = Shadow(...))` 只作用于歌词正文，不改字号行高，不触发 `LazyColumn` 重新测量。

### 动效

4. **迷你条文字切歌淡入**（`ui/AppRoot.kt`）：以 `current.id` 为 key，200ms 淡入 + 6dp 上浮，仅 `alpha`/`translationY`；减弱动效时初值即终值。


## 三、本轮已执行的优化与美化（第十五轮 · v1.4.4）

> 本轮为纯 UI 层动效/视觉批次：不改路由结构、状态管理与播放内核，不触碰 QuickJS 与音源脚本。全部 `.kt` 改动通过 `scripts/selfcheck.py` 自检，构建由 GitHub Actions 云端验证。

**动效（6 项）**

1. **返回转场方向**（`ui/navigation/Navigator.kt`、`ui/AppRoot.kt`）：`Navigator` 记录最近一次导航是否为 `pop`，`AnimatedContent` 的 `transitionSpec` 据此选择正/反向位移。原实现前进与返回都是「右进左出」，按返回时与手势方向相反。
2. **播放队列律动音柱**（新增 `ui/components/PlayingIndicator.kt`）：当前曲目由「主色 Play 图标」改为三条不同周期音柱；暂停时静帧半高，不保留无限动画；仅当前行使用，Compose 动画遵循系统时长缩放。
3. **列表增删/排序动画**：7 个列表页的 `LazyColumn` 条目加 `Modifier.animateItem()`（均使用稳定 key），覆盖搜索音乐/歌单、收藏、播放队列、我的列表清单与歌单内、歌单详情、榜单详情、首页榜单。
4. **播放/暂停图标 Crossfade**（`PlayerScreen.kt`、`AppRoot.kt`，180ms），消除同位置硬切。
5. **「回到当前歌词」浮层**（`PlayerScreen.kt`）：淡入 + 上浮 180ms，退出反向。
6. **弹窗进场**（`ui/components/Glass.kt`）：scrim 淡入 + 面板 0.94 缩放淡入（160/200ms）；独立窗口内实现，不引入 backdrop 跨窗口采样。

**视觉与可访问性（4 项）**

7. **迷你条背景降级修复**（`AppRoot.kt`）：关闭模糊或 API < 31 时原备选分支无背景，文字压在列表上；补近不透明表面 + 细描边（深/浅两套色）。
8. **迷你条进度平滑**（同文件）：500ms 位置更新之间用等速 tween 连接，消除进度线跳格；补 `progressBarRangeInfo` 读屏语义。
9. **按压反馈统一**（同文件）：迷你条播放/队列按钮补按下缩放（0.86，spring），与播放页一致。
10. **多选底栏过渡与窄屏适配**（`MyListsScreen.kt`）：淡入 + 纵向展开/收起；四个按钮横向可滚动，避免大字号下「移除」被挤出屏幕。

**未做（有意规避）**：未引入骨架屏/新依赖、未改列表分页与播放解析时序、未调整底部预留高度（避免滚动中改 padding 造成跳动），平板/横屏内容宽度上限留作后续。

---

### 更早轮次（第十二轮 · v1.4.1）

> 延续「在线 + 本地」双模定位，本轮做数据一致性、启动健壮性与 UI 组件化的综合复审。坚持低风险原则：不触碰 QuickJS 引擎、音源协议与 assets 脚本，全部改动通过 `scripts/selfcheck.py` 静态自检，云端构建由 GitHub Actions 验证。

**正确性 / Bug 修复（3 项）**

1. **备份导入重复 id 崩溃**（`core/repository/BackupRepository.kt`）：`playlists`/`playlistMusics` 只做 id 白名单过滤未去重，重复 id 会以稳定 key 触发列表页 "Duplicate key" 崩溃。补 `distinctBy`（歌单按 id、歌曲按 listId+id），与 `favorites` 的既有去重策略对齐。
2. **暂停态删当前歌自动播放**（`core/player/PlayerRepository.kt`）：`removeFromQueue` 走 `playQueue` 无条件 `play()`，破坏暂停状态；给 `playQueue` 加 `autoPlay` 参数（默认 true），删除场景传 `player.playWhenReady`。
3. **上次播放状态写盘乱序**（同文件）：多处并发 `saveLastPlay()` 完成顺序不定，旧状态可能最后落盘。新增 `lastPlayJob` 取消在途写只保留最新一次，`clear`/`release` 一并取消。

**健壮性（2 项）**

4. **启动初始化链脆弱**（`LxApp.kt`）：4 个设置监听此前嵌在主链里，前序未保护的 `.first()`（DataStore 损坏首读抛异常）失败会让后续监听不注册且无感知。拆为独立协程 + `.catch` 兜底，播放模式/上次播放/自动开播逐项 `runCatching`。
5. **HTTP 超时无上限**（`core/request/HttpClient.kt`）：`fetch` 自定义 timeout 只设下限，脚本传超大值会长期挂住请求；改 `coerceIn(1L, 120_000L)`。

**UI 美化 / 可维护性 / 可访问性（5 项）**

6. **公共空态组件 `EmptyHint`**（新增 `ui/components/EmptyHint.kt`）：抽取收藏页/播放队列两处空态，留白统一 56dp，登记进自检符号表。
7. **自定义音源空态**（`SourceScreen.kt`）：零导入空白改为 `EmptyHint` 提示。
8. **收藏页错误提示可关闭**（`FavoriteScreen.kt`/`FavoriteViewModel.kt`）：常驻红字改为 `GlassDialog`，成功清除错误，新增 `clearError()`。
9. **播放队列排序热区 32→48dp**（`PlayQueueScreen.kt`）：补齐全站 48dp 无障碍统一最后一处遗漏。
10. **多选态无障碍语义**（`MyListsScreen.kt`）：选中图标 `contentDescription` 随状态切换「已选中/未选中」。
11. **【版本】** `versionCode` 14 → 15，`versionName` 1.4.0 → 1.4.1，README/CHANGELOG 同步。

### 上一轮已执行（第十一轮 · v1.4.0）

> 延续「在线 + 本地」双模定位，本轮在第十轮基础上做了一轮覆盖正确性、性能与可访问性的综合复审。坚持低风险原则：不触碰 QuickJS 引擎、音源协议与 assets 脚本，全部改动通过 `scripts/selfcheck.py` 静态自检，云端构建由 GitHub Actions 验证。

**正确性 / Bug 修复（5 项）**

1. **搜索无结果误显历史**（`ui/screens/search/SearchScreen.kt`）：历史分支原只判「结果为空」，而搜索后输入框仍保留关键词，导致任何无结果搜索都命中历史并 `return@Column`，「没有找到相关音乐」空态形同虚设。补上 `textFieldState.text.isBlank()` 判断——有输入走空态、清空输入才看历史。
2. **歌单广场错误重复渲染**（`ui/screens/songlist/SongListScreen.kt`）：原在空态分支之前单独渲染一遍红色 error 文案，空态分支内又有 `RetryHint`，加载失败且列表空时同一句错误出现两遍。删除前者，统一由 `RetryHint` 展示。
3. **`fetchSync` 不支持 PUT/PATCH**（`core/request/HttpClient.kt`）：原仅 `post` 且带 body 时才构造 RequestBody，音源脚本通过 SDK 桥用 PUT/PATCH 取链/签名时，OkHttp 因 body 为 null 抛 `IllegalArgumentException` 整次失败，且与异步 `fetch()` 行为不一致。改为 `post/put/patch` 无显式 body 时补空 body，并把方法集合提为 `BODY_METHODS` 常量避免每次请求重建。
4. **版本号比较忽略大写 V**（`core/repository/UpdateRepository.kt`）：`compareVersion` 原只 `removePrefix("v")`，大写 `V1.4.0` 这类 tag 会被解析成 `0.0.0` 误判为旧版。补 `removePrefix("V")`，与同文件 `parse()` 对齐。
5. **定时分钟数 Int 溢出**（`core/player/PlayerRepository.kt`）：`minutes * 60` 为 Int 运算，持久化异常大值会溢出为负使 deadline 立即到期、播放被瞬间暂停。改为 Long 运算后 `coerceAtMost(Int.MAX_VALUE)` 再转 Int。

**性能（4 项）**

6. **在线歌单翻页去重 O(N²)→O(N)**（`core/repository/MyListsRepository.kt`）：`pullOnlineMusics` 原每首歌都线性扫已收集列表判重，翻页到上万首明显卡顿。改用 `HashSet` 记录已见 id，追加时 O(1) 查重并顺带去掉批内重复。
7. **歌单同步/保存按 id 查旧记录 O(N²)→O(N)**（同文件）：`overwriteOnlineMusics`、`saveMusicsLocked` 原在 `mapIndexed` 内对每首歌 `existing.firstOrNull { it.id == ... }`，改为循环外一次 `associateBy { it.id }` 哈希查找。
8. **备份恢复歌单表 O(N) 次写盘→1 次**（`core/repository/BackupRepository.kt`）：原逐条 `deletePlaylist` + N 次 `upsertPlaylist`，每条都全量重写 `playlist.json`。改为只删被移除列表的歌曲文件，再一次性 `replacePlaylists` 覆盖整表（该方法已存在，语义等价）。
9. **HTTP 方法集合常量化**（`core/request/HttpClient.kt`）：见第 3 项，`setOf("post","put","patch")` 由每次请求新建改为伴生对象常量。

**健壮性（2 项）**

10. **「不喜欢」规则写盘静默失败**（`core/repository/DislikeRepository.kt`）：原先更新内存 `_rules.value` 再吞掉写盘异常，造成「界面显示已保存、重启后规则丢失」。改为仅在落盘成功后更新内存；临时文件名加 UUID，与 AppDatabase 一致防并发覆盖。
11. **死代码清理**（`core/repository/MusicRepository.kt`）：删除从未使用的 `sourceMutex` 及其 import，避免误导为已有锁保护。

**UI 美化 / 可访问性（5 项）**

12. **空态视觉统一**：播放队列「队列是空的」（`PlayQueueScreen.kt`）、我的列表清单空态（`MyListsScreen.kt` 的 `ListOfLists`）由左对齐裸文字改为居中 + 40dp 占位图标 + 次要色文案，与收藏页/榜单详情一致。
13. **首页榜单加载提示**（`ui/screens/home/HomeScreen.kt`）：原 `loading=true` 时榜单区一片空白，改为在空列表时区分「正在加载…/加载失败可重试/暂无榜单」三态。
14. **搜索错误重试热区达标**（`ui/screens/search/SearchScreen.kt`）：两处 `Text + clickable + padding(8dp)` 改用公共 `RetryHint`（约 44dp 热区 + 圆角按压反馈），消除重复。
15. **触控热区扩至 48dp**：设置页「不喜欢」删除（约 32→48dp）、首页热搜榜每行（约 30→48dp）。
16. **长按删除可发现性**（`ui/screens/source/SourceScreen.kt`）：自定义音源标题行右侧补 footnote「长按音源可删除」提示。
17. **【版本】** `versionCode` 13 → 14，`versionName` 1.3.9 → 1.4.0，README/ANALYSIS/CHANGELOG 同步。

### 上一轮已执行（第十轮 · v1.3.9）

> 第十轮为全源码复审后的系统性可访问性与空态美化专项：统一全站触控热区到无障碍标准、统一空态视觉语言、补齐无障碍语义，并抽取三处公共抽象消除重复。改动全部落在 UI 层视觉/触控与集合工具，不触碰播放内核、QuickJS 引擎与音源协议。

1. **【美化/可访问性/可维护性】返回键公共组件 `BackIconButton`**（新增 `ui/components/BackIconButton.kt`）：音源、榜单详情、歌单广场、歌单详情、我的列表、播放队列 6 个二级页此前各自用 `Icon(28dp)` 直接挂 `clickable`，热区仅 28dp（低于 Material 48dp 最小目标）且代码重复。统一为 `BackIconButton`——外层 48dp `Box` 承载点击、内层 `Icon` 保持 28dp 视觉尺寸，`contentDescription` 统一「返回」；6 处替换并把符号登记进 `selfcheck.py` 的 `PROJECT_SYMBOLS`。
2. **【可访问性】多处图标/文字热区扩至 48dp**：收藏页、我的列表（列表内 + 列表清单）、播放队列的删除图标（24→48dp）、搜索结果收藏键（24→48dp）、播放队列「清空」文字（约 28→48dp）、播放页错误恢复「下一首」（约 20→44dp 带圆角裁剪）。均为外层 Box 承载点击、内层视觉尺寸不变，交互语义与事件消费行为不变。
3. **【美化】空态视觉语言统一**：收藏页「还没有收藏的歌曲」、我的列表列表内空态由左对齐裸文字改为居中 + 40dp `Favorites` 占位图标 + 次要色文案；**榜单详情**此前搜索无匹配时因空态判断只看 `musicList` 而渲染零条造成「空白列表」，补上专属「没有匹配的歌曲」空态 item 与「正在加载榜单…」加载提示，与歌单详情对齐。
4. **【可访问性】播放页收藏按钮语义**（`ui/screens/player/PlayerScreen.kt`）：`contentDescription` 由固定「收藏」改为随 `favoriteIds` 切换「收藏/取消收藏」，读屏用户可感知当前状态，与搜索页一致。
5. **【可维护性/性能】翻页去重公共扩展 `appendDistinctById`**（新增 `core/model/ListExt.kt`）：榜单详情、歌单详情、歌单广场三处此前重复 `list + result.filterNot { it.id in list.map { x -> x.id } }`，谓词内每次比较都重建 id 集合（O(N·M)）。抽为泛型扩展——id 集合只构建一次（O(N+M)），并用 `seen.add` 顺带去掉新批次内部重复 id，避免重复 key 导致 LazyColumn 崩溃。
6. **【可维护性】重试提示公共组件 `RetryHint`**（新增 `ui/components/RetryHint.kt`）：首页、歌单广场、榜单详情、歌单详情四处 error 文字 + clickable + padding 的重复写法统一为 `RetryHint`（error 色 + 圆角裁剪 + 约 44dp 热区），同样登记进自检符号表。
7. **【整洁】** 合并 `PlayerRepository.kt` 中 `LyricLine` 的两行重复 KDoc。
8. **【版本】** `versionCode` 12 → 13，`versionName` 1.3.8 → 1.3.9，README/ANALYSIS/CHANGELOG 同步。

### 上一轮已执行（第九轮 · v1.3.8）

1. **【UI/UX】歌词手动回看与自动跟随冲突修复**（`ui/screens/player/PlayerScreen.kt`）：此前 `LyricView` 的自动滚动 `LaunchedEffect(currentIndex, ...)` 每切一句歌词就调用 `animateScrollToItem`，用户上滑回看旧歌词时会被立刻拽回当前行，无法阅读。本轮新增 `followCurrent` 跟随开关——`LazyColumn` 上挂 `pointerInput` **只观察 touch down、不消费手势**（`awaitEachGesture`/`awaitFirstDown(requireUnconsumed=false)`），用户手指按下歌词区即置 `followCurrent=false` 暂停跟随，滚动/点击歌词行交互不受影响；暂停时浮出「回到当前歌词」玻璃按钮一键恢复，点歌词跳转（`onSeek`）与切歌（`currentId` 变化）也自动恢复跟随，恢复时 `animateScrollToItem` 立即把当前行滑回居中。
2. **【正确性】时长显示支持小时与负值钳制**（`ui/screens/player/PlayerViewModel.kt`）：`formatTime` 原实现固定 `MM:SS`，超 1 小时音频会显示成 `75:30` 这类不易读的值，且负毫秒会产生乱码。改为 `ms` 钳到非负、按 `H:MM:SS` 格式化超过 1 小时的时长，普通歌曲仍用 `MM:SS`。
3. **【并发健壮性】搜索翻页重复请求竞态**（`ui/screens/search/SearchViewModel.kt`）：`search()` 与 `loadMore()` 原先把 `loading=true` 放在 `viewModelScope.launch` 内异步执行，两次快速触发 `loadMore` 之间、`loadMore` 与新搜索之间都存在守卫失效窗口，可能重复请求同一页。改为在 launch 前**同步置位** `loading` 并清空 `errorMessage`，封住竞态窗口；已有的 `requestSeq` 版本锁继续负责丢弃迟到结果。
4. **【版本】** `versionCode` 11 → 12，`versionName` 1.3.7 → 1.3.8，README/ANALYSIS/CHANGELOG 同步。

### 上一轮已执行（第八轮 · v1.3.7）

1. **【正确性】定时停止冷启动恢复**（`LxApp.kt`）：启动时读取持久化的 `timeoutExitMinutes`，>0 则调用 `startTimeoutExit` 恢复倒计时；采用「重启后按设定分钟数重新计时」语义（不跨重启续算绝对时间），避免隔天打开时定时器已过期或行为不符预期。
2. **【正确性】倒计时抗后台漂移**（`core/player/PlayerRepository.kt`）：定时停止由「每 `delay(1000)` 减 1」改为按 `deadline - System.currentTimeMillis()` 重算剩余秒数，App 退到后台被冻结后 `delay` 被拉长也不再让定时严重变慢。
3. **【可访问性/美化】迷你条图标热区**（`ui/AppRoot.kt`）：播放/队列按钮点击热区从 36/28dp 提至 48dp，用外层 Box 承载点击、内层 Icon 保持原视觉尺寸，避免放大过冲。
4. **【可访问性/美化】播放页控制热区**（`ui/screens/player/PlayerScreen.kt`）：`PlayerIconButton` 热区统一 ≥48dp（主播放键 64dp 保持），次控制间距 28→8dp 保持视觉密度，播放模式标签命中区加大并加圆角裁剪。
5. **【版本】** `versionCode` 10 → 11，`versionName` 1.3.6 → 1.3.7，同步 README。

**质量保证**：所有 `.kt` 改动均通过 `scripts/selfcheck.py` 静态自检；云端构建由 GitHub Actions 验证（推送到 master 自动触发，自检为流水线前置步骤）。

### 上一轮已执行（第七轮 · v1.3.6）

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
- v1.3.6（第七轮）：歌词 offset 时序 + 播放竞态收口 + 设置页/歌单同步性能 + 原子写与 HTTP 并发 + 深色模式与按压反馈等美化 + CI 自检
- v1.3.7（第八轮）：定时停止冷启动恢复 + 倒计时抗漂移 + 迷你条/播放页触控热区与播放模式命中区美化
- v1.3.8（第九轮）：歌词手动回看不被打断（跟随暂停 + 回到当前入口）+ 长音频时长 H:MM:SS + 搜索翻页重复请求竞态修复
- v1.3.9（第十轮）：全站可访问性热区达标（返回键/删除键/收藏键/清空/下一首恢复）+ 空态视觉语言统一 + BackIconButton/RetryHint/ListExt 公共抽象抽取
- v1.4.0（第十一轮）：正确性修复（搜索空态/重复错误/PUT-PATCH 取链/版本号 V 前缀/定时溢出）+ 多处 O(N²)→O(N) 与写盘合并 + 规则落盘一致性 + 空态统一与热区达标等美化
- v1.4.1（第十二轮）：备份去重防崩溃 + 暂停删歌/写盘乱序修复 + 启动链健壮性 + HTTP 超时上限 + EmptyHint 空态组件化 + 音源空态/错误对话框/排序热区/多选语义等美化
- v1.4.2（第十三轮）：检查更新多源兜底 + 最大版本选取 + 新版本下载入口
- v1.4.3（第十四轮）：对照原版 lx-music-mobile v1.9.2 对齐默认设置、定时退出、禁用切歌、出错自动切歌、搜索历史与不喜欢匹配
- v1.4.4（第十五轮）：返回转场方向修正 + 播放队列律动音柱 + 全站列表增删/排序动画 + 弹窗/歌词浮层/图标进场过渡 + 迷你条背景降级与进度平滑 + 多选底栏过渡与窄屏滚动
- v1.5.0（第二十一轮）：列表显示设置（来源/专辑/时长）在歌单详情与搜索结果生效 + 设置页初值对齐落盘默认
- v1.5.1（第二十二轮）：我的列表接入三项显示设置 + 当前播放曲目律动指示
- v1.5.2（第二十三轮）：通知栏封面开关（原版 isShowNotificationImage）+ 迷你条封面切歌淡入
- v1.5.3（第二十四轮）：榜单详情接入三项显示设置 + 播放页「暂无歌词」统一空态
- v1.6.0（第二十五轮）：桌面歌词悬浮窗 + 主题主色 + 歌词字体阴影 + 迷你条文字切歌淡入
- v1.7.0（第二十六轮）：播放缓存 + 歌词/链接元数据缓存 + 列表音质角标
- v1.7.1（第二十七轮）：播放队列接入列表显示设置 + 搜索页音质角标去重与换源刷新 + 三处长按读屏语义化 + 音源导入按钮响应式
- v1.7.2（第二十八轮）：导出自定义音源脚本 + 首页热搜列表接入统一动效 + 桌面歌词对齐标题消歧
- v1.7.3（第二十九轮）：音源列表展示脚本 @description + 版本号补 v 前缀 + 搜索页初始欢迎语 + 联想词单行截断
- **v1.7.4（本轮 · 第三十轮）**：播放缓冲状态提示（对齐原版 Status.tsx）+ 翻译首帧默认值对齐 + 罗马音/翻译副行字号随主歌词联动

---

## 六、后续建议路线图（未纳入本轮）

以下项收益明确但改动面/风险较大（多涉及 QuickJS 引擎并发或播放时序，本地无 Java 无法编译验证），留待具备真机/编译验证条件时实施：

1. 换源取词/取链改 `async` 并发，取首个有效结果即取消其余，降低最坏等待（涉及引擎并发调用，需验证线程安全）。
2. `restoreLastPlay` 对过期的在线签名 URL 先异步重解析再灌入播放器，避免冷启动首播失败一次（涉及时序，需真机验证）。
3. AppDatabase 所有「读改写」统一加 DB 级 `Mutex`，彻底消除并发覆盖（当前已用 UUID 临时名隔离低风险子集）。
4. 后台播放 `MediaSession` 的会话令牌与锁屏控件封面同步（当前 toMediaItem 已提供 metadata，可进一步对齐原版通知样式）。

> 本报告随源码一同维护。详细变更记录见 [CHANGELOG.md](CHANGELOG.md)。
