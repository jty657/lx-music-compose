# 更新日志

本项目为洛雪音乐 Compose 移植版「聆序」。以下记录基于对全部源码的系统审查所执行的优化与美化。

## 第八轮：定时恢复 + 抗漂移 + 触控热区美化（v1.3.7，本次）

> 项目定位：**同时具备「在线」与「本地」功能的音乐播放器**。以下改动覆盖播放内核与界面，在线/本地双模共用，不影响本地数据。

### 正确性
- **定时停止冷启动恢复**（`LxApp.kt`）：读取持久化的 `timeoutExitMinutes`，重启后自动恢复定时倒计时。语义为「按设定分钟数重新计时」（不跨重启续算绝对时间），避免隔天打开时定时器行为异常。
- **倒计时抗后台漂移**（`core/player/PlayerRepository.kt`）：由「每次 delay 减 1」改为按绝对到期时间 `deadline - now` 重算剩余秒数，App 退到后台被冻结后不再因协程延迟拉长而定时变慢。

### 美化 / 可访问性
- **迷你播放条热区**（`ui/AppRoot.kt`）：播放/队列按钮点击热区 36/28dp → 48dp，外层 Box 承载点击、内层 Icon 保持原尺寸。
- **播放页控制热区**（`ui/screens/player/PlayerScreen.kt`）：`PlayerIconButton` 热区统一 ≥48dp（主播放键 64dp 不变）；次控制间距 28→8dp 保持视觉密度；播放模式标签命中区加大并加圆角裁剪。

### 工程
- 版本 1.3.6 → 1.3.7（versionCode 10 → 11），README/ANALYSIS 同步；全部 `.kt` 改动通过 `scripts/selfcheck.py` 自检，CI 构建验证。

## 第一轮：系统能力对齐 + 全面优化（c918d77）

### 系统能力（对齐原版反编译报告）
- 新增忽略电池优化：申请权限 + 设置页入口，保障后台持续播放
- 新增应用内检查更新：UpdateRepository（version.json / GitHub Releases 双格式解析 + 版本比较）
- 新增设备信息展示：设备名 / 型号 / ABI / 系统版本 / 语言

### 核心层修复
- **播放器**：错误提示不再被状态回调瞬间抹掉；进度轮询仅在播放时进行，避免后台每 500ms 触发全量重组；切歌先清旧封面避免残留；`release()` 补全 job 清理；删除冗余的 shuffle 赋值
- **脚本引擎**：`awaitInit` 失败快速抛错，避免二次超时白等；`destroyScript` 唤醒等待方；用 LAZY 启动消除请求入表竞态；`destroy` 改用 `scope.cancel()` 彻底关闭；安全处理 native call 参数
- **内置 SDK 桥**：`destroy` 取消全部定时器并关闭线程池，避免资源泄漏
- **设置仓库**：用 `runCatching` 修复 `blurRadius` 类型不兼容导致的升级崩溃
- **备份仓库**：拒绝空 / 损坏备份导入，避免误清空用户收藏与歌单

### UI 修复与美化
- 播放页改为全屏沉浸，不再叠加迷你条与底栏
- 播放队列 key 去除 index，避免排序 / 删除后整表重组；上下移箭头图标化
- 搜索页音源按钮加选中态、补搜索空态、结果列表加 weight
- 多个列表页 LazyColumn 加 weight，防止操作栏被挤出
- 封面加载中加音符占位；顶栏深色板色统一

## 第二轮：剩余问题清理（本次）

### UI 体验
- **首页**：修复「有热搜时排行榜被完全屏蔽」——热搜与榜单现在并存，按权重分配空间
- **设置页**：补齐 GlassTopBar，与其它二级页顶栏样式统一
- **弹窗**：GlassDialog 支持纵向滚动，避免选项过多时小屏内容被截断
- **播放页**：暂停时呼吸动画改用有限动画，不再空转重复

### 核心层
- **歌单聚合搜索**：由串行改为并发（与音乐搜索一致），最坏耗时从 5× 单源超时降为单源超时
- **HTTP fetch**：由每次请求新建裸线程改为固定线程池（4 线程），避免高频搜索线程泛滥
- **native call 参数**：改为安全取值，脚本传参异常不再打断 JS 线程

### 数据层
- **损坏文件隔离**：音源列表 / 歌单 JSON 解析失败时，把损坏文件改名备份（`.corrupt.<时间戳>`），避免下次写入把空数据覆盖回原文件造成用户数据无声丢失
- **并发可见性**：给两个加载标志加 `@Volatile`，消除多 IO 线程间的可见性竞态

## 第三轮：全量源码复审 + 数据健壮性 + 性能 + UI 打磨（本次）

