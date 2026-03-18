/**
 * @file PocoAsyncImage.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 提供项目统一的异步图片组件包装。
 */
package com.poco.dishvision.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * 项目统一异步图片组件；当模型为空时输出占位层，避免 UI 空洞。
 *
 * @param model 图片模型。
 * @param contentDescription 无障碍描述。
 * @param modifier 外层 Modifier。
 * @param contentScale 内容缩放方式。
 */
@Composable
fun PocoAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    if (model == null) {
        Box(
            modifier = modifier.background(Color(0xFF1D2635)),
        )
        return
    }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier.fillMaxSize(),
        contentScale = contentScale,
    )
}
