/**
 * @file MenuEntityMapper.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 提供 MenuCatalog 与 Room entities（实体）之间的映射逻辑。
 */
package com.poco.dishvision.core.data.local.mapper

import com.poco.dishvision.core.data.local.db.entity.MenuCategoryEntity
import com.poco.dishvision.core.data.local.db.entity.MenuItemEntity
import com.poco.dishvision.core.data.local.db.entity.MenuMetadataEntity
import com.poco.dishvision.core.model.menu.AvailabilityWindow
import com.poco.dishvision.core.model.menu.DisplayBadge
import com.poco.dishvision.core.model.menu.MenuCatalog
import com.poco.dishvision.core.model.menu.MenuCategory
import com.poco.dishvision.core.model.menu.MenuItem
import com.poco.dishvision.core.model.menu.PriceInfo
import com.poco.dishvision.core.model.menu.ThemeConfig
import java.time.Instant
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * 实体映射器，负责：
 * 1) 将模型展开到 metadata/category/item 三表；
 * 2) 将三表数据重建为 MenuCatalog 模型。
 *
 * @param json JSON 序列化器，用于编码/解码嵌套列表字段。
 */
class MenuEntityMapper(
    private val json: Json,
) {

    // 复用 serializer（序列化器）避免重复创建对象。
    private val availabilityWindowListSerializer = ListSerializer(AvailabilityWindow.serializer())
    private val displayBadgeListSerializer = ListSerializer(DisplayBadge.serializer())
    private val stringListSerializer = ListSerializer(String.serializer())

    /**
     * 将目录模型映射为元数据实体。
     *
     * @param catalog 目录模型。
     * @param importedAtEpochMilli 导入时间戳。
     * @return 元数据实体。
     */
    fun toMetadataEntity(
        catalog: MenuCatalog,
        importedAtEpochMilli: Long,
    ): MenuMetadataEntity {
        return MenuMetadataEntity(
            catalogId = catalog.catalogId,
            schemaVersion = catalog.schemaVersion,
            restaurantName = catalog.restaurantName,
            lastUpdatedAtEpochMilli = catalog.lastUpdatedAt.toEpochMilli(),
            primaryColorHex = catalog.themeConfig.primaryColorHex,
            accentColorHex = catalog.themeConfig.accentColorHex,
            backgroundColorHex = catalog.themeConfig.backgroundColorHex,
            surfaceColorHex = catalog.themeConfig.surfaceColorHex,
            textColorHex = catalog.themeConfig.textColorHex,
            importedAtEpochMilli = importedAtEpochMilli,
        )
    }

    /**
     * 将目录模型映射为分类实体集合。
     *
     * @param catalog 目录模型。
     * @return 分类实体集合。
     */
    fun toCategoryEntities(catalog: MenuCatalog): List<MenuCategoryEntity> {
        return catalog.categories.map { category ->
            MenuCategoryEntity(
                categoryId = category.categoryId,
                catalogId = catalog.catalogId,
                displayName = category.displayName,
                subtitle = category.subtitle,
                sortOrder = category.sortOrder,
                description = category.description,
            )
        }
    }

    /**
     * 将目录模型映射为菜品实体集合。
     *
     * @param catalog 目录模型。
     * @return 菜品实体集合。
     */
    fun toItemEntities(catalog: MenuCatalog): List<MenuItemEntity> {
        return catalog.categories.flatMap { category ->
            category.items.mapIndexed { index, item ->
                MenuItemEntity(
                    itemId = item.itemId,
                    catalogId = catalog.catalogId,
                    categoryId = category.categoryId,
                    itemSortOrder = index,
                    name = item.name,
                    description = item.description,
                    imageUrl = item.imageUrl,
                    currencyCode = item.priceInfo.currencyCode,
                    amountMinor = item.priceInfo.amountMinor,
                    originalAmountMinor = item.priceInfo.originalAmountMinor,
                    unitLabel = item.priceInfo.unitLabel,
                    availabilityWindowsJson = json.encodeToString(
                        availabilityWindowListSerializer,
                        item.availabilityWindows,
                    ),
                    displayBadgesJson = json.encodeToString(
                        displayBadgeListSerializer,
                        item.displayBadges,
                    ),
                    tagsJson = json.encodeToString(stringListSerializer, item.tags),
                )
            }
        }
    }

    /**
     * 将三张表的数据重建为目录模型。
     *
     * @param metadata 元数据实体。
     * @param categories 分类实体集合。
     * @param items 菜品实体集合。
     * @return 目录模型。
     */
    fun toCatalog(
        metadata: MenuMetadataEntity,
        categories: List<MenuCategoryEntity>,
        items: List<MenuItemEntity>,
    ): MenuCatalog {
        // 先按 categoryId 聚合菜品，减少后续遍历复杂度。
        val itemsByCategoryId = items.groupBy { entity -> entity.categoryId }
        return MenuCatalog(
            schemaVersion = metadata.schemaVersion,
            catalogId = metadata.catalogId,
            restaurantName = metadata.restaurantName,
            lastUpdatedAt = Instant.ofEpochMilli(metadata.lastUpdatedAtEpochMilli),
            themeConfig = ThemeConfig(
                primaryColorHex = metadata.primaryColorHex,
                accentColorHex = metadata.accentColorHex,
                backgroundColorHex = metadata.backgroundColorHex,
                surfaceColorHex = metadata.surfaceColorHex,
                textColorHex = metadata.textColorHex,
            ),
            categories = categories
                .sortedWith(compareBy<MenuCategoryEntity> { it.sortOrder }.thenBy { it.categoryId })
                .map { categoryEntity ->
                    MenuCategory(
                        categoryId = categoryEntity.categoryId,
                        displayName = categoryEntity.displayName,
                        subtitle = categoryEntity.subtitle,
                        sortOrder = categoryEntity.sortOrder,
                        description = categoryEntity.description,
                        items = itemsByCategoryId[categoryEntity.categoryId]
                            .orEmpty()
                            .sortedWith(compareBy<MenuItemEntity> { it.itemSortOrder }.thenBy { it.itemId })
                            .map(::toMenuItem),
                    )
                },
        )
    }

    /**
     * 将菜品实体转换为领域模型对象。
     *
     * @param entity 菜品实体。
     * @return 菜品领域模型。
     */
    private fun toMenuItem(entity: MenuItemEntity): MenuItem {
        return MenuItem(
            itemId = entity.itemId,
            name = entity.name,
            description = entity.description,
            imageUrl = entity.imageUrl,
            priceInfo = PriceInfo(
                currencyCode = entity.currencyCode,
                amountMinor = entity.amountMinor,
                originalAmountMinor = entity.originalAmountMinor,
                unitLabel = entity.unitLabel,
            ),
            availabilityWindows = json.decodeFromString(
                availabilityWindowListSerializer,
                entity.availabilityWindowsJson,
            ),
            displayBadges = json.decodeFromString(
                displayBadgeListSerializer,
                entity.displayBadgesJson,
            ),
            tags = json.decodeFromString(stringListSerializer, entity.tagsJson),
        )
    }
}