本轮对全部源码逐模块复审，除美化外修复了多个此前遗漏的真实缺陷。

### 数据安全（核心 bug 修复）
- **备份导入（严重·数据安全）**：`BackupData.version` 有默认值且旧序列化器未开 `encodeDefaults`，导致「空文件检测」用的 `version<=0` 永远不成立——空 JSON `{}` 会被当作合法备份导入并清空全部收藏与歌单。改为 decode 前先做 JSON 结构校验（必须是对象且含数据键），并开启 `encodeDefaults` 让 `version` 真正落盘；单歌单分享文件同步处理。
- **损坏文件隔离**：`getPlaylistMusics` / `loadFavorites` / `loadSearchHistory` / `loadLastPlay` 原先解析失败静默当空、下次写入覆盖；统一复用 `readJsonOrNull`（解析失败改名 `.corrupt.<时间戳>` 隔离），损坏文件不再被无声覆盖。

### 引擎与网络
- **Promise 等待**：`BuiltinSdkBridge.awaitPromise` 加 `AtomicBoolean` 一次性守卫 + `invokeOnCancellation` 清理，杜绝超时/取消后 JS 回调重复 resume 与 continuation 泄漏；整段包 `runCatching`，SDK 线程异常不再外泄。
- **定时器桥**：`QuickJSEngine` 的 `set_timeout` 参数改为安全取值（`getOrNull` + 类型判断 + 非负钳制）并包 `runCatching`，异常脚本传参不再打断 JS 线程。
- **音质列表**：`MusicRepository` / `ScriptEngineManager` 解析 `qualitys` 时用安全类型转换，遇到非字符串元素不再抛异常中断协程。
- **磁盘缓存**：移除 `Cache.delete()`（会关闭底层 DiskLruCache 导致缓存永久失效），清缓存改为仅 `evictAll()`。

### 播放稳定性
- **队列去重**：`addToQueue` 按歌曲 id 去重，避免「加入播放队列」把已在队列的歌重复加入、造成 LazyColumn 重复 key 崩溃。

### 性能
- **歌词重组隔离**：`LyricView` 改为只接收 `lyricLines / currentIndex / currentId` 等必要参数，而非整个 `PlayState`；播放进度每 500ms 变化不再触发歌词列表全量重组。

### 设置与 UI 美化
- **倍速/音量落盘**：节流写盘补上「离开设置页提交最终值」（pending + DisposableEffect），修复原先 `commit*` 无调用方、最后调整丢失的问题；播放器内设置弹窗滑块统一为 Miuix 风格。
- **分区标题**：设置页分组标题放大一档 + primary 色 + 间距，层次更清晰。
- **图标热区**：播放队列上移/下移图标点击热区从 20dp 扩到 32dp。
- **搜索历史**：改为 `weight + verticalScroll`，最多 30 条历史在小屏上也能完整查看；删除重复间距。
- **代码整洁**：修正 AppRoot 迷你播放条、首页榜单、搜索/音源/榜单/歌单等页面多处编辑残留的缩进错乱；清理收藏页重复 import。

## 第四轮：全量复审（数据安全补漏 + 播放竞态 + 网络与引擎健壮性 + 交互打磨）（本次）

本轮再次对全部源码逐模块复审，修复了前几轮遗漏的若干真实缺陷，并做了多处体验美化。

### 数据安全补漏
- **不喜欢列表损坏隔离**：`DislikeRepository.load` 原先解析失败静默当空，下一次写入会把「空规则 + 新规则」覆盖回损坏文件，用户全部「不喜欢」规则无声丢失。改为解析失败时把文件改名 `.corrupt.<时间戳>` 隔离（与 AppDatabase 同类机制对齐）。
- **列表 id 碰撞**：`MyListsRepository` 三处创建列表的 id 只用毫秒时间戳，同毫秒连续创建/收藏会撞 id 导致列表互相覆盖丢失。改为时间戳 + 随机后缀（与 `ApiSourceRepository` 一致）。
- **在线列表同步条数**：`overwriteOnlineMusics` 返回过滤前的条数，导致进度/提示虚高；改为返回实际写入（过滤后）的条数。

