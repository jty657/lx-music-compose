# 更新日志

## 第五十三轮：搜索加载态与字号实时预览（v1.9.2，本次）

> 对照原版 lx-music-mobile v1.9.2 做低风险「功能差异 + UI/动效」打磨。仅修改
> `SearchScreen.kt` / `SettingScreen.kt` 两个源码文件，`scripts/selfcheck.py` 通过；按项目约定
> 构建由 GitHub Actions 云端执行。零新增依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### UI / 动效

- **搜索首屏加载态补齐**（`SearchScreen.kt`）：首次搜索空窗期（`loading && !hasMore && 无错误 &&`
  结果/歌单均为空）此前 `musicEmpty` 与歌单空态分支都因 `!loading` 被排除，整块空白像「搜索没反应」。
  现复用全站 `LoadingHint`（三点流水灯，`LocalReduceMotion` 时静帧），拿到结果自然消失；已有旧结果
  时不显示，避免换源/二次搜索列表闪动。
- **字号实时预览**（`SettingScreen.kt`）：字号档位下方加一行主色示例文字「字体大小预览 Aa 聆序音乐」。
  固定基准 `16.sp`，经 `LxTheme` 注入的 `LocalDensity` 随全站 `fontScale` 即时缩放，点档位当帧可见，
  对齐原版 `FontSize.tsx` 的 `SizeText`；纯展示、单行省略、不参与交互。

### 假设与待确认

- 加载提示文案「正在搜索…」为移植版措辞，原版用下拉刷新指示器而非内联提示；语义等价，无命中区变化。
- 预览固定 16.sp 基准仅用于展示缩放比例，不代表正文最终字号；如需与正文一致可改用 `body2` 字号。
- 歌词边缘渐隐、进度条缓冲段、自动滚回当前歌词、下拉刷新等差异经评估需实验性 API / 改播放内核 / 新依赖，
  本轮按低风险原则跳过，详见 ANALYSIS.md。

## 第五十二轮：热搜首帧与联想退场打磨（v1.9.2）

> 对照原版 lx-music-mobile v1.9.2 做低风险「功能差异 + UI/动效」打磨。仅修改
> `HomeScreen.kt` / `SearchScreen.kt` 两个源码文件，`scripts/selfcheck.py` 通过；按项目约定
> 构建由 GitHub Actions 云端执行。零新增依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### UI / 动效

- **首页热搜首帧不再闪烁**：`isShowHotSearch` 订阅初值 `true` → `false`，与落盘默认和原版
  `defaultSetting` 一致（此前冷启动会先闪出热搜再消失）。
- **搜索联想下拉补齐退场淡出**：改用 `AnimatedVisibility` 统一进出场（`fadeIn`/`fadeOut`，
  `MotionBaseMs` 240ms），只动 alpha；`LocalReduceMotion` 时 0ms 静帧。退场期间保留最后一版
  非空词条，避免淡出空卡片。

### 假设与待确认

- 退场时长沿用全站 240ms；如需更快可改为 `MotionFastMs`（160ms）。
- 本轮为交互打磨而非与原文 1:1；不改联想词点击、热搜点击与读屏语义。
- 横屏独立歌词字号因移植版无横屏歌词布局，未纳入本轮。

## 第五十轮：深色模式隐藏背景（v1.9.2）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」低风险补齐。本轮对齐原版
> `theme.hideBgDark`，修改 4 个源码文件，通过 `scripts/selfcheck.py`；构建按约定走 GitHub Actions
> 云端。零新增依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能（对齐原版设置项）

- **深色模式隐藏背景**（`theme.hideBgDark`，原版 `themes/index.ts` 在 `isDark && hideBgDark` 时不绘制
  主题背景大图）：移植版此前缺失该键。本轮新增偏好键、Flow、setter（默认 false，与原版一致）、
  ViewModel StateFlow/setter。设置页「基本设置」在「播放页动态背景」开启后追加「深色模式隐藏背景」
  开关；播放页在 `dynamicBg && !(hideBgDark && darkTheme)` 时取封面主色，深色 + 开启时隐藏这层渐变。
  移植版背景是封面主色渐变（原版是主题背景大图），语义等价映射。

### 视觉 / 动效 / 可访问性

- 淡入淡出复用 `dynamicBg` 的 `animateColorAsState` 320ms 通道：`coverTint` 变 null 时 `bgColor`
  平滑过渡到透明，渐变整层淡出，而非在开关切换处硬切；`LocalReduceMotion` 时 0ms 静帧。
- 渐变层仅在 `dynamicBg` 开启时常驻承载动画，透明渐变无可见绘制；开关沿用全站 `SwitchPreference`
  与 Miuix 语义色，无新增主题变量、无逐帧动画、无布局抖动。

### 假设与待确认

- 原版隐藏的是主题皮肤背景大图，移植版无背景图概念，映射为隐藏播放页封面主色渐变；若后续引入
  真正的主题背景图，需把该开关同时接到背景图上。
- 该开关仅在 `dynamicBg=true` 时显示并生效（无背景时开关无意义），原版开关始终可见，交互形态
  略有差异、语义等价；默认值 false 与原版一致，老用户升级零突变。

## 第四十八轮：关于页开源项目入口（v1.9.2）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」低风险补齐。本轮对齐原版
> `About.tsx` 缺失的开源地址 / 最新版下载 / 常见问题入口，仅修改 `SettingScreen.kt` 一个源码文件，
> 通过 `scripts/selfcheck.py`；构建按约定走 GitHub Actions 云端。零新增依赖、不改路由/状态管理/
> 播放内核、不触碰 QuickJS 与音源协议。

### 功能（对齐原版「关于」页）

- **开源项目入口**（原版 `settings/About.tsx`）：原版「关于」页给出开源地址、最新版下载、
  常见问题、许可协议等外链，移植版此前只有版本号与设备信息，用户无从查看项目出处与获取帮助。
  本轮在「关于」分区新增「开源项目」入口，弹窗内汇总四条链接（移植版仓库、原版仓库、
  最新版下载、常见问题），点击经系统浏览器打开（复用检查更新的 `ACTION_VIEW` 模式）。

### 视觉 / 交互 / 可访问性

- 复用全站 `ArrowPreference` + `GlassDialog` + `TextActionButton`：入口与设置项同一视觉层级，
  链接按钮自带按压缩放（`Motion.press`）、48dp 命中区、`Role.Button` 读屏语义、
  `LocalReduceMotion` 静帧降级，符合减弱动效与低性能设备约定。
- 每条链接独立 `runCatching`，单个链接无浏览器时不弹窗崩溃、不影响其它项；纯 UI 层改动，
  不读写偏好键、不改任何业务逻辑。

### 假设与待确认

- 外链地址按当前仓库实际地址填写（移植版 `jty657/lx-music-compose`，原版 `lyswhut/lx-music-mobile`）；
  若仓库迁移需同步更新 `aboutLinks`。
- 原版「关于」页的付费免责声明与「许可协议」长文未逐字移植（篇幅大、无现成文案资源），
  本轮以 shorter 说明 + 外链替代，语义等价、风险更低；如需 1:1 文案后续单独评估。

## 第四十七轮：点歌切换列表开关（v1.9.2）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」低风险补齐。本轮对齐原版一个设置项
> `list.isClickPlayList`，修改 5 个源码文件，通过 `scripts/selfcheck.py` 与本地 `assembleDebug`；
> 构建按约定走 GitHub Actions 云端。零新增依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能（对齐原版设置项）

- **点歌切换到当前列表**（`list.isClickPlayList`，原版 `settings/List/IsClickPlayList.tsx`）：原版设置项
  「点击列表里的歌曲时自动切换到当前列表播放（仅对「歌单」和「排行榜」有效）」此前移植版缺失。
  本轮新增偏好键、设置页开关，并在歌单详情 / 榜单详情的列表点歌分支接入：
  - **默认开启**：保持移植版既有「点歌即用整个列表作为队列」行为，老用户升级零突变。
  - **关闭**：点歌只播放单曲（`playSingle`），不替换当前播放队列。
  - 「我的列表」收藏不在该开关作用域（与原版一致），保持原样。
- **播放全部修正**（`BoardDetailScreen.kt`）：榜单「播放全部」此前复用点歌方法，开关关闭时会退化为
  单曲；本轮改为始终 `playQueue(queue, 0)`，确保整队列播放不受开关影响。

### 视觉 / 交互 / 可访问性

- 设置开关沿用 Miuix `SwitchPreference` 与全站分区样式，位于「列表设置」区「记住列表位置」之后；
  副标题就近说明两种行为，无新增主题变量、无逐帧动画、无布局抖动。

## 第四十六轮：运行日志查看/清空（v1.9.1）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮新增 1 个源码文件、
> 修改 3 个源码文件，通过 `scripts/selfcheck.py` 静态自检，构建按约定走 GitHub Actions 云端。
> 零新增依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能（原版缺失项）

- **应用内运行日志**（新增 `core/system/AppLog.kt`，对齐原版 `utils/log.ts` 的 getLogs / clearLogs 与
  `settings/Other/Log.tsx` 查看弹窗）：原版把音源脚本 console 与错误日志写入文件，设置页可查看/清空；
  移植版此前只有「记录自定义源日志」开关，日志仅打到 logcat，普通用户无法在机内查看，音源导入/
  搜索失败难以自查。本轮提供纯内存、有上限（最近 300 行）、线程安全（ConcurrentLinkedDeque +
  `@Synchronized` 时间戳格式化的环形缓冲，单行截断 500 字符），零依赖、零落盘、不扩大备份/隐私面。

### 接线（旁路、不碰核心）

- **音源脚本 console**（`ScriptEngineManager.kt`）：`ScriptAction.Log` 分支在原有 `scriptLogEnabled`
  的 logcat 输出之外，把同一条 `runCatching { AppLog.append(...) }` 镜像到缓冲；日志设施异常绝不影响引擎。
- **内置 SDK console**（`BuiltinSdkBridge.kt`）：`setConsole` 的 log/info/warn/error 四个回调经私有
  `mirror()` 旁路写入缓冲，便于查看搜索/取词/取链实际输出；logcat 行为与 SDK 初始化流程零变化。
- **设置页**（`SettingScreen.kt`）：「其他」分区新增「运行日志」入口（副标题带当前条数），点击取一次
  快照打开查看弹窗；弹窗限高 360dp 可滚动、空态有提示，底部「清空」（红色 `TextActionButton`）与
  「完成」。快照式展示避免日志持续写入导致文本跳动；弹窗进场沿用 `GlassDialog` 的 alpha 淡入。

### 视觉 / 动效 / 可访问性

- 复用全站设计语言：`ArrowPreference` 入口、`TextActionButton` 按压反馈（48dp 热区、`Role.Button`
  读屏语义、`LocalReduceMotion` 静帧降级）、`GlassDialog` 统一进出场；未新增主题变量或逐帧动画。
- 限高滚动只影响自身弹窗，不触发布局抖动；长日志在弹窗内滚动，不撑破屏幕。

### 已知差异 / 待确认

- 原版日志落盘并可跨重启保留、含「同步错误日志」开关与网络上报；本轮为内存缓冲、仅本次运行可见、
  重启清空（对应副标题文案已注明）。落盘需新增文件读写与损坏隔离，风险偏高，留待后续单独立项。
- 原版 `getLogs` 按 `----lx log----` 分段并倒序展示；本轮为简单的时间戳单行列表，语义等价但格式不同。

