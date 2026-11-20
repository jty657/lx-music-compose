# 更新日志

本项目为洛雪音乐 Compose 移植版「聆序」。以下记录基于对全部源码的系统审查所执行的优化与美化。

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

## 质量保证

- 所有改动均通过 `scripts/selfcheck.py` 静态自检
- 每次推送均由 GitHub Actions 云端构建验证（本地无 Java 环境，不本地编译）
- 构建产物：APK artifact（约 15 MB）
