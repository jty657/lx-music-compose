package com.lingxu.music.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 内容列允许排布的最大宽度。
 *
 * LX Music 手机版是纯手机布局：卡片 `.fillMaxWidth()`，在 10 寸平板上会被拉成
 * 1000dp+ 宽的一条——歌名在左、时长在右，视线要横扫半个屏幕才能读完一行。
 * 本轮不加任何响应式结构（不引入两栏、不改导航形态），只做一件低风险的事：
 * **给内容列一个最大宽度并居中**，超出部分留白。手机（<600dp）完全无变化。
 */
val ContentMaxWidth = 840.dp

/**
 * 在调用处直接使用 `Modifier.widthIn(max = ContentMaxWidth)` 即可，不必包裹：[ContentWidthLimit]
 * 适合作为容器，但各页面 Column 的闭合位置各异，用 Modifier 更小步、更易回滚。
 *
 * `ContentWidthLimit` 把内容列限制在 [ContentMaxWidth] 内并水平居中。窄屏下等价于原样铺满。
 *
 * 注意：必须是 `BoxWithConstraints` 取父级可用宽度，不能用 `fillMaxWidth(0.9f)` 之类
 * 的比例写法——比例在手机上同样会缩窄，而这里只在「超过上限」时生效。
 */
@Composable
fun ContentWidthLimit(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        val target = if (maxWidth > ContentMaxWidth) ContentMaxWidth else maxWidth
        Box(Modifier.widthIn(max = target).fillMaxWidth()) { content() }
    }
}