## 第四十五轮：搜索页热搜与历史并列显示（v1.9.0）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮仅
> `SearchScreen.kt` 一个源码文件改动，通过 `scripts/selfcheck.py` 静态自检，构建按约定走 GitHub
> Actions 云端。零新增依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **热搜 + 历史并列显示**（`SearchScreen.kt` / 空态引导区）：原版搜索页 `BlankView` 在「显示热搜」
  `search.isShowHotSearch` 或「显示历史」`search.isShowHistorySearch` 任一开启、且输入为空/未搜索/
  无结果时，热搜在上、历史在下并列展示。移植版此前热搜块与历史块是两个各自 `return@Column` 的
  独立分支，历史优先命中后热搜永远不渲染，两开关同开时热搜被吞掉。本轮把两块收拢为一次
  `showHotBlock || showHistoryBlock` 判断、并列渲染：热搜在前（对齐原版 `HotSearch` 位于
  `HistorySearch` 之前），历史在后继续用 `.weight(1f).verticalScroll()` 承载最多 30 条记录。
  两块的原始触发条件逐字保留（`blankNoResults` 仅抽出公共部分），空白结果/空态语义零变化。

### 动效 / 视觉 / 可访问性

- **进场动效统一**：热搜与历史共用一个 `animateFloatAsState`（`MotionBaseMs`，仅动 alpha，不触发
  布局抖动），`LocalReduceMotion` 时 0ms 静帧到位；此前仅热搜有淡入、历史为硬切。
- **并列层级间距**：仅当两块同时显示时，在热搜与历史之间插入 12dp 间距；仅一块显示时不产生多余
  空白，避免单独历史/单独热搜场景出现空档。历史词条的 `historyRowClickable`（按压缩放 + 读屏语义）、
  热搜词条的 `FilterChip`、`TextActionButton`「清空」按钮全部保持不变。

### 已知差异 / 待确认

- 原版历史与热搜同处一个 `ScrollView`，两块均可整体滚动；移植版历史用 `.weight(1f)` 占满剩余空间
  单独滚动、热搜固定在上（横向 `LazyRow`）。两开关同开时的可见性与顺序已对齐，滚动容器结构差异
  为移植版既有布局，本轮未改（避免触碰外层 `Column` 布局与滚动上报逻辑）。
- 未引入长按手势、未改历史 30 条上限与清空逻辑。

### 工程

- 版本 1.8.9 → 1.9.0（versionCode 46 → 47）；README / ANALYSIS 同步。

## 第四十四轮：定时停止支持任意分钟（v1.8.9，上一轮）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮仅
> `SettingScreen.kt` 一个源码文件改动，通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub
> Actions 云端。零新增依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **定时停止自定义分钟**：原版 `TimeoutExitEditModal` 允许输入任意分钟数，移植版此前「定时停止
  播放」弹窗仅提供 15/30/45/60/90/120 六档固定值。本轮在固定档位下方新增「自定义分钟（1-1440）」
  输入框：
  - 数字键盘、单行、`OutlinedTextField` 内联错误提示（`isError` + `supportingText`）；
  - 合法值（1-1440，即 24 小时内）走既有 `viewModel.setTimeoutExit(min)`，底层 `PlayerRepository.
    startTimeoutExit(Int)` 本就支持任意分钟并做 `Long` 运算溢出钳制；
  - 非法输入时「开始定时」按钮不响应，错误就地提示，不额外弹窗、不改动播放状态；
  - 每次打开弹窗清空上次输入（对齐原版每次从空输入开始）。

### 动效 / 交互 / 可访问性

- 「开始定时」复用全站公共 `TextActionButton`：按压缩放（`Motion.press`）、48dp 命中区、
  `Role.Button` 读屏语义、`LocalReduceMotion` 静帧降级，与设置页「删除」规则、搜索页「清空」
  历史同一手感；无新增主题变量、无新增逐帧动画。

### 已知差异 / 待确认

- 自定义分钟上限取 1440（24 小时）为保守约束；原版理论可输入更大值，但超大值在移动端无实际
  意义且受 `setTimeoutExit` 钳制，未放开。如需与原版完全一致（无上限），后续单独评估。
- 原版时间编辑弹窗内含「播完再退」选项，移植版该开关已在弹窗外独立提供（「播完当前歌曲再停」），
  语义等价，未重复放入本弹窗。
- 运行日志查看/清空、自动清理已播放列表、简繁歌词、同步、评论、逐字歌词等差异项风险或依赖偏高，
  本轮未纳入（详见问题清单）。

### 工程

- 版本 1.8.8 → 1.8.9（versionCode 45 → 46）；README / ANALYSIS 同步。

## 第四十三轮：播放页动态背景（theme.dynamicBg）（v1.8.8，上一轮）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮新增
> 原版 `theme.dynamicBg` 能力（移植版此前完全缺失），改动 5 个源码文件 + 版本号与文档，
> 通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub Actions 云端。零新增依赖、
> 不改路由/状态管理结构/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐（原版缺失项）

- **播放页动态背景**（`SettingsRepository.kt` / `SettingViewModel.kt` / `SettingScreen.kt` /
  `PlayerScreen.kt` / `TrackCover.kt`）：原版设置 `theme.dynamicBg`（默认 false）会让播放页
  背景跟随当前播放封面主色，移植版此前该偏好完全缺失、播放页背景恒为主题纯色。本轮补齐：
  - 新增偏好键 `theme.dynamicBg` 与 `Flow<Boolean>`（默认 false，与原版 defaultSetting 一致），
    ViewModel 暴露 `StateFlow` 与 setter，设置页「基本设置 → 主题色」下方新增开关；
  - 播放页在内容最底层绘制封面主色的垂直渐变光晕，开关关闭 / 取色失败时整层退化为无操作。
  - **取色复用现有封面缓存链路**（`TrackCover.kt` 的 `rememberCoverBitmap`，96px 小图 + 4bit
    直方图取频次最高色桶 + 跳过近黑近白 + 向中灰降饱和提亮），与播放页大图共享内存缓存与
    并发治理，同一 URL 只解码一次、不产生额外网络请求、不引入任何新依赖。

### 动效 / 视觉 / 可访问性

- **切歌平滑换色**：背景主色经 `animateColorAsState` 320ms 过渡，切歌时不再硬切；减弱动效
  （`LocalReduceMotion`）时时长为 0，一帧到位，符合低性能设备与前庭敏感用户约定。
- 渐变透明度固定 0.18f，配合前景内容的内边距，不影响歌词/控件可读性；深浅主题下取色均向
  中灰靠拢，避免深色封面把背景压得过暗。

### 已知差异 / 待确认

- 原版 dynamicBg 用高斯模糊的封面大图铺满背景，本轮采用「主色渐变光晕」的保守实现：视觉更
  克制、性能开销更低，且能复用既有缓存链路；若需 1:1 复刻模糊背景，需引入额外模糊处理，
  与本项目「不引入重依赖、低风险」约定冲突，留待后续具备真机验证条件时评估。
- 取色用 4bit 直方图近似主色，非精确算法；对色彩极丰富的封面取到的是占比最高色桶，观感稳定
  但可能与视觉「第一眼主色」略有出入，属可接受的保守近似。
- 开关默认关闭，老用户升级后播放页观感零突变；开启后才出现动态背景（与原版默认一致）。

### 工程

- 版本 1.8.7 → 1.8.8（versionCode 44 → 45）；README / ANALYSIS 同步。

## 第四十二轮：播放页歌词位置锁定 + 胶囊交互统一（v1.8.7，上一轮）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮仅
> `PlayerScreen.kt` 一个源码文件改动，通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub
> Actions 云端。零新增依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **播放页歌词位置锁定快捷入口**（`PlayerScreen.kt` / `PlayerMoreSettings`）：原版播放页
  `MoreBtn/DesktopLyricBtn` 短按切换桌面歌词开关、长按切换位置锁定（`desktopLyric.isLock`），
  移植版此前在播放「更多设置」弹窗只提供开/关，锁定须跳到「设置 → 桌面歌词 → 锁定歌词位置」。
  本轮在开/关胶囊下方就近补「锁定歌词位置 未锁定/已锁定」两档：读既有
  `settings.desktopLyricLocked` Flow，写入走既有 `setDesktopLyricLocked()`，由 LxApp 的 Flow
  监听实时同步给悬浮窗（`DesktopLyric.setLocked`，锁定时禁止拖动、轻点暂停/继续照常）。
  零新增偏好键、零新增 ViewModel 方法。

### 动效 / 视觉 / 可访问性

- **胶囊交互统一**（`PlayerMoreSettings`）：该弹窗的「歌词对齐」「桌面歌词」「锁定歌词位置」
  三组选择项此前是手写裸 `Text` 胶囊（选中态硬切底色、无按压反馈、无减弱动效门控），现统一改用
  全站公共 `FilterChip`——自带按压缩放（0.96，`Motion.press`）、选中态底色渐变（160ms）、
  48dp 命中区、`LocalReduceMotion` 静帧降级与读屏「已选中/未选中」语义，与搜索页 / 设置页
  同一交互语言。无新增主题变量、无逐帧动画，亮/暗主题观感一致。

### 已知差异 / 待确认

- 原版 `DesktopLyricBtn` 为「短按开关 + 长按锁定」复用同一图标，本轮沿用移植版既有的「弹窗内
  显式两档胶囊」形态（可读性更好、易发现），未做长按手势（避免引入 `combinedClickable` 与长按
  判定复杂度）；功能语义与操作入口均已对齐。
- 锁定胶囊与开关同处弹窗，锁定时若桌面歌词未开启不会有可见效果（与原版独立偏好语义一致）。

### 工程

- 版本 1.8.6 → 1.8.7（versionCode 43 → 44）；README / ANALYSIS 同步。

## 第四十一轮：桌面歌词阴影色联动（v1.8.6，上一轮）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异」的低风险补齐。本轮 1 个源码文件改动
> 全部通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub Actions 云端。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐（视觉 / 可访问性）

- **桌面歌词阴影色联动**（`DesktopLyric.kt`）：原版桌面歌词 9 档主题色板中，仅「黑」字档
  （`#000000`）配白色阴影，其余 8 档配 `rgba(0,0,0,0.6)` 黑阴影，用于保证深色透明悬浮窗上黑字
  仍可辨认。移植版此前阴影色写死为 `0xB3000000`（黑），用户选「黑」色歌词时会出现
  「黑字 + 黑阴影」难以看清的可访问性问题。本轮在 `refresh()` 中按当前行颜色（`playedColorOverride
  ?: accentColor`）的相对亮度（Rec.709）推导阴影色：深色字（亮度 < 0.15）用近 70% 白阴影，
  其余沿用历史黑阴影。刷新指纹 `key` 已含颜色状态，改色即时生效；仅调 `setShadowLayer`，
  不重建窗口、不触碰 span 文本与播放内核。

### 动效 / 视觉

- 阴影切换随歌词/颜色刷新一次性重设，无逐帧动画、无布局抖动，符合 `LocalReduceMotion` 与
  低性能设备约定。默认（跟随主题/亮色主题字）继续用黑阴影，老用户升级观感零突变。

### 已知差异 / 待确认

- 判定阈值取 0.15（保守）：纯黑(0)命中白阴影，原版色板其余 8 档（最暗的红 #ff1222 亮度约 0.27）
  均高于阈值、保持黑阴影，与原版 8:1 的表现精确一致。极深非黑自定义色（如深灰）也会走白阴影，
  属合理的可读性增强；如需 100% 复刻原版「仅 #000000 白阴影」，可将判定改为等值比较。
- 本轮未新增独立的 `lyricShadowColor` 偏好键与设置 UI：原版并无独立阴影色选择入口，它是色板
  选择 `lyricPlayedColor` 时联动写入的；联动方案行为与原版一致、改动面更小（单文件、易回滚）。

### 工程

- 版本 1.8.5 → 1.8.6（versionCode 42 → 43）；README / ANALYSIS 同步。

