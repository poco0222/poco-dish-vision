/**
 * @file SettingsViewModel.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 从 AppPreferences 与 MenuRepository 组合设置页状态。
 */
package com.poco.dishvision.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.poco.dishvision.core.data.preferences.AppPreferences
import com.poco.dishvision.core.data.repository.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 设置页 ViewModel，负责展示当前数据源模式、本地刷新时间与目录版本。
 *
 * @param menuRepository 菜单仓储。
 * @param appPreferences 应用偏好封装。
 */
class SettingsViewModel(
    private val menuRepository: MenuRepository,
    private val appPreferences: AppPreferences,
) : ViewModel() {

    private val catalogVersionState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        appPreferences.settings,
        catalogVersionState,
    ) { preferenceSnapshot, catalogVersion ->
        SettingsUiState(
            sourceModeLabel = preferenceSnapshot.sourceModeLabel,
            lastRefreshAt = preferenceSnapshot.lastRefreshAt,
            catalogVersion = catalogVersion,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = previewSettingsUiState(),
    )

    init {
        viewModelScope.launch {
            runCatching {
                menuRepository.refreshFromLocalAsset()
            }

            menuRepository.observeCatalog()
                .catch {
                    // 设置页在仓储异常时保留 preview 占位，避免空白屏。
                }
                .collect { catalog ->
                    catalogVersionState.value = "Schema v${catalog.schemaVersion}"
                }
        }
    }

    companion object {

        /**
         * 提供 Compose `viewModel()` 可消费的 Factory。
         *
         * @param menuRepository 菜单仓储。
         * @param appPreferences 应用偏好封装。
         * @return ViewModel 工厂。
         */
        fun provideFactory(
            menuRepository: MenuRepository,
            appPreferences: AppPreferences,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(
                        menuRepository = menuRepository,
                        appPreferences = appPreferences,
                    ) as T
                }
            }
        }
    }
}

/**
 * 设置页 preview 状态。
 *
 * @return 预览状态。
 */
internal fun previewSettingsUiState(): SettingsUiState {
    return SettingsUiState(
        sourceModeLabel = AppPreferences.SOURCE_MODE_LOCAL,
        lastRefreshAt = "2026-03-19T08:00:00Z",
        catalogVersion = "Schema v1",
    )
}
