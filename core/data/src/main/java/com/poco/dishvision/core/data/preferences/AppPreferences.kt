/**
 * @file AppPreferences.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 封装应用轻量配置与同步状态的 DataStore 访问。
 */
package com.poco.dishvision.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 设置页所需的轻量配置快照。
 *
 * @property sourceModeLabel 当前数据源模式标签。
 * @property lastRefreshAt 最近一次刷新时间。
 */
data class AppPreferenceSnapshot(
    val sourceModeLabel: String,
    val lastRefreshAt: String?,
)

/**
 * 应用偏好封装，当前管理：
 * 1) 数据源模式；
 * 2) 最近一次本地刷新时间。
 *
 * @param dataStore Preferences DataStore。
 */
class AppPreferences(
    private val dataStore: DataStore<Preferences>,
) {

    val settings: Flow<AppPreferenceSnapshot> = dataStore.data.map { preferences ->
        AppPreferenceSnapshot(
            sourceModeLabel = preferences[SOURCE_MODE_KEY] ?: SOURCE_MODE_LOCAL,
            lastRefreshAt = preferences[LAST_REFRESH_AT_KEY],
        )
    }

    /**
     * 在本地数据导入完成后刷新设置页状态。
     *
     * @param refreshedAt 本次刷新时间。
     */
    suspend fun markLocalSourceRefreshed(refreshedAt: Instant) {
        dataStore.edit { preferences ->
            preferences[SOURCE_MODE_KEY] = SOURCE_MODE_LOCAL
            preferences[LAST_REFRESH_AT_KEY] = refreshedAt.toString()
        }
    }

    companion object {
        private val SOURCE_MODE_KEY = stringPreferencesKey("source_mode")
        private val LAST_REFRESH_AT_KEY = stringPreferencesKey("last_refresh_at")

        /**
         * Phase 1 仅支持本地模式，统一以 Local 文案展示。
         */
        const val SOURCE_MODE_LOCAL = "Local"
    }
}
