/**
 * @file MenuItemGridPoseCacheTest.kt
 * @author PopoY
 * @date 2026-03-21
 * @description 验证 Browse 网格 pose cache（姿态缓存）在卡片重新进入可视窗时的起始姿态契约。
 */
package com.poco.dishvision.feature.menu

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * `resolveBrowseCardEnterMotion` / `resolveBrowseCardPoseCacheSnapshot` 契约测试。
 */
class MenuItemGridPoseCacheTest {

    /**
     * @description 进入可视窗时若已有历史姿态，应优先复用缓存姿态作为动画起点。
     * @author PopoY
     */
    @Test
    fun cached_pose_should_be_reused_as_enter_motion_seed() {
        val cachedPose = BrowseCardFocusMotion(
            scale = 0.82f,
            alpha = 0.72f,
            offsetXRatio = 1f,
            offsetYRatio = 0f,
            pivotX = 1f,
            pivotY = 0f,
            zIndex = 2f,
            showExpandedDetails = true,
        )
        val targetPose = BrowseCardFocusMotion(
            scale = 1.24f,
            alpha = 1f,
            pivotX = 0f,
            pivotY = 0f,
            zIndex = 8f,
            showExpandedDetails = true,
        )

        val enterPose = resolveBrowseCardEnterMotion(
            targetMotion = targetPose,
            cachedPose = cachedPose,
        )

        assertEquals(0.82f, enterPose.scale, 0.0001f)
        assertEquals(0.72f, enterPose.alpha, 0.0001f)
        assertEquals(1f, enterPose.offsetXRatio, 0.0001f)
        assertEquals(0f, enterPose.offsetYRatio, 0.0001f)
        assertFalse(enterPose.showExpandedDetails)
    }

    /**
     * @description 无缓存时应使用轻量 enter pose，避免新卡直接以目标姿态硬切入场。
     * @author PopoY
     */
    @Test
    fun missing_cache_should_fallback_to_lightweight_enter_pose() {
        val targetPose = BrowseCardFocusMotion(
            scale = 1.24f,
            alpha = 1f,
            offsetXRatio = 1f,
            offsetYRatio = -1f,
            pivotX = 0f,
            pivotY = 1f,
            zIndex = 8f,
            showExpandedDetails = true,
        )

        val enterPose = resolveBrowseCardEnterMotion(
            targetMotion = targetPose,
            cachedPose = null,
        )

        assertEquals(0.94f, enterPose.scale, 0.0001f)
        assertEquals(0.88f, enterPose.alpha, 0.0001f)
        assertEquals(0f, enterPose.offsetXRatio, 0.0001f)
        assertEquals(0f, enterPose.offsetYRatio, 0.0001f)
        assertEquals(0f, enterPose.pivotX, 0.0001f)
        assertEquals(1f, enterPose.pivotY, 0.0001f)
        assertFalse(enterPose.showExpandedDetails)
    }

    /**
     * @description 写入 pose cache 时应剥离详情展开态，避免离屏卡片回到视口后抢占详情区。
     * @author PopoY
     */
    @Test
    fun pose_cache_snapshot_should_strip_expanded_details_flag() {
        val snapshot = resolveBrowseCardPoseCacheSnapshot(
            targetMotion = BrowseCardFocusMotion(
                scale = 1.24f,
                alpha = 1f,
                zIndex = 8f,
                showExpandedDetails = true,
            ),
        )

        assertEquals(1.24f, snapshot.scale, 0.0001f)
        assertEquals(1f, snapshot.alpha, 0.0001f)
        assertEquals(8f, snapshot.zIndex, 0.0001f)
        assertFalse(snapshot.showExpandedDetails)
    }
}