### 播放稳定性
- **切歌保存正确曲目**：`next()/prev()` 原先在 `seekTo` 后立即 `saveLastPlay()`，此刻 `state.current` 仍是切歌前的旧歌，导致冷启动恢复定位到错误的歌。改为在 `onMediaItemTransition`（state 已更新）中统一保存。
- **切歌后旧解析协程误播**：按需解析 URL 的协程仅在 state 更新时校验了歌曲 id，但随后的 `replaceMediaItem/play` 在校验之外，快速切歌时会把旧歌 URI 塞进新歌位置造成误播。把播放器操作一并纳入 id 校验。
- **playQueue 去重与竞态**：在线榜单/歌单可能含重复歌曲 id，直接入队会让以 id 为 key 的队列列表抛 "Duplicate key" 崩溃；入队前按 id 去重并重算下标。同时用 Job 取消上一次未完成的解析，避免快速连点时旧请求后到覆盖用户最新选择。
- **歌词负时间戳**：LRC 的 `offset` 为正且歌词靠前时，`ms-offset` 可能为负，导致第 0 秒就高亮首行；钳制到 0。

### 引擎与网络健壮性
- **HTTP 连接复用**：`HttpClient.fetch` 原先每次请求都 `newBuilder().build()` 新建 client（独立连接池/线程池），高频搜索与封面加载时资源堆积。改为按超时值缓存复用 client。
- **fetch 回调竞态**：`BuiltinSdkBridge.destroy()` 关闭线程池后，仍在途的 fetch 回调直接向已关闭的 executor 提交会抛 `RejectedExecutionException`。新增安全投递守卫，销毁期间的迟到回调安全沉默。

### UI 交互与美化
- **设置滑块回弹**：倍速/音量/模糊强度滑块原先被 `LaunchedEffect` 用节流回写的持久化值回灌，拖动时滑块被拽回旧值产生抖动。改为只用本地值驱动，最终值在离开页面时提交（与播放器内设置弹窗一致）。
- **迷你播放条状态**：`miniBarVisible` 由 `LaunchedEffect` 写回 state 改为 `derivedStateOf` 派生，消除切页瞬间闪旧值与多余重组；tab 切换改用 `Navigator.setRoot()` 一次性重置，避免两次快照变更造成的动画抖动。
- **歌单详情空态**：加载中显示进度提示、无数据时显示空态文案、空列表禁用「播放全部」按钮，用户不再面对空白页或无反馈的点击。
- **搜索页**：移除与列表内重试项重复的顶部错误红字；歌单结果滚动也上报给迷你播放条自动隐藏。
- **收藏页**：删除按钮的 `interactionSource` 改为 `remember` 创建，避免每次重组新建对象丢失按压状态。

## 第五轮：全量复审（数据安全补漏 + 资源治理 + 图标语义 + 交互细节）（2026-11）

本轮再次对全部源码逐模块复审，在已有四轮优化基础上，修复了 3 处静态可确认的遗留缺陷，并做了 UI 图标与交互细节打磨。

### 数据安全
- **新建歌单 id 冲突**：`MyListsRepository.create()` 此前只用毫秒时间戳作 id，与同文件的 `collectSongList` / `collectBoard` 不一致；同毫秒连续新建两个歌单会撞 id、后者覆盖前者导致歌单静默丢失。id 改为「时间戳 + 随机后缀」，三处生成策略统一。
- **不喜欢列表竞态**：`DislikeRepository` 此前无 `loaded` 守卫也无锁，冷启动异步 `load()` 未完成就添加规则会用「空列表 + 新规则」覆盖磁盘已有规则，并发 `add` 也会丢更新。参照 `FavoritesRepository` 引入 `Mutex` + `loaded` 守卫，`load/add/remove` 全部纳入临界区，读取逻辑抽为 `loadLocked()` 复用。

### 资源治理
- **HTTP client 无界增长**：`HttpClient.clientForTimeout()` 以脚本传入的任意 timeout 为 key 缓存 client，每个 client 带独立连接池/线程池；第三方音源用递增 timeout 取链/取词时会无界创建。增加容量上限（8 档），已缓存档位继续复用，新档位达上限后退回默认 client，杜绝资源累积。

### UI 美化与交互
- **首页入口图标语义化**：此前「音源管理 / 我的列表 / 歌单广场」三个入口都用 `Layers` 图标，语义混乱。改为音源管理=`File`、我的列表=`Layers`、歌单广场=`Music`，三个入口一眼可辨（均为项目已依赖图标，无新增依赖）。
- **搜索历史「清空」热区**：仅靠文字高度约 20dp，低于 48dp 可访问性下限；上下各补 10dp padding，手指更易点中。

## 第七轮：全量复审（歌词时序 + 播放竞态 + 重组性能 + 深色模式美化）（v1.3.6，本次）

本轮对全部源码逐模块复审（core 层与 UI 层并行审查），修复 2 处正确性/时序问题、2 处性能热点、2 处并发健壮性问题，并完成多项 UI 美化与工程改进。完整分析见 [ANALYSIS.md](ANALYSIS.md)。