## 第四十轮：播放页桌面歌词快捷开关（v1.8.5，上一轮）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异」的低风险补齐。本轮改动全部通过
> `scripts/selfcheck.py` 静态自检，构建走 GitHub Actions 云端。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **播放页桌面歌词快捷开关**（`PlayerViewModel.kt` / `PlayerScreen.kt`）：原版播放页 `MoreBtn` 提供
  `DesktopLyricBtn`（短按切换桌面歌词开关），移植版此前只能跳到「设置 → 桌面歌词」操作。本轮在播放
  「更多设置」弹窗内就近补「桌面歌词 开/关」两档胶囊：
  - `PlayerViewModel` 新增 `setDesktopLyricEnabled()`，与 `SettingViewModel` 同一语义（写偏好 + 有权限时
    立即建/拆悬浮窗），复用既有 `DesktopLyric.canShow/start/stop`，零新增偏好键。
  - 弹窗开关读既有 `settings.desktopLyricEnabled` Flow；关闭直接停悬浮窗，开启前检查悬浮窗权限，
    未授权则拉起系统授权页（与设置页同源），避免写入 true 却看不到歌词。

### 动效 / 视觉

- 复用项目既有语言：开/关胶囊与同弹窗「歌词对齐」胶囊同形态（`ChipShape` + 语义色选中态 + `Role.Tab`），
  无新增主题变量、无新增逐帧动画，符合减弱动效与低性能设备约定。

### 已知差异 / 待确认

- 原版 `DesktopLyricBtn` 为「短按切换开关 + 长按切换锁定」，本轮只补核心的短按开关；位置锁定仍可在
  「设置 → 桌面歌词 → 位置锁定」操作（长按入口未做，避免引入 `combinedClickable` 与长按判定复杂度）。
- 授权返回后本弹窗不自动打开开关（设置页有窗口焦点复检会自动开）；用户授权后需再点一次「开启」。
  原因是弹窗内做焦点复检需引入 `DisposableEffect`/`LocalView`，收益低、风险相对高，故按低风险策略略过。

### 工程

- 版本 1.8.4 → 1.8.5（versionCode 41 → 42）；README / ANALYSIS 同步。

## 第三十九轮：桌面歌词垂直对齐 + 播放页歌词设置 + 批量添加（v1.8.4，上一轮）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮改动
> 全部通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub Actions 云端。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **桌面歌词垂直对齐**（`SettingsRepository.kt` / `DesktopLyric.kt` / `LxApp.kt` / `SettingViewModel.kt` / `SettingScreen.kt`）：
  原版 `desktopLyric.textPosition.y`（top/center/bottom，默认 top）此前移植版完全缺失，只有水平 X 对齐。本轮补齐：
  - 新增偏好键与 Flow（默认 top）、setter；`LxApp` 冷启动订阅、独立协程 + catch 同步给原生悬浮窗；
  - `DesktopLyric` 拆分水平/垂直两个 gravity 分量并合成写回两个 TextView，`setTextPositionY()` 实时生效、不重建窗口；
  - 设置页「桌面歌词对齐」下新增「垂直位置」三档胶囊（顶部/居中/底部）。
  - 默认 top 与原版一致；悬浮 TextView 为 WRAP_CONTENT，垂直 gravity 无多余空间可分配，老用户升级观感零突变。
- **播放页就近提供歌词字号/对齐**（`PlayerScreen.kt`）：原版播放页 `SettingPopup` 提供歌词字号与左/居中对齐，
  移植版此前仅在「设置 → 歌词设置」可调。本轮在「更多设置」弹窗内补歌词字号滑块（0.8~1.8，松手落盘）
  与对齐两档胶囊，直读直写既有偏好，不改 ViewModel。
- **我的列表批量添加**（`MyListsScreen.kt`）：原版多选底栏同时有「移动」与「添加」，移植版此前只有「移动」。
  本轮复用「移动到」对话框，新增复制模式：只写入目标列表、源列表保留；ViewModel 新增 `addMusics()`，
  repository 内部已按 id 去重。

### 动效 / 视觉

- 本轮复用项目既有动效语言：设置页垂直位置胶囊沿用 `FilterChip`（按压缩放 0.96 + 选中态渐变 + 减弱动效静帧）；
  播放页歌词对齐胶囊沿用播放模式同款胶囊形态；未新增逐帧动画，符合减弱动效与低性能设备约定。

### 已知差异 / 待确认

- 桌面歌词垂直对齐在单行（WRAP_CONTENT 贴合内容）下与 center 观感无可见差异，主要在多行 + 固定高度场景体现；
  保留该设置以与原版配置键完全对齐。
- 播放页歌词字号/对齐写入经协程落盘，未做节流（低频操作，滑块用本地态只在松手写一次）。

### 工程

- 版本 1.8.3 → 1.8.4（versionCode 40 → 41）；README / ANALYSIS 同步。

## 第三十八轮：未播放歌词颜色（v1.8.3，上一轮）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮改动
> 全部通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub Actions 云端。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **未播放歌词颜色**（`SettingsRepository.kt` / `SettingViewModel.kt` / `DesktopLyric.kt` / `SettingScreen.kt` / `LxApp.kt`）：
  原版 `desktopLyric.style.lyricUnplayColor` 控制桌面歌词后续行（未播放歌词）颜色，移植版此前固定为次要白、无法单独选色。本轮补齐：
  - 新增偏好键 `desktopLyric.style.lyricUnplayColor`（仅接受合法 `#RRGGBB`，损坏值回退 null）与 setter（null 清除覆盖）；
  - 设置页「歌词设置 → 未播放歌词颜色」提供 10 色色板 +「默认」项，两行排布避免窄屏挤压；
  - `DesktopLyric` 新增 `setUnplayColor()`，后续行按所选色渲染；改色只重绘 `TextView` span、不重建窗口、不打断播放；
  - `LxApp` 冷启动订阅新设置，`catch` 隔离损坏偏好。

### 视觉 / 兼容性

- **默认观感不变**：未选色时（null）沿用历史「次要白」后续行，老用户升级零突变；当前行配色（`lyricPlayedColor`）不受影响，两者独立。
- 色值切换纳入歌词刷新指纹但不重建 View；不新增逐帧动画，符合减弱动效与低性能设备约定。

### 工程

- 版本 1.8.2 → 1.8.3（versionCode 39 → 40）；README 同步。

## 第三十七轮：桌面歌词主题色板（v1.8.2，上一轮）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮改动
> 全部通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub Actions 云端。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **桌面歌词主题色板**（`SettingsRepository.kt` / `SettingViewModel.kt` / `DesktopLyric.kt` / `SettingScreen.kt` / `LxApp.kt`）：
  原版 `desktopLyric.style.lyricPlayedColor` 有 9 色色板（`LyricDesktop/Theme.tsx`），移植版此前只跟随主题主色、无法单独选色。本轮补齐：
  - 新增偏好键 `desktopLyric.style.lyricPlayedColor`（仅接受合法 `#RRGGBB`，损坏值回退 null）与 setter（null 清除覆盖）；
  - 设置页「歌词设置 → 桌面歌词对齐 → 歌词颜色」提供原版 9 色色板 + 「跟随主题」默认项，两行排布避免窄屏挤压；
  - `DesktopLyric` 新增 `setPlayedColor()`，当前行歌词（含 ♪）整体按所选色渲染，后续行仍用次要白色体现层级；改色只重绘文本、不重建窗口、不打断播放；
  - `LxApp` 冷启动订阅新设置，`catch` 隔离损坏偏好。

### 视觉 / 兼容性

- **默认观感不变**：未选色时（null）沿用原「主题主色高亮当前行 ♪」行为，老用户升级零突变；选色后由「♪ 单字符着色」扩展为「整行着色」，更贴近原版 playedColor 语义。
- 色值切换纳入歌词刷新指纹但不重建 View；不新增逐帧动画，符合减弱动效与低性能设备约定。

### 工程

- 版本 1.8.1 → 1.8.2（versionCode 38 → 39）；README 同步。

## 第三十六轮：悬浮歌词宽度设置（v1.8.1，上一轮）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮改动
> 全部通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub Actions 云端。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **悬浮歌词宽度设置**（`SettingsRepository.kt` / `SettingViewModel.kt` / `DesktopLyric.kt` / `SettingScreen.kt`）：
  原版 `desktopLyric.width`（10~100%，默认 100%）此前移植版完全缺失——悬浮窗宽度固定为
  「屏幕宽 − 24dp」，窄屏/横屏或想让歌词不遮挡两侧内容时无法调整。本轮沿上一轮
  `maxLineNum` 的同一条「DataStore → ViewModel → 原生悬浮窗」链路补齐：
  - 新增 `desktopLyric.width` 偏好键（钳制 10~100，默认 100）与同步 setter；
  - `DesktopLyric` 新增 `widthPercent` 与 `setWidthPercent()`，抽 `computeWidth()` 供启动与实时调整共用，
    窗口已存在时按新百分比 `updateViewLayout` 且保留当前 x/y 位置，不重建窗口、不打断播放；
  - 设置页「歌词设置 → 歌词宽度」新增滑块，拖动即时生效、节流写盘、离开页面补写；
  - `LxApp` 冷启动订阅新设置，偏好损坏时 `catch` 隔离，不拖垮其它初始化。

### 视觉 / 兼容性

- **默认观感不变**：100% 时沿用历史「屏幕宽 − 24dp」内边距（非真正满屏），老用户升级后视觉零突变；
  低于 100% 才按占比计算，最小 160dp 保证歌词可读。
- 宽度变化只改 Window 布局参数，不触发歌词文本重算、不进刷新指纹，零额外逐帧开销。

### 工程

- 版本 1.8.0 → 1.8.1（versionCode 37 → 38）；README 同步。

## 第三十五轮：桌面歌词多行显示（v1.8.0）

> 对照原版 lx-music-mobile v1.9.2 的 `desktopLyric.maxLineNum` 补齐移植版缺失的一项能力。
> 本轮改动全部通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub Actions 云端。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议；沿现有设置链路实现，可单独回滚。

### 功能对齐

- **桌面歌词多行显示**（原版 `desktopLyric.maxLineNum`，1~8 行，默认 5）：此前移植版悬浮窗固定只显示
  当前行 + 翻译副行，长歌词一次只能看一句；原版按该设置显示从当前行起的连续多行。本轮在
  `SettingsRepository` 新增 `desktopLyric.maxLineNum`（DataStore int key，读取钳制 1~8，默认 5），
  `SettingViewModel` 暴露状态与节流 setter，`SettingScreen` 在「歌词设置 → 单行显示」下方新增
  「显示行数」滑块（即时生效、离开页面补写），`LxApp` 冷启动订阅同步给原生悬浮窗。
- **原生渲染**（`DesktopLyric.kt`）：`refresh` 从当前行起按 `maxLineNum` 拼接连续歌词（跳过空行、
  不足则提前结束），当前行「♪」用主题主色、后续行用次要白 `0xB3FFFFFF` 体现视觉层级；内容指纹
  纳入 `maxLineNum`，保证只改行数也会重排。`mainText.maxLines` 随笔数/单行开关即时更新，「单行显示」
  开启时强制 1 行、此设置暂不生效（summary 有提示）。多行全程只改文本与换行，不重建窗口、
  不触碰播放状态机。

### 工程

- 版本 1.7.7 → 1.8.0（versionCode 36 → 37）；README、ANALYSIS、CHANGELOG 同步。
- 验证：`python3 scripts/selfcheck.py` 通过（exit 0）；云端构建由 GitHub Actions 验证。

## 第三十四轮：记住列表滚动位置（v1.7.7）

> 对照原版 lx-music-mobile v1.9.x 的 `list.isSaveScrollLocation` 补齐移植版完全缺失的一项能力。
> 本轮改动全部通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub Actions 云端。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议；纯 UI 层实现，可单独回滚。

### 功能对齐

- **记住列表滚动位置**（原版 `list.isSaveScrollLocation`，默认 `true`）：此前从歌单详情、榜单详情、
  歌单广场、我喜欢、我的列表、播放队列返回时总是从顶部开始，长列表要重新滑回原处；原版会停在
  离开时的位置。新增公共组件 `ScrollPositionStore.kt`：按页面 key（含音源/歌单 id 隔离）在进程内
  保存「首个可见项下标 + 行内偏移」，返回再进入时恢复；详情页数据异步到达后补一次滚动，确保
  恢复生效。关闭开关时传 null 走原来的从顶部开始，行为可回退。
