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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Text
import com.poco.dishvision.core.data.preferences.AppPreferences
import com.poco.dishvision.core.data.repository.MenuRepository

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
        SettingsScreen(
            uiState = previewSettingsUiState(),
            modifier = modifier,
        )
        return
    }

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            menuRepository = menuRepository,
            appPreferences = appPreferences,
        ),
    )
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        modifier = modifier,
    )
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
                    colors = listOf(
                        Color(0xFF0E1520),
                        Color(0xFF182437),
                        Color(0xFF0A1018),
                    ),
                ),
            )
            .testTag("settings-screen"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = "设置",
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "查看当前数据源与本地目录状态",
                color = Color(0xCCE4EAF4),
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xCC152131),
                shape = RoundedCornerShape(26.dp),
            )
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            color = Color(0xCCF4F7FB),
        )
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