### 正确性 / 时序
- **歌词翻译/罗马音静默不显示**（`LrcParser`）：翻译与罗马音各自解析自身文本的 `[offset:]`，而多数音源只在主歌词带 offset。两者时间轴整体错位，合并时 `translations[line.timeMs]` 全部落空。改为 `parseSingle(lrc, offsetOverride)`，翻译/罗马音继承主歌词 offset，时间轴对齐后翻译/罗马音正常显示。
- **清空队列后被旧请求复活**（`PlayerRepository`）：播放 URL 解析是挂起操作，`clear()`/`release()` 后旧协程仍会回填队列并 `replaceMediaItem`/`play`，甚至操作已释放的播放器。新增 `playGeneration` 代号，playQueue/clear/release 递增并取消在途 job；回填前校验代号，过期结果直接丢弃；clear/release 同步清理 `resolvingIndexes`。

### 性能
- **设置页整页重组**（`SettingScreen`）：原订阅整个 `PlayState`，播放进度每 500ms 更新就让本页几十个 Preference/Slider 全量重组。改为只订阅 `state.map { it.timeoutExitRemaining }`。
- **歌单同步 O(N²) 写盘**（`AppDatabase` / `MyListsRepository`）：`persist` 原逐条 `upsertPlaylist`（每次全量重写 playlist.json），`overwriteOnlineMusics` 又调两次。新增 `replacePlaylists` 一次落盘并合并字段更新，写盘从约 2N 次降为 1 次。

### 稳定性 / 并发
- **原子写临时文件名冲突**（`AppDatabase`）：并发写同一文件的 `.tmp` 会互相覆盖、产生残缺 JSON（进而被判损坏隔离）。临时名加 UUID，最后写入者胜出，保证落盘完整。
- **HTTP client 容量竞态**（`HttpClient`）：自定义超时 client 的 `size>=8` 检查与 `getOrPut` 非原子，并发下可瞬时超上限。纳入 `synchronized` 块。

### UI 美化
- **卡片按压反馈**（`Glass.kt`）：`GlassSurface` 点击从 `indication=null` 改为 `LocalIndication.current`，全 App 卡片获得涟漪反馈，外层 clip 保证不溢出圆角。
- **深色模式启动过渡**（`values-night/themes.xml` + `themes.xml` + `AppRoot`）：新增夜间主题，暗色启动窗口避免白屏闪烁；状态栏图标极性按运行时主题同步，深色主题下图标不再不可读。
- **歌单搜索空态**（`SearchScreen`）：歌单模式无结果不再误显「没有找到相关音乐」，改为专属空态。
- **播放页错误恢复**（`PlayerScreen`）：错误提示由单行截断改为两行，并附「下一首」出口。
- **无效按钮替换**（`PlayerScreen`）：「加入播放队列」对正在播放的歌是空操作，改为「复制歌曲信息」。
- **设置页层级**（`SettingScreen`）：「字体大小」改用分区标题样式，系统能力项补「系统」分区标题。

### 工程
- **CI 静态自检**（`.github/workflows/build.yml`）：构建前先跑 `python3 scripts/selfcheck.py`，提前拦截云端才会暴露的低级错误。

### 版本
- `versionCode` 9 → 10，`versionName` 1.3.5 → 1.3.6。

## 第六轮：全量复审（搜索竞态 + 歌词性能 + 热搜美化）（v1.3.5）

本轮再次对全部源码逐模块复审，修复 1 处并发正确性问题、1 处性能热点，并做了 1 处 UI 美化。完整分析见 [ANALYSIS.md](ANALYSIS.md)。

### 正确性
- **搜索结果竞态**：`SearchViewModel.search()` / `loadMore()` 此前对并发请求无守卫，快速切换关键词或音源时，旧请求可能后到并覆盖当前列表（表现为「搜了 B 却显示 A 的结果」）。引入 `requestSeq` 版本锁，仅最新批次可写回结果、错误与 `loading`，翻页也受同一守卫保护。

### 性能
- **歌词高亮定位**：`PlayState.currentLyricIndex` 由逐行线性扫描改为升序二分查找；进度每 500ms 更新、歌词可达数百行时，定位成本从 O(n) 降为 O(log n)。

### UI 美化
- **热搜榜视觉层级**：首页热搜前三名序号与歌名改用主色强调、字号放大一档，其余名次弱化为次要色，榜单重点更清晰（此前所有名次同色，无视觉重点）。

### 版本
- `versionCode` 8 → 9，`versionName` 1.3.4 → 1.3.5。

## 质量保证

- 所有改动均通过 `scripts/selfcheck.py` 静态自检
- 每次推送均由 GitHub Actions 云端构建验证（本地无 Java 环境，不本地编译）
- 构建产物：APK artifact（约 15 MB）