- **设置项**：「列表设置 → 记住列表位置」开关（默认开启，与原版默认值一致），一处下发到各列表页，
  不为每个页面增加订阅。

### 工程

- 新增 `ui/components/ScrollPositionStore.kt`；改动 11 个文件，均为小步、可回滚改动。

## 第三十三轮：搜索历史时机 + 到底提示 + 音源删除二次确认（v1.7.7，本次）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI」的低风险补齐。本轮改动
> 全部通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub Actions 云端。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **确认搜索即记历史，与请求成败无关**（`SearchViewModel.kt:search`）：原版 `Search/index.tsx:90`
  在 `handleSearch` 中无条件 `addHistoryWord(text)`，无论请求成功、失败或断网都会留存词条。
  移植版此前 `recordHistory(text)` 只写在音乐/歌单两处 `onSuccess` 内，网络异常或音源报错时
  搜索词被丢弃。本轮把记录移到请求发出前无条件执行（内部仍按「显示搜索历史」开关门控，
  并保留去重与 15 条上限），搜索链路、切源重搜与播放逻辑均不变。

### UI / 交互

- **搜索结果到底提示**（`SearchScreen.kt`）：对齐原版 `OnlineList` 的 `list_end`（「到底啦~」）。
  此前 `hasMore=false` 时列表末尾只是静默隐藏加载按钮，用户难以分辨「已加载完」与「加载中断」。
  新增共用组件 `ListEndHint`（次要色 footnote、纯静态、无动画），仅在有结果、无更多、
  非加载中且无错误时展示；音乐与歌单两个结果列表一致，不触发布局抖动或逐帧调度。
- **音源删除二次确认**（`SourceScreen.kt`）：对齐原版 `UserApiEditModal/List.tsx` 的
  `confirmDialog` + `user_api_remove_tip`。此前管理弹窗点「删除」立即移除、误触不可撤销；
  现在先弹确认框并显示音源名（`你真的要移除「xxx」吗？`），确认后才调用原 `delete(id)`，
  删除逻辑本身不变。

### 工程

- 版本 1.7.6 → 1.7.7（versionCode 35 → 36）；README、ANALYSIS、CHANGELOG 同步。
- 验证：`python3 scripts/selfcheck.py` 通过（exit 0）；云端构建由 GitHub Actions 验证。

## 第三十二轮：搜索音源持久化 + 音源 chip 自动滚到选中项（v1.7.6）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮改动
> 全部通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub Actions 云端。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **搜索音源冷启动恢复与切源落盘**（`SettingsRepository.kt` / `SearchViewModel.kt` / `SearchScreen.kt`）：
  原版 `utils/data.ts` 的 `getSearchSetting/saveSearchSetting` 除 `search.type` 外还持久化 `search.source`
  （`Search/index.tsx:70` 切源即 `saveSearchSetting({ source })`，冷启动用持久化 source 初始化来源列表）。
  移植版上一轮已补齐 `search.type`，但 `selectedSource` 仍仅 `rememberSaveable` —— 进程被杀后冷启动回到
  「全部」，用户上次选的搜索音源丢失。本轮新增 `search.source` key（默认 `all`），切源即时落盘、冷启动读取
  恢复；恢复值做白名单校验（`all` + 内置源），非法/旧版本自定义源一律回落 `all`，避免选中态落空。搜索链路
  与切源重搜逻辑不变。

### UI / 动效

- **音源 chip 自动滚到选中项**（`SearchScreen.kt`）：恢复的音源（如 wy/mg）在横向 chip 行较靠后，
  此前选中态可能停在可视区外，用户看不到当前音源。给音源 `LazyRow` 绑定 `rememberLazyListState`，
  选中音源变化时 `animateScrollToItem` 平滑滚到该项；走 Motion 语义（Compose 默认 spring），减弱动效
  系统下由平台动画时长缩放统一降级为静帧。无新增依赖、无布局抖动（只滚动容器，不改列表结构）。

### 工程

- 版本 1.7.5 → 1.7.6（versionCode 34 → 35）；README、ANALYSIS、CHANGELOG 同步。
- 验证：`python3 scripts/selfcheck.py` 通过（exit 0）；云端构建由 GitHub Actions 验证。

## 第三十一轮：搜索类型持久化 + 导入上限前置 + 首页副标题省略（v1.7.5，本次）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮改动
> 全部通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub Actions 云端。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **搜索类型冷启动恢复**（`SettingsRepository.kt` / `SearchViewModel.kt`）：原版 `utils/data.ts`
  的 `getSearchSetting/saveSearchSetting` 会持久化 `search.type`（音乐/歌单），冷启动恢复。移植版
  此前 `SearchViewModel.searchType` 仅为内存默认值，用户切到「歌单」后重启 App 会回到「音乐」。
  本轮新增 `search.type` DataStore key（默认 `music`），`init` 读取恢复、`setType` 即时写回，
  搜索链路与类型切换逻辑不变。
- **音源导入上限前置拦截**（`SourceViewModel.kt`）：原版 `ImportBtn.tsx` 点导入即校验数量上限。
  移植版此前在网络下载/文件读取**完成后**才 `count()` 校验，达到 20 个上限仍会白跑一次下载/读文件。
  本轮把上限校验前移到下载/读文件之前入口处立即 return，落库前保留原有二次校验（防并发），
  错误文案统一按 `MAX_COUNT` 常量生成。

### UI / 响应式

- **首页入口卡副标题单行省略**（`HomeScreen.kt`）：「本地歌单与收藏」「平台歌单」两处副标题此前
  无 `maxLines`，系统大字号或窄屏下会换行撑高卡片、与相邻卡片不等高。补 `maxLines=1` +
  `TextOverflow.Ellipsis`，与同页「音源管理」副标题的处理保持一致。纯静态裁行，无逐帧开销。

### 工程

- 版本 1.7.4 → 1.7.5（versionCode 33 → 34）；README、ANALYSIS 同步。
- 验证：`python3 scripts/selfcheck.py` 通过；云端构建由 GitHub Actions 验证。

## 第三十轮：播放页状态对齐 + 歌词字号联动（v1.7.4，本次）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮改动
> 全部通过 `scripts/selfcheck.py` 静态自检与 `assembleDebug --no-build-cache` 全量编译。
> 零新增依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **播放缓冲状态提示**（`PlayerScreen.kt`）：原版 `Status.tsx` 在播放页显示加载/缓冲文案
  （`player__buffering`「缓冲中...」）。移植版 `state.loading` 早已按 `Player.STATE_BUFFERING`
  维护，但 UI 从未展示——切歌取链接/缓冲时进度条不动、无任何反馈。本轮在错误提示同一位置
  补一行次要色「缓冲中…」状态（与 error 互斥），淡入淡出、减弱动效时静帧到位，只动 alpha 不挤布局。

### UI / 一致性

- **翻译歌词首帧不再闪现**（`PlayerScreen.kt`）：`showLrcTranslation` 的 `collectAsState(initial=...)`
  由 `true` 改为 `false`，与落盘默认（`SettingsRepository`、`defaultSetting.ts` 均为 false）一致，
  消除未改过设置的用户冷启动「先闪出翻译行、DataStore 读到 false 后再熄灭」。
- **罗马音/翻译副行字号随主歌词联动**（`PlayerScreen.kt`）：副行此前固定 `footnote1`，
  用户调大「歌词字号」时主行变大、副行不变，层级失衡。改为 `baseSp * 0.8`（对齐原版
  `Lyric.tsx` 的 `size * 0.8`），字号设置对主副行一致生效。

### 工程

- 版本 1.7.3 → 1.7.4（versionCode 32 → 33）；README 同步。

## 第二十九轮：音源描述/版本格式 + 搜索欢迎语与联想词（v1.7.3）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮改动
> 全部通过 `scripts/selfcheck.py` 静态自检，构建走 GitHub Actions 云端。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **音源列表展示脚本描述**（`SourceScreen.kt`）：原版 `List.tsx` 在音源名下方单独一行展示
  脚本头部 `@description`。此前移植版的 `ApiSourceRepository.matchInfo` 已解析并入库该字段，
  但 UI 只显示作者+版本，用户在音源列表看不到脚本用途。本轮补一行描述（次要色、最多两行省略）。
- **版本号补 v 前缀**（`SourceScreen.kt`）：原版 `formatVersionName` 对纯数字版本加 `v`
  （如 `1.2.3 → v1.2.3`）。本轮新增同语义的 `formatVersionName`，数字开头补 v，其余原样。
- **搜索页欢迎语**（`SearchScreen.kt`）：原版 `BlankView` 在历史、热搜两个开关都关闭时显示
  「搜我所想~~😉」。移植版此前该场景一路落到空结果列表、整页空白。本轮在初始空白态
  （两开关关 + 未搜索 + 输入空 + 无结果）复用 `EmptyHint` 展示居中引导语，沿用全站空态的
  淡入上浮动效与减弱动效静帧降级。

### UI / 动效 / 可访问性

- **联想词单行截断**（`TextActionButton.kt` / `SearchScreen.kt`）：原版 `TipList` 用
  `numberOfLines={1}`。移植版联想词此前可换行撑高下拉、挤动结果列表。给公共
  `TextActionButton` 增加可选 `maxLines`（默认 `Int.MAX_VALUE`，既有调用方行为完全不变），
  联想词处传 `maxLines = 1` + 尾部省略。只动 Text 的裁行，不触发布局抖动。

### 工程

- 版本 1.7.2 → 1.7.3（versionCode 31 → 32）；README 同步。
- 验证：`python3 scripts/selfcheck.py` 通过；云端构建由 GitHub Actions 验证。

## 第二十八轮：导出音源脚本 + 动效/文案收尾（v1.7.2）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐。本轮改动通过
> `scripts/selfcheck.py` 静态自检与本地 `assembleRelease`（JDK 20 + SDK 37）构建。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **导出自定义音源脚本**（`SourceViewModel.kt` / `SourceScreen.kt`）：对齐原版 `ScriptImportExport` 的导出方向。
  此前移植版只有网络/文件/剪贴板三种**导入**，无法把已导入的脚本导出备份或分享。本轮长按音源弹出的
  对话框由「删除确认」扩展为「管理音源」，提供「导出脚本 / 删除」两个操作；导出走 AndroidX SAF
  `CreateDocument` 选择保存位置，复用仓库既有的 `getScript(id)` 读取脚本文本写入，不再新增数据源或依赖。
  长按读屏标签由「删除音源」改为更准确的「管理音源」，列表提示同步为「长按音源可导出或删除」。

### UI / 动效 / 文案

- **首页热搜列表接入统一列表动效**（`HomeScreen.kt`）：热搜 `LazyColumn` 此前是全站唯一未套
  `animateListItem()` 的列表，刷新时增删/位移为硬切换。接入后与榜单及全站列表一致，减弱动效时静帧到位。
- **桌面歌词对齐标题消歧**（`SettingScreen.kt`）：桌面歌词区块内的分节标题原与主歌词页「歌词对齐」同名，
  改为「桌面歌词对齐」，避免用户误认为是同一设置项。
- **代码整洁**（`AppRoot.kt`）：删除重复的 `toArgb` import。

## 第二十七轮：列表显示设置对齐 + 音质角标去重 + 读屏语义（v1.7.1）

> 对照原版 lx-music-mobile v1.9.2 继续做「功能差异 + UI/动效」的低风险补齐，本轮三批改动均通过
> `scripts/selfcheck.py` 静态自检与本地 `assembleRelease`（JDK 20 + SDK 37）构建。零新增依赖、
> 不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。

### 功能对齐

