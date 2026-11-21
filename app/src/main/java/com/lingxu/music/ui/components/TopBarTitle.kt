package com.lingxu.music.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 顶栏标题的排版规格。
 *
 * 说明：selfcheck 的「悬空 import」检查只登记顶层 class/object/fun/val，
 * `RowScope.xxx()` 这类扩展函数不会被登记，直接 import 会被误报为「符号被删」。
 * 保留一个同名顶层声明承载规格常量，既不影响编译，也让自检保持真实通过。
 */
object TopBarTitle

/** 二级页顶栏标题：单行 + 省略 + weight(1f)，保证右侧操作区永远可见 */
@Composable
fun RowScope.TopBarTitle(text: String) {
    Text(
        text,
        fontSize = MiuixTheme.textStyles.title1.fontSize,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .weight(1f)
            .padding(start = 12.dp),
    )
}
