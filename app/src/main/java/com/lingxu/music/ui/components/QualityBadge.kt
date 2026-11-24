package com.lingxu.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingxu.music.core.model.MusicInfo
import com.lingxu.music.core.model.QualityConst
import com.lingxu.music.ui.theme.isInDarkTheme
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 音质档位（由高到低） */
private enum class QualityTier { LOSSLESS_24BIT, LOSSLESS, HIGH }

/**
 * 列表行音质角标（对齐原版 `components/common/Badge` 与 `OnlineList/ListItem` 的音质标注）。
 *
 * ### 为什么要有它
 * 原版列表在歌名右侧用小号彩色字标出「24bit / 无损 / 高品质」，用户扫一眼就能判断这条结果
 * 值不值得点。移植版**完全没有这个信息**：搜索列表里 128k 与 flac24bit 长得一模一样，
 * 用户只能播了才知道——这是与原版差距最直观的一处视觉层级缺失。
 *
 * ### 设计约束
 * - 只读 [MusicInfo._qualitys]（顶层字段，不在 meta 里），不发起任何请求；
 * - 用主色系三档（24bit / 无损 / 高品质），与 Miuix 语义色一致，深浅色自动适配；
 * - 字号 9sp、无边框无底色，视觉重量低于歌名与副标题，不抢主信息；
 * - 无可用音质（本地文件、部分用户音源）时**不渲染**，避免列表出现空洞。
 */
@Composable
fun QualityBadge(music: MusicInfo, modifier: Modifier = Modifier) {
    // 音质档位由 _qualitys / qualitys 决定，而不是恒定不变的 id：同一首歌换源后 id 可能不变、
    // 但可用音质会变（如从 128k 源切到 flac 源）。原 key 只含 id 会缓存旧档位、角标不刷新。
    // 把音质字段纳入 key，换源重组时会重新判定；这些对象都很小，比较成本可忽略。
    val tier = remember(music.id, music.source, music._qualitys, music.qualitys) {
        qualityTierOf(music)
    } ?: return
    val dark = isInDarkTheme()
    val (label, color) = when (tier) {
        QualityTier.LOSSLESS_24BIT -> "24bit" to if (dark) {
            androidx.compose.ui.graphics.Color(0xFFFFC56B)
        } else {
            androidx.compose.ui.graphics.Color(0xFFB26A00)
        }
        QualityTier.LOSSLESS -> "无损" to MiuixTheme.colorScheme.primary
        QualityTier.HIGH -> "高品质" to MiuixTheme.colorScheme.onSurfaceVariantSummary
    }
    Text(
        label,
        modifier = modifier
            // 小圆角淡底 + 彩色字：比纯彩色文字更容易在浅色歌名旁被识别为「标签」
            .background(
                color = color.copy(alpha = if (dark) 0.16f else 0.12f),
                shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 4.dp, vertical = 1.dp),
        fontSize = 9.sp,
        color = color,
        lineHeight = 12.sp,
    )
}

/**
 * 按可用音质判定档位。与原版 `ListItem` 的判定顺序一致：
 * flac24bit > flac/ape > 320k。256k/192k 也算「高品质」——
 * 原版只认 320k，但本项目部分源只返回 192k，标出来仍有信息价值。
 */
private fun qualityTierOf(music: MusicInfo): QualityTier? {
    val available = music._qualitys.keys + music.qualitys.mapNotNull { it.type }
    return when {
        QualityConst.FLAC24 in available -> QualityTier.LOSSLESS_24BIT
        QualityConst.FLAC in available || QualityConst.APE in available -> QualityTier.LOSSLESS
        QualityConst.Q320 in available || QualityConst.Q192 in available -> QualityTier.HIGH
        else -> null
    }
}