- **播放队列副标题接入列表显示设置**（`PlayQueueScreen.kt`）：原版 `list.isShowSource / isShowInterval /
  isShowAlbumName` 三项开关此前在收藏页、搜索页、歌单/榜单详情均已生效，唯独播放队列固定显示
  「歌手 · 来源」、无视设置。本轮按收藏页 `FavoriteCard` 同一套 `buildList` 组装逻辑接入三项开关，
  全站列表页显示设置至此完全一致。

### UI / 视觉层级

- **搜索页音质角标去重**（`SearchScreen.kt`）：搜索结果此前在歌名右侧有 `QualityBadge` 角标、副标题下方
  又有一行纯文字音质标签，是全站唯一的「双重音质」展示，多出的一行还撑高卡片、压缩长列表可见条数。
  去重后只保留角标，与收藏/歌单/榜单/我的列表一致。
- **音质角标换源刷新**（`QualityBadge.kt`）：角标档位此前用 `remember(music.id)` 缓存，同一首歌换源后
  id 可能不变、但可用音质会变（如 128k 源切到 flac 源），导致角标停留在旧档位。key 纳入
  `music.source / _qualitys / qualitys`，换源重组时重新判定。

### 可访问性 / 响应式

- **长按读屏语义化**（`SourceScreen.kt` / `SongListScreen.kt` / `MyListsScreen.kt`）：`GlassSurface` 的
  `onLongClickLabel` 默认是「打开歌曲操作」，但音源页长按是删音源、歌单广场是收藏、我的列表是重命名，
  TalkBack 会误播。三处分别传入准确标签（删除音源 / 收藏歌单 / 重命名列表）。
- **音源导入按钮响应式**（`SourceScreen.kt`）：网络/文件/剪贴板三个导入按钮原用固定 `Row`，系统大字号或
  窄屏下「剪贴板导入」被挤出屏幕。改 `LazyRow` + `spacedBy(8.dp)`，与搜索页音源 chip、多选底栏一致，
  超出可横滑、按钮不压缩不裁切。

### 已知差异 / 待确认

- 音源「更新提示」开关当前仅存储状态、未接入实际更新检测（原版 `syncSourceList`）；完整实现需在数据库
  保存脚本来源 URL 并新增网络检测与冲突提示，改动面偏大，本轮未纳入——列为后续待确认项。

### 工程
- 版本 1.7.0 → 1.7.1（versionCode 29 → 30）；README / ANALYSIS 同步。
- 验证：`python3 scripts/selfcheck.py` 通过；`./gradlew assembleRelease --offline` 三批 + 最终构建退出码均为 0。

---

## 第二十六轮：播放缓存 + 元数据缓存 + 音质角标（v1.7.0）

> 对照原版 lx-music-mobile v1.9.2，本轮补三项此前**完全缺失**的能力：播放缓存（离线缓存）、
> 歌词/链接元数据缓存的管理入口、列表音质角标。零新增依赖（`media3-datasource` 本就是
> `media3-exoplayer` 的传递依赖）、不改路由/状态管理/播放状态机、不触碰 QuickJS 与音源协议；
> 全部 `.kt` 改动通过 `scripts/selfcheck.py`，并本地 `assembleRelease`（JDK 20 + SDK 37）验证。

### 功能对齐（原版缺失项）

- **播放缓存**（原版 `player.cacheSize`，新增 `core/player/PlaybackCache.kt`）：移植版此前完全没有
  播放缓存，同一首歌反复播放每次都要回源，弱网/切后台回来常卡在 buffering。本轮用 `SimpleCache` +
  `LeastRecentlyUsedCacheEvictor` + `CacheDataSource` 组成 `cache -> http` 数据源链：
  - 默认 1024 KB（与原版 `'player.cacheSize': '1024'` 一致），档位 关闭 / 1 / 32 / 64 / 128 / 256 / 512 MB；
  - 缓存目录 `<cacheDir>/player`，与封面 HTTP 缓存（`<cacheDir>/http`）分开，可单独清理；
  - 设置页「播放设置 → 播放缓存」显示占用/上限，可切换档位与清空。
  - **关键实现约束**：media3 1.8.0 的 `ExoPlayer` 实例没有 `setMediaSourceFactory`（仅 Builder 有），
    而重建播放器会打断正在播放的歌。因此 ExoPlayer 持有一个**稳定的** `SwitchableDataSourceFactory`，
    内部按当前缓存实例决定是否套缓存层——切换容量只换引用，播放器完全无感。

- **歌词与播放链接缓存**（对齐原版 `utils/data.ts` 的 lyric / musicUrl 缓存，新增 `core/repository/MetaCache.kt`）：
  移植版此前每次切歌都重新取词，且歌词无效时还会触发「五源换源」，最坏单次切歌 6 次网络请求。
  本轮加「源+id」为键的 LRU 缓存（歌词 300 条、链接 200 条，链接 10 分钟过期以避开签名失效）。
  设置页「其他 → 歌词与链接缓存」可查看条数并分别清理。
  - 链接缓存带 10 分钟 TTL；播放报错时自动作废该曲链接缓存，避免「出错→自动下一首→切回来」
    反复拿到同一个失效链接。
  - **已知差异**：原版落盘、迁移跨版本；本轮用内存 LRU（落盘需新增序列化 + 损坏隔离，
    本项目已在该处踩过坑），冷启动后重新取词一次。

- **列表音质角标**（对齐原版 `components/common/Badge` + `OnlineList/ListItem`，新增 `ui/components/QualityBadge.kt`）：
  原版列表在歌名右侧标出 24bit / 无损 / 高品质，玩家扫一眼即可判断结果质量；
  移植版此前完全无此信息，128k 与 flac24bit 显示完全相同。本轮在搜索、收藏、歌单详情、
  我的列表、榜单详情 **5 个列表页**统一接入：判定顺序 `flac24bit > flac/ape > 320k/192k`，
  9sp 小字 + 淡底圆角标签，歌名过长时优先压缩歌名（`weight(1f, fill = false)`），
  无可用音质时不渲染（本地文件、部分用户音源）。

### 工程
- 版本 1.6.0 → 1.7.0（versionCode 28 → 29）；README 同步。
- 验证：`python3 scripts/selfcheck.py` 通过；`./gradlew assembleRelease --offline` 退出码 0。

## 第二十五轮：桌面歌词 + 主题主色 + 歌词阴影（v1.6.0）

> 对照原版 lx-music-mobile v1.9.2 的源码，本轮补两项此前完全缺失的**功能**（桌面歌词、主题色）、
> 一项设置对齐（歌词字体阴影），并打磨一处切歌动效。不引入依赖、不改路由/状态管理与播放内核，
> 不触碰 QuickJS 与音源协议；全部 `.kt` 改动通过 `scripts/selfcheck.py`，并由本地 `assembleRelease`
> 与 GitHub Actions 云端构建双重验证。

### 功能对齐（原版缺失项）
- **桌面歌词悬浮窗**（原版 `desktopLyric.*`，新增 `core/system/DesktopLyric.kt`）：移植版此前完全没有
  悬浮歌词——切到其它应用后只能靠通知栏判断在放什么。本轮用 `WindowManager` + 原生 `TextView` 实现：
  单行歌词 + 翻译副行、主题主色标记「♪」、暂停时整体降到 55% 不透明度；上下拖动移动并记住位置
  （`SharedPreferences`），轻点切换播放/暂停。**不进 Compose 树**：悬浮窗是独立 Window，没有
  `ViewTreeLifecycleOwner`，放 `ComposeView` 需要额外挂 lifecycle/savedstate 所有者，属于为样式引入
  新风险；原生 View 也不会随播放进度触发任何 Compose 重组。
  - 权限：清单补 `SYSTEM_ALERT_WINDOW`；设置页开关在未授权时引导 `ACTION_MANAGE_OVERLAY_PERMISSION`，
    返回后按**窗口焦点变化**复检权限并自动打开开关（不引入 lifecycle-compose 依赖）。
  - 生命周期：只在开关开启且已授权时创建；进程被杀则悬浮窗随之消失，下次打开 App 按开关自动恢复；
    所有窗口操作 `runCatching` 兜底，权限被撤销或 ROM 拒绝时只静默失败，不影响播放。
- **主题主色**（原版 `theme.id` 皮肤体系）：此前只有「跟随系统 / 浅色 / 深色 / 取色」四种**明暗模式**，
  没有任何主色可选。本轮不照搬原版多份色板文件（每套几十个色值，与 Miuix 色彩体系不兼容），
  而是由**一个主色**交给 Miuix 推导整套调色板（明暗两套、语义色全部自动生成，对比度由库保证）：
  默认 / 青绿 / 靛蓝 / 黛紫 / 暖橙 / 绯粉，`default` 即升级前外观；取色模式下用户主色不生效（与原版一致）。

### 设置对齐
- **歌词字体阴影**（原版 `theme.fontShadow`，默认 `false`）：给歌词文字加一层轻投影，浅色封面/壁纸下更清晰。
  只改绘制样式，不改字号与行高，因此不会引起 LazyColumn 重新测量。

### UI / 动效打磨
- **迷你条歌名/歌手切歌淡入**（`ui/AppRoot.kt`）：封面已有 220ms 淡入而文字此前是硬切，缓存命中切歌时
  出现「封面在动、文字已就位」的割裂感；补 200ms 淡入 + 6dp 上浮（仅 alpha/translationY），
  减弱动效时初值即终值、静帧到位。

### 工程
- 版本 1.5.3 → 1.6.0（versionCode 27 → 28）；README 功能表同步。
- 验证：`python3 scripts/selfcheck.py` 通过；本地 `./gradlew assembleRelease --offline` 退出码 0（JDK 20 + SDK 37）；
  云端 `Build APK` 工作流复核。

## 第二十四轮：榜单详情显示设置对齐与歌词空态统一（v1.5.3，本次）

> 对照原版 lx-music-mobile v1.9.2 补齐一项列表显示设置，并统一一处空态语言。不引入依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议；全部 `.kt` 改动通过 `scripts/selfcheck.py`。

### 功能对齐（原版设置项在最后一页生效）
- **榜单详情副标题接入三项列表显示设置**（`ui/screens/board/BoardDetailScreen.kt`）：原版 `list.isShowSource / isShowInterval / isShowAlbumName` 三项开关此前已在收藏页、歌单详情、搜索结果、我的列表四处生效，唯独榜单详情页仍硬编码「歌手 · 来源」。本轮按收藏页 `FavoriteCard` 的 `buildList` 组装方式接入——歌手恒显示，专辑名/来源/时长按需拼接，空片段过滤后不会出现悬空的「 · 」。至此三项设置在全站五个列表页全部生效。

### UI / 动效打磨
- **播放页「暂无歌词」接入统一空态**（`ui/screens/player/PlayerScreen.kt`）：这是全站最后一处裸 `Box + Text` 文字空态。改用公共 `EmptyHint`（居中 + 占位图标 + 上浮淡入入场，减弱动效时静帧），与收藏/队列/歌单/榜单/搜索等页面的空态语言一致。

### 工程
- 版本 1.5.2 → 1.5.3（versionCode 26 → 27）；README 同步。

## 第二十三轮：通知栏封面开关与迷你条封面转场（v1.5.2）

> 对照原版 lx-music-mobile v1.9.2 补齐一项设置功能，并打磨一处封面转场。不引入依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议；全部 `.kt` 改动通过 `scripts/selfcheck.py`。

### 功能对齐（原版缺失项）
- **通知栏/锁屏封面显示开关**（原版 `player.isShowNotificationImage`，默认 `true`；`SettingsRepository` / `SettingViewModel` / `SettingScreen` / `PlayerRepository` / `LxApp`）：此前移植版 `toMediaItem()` 无条件设置 `artworkUri`，用户无法关闭通知栏封面。新增同名设置项（基本设置→播放设置分组，默认 `true` 与原版一致）；关闭后媒体通知与锁屏只显示歌名/歌手，播放页封面不受影响。切换时通过 `replaceMediaItem` 即时刷新当前媒体项 metadata——它保留播放位置与播放/暂停状态、不触发切歌回调，听感无变化；当前项未解析出播放地址时跳过刷新，避免写回空 URI 误报错误。

