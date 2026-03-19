/**
 * @file SettingsRoute.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 设置页 Route，负责桥接 ViewModel 与设置页 UI。
 */
package com.poco.dishvision.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Text
import com.poco.dishvision.core.data.preferences.AppPreferences
import com.poco.dishvision.core.data.repository.MenuRepository
import com.poco.dishvision.core.ui.components.GlassSurface
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.Dimens
import com.poco.dishvision.core.ui.theme.PocoTheme

/**
 * 设置页入口 Route。未注入真实依赖时回退到 preview，便于先完成 UI 测试。
 *
 * @param menuRepository 菜单仓储。
 * @param appPreferences 应用偏好。
 * @param modifier 外层 Modifier。
 */
@Composable
fun SettingsRoute(
    menuRepository: MenuRepository? = null,
    appPreferences: AppPreferences? = null,
    modifier: Modifier = Modifier,
) {
    if (menuRepository == null || appPreferences == null) {
        PocoTheme {
            SettingsScreen(
                uiState = previewSettingsUiState(),
                modifier = modifier,
            )
        }
        return
    }

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            menuRepository = menuRepository,
            appPreferences = appPreferences,
        ),
    )
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    PocoTheme {
        SettingsScreen(
            uiState = uiState,
            modifier = modifier,
        )
    }
}

/**
 * 设置页主界面。
 *
 * @param uiState 设置页状态。
 * @param modifier 外层 Modifier。
 */
@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = ColorTokens.SettingsBackgroundGradient,
                ),
            )
            .testTag("settings-screen"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenHorizontalPadding, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = "设置",
                color = ColorTokens.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "查看当前数据源与本地目录状态",
                color = ColorTokens.TextSecondary,
            )
            SettingsInfoCard(
                label = "当前数据源",
                value = uiState.sourceModeLabel,
            )
            SettingsInfoCard(
                label = "最近刷新",
                value = uiState.lastRefreshAt ?: "尚未记录",
            )
            SettingsInfoCard(
                label = "目录版本",
                value = uiState.catalogVersion ?: "未导入目录",
            )
        }
    }
}

/**
 * 设置页信息卡片。
 *
 * @param label 字段标题。
 * @param value 字段值。
 */
@Composable
private fun SettingsInfoCard(
    label: String,
    value: String,
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth(),
        containerColor = ColorTokens.GlassSurface,
        borderColor = ColorTokens.GlassBorderSubtle,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        contentSpacing = 8.dp,
    ) {
        Text(
            text = label,
            color = ColorTokens.TextMuted,
        )
        Text(
            text = value,
            color = ColorTokens.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