### UI / 动效打磨
- **迷你条封面切歌淡入**（`ui/AppRoot.kt`）：播放页封面已有 260ms 淡入+缩放，但底部迷你条此前直接硬切封面（缓存命中时尤为突兀）。以歌曲 id 为 key 加 220ms 淡入 + 0.92→1 轻微缩放，与播放页入场语言一致；只动 opacity/scale（transform），不触发布局；减弱动效时静帧到位，无逐帧调度。

### 工程
- 版本 1.5.1 → 1.5.2（versionCode 25 → 26）；README 同步。

## 第二十二轮：我的列表功能对齐（v1.5.1）

> 对照原版 lx-music-mobile v1.9.2 的《我的列表》歌曲行，补齐两处显示层功能：三项列表显示设置（来源/时长/专辑名）在本页生效，以及当前播放曲目的播放态指示。不引入依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议；全部 `.kt` 改动通过 `scripts/selfcheck.py`。

### 功能对齐（原版缺失项）
- **副标题接入三项列表显示设置**（`ui/screens/mylist/MyListsScreen.kt`）：原版《我的列表》歌曲行（`Mylist/MusicList/ListItem.tsx`）会按 `list.isShowAlbumName` 追加专辑名、按 `list.isShowInterval` 显示时长，并受 `list.isShowSource` 控制来源；此前移植版该页副标题固定只有歌手名，用户在设置里的三项开关对本页完全无效。本轮按收藏页 `FavoriteCard` 的 `buildList` 组装方式接入——歌手恒显示，专辑名/来源/时长按需拼接，空片段过滤后不会出现悬空的「 · 」。至此三项设置已在收藏页、歌单详情、搜索结果、我的列表四处全部生效。
- **当前播放曲目指示**（同文件）：原版《我的列表》对正在播放的曲目用 `activeIndex` 高亮；此前移植版每行都是统一的 Play 图标，无法识别当前曲目。本轮复用播放队列页已有的 `PlayingIndicator`——当前曲目显示三条律动音柱（暂停时静帧、减弱动效时静帧），歌名染主色；只订阅 `current?.id` 与 `isPlaying` 两个派生字段，播放进度每秒变化不会触发本页重组。多选态下让位给选中/未选中图标，避免语义冲突。

### 工程
- 版本 1.5.0 → 1.5.1（versionCode 24 → 25）；README 同步。

## 第二十一轮：列表显示设置全站对齐（v1.5.0）

> 对照原版 lx-music-mobile v1.9.2 的 `list.isShowSource / isShowInterval / isShowAlbumName` 三项显示设置，把它们从「仅收藏页生效」补齐到歌单详情页与搜索结果页。不引入依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议；全部 `.kt` 改动通过 `scripts/selfcheck.py`。

### 功能对齐（原版设置项在更多页面生效）
- **歌单详情页 / 搜索结果页副标题**（`ui/screens/songlist/SongListDetailScreen.kt`、`ui/screens/search/SearchScreen.kt`）：此前两页分别固定显示「歌手 · 来源」与「歌手 · 时长」，用户在设置里的「显示歌曲来源 / 显示歌曲时长 / 显示专辑名」三项开关对这两页完全无效。本轮按收藏页 `FavoriteCard` 的既有 `buildList` 组装方式接入三开关——歌手恒显示，专辑名/来源/时长按需拼接，与收藏页行为一致。空片段经 `filter` 过滤，不再出现时长为空时尾部悬空的「 · 」。
- **设置页初值一致性**（`ui/screens/setting/SettingScreen.kt`）：修正 4 处 `collectAsState(initial=...)` 与落盘默认不符导致的首帧闪烁——歌词翻译 `true→false`、显示热搜 `true→false`、显示历史 `true→false`、添加位置 `bottom→top`，均与 `SettingsRepository` 默认值对齐。

### 工程
- 版本 1.4.9 → 1.5.0（versionCode 23 → 24）；README 同步。

## 第二十轮：歌词逐句定位线与拖动滑块反馈（v1.4.9）

> 对照原版 lx-music-mobile v1.9.2 补齐一项播放页功能，并打磨迷你条拖动反馈。不引入依赖、不改路由/状态管理/播放内核、不触碰 QuickJS 与音源协议。全部 `.kt` 改动通过 `scripts/selfcheck.py`，并由本地 `assembleRelease` 实编译通过（云端 Actions 复核）。

### 功能对齐（原版缺失项）
- **歌词逐句定位线**（原版 `playDetail.isShowLyricProgressSetting` + `PlayLine`，`SettingsRepository` / `SettingViewModel` / `SettingScreen` / `PlayerScreen`）：原版在播放页设置里可开启「歌词进度线」——手动滚动歌词时，视线（距顶部 40%）处浮出一条虚线刻度、该句时间戳和一个播放键，点播放键跳到该句。移植版此前无此功能。本轮新增同名设置项（默认 `false`，与原版一致），开启后在 `LyricView` 中由 `LazyListState.layoutInfo` 取视线最近的歌词行，叠加淡入的定位线（180ms，减弱动效时瞬时），点击跳转复用已有 `onSeek` 并恢复自动跟随。只读布局信息、不拦截手势、不增加列表占位项，跟随自动滚动时不显示。

### UI / 动效打磨
- **迷你条进度拖动滑块**（`ui/AppRoot.kt`）：v1.4.8 加入的拖动手势此前只有细线跟随，缺少「正在拖动」的视觉锚点。拖动时现在显示一个 10dp 主色滑块，中心对齐进度点，用 `graphicsLayer` 的 translation + 轻回弹缩放（只动 transform，不改布局）；松手即消失。减弱动效时滑块直接满尺寸出现，无回弹。

### 工程
- 版本 1.4.8 → 1.4.9（versionCode 22 → 23）；README 同步。
- 验证：`python3 scripts/selfcheck.py` 通过；`assembleRelease --offline` 退出码 0；首次编译发现 `drawBehind` lambda 内读取 `MiuixTheme.colorScheme` 的 Compose 作用域错误，已将颜色提到 Composable 作用域后复编通过（详见报告）。

## 第十九轮：迷你条进度条拖动跳转（v1.4.8）

> 本轮对照原版 lx-music-mobile v1.9.2 做一项交互功能对齐：原版 `common.allowProgressBarSeek`。不触碰 QuickJS 引擎、音源协议、播放内核、路由结构与状态管理；复用播放页已有的 `seekTo`，未新增任何依赖。所有 `.kt` 改动通过 `scripts/selfcheck.py` 静态自检，构建由 GitHub Actions 云端验证。

### 功能对齐（原版缺失项）
- **底部迷你条进度条可拖动跳转**（原版 `common.allowProgressBarSeek`，默认 `true`，`SettingsRepository` / `SettingViewModel` / `SettingScreen` / `AppRoot`）：此前迷你条底部进度线纯装饰、不可交互，用户在列表页浏览时若想跳转播放位置必须先进入播放页。本轮给进度线加水平拖动手势——拖动时进度线实时跟随手指，松手才真正 `seekTo`（拖动过程不逐帧跳转）。新增设置开关「进度条可拖动」（默认开启，与原版一致），关闭后行为完全回到改动前。命中区由 2dp 视觉线扩为 16dp 透明容器（视觉线仍贴底 2dp，位置尺寸不变），解决细线难以点中的问题。
- **无障碍与减弱动效**：进度区保留 `progressBarRangeInfo` + 「播放进度」读屏语义；拖动为即时位置反馈，非装饰动画，遵循系统「移除动画」时不引入任何补间。
- 版本 1.4.7 → 1.4.8（versionCode 21 → 22）。

## 第十八轮：歌曲来源名称（原名/别名）对齐（v1.4.7）

> 本轮对照原版 lx-music-mobile v1.9.2 做一项功能对齐：原版 `common.sourceNameType`。不触碰 QuickJS 引擎、音源协议、播放内核、路由结构与状态管理。所有 `.kt` 改动均通过 `scripts/selfcheck.py` 静态自检，并在本地以 AGP 9.4.1 + Gradle 9.7.1 + compileSdk 37 执行 `assembleRelease` 实编译（产物 APK），同时由 GitHub Actions 云端复核。

### 功能对齐（原版缺失项）
- **歌曲来源名称：原名 / 别名切换**（原版 `common.sourceNameType`，`SettingsRepository` / `SettingViewModel` / `SettingScreen` / `LxApp` / `core/model/Models.kt` / `HomeViewModel`）：原版设置里有「歌曲来源名称」开关，可在别名（小蜗音乐、小枸音乐…）与原名（酷我音乐、酷狗音乐…）间切换，移植版此前恒显示原名。新增设置项（基本设置分组，点击循环，与主题/语言同一交互语言），默认 `real`（原名）——保持移植版历史显示，老用户升级后观感不变；原版默认为 `alias`，差异见报告待确认项。
- **响应式实现（10 处调用点零改动）**：`SourceConst` 增加两套显示名映射，并用 Compose `mutableStateOf` 承载当前类型；所有 `displayName()` 调用点均在 Composable 中读取该 State，切换后全站音源名（音源管理/搜索/榜单/歌单/收藏/队列）即时重组刷新。`LxApp` 启动时收集设置同步全局 State（`catch` 兜底防 DataStore 首读损坏）；首页标签改由 `displayName()` 实时读取，不再用缓存字符串。未收录的 `user_api` 源原样返回 id，自定义源名仍走原缓存路径。
- 版本 1.4.6 → 1.4.7（versionCode 20 → 21）。

## 第十七轮：启动直达播放页与音源选中态视觉（v1.4.6，本次）

> 本轮对照原版 lx-music-mobile v1.9.2 做一项功能对齐与一项 UI 打磨，不触碰 QuickJS 引擎、音源协议、播放内核、路由结构与状态管理。所有 `.kt` 改动均通过 `scripts/selfcheck.py` 静态自检，并在本地以 AGP 9.4.1 + Gradle 9.7.1 + compileSdk 37 执行 `assembleRelease` 实编译（产物 APK），同时由 GitHub Actions 云端复核。

### 功能对齐（原版缺失项）
- **启动时打开播放详情页**（原版 `player.startupPushPlayDetailScreen`，`SettingsRepository` / `SettingViewModel` / `SettingScreen` / `AppRoot`）：原版有此开关而移植版缺失。新增设置项（默认 `false`，与原版 `defaultSetting` 一致）；开启后冷启动若检测到上次播放记录，进入根页 Home 时一次性 push 播放页。用 `startupNavDone` 守护只触发一次，且仅在仍停留 Home 时进入，不会覆盖用户已手动导航的状态。上次播放恢复是异步的，故观察 `state.map { current != null }` 待恢复完成后再判定。纯设置/导航层，不改播放逻辑。

### UI / 动效打磨
- **音源选中态整卡高亮**（`ui/screens/source/SourceScreen.kt`）：内置音源与自定义音源的「使用中」项由单一的「✓ 使用中」文字，改为整卡主色淡染（10% 主色）+ 音源名主色，与播放队列「正在播放」、搜索音源 chip 的选中态统一为同一设计语言。切换音源时由 `GlassSurface` 内置的 `animateColorAsState`（160ms）平滑淡变，遵循静帧降级。仅视觉反馈，`switchSource` 逻辑不变。
- 版本 1.4.5 → 1.4.6（versionCode 19 → 20）。

## 第十六轮：触感反馈、弹窗进出场与播放模式可达性（v1.4.5）

> 本轮只做 UI 层交互手感与可达性打磨，不触碰 QuickJS 引擎、音源协议、播放内核、路由结构与状态管理。所有 `.kt` 改动均通过 `scripts/selfcheck.py` 自检，并在本地以 AGP 9.4.1 + Gradle 9.7.1 + compileSdk 37 执行 `assembleRelease` 实机等价编译（产物 APK），非仅云端验证。

### 交互手感
- **列表卡片按压反馈**（`ui/components/Glass.kt`）：`GlassSurface` 补三层反馈 —— 系统涟漪保留、填充色自动加深一档（180ms）、整卡缩到 0.98（spring 回弹，只动 transform 不触发重新布局）。此前全站卡片「按下去毫无变化」，点按是否命中全靠猜。长按同样生效（`collectIsPressedAsState` 覆盖 press 全程）。
- **弹窗退出动画与窗口淡入**（同文件）：`GlassDialog` 点「取消」/返回键/点外部时先播 120ms 退出动画再真正 dismiss（此前是直接消失，与入场动画不对称）；同时关掉系统自带 dim，改由自身 scrim 承担遮罩，并让 Dialog 窗口 alpha 跟随 scrim 进度，消除点「播放设置」时的黑底闪一下。
- **系统「移除动画」降级**：`animationsDisabled()` 读取 `ANIMATOR_DURATION_SCALE`，为 0 时跳过手写的按压缩放与窗口淡入，保证「关掉动画 = 立刻到位」。

### 视觉层级
- **顶栏分隔线**（`GlassTopBar`）：顶栏底部加 1px 分隔线（浅色 4%、深色 8%），解决滚动时内容「贴上」标题区、缺少边界的问题。
- **播放队列当前曲目整卡高亮**（`ui/screens/playqueue/PlayQueueScreen.kt`）：当前曲目由「只把歌名染主色」改为整卡主色淡染 + 主色描边，对齐原版播放列表的「正在播放」底色，长队列中扫视定位更快。
- **首页入口层级**（`ui/screens/home/HomeScreen.kt`）：音源管理卡用主色淡染强调，区分于下方并列的两张功能卡（此前三张卡材质完全一致，看不出主次）。
- **首页加载骨架**（同文件）：榜单加载中由一行小字改为 3 个与真实卡片同高的静态骨架块，避免「空白 → 列表突然出现」的跳变；空态改用统一的 `EmptyHint`。

### 可达性 / 可用性
- **播放模式分段控件**（`ui/screens/player/PlayerScreen.kt`）：5 个长文案（「列表循环播放」「禁用歌曲切换」…）横排总宽超过屏宽，原先的固定 `Row` 没有滚动容器，窄屏或大字号下末尾两项会被裁掉、根本无法选中。改为横向可滚动 + 选中项主色胶囊底，选中态不再只靠文字颜色区分，触控高度也提到约 44dp。

### 修复
- **迷你播放条隐藏判定**（`ui/components/ScrollReporter.kt`）：此前只比较 `firstVisibleItemIndex`，长列表里「点条目前挪半屏」「下拉回弹」这类不动下标的滚动不会被识别，导致播放条该藏不藏或反复闪。改为同时观察行内偏移量，并以 8px 作为抖动阈值。

---

本项目为洛雪音乐 Compose 移植版「聆序」。以下记录基于对全部源码的系统审查所执行的优化与美化。

## 第十五轮：转场方向、列表动效与播放态可视化（v1.4.4）

> 项目定位不变：**同时具备「在线」与「本地」功能的音乐播放器**。本轮只做 UI 层动效与视觉细节的低风险打磨，不触碰 QuickJS 引擎、音源协议、播放内核、路由结构与状态管理，在线/本地双模行为均不变。全部 `.kt` 改动通过 `scripts/selfcheck.py` 自检，构建由 GitHub Actions 云端验证。

### 动效 / 交互
- **返回转场方向修正**（`ui/navigation/Navigator.kt` + `ui/AppRoot.kt`）：此前无论前进还是返回，页面都从右侧滑入、向左滑出，按返回时方向感与手势相反。导航器记录最近一次是否为 `pop`，转场据此反向：返回时上一页从左侧滑入、当前页向右滑出；tab 切换与前进仍保持原方向。
- **播放队列当前曲目律动音柱**（新增 `ui/components/PlayingIndicator.kt`）：当前曲目原先只是把 Play 图标染成主色，与普通行差异很弱。改为三条不同周期的音柱表达「正在出声」，暂停时停在半高静帧（不空转动画），并带「正在播放/已暂停」读屏语义。列表中仅当前一行使用。
- **列表增删/排序动画**：搜索结果（音乐/歌单）、收藏、播放队列、我的列表（清单与歌单内）、歌单详情、榜单详情、首页榜单的 `LazyColumn` 条目统一加 `Modifier.animateItem()`，插入/移除/上移下移时平滑让位，不再瞬移（均使用稳定 key）。
- **播放/暂停图标过渡**（`PlayerScreen.kt` / `AppRoot.kt`）：播放页主控与迷你条图标改用 `Crossfade`（180ms），避免同一位置硬切。
- **「回到当前歌词」浮层**（`PlayerScreen.kt`）：手动回看歌词时由突然出现改为淡入 + 轻微上浮（180ms），退出反向。
- **弹窗进场**（`ui/components/Glass.kt`）：`GlassDialog` 增加 scrim 淡入 + 面板 0.94→1 缩放淡入（160–200ms），不再整块「啪」地出现；仍是独立窗口，不涉及 backdrop 采样。
- **玻璃卡片选中态颜色过渡**（同文件）：`GlassSurface` 的填充色走 `animateColorAsState`（180ms），搜索页音源 chip 等选中/取消不再硬切。
- **迷你播放条**（`AppRoot.kt`）：进度线由 500ms 一次的跳格改为等速补间连起来；播放/队列按钮补按压缩放反馈（与播放页一致）。
- **我的列表多选底栏**（`MyListsScreen.kt`）：进入/退出多选时淡入并纵向展开，避免列表突然被顶短；窄屏或大字号下四个按钮横向可滚动，「移除」始终可达。

### 修复 / 可访问性
- **关闭模糊后迷你条无背景**（`AppRoot.kt`）：模糊关闭或系统低于 API 31 时，`drawBackdrop` 分支不生效，而备选分支没有任何背景，歌名/歌手直接压在滚动内容上难以阅读。补近不透明表面色 + 细描边（深色模式用 0xFF1C1C1E），可读性与玻璃态一致。
- **迷你条进度可读屏**（同文件）：进度细线补 `progressBarRangeInfo` 与「播放进度」描述，读屏用户可获知百分比，而非只有装饰线条。
- 版本 1.4.3 → 1.4.4（versionCode 17 → 18），README/ANALYSIS 同步。

## 第十四轮：对照原版 lx-music-mobile v1.9.2 的行为对齐（v1.4.3）

> 本轮以 `lyswhut/lx-music-mobile`（v1.9.2）源码为基准逐模块核对，只改「与原版不一致」之处，不触碰 QuickJS 引擎、assets 脚本与音源协议。每项均标注原版出处。项目定位不变：**同时具备「在线」与「本地」功能的音乐播放器**。

### 默认设置对齐（原版 `src/config/defaultSetting.ts`）
- `list.addMusicLocationType` 默认 **`top`**：此前本项目默认 `bottom`，导致「添加到列表/收藏」落到末尾，与原版相反。
- `player.isShowLyricTranslation` 默认 **`false`**：此前默认 `true`，新装用户会直接看到翻译歌词。
- `search.isShowHotSearch` / `search.isShowHistorySearch` 默认 **`false`**：此前默认 `true`。
- `SettingViewModel` 的 UI 初值同步，避免首帧显示与落盘值不一致。

### 播放器语义对齐
- **定时退出到点退出应用**（原版 `timeoutExit.exit()` → `exitApp`）：此前到点仅 `pause()`。现未开启「播完当前曲再退」时到点 `finishAffinity()` 退出应用；开启时仍等当前曲播完再停。
- **「禁用歌曲切换」模式播完不切歌**（原版 `getNextPlayMusicInfo` 对 `none` 返回 null）：ExoPlayer 在 `REPEAT_MODE_OFF` 下会自动进入下一项，现回退到刚播完的曲目再暂停，不再「跳到下一首后暂停」。
- **播放出错 5 秒后自动切下一首**（原版 `playerEvent.addDelayNextTimeout(5000)` 与 `player__error` 文案）：此前出错仅显示提示并卡在该曲。切歌/清空/释放会取消待执行的自动切歌。
- **播放模式文案**（原版 `lang/zh-cn.json`）：列表循环播放 / 顺序播放 / 随机播放 / 单曲循环播放 / 禁用歌曲切换（第五项原版即「禁用歌曲切换」，并非「单曲播放」）。

### 搜索与规则对齐
- **搜索历史**：上限 30 → **15**（原版 `store/search/action.ts`）；未开启「显示搜索历史」时**不再记录**（原版 `addHistoryWord` 守卫）。
- **不喜欢列表匹配忽略大小写**（原版 `utils/dislikeManage.ts` 统一 `toLocaleLowerCase`）：英文歌名大小写不同也能命中。

### 已确认但本轮未改（高风险/大工程，列入后续）
- 随机播放算法：原版为「每次随机（可重复）+ 记忆上一随机」，本项目用 ExoPlayer shuffle（体验相近、语义不同）。
- 「稍后播放 / 播放历史 / 临时列表」：原版 `tempPlayList`、`playedList`、`LIST_IDS.TEMP`，本项目暂无。
- 备份格式：原版为「列表数组 + 设置 + 不喜欢」结构，与本项目 JSON 不兼容，需专门做导入映射。
- 主题系统、桌面歌词、下载、逐字歌词：原版独有的大功能，需分批移植。
- 版本 1.4.2 → 1.4.3（versionCode 16 → 17）。

## 第十三轮：更新检查可用性修复 + 下载入口（v1.4.2）

> 项目定位不变：**同时具备「在线」与「本地」功能的音乐播放器**。本轮只改更新检查链路与设置页提示，不触碰 QuickJS 引擎、音源协议与 assets 脚本，在线/本地双模行为不受影响。全部 `.kt` 改动通过 `scripts/selfcheck.py` 自检，构建由 GitHub Actions 云端验证。

### Bug 修复 / 功能可用性
- **「检查更新」长期失效**（`UpdateRepository.kt`）：更新源此前只有 `releases/latest`，而本仓库以 Actions 产物分发、从未发布 Release，该端点长期 404，用户点「检查更新」永远得到「无法连接更新源」。现改为多源兜底：`releases/latest` → `releases` 列表 → `tags` 列表 → `commits/master`，并对数组响应取首项、从提交信息（如 `v1.4.2: …`）解析版本号；即使没有 Release 也能给出正确的「已是最新 / 有新版本」结论。
- **多源结果取最大版本**（同文件）：不再「首个成功即返回」——Releases 列表首项可能比标签旧，先命中会误报旧版本；改为遍历全部源后按 `compareVersion` 取最大。
- **发现新版本却无下载入口**（`SettingViewModel.kt` / `SettingScreen.kt`）：`downloadUrl` 此前被丢弃，对话框只有一句「好」。现把下载地址透传到 UI，命中新版本时显示「前往下载」按钮，用系统浏览器直开；优先取 Release 资产里的 `.apk` 直链，缺失时回退 Releases 页面。
- 版本 1.4.1 → 1.4.2（versionCode 15 → 16），README 同步。

## 第十二轮：数据一致性与启动健壮性 + 空态组件化（v1.4.1）

> 项目定位不变：**同时具备「在线」与「本地」功能的音乐播放器**。本轮延续低风险原则，不触碰 QuickJS 引擎、音源协议与 assets 脚本，在线聚合搜索/播放与本地歌单/收藏/队列双模行为均不受影响。全部 `.kt` 改动通过 `scripts/selfcheck.py` 自检，构建由 GitHub Actions 云端验证。

### Bug 修复 / 正确性
- **备份导入重复 id 会崩溃**（`BackupRepository.kt`）：`favorites` 早已去重，但 `playlists`/`playlistMusics` 仅做白名单过滤未去重；手工编辑或旧版本备份若含重复 id，会以 id 作 LazyColumn 稳定 key 触发 "Duplicate key" 崩溃。补 `distinctBy`（歌单按 id、歌曲按 listId+id）。
- **暂停时删除当前曲目会突然开播**（`PlayerRepository.kt`）：`removeFromQueue` 删当前项走 `playQueue`，而后者无条件 `player.play()`，导致暂停态被破坏。给 `playQueue` 增加 `autoPlay` 参数（默认 true 保持其它调用方行为），删除场景传 `player.playWhenReady` 沿用删除前播放状态。
- **快速连续切歌上次播放状态可能写反**（`PlayerRepository.kt`）：多处 `saveLastPlay()` 并发写同一文件、完成顺序不定，旧状态可能最后落盘导致冷启动恢复错曲目。新增 `lastPlayJob`，每次取消在途写只保留最新一次，`clear`/`release` 一并取消。

### 健壮性
- **启动初始化链一处失败拖垮全局**（`LxApp.kt`）：设置项变化监听（日志开关/音频焦点/倍速/音量）此前嵌在主初始化协程里，前面任一未保护的 `.first()` 读取（DataStore 偏好损坏时首读会抛异常）失败都会让后续监听不注册且表面「正常启动」。改为 4 个独立协程启动并加 `.catch` 兜底；播放模式/上次播放/自动开播等读取逐项 `runCatching` 保护。
- **HTTP 超时无上限**（`HttpClient.kt`）：`fetch` 的自定义 timeout 只有下限，脚本传超大值会让请求长期挂住占住协程与连接池；改为 `coerceIn(1L, 120_000L)` 加上限。

### 美化 / 可维护性 / 可访问性
- **新增公共空态组件 `EmptyHint`**（`ui/components/EmptyHint.kt`）：收藏页、播放队列两处「居中 + 占位图标 + 次要色文案」空态抽取为统一组件，留白收敛为 56dp，消除重复并登记进自检符号表。
- **自定义音源空态**（`SourceScreen.kt`）：零导入时自定义音源列表由一整块空白改为 `EmptyHint`（「还没有自定义音源，点上方按钮导入」），与全站其它列表页一致。
- **收藏页错误提示可关闭**（`FavoriteScreen.kt` / `FavoriteViewModel.kt`）：加入列表失败的常驻红字改为与榜单详情一致的 `GlassDialog`（可关闭），成功时清除错误，新增 `clearError()`。
- **播放队列排序热区达标**（`PlayQueueScreen.kt`）：上移/下移图标点击热区 32dp → 48dp（图标视觉尺寸不变），补齐全站 48dp 无障碍统一的最后一处遗漏。
- **多选态无障碍语义**（`MyListsScreen.kt`）：多选模式下歌曲行图标 `contentDescription` 由 null 改为随选中状态切换「已选中/未选中」，读屏用户可感知。
- 版本 1.4.0 → 1.4.1（versionCode 14 → 15），README/ANALYSIS 同步。

## 第十一轮：正确性与性能综合复审 + 空态/热区美化（v1.4.0）

> 项目定位不变：**同时具备「在线」与「本地」功能的音乐播放器**。本轮在第十轮基础上做覆盖正确性、性能与可访问性的综合复审，坚持低风险原则，不触碰 QuickJS 引擎、音源协议与 assets 脚本，在线/本地双模行为均不受影响。

### Bug 修复 / 正确性
- **搜索空态被历史遮蔽**（`SearchScreen.kt`）：搜索后输入框保留关键词，而历史分支只判结果为空，导致任何无结果搜索都误显「搜索历史」并遮蔽「没有找到相关音乐」空态；补上输入框为空判断。
- **歌单广场错误重复渲染**（`SongListScreen.kt`）：加载失败且列表空时同一句错误文案出现两遍（顶部红字 + 空态 RetryHint），删除顶部红字，统一由 RetryHint 展示。
- **`fetchSync` 支持 PUT/PATCH**（`HttpClient.kt`）：原仅 post 带 body 才构造 RequestBody，音源脚本用 PUT/PATCH 取链/签名会因 body 为 null 抛异常失败；补齐空 body 处理，并与异步 `fetch()` 方法集合复用 `BODY_METHODS` 常量。
- **版本号比较兼容大写 V**（`UpdateRepository.kt`）：`compareVersion` 原只去小写 `v`，`V1.4.0` 会被解析成 0.0.0 误判为旧版；补去 `V`。
- **定时分钟数溢出防护**（`PlayerRepository.kt`）：`minutes * 60` 的 Int 溢出会令定时立即到期、播放被瞬间暂停；改 Long 运算后钳制到 Int 上限。

### 性能
- **在线歌单翻页去重**（`MyListsRepository.kt`）：`pullOnlineMusics` 由 O(N²) 线性判重改为 HashSet O(1) 查重，并去掉批内重复 id。
- **歌单同步/保存查重**（同文件）：`overwriteOnlineMusics`、`saveMusicsLocked` 内按 id 查旧记录由线性扫描改为 `associateBy` 哈希查找。
- **备份恢复写盘合并**（`BackupRepository.kt`）：歌单表恢复由逐条 delete+upsert（O(N) 次全文件写盘）改为一次 `replacePlaylists` 覆盖。

### 健壮性 / 工程
- **「不喜欢」规则落盘一致性**（`DislikeRepository.kt`）：写盘失败不再被静默吞掉造成「界面已保存、重启丢失」，改为落盘成功后才更新内存；临时名加 UUID 防并发覆盖。
- **死代码清理**（`MusicRepository.kt`）：删除未使用的 `sourceMutex`。
- 版本 1.3.9 → 1.4.0（versionCode 13 → 14），README/ANALYSIS 同步；全部 `.kt` 改动通过 `scripts/selfcheck.py` 自检，CI 构建验证。

### 美化 / 可访问性
- **空态视觉统一**：播放队列、我的列表清单空态由左对齐裸文字改为居中 + 占位图标 + 次要色文案。
- **首页榜单三态提示**（`HomeScreen.kt`）：加载中显示「正在加载榜单…」，不再空白。
- **搜索错误重试**（`SearchScreen.kt`）：两处重试入口改用公共 `RetryHint`（热区约 44dp + 按压反馈）。
- **热区达标**：设置页「不喜欢」删除、首页热搜榜每行扩至约 48dp。
- **长按删除可发现性**（`SourceScreen.kt`）：自定义音源补「长按音源可删除」提示。

## 第十轮：全站可访问性与空态美化 + 公共组件抽取（v1.3.9）

> 项目定位：**同时具备「在线」与「本地」功能的音乐播放器**。本轮为一次全源码复审后的系统性美化与可访问性打磨，改动全部落在 UI 层的视觉/触控细节与重复的抽取，不涉及播放内核、音源协议与 QuickJS 引擎，在线/本地双模行为均不受影响。

### 美化 / UI 一致性
- **返回键统一公共组件**（新增 `ui/components/BackIconButton.kt`）：此前 6 个二级页（音源/榜单详情/歌单广场/歌单详情/我的列表/播放队列）各自用 `Icon(28dp)` 直接挂 clickable，热区仅 28dp。统一为 `BackIconButton`——图标视觉仍 28dp，点击热区撑到 48dp（Material 无障碍最小目标），语义统一为「返回」，消除 6 处重复代码。
- **空态视觉语言统一**：收藏页「还没有收藏的歌曲」、我的列表列表内空态、榜单详情列表内空态，此前是左对齐一行裸文字，现统一为居中 + 40dp 占位图标 + 次要色文案，与歌单详情/搜索页既有空态一致。
- **榜单详情搜索空态**（`BoardDetailScreen.kt`）：此前榜单非空但搜索关键词无匹配时，`shownMusic` 为空而 `musicList` 非空，不触发空态导致列表一片空白；现新增专属「没有匹配的歌曲」空态 item，并补上与歌单详情一致的「正在加载榜单…」加载提示。

### 可访问性
- **删除/移除图标热区**：收藏页、我的列表（列表内 + 列表清单）、播放队列的删除图标热区由 24dp → 48dp，视觉图标尺寸保持 24dp 不变（外层 Box 承载点击、内层 Icon 居中）。
- **搜索结果收藏按钮**（`SearchScreen.kt`）：收藏/取消收藏图标热区 24dp → 48dp。
- **播放页收藏按钮语义**（`PlayerScreen.kt`）：`contentDescription` 由固定「收藏」改为随状态切换「收藏/取消收藏」，与搜索页一致，读屏用户可感知当前状态。
- **播放页错误恢复**（`PlayerScreen.kt`）：播放失败时的「下一首」文字热区由约 20dp 扩到带圆角裁剪的 44dp 命中区，这是错误态唯一恢复出口，提升可点中率。
- **播放队列「清空」**（`PlayQueueScreen.kt`）：破坏性操作文字热区由约 28dp 扩到 48dp 方形命中区。
- **加载失败重试提示**（新增 `ui/components/RetryHint.kt`）：此前首页/歌单广场/榜单详情/歌单详情四处各写一遍 error 文字 + clickable + padding，热区偏小；统一为 `RetryHint`——error 色 + 圆角裁剪 + 约 44dp 热区。

### 可维护性 / 性能
- **翻页去重统一**（新增 `core/model/ListExt.kt`）：三处列表页此前重复写 `list + result.filterNot { it.id in list.map { x -> x.id } }`，谓词里每次比较都重建 id 集合（O(N·M)）。抽为 `appendDistinctById` 扩展——id 集合只构建一次（O(N+M)），并用 `seen.add` 顺带去掉新批次内部重复 id，避免重复 key 让 LazyColumn 崩溃。
- **清理重复 KDoc**（`core/player/PlayerRepository.kt`）：`LyricLine` 上两行重复注释合并为一行。
- **自检覆盖**（`scripts/selfcheck.py`）：`PROJECT_SYMBOLS` 登记 `BackIconButton` / `RetryHint`，漏 import 也可本地拦截。

### 工程
- 版本 1.3.8 → 1.3.9（versionCode 12 → 13），README/ANALYSIS 同步；全部 `.kt` 改动通过 `scripts/selfcheck.py` 自检，CI 构建验证。

## 第九轮：歌词手动回看 + 长音频时长 + 翻页竞态（v1.3.8）

> 项目定位：**同时具备「在线」与「本地」功能的音乐播放器**。本轮改动作用于在线/本地共用的播放页与搜索页，不影响本地数据。

### UI / UX
- **歌词手动回看不被打断**（`ui/screens/player/PlayerScreen.kt`）：此前播放页歌词每切一句就自动滚回当前行，用户上滑回看时被强行拽回，无法阅读。新增跟随开关——手指按下歌词区即暂停自动跟随（用 `pointerInput` 只观察按下、不消费手势，滚动/点击不受影响），浮出「回到当前歌词」按钮一键恢复；点歌词跳转或切歌也自动恢复，恢复时当前行滑回居中。

### 正确性
- **长音频时长显示**（`ui/screens/player/PlayerViewModel.kt`）：`formatTime` 由固定 `MM:SS` 改为超过 1 小时用 `H:MM:SS`，并钳制负毫秒，避免 `75:30` 这类难读值与负值乱码。

### 并发健壮性
- **搜索翻页重复请求**（`ui/screens/search/SearchViewModel.kt`）：`search()`/`loadMore()` 把 `loading=true` 从 launch 内异步执行改为 launch 前同步置位，封住快速连点/换源时的重复翻页竞态窗口；`requestSeq` 版本锁继续丢弃迟到结果。

### 工程
- 版本 1.3.7 → 1.3.8（versionCode 11 → 12），README/ANALYSIS 同步；全部 `.kt` 改动通过 `scripts/selfcheck.py` 自检，CI 构建验证。

## 第八轮：定时恢复 + 抗漂移 + 触控热区美化（v1.3.7）

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
