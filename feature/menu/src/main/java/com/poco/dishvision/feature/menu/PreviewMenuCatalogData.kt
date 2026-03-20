/**
 * @file PreviewMenuCatalogData.kt
 * @author PopoY
 * @date 2026-03-20
 * @description 提供菜单页 preview 菜单数据，确保 UI 测试入口与 docs/菜单.md 保持一致。
 */
package com.poco.dishvision.feature.menu

import com.poco.dishvision.core.model.menu.DisplayBadge
import com.poco.dishvision.core.model.menu.MenuCategory
import com.poco.dishvision.core.model.menu.MenuItem
import com.poco.dishvision.core.model.menu.PriceInfo

/**
 * 菜单分类 preview 种子，避免在 ViewModel 中混入大段静态定义。
 *
 * @property categoryId 分类 ID。
 * @property displayName 分类名称。
 * @property subtitle 分类副标题。
 * @property sortOrder 分类排序。
 * @property description 分类描述。
 * @property items 分类下的预览菜品。
 */
private data class PreviewMenuCategorySeed(
    val categoryId: String,
    val displayName: String,
    val subtitle: String,
    val sortOrder: Int,
    val description: String,
    val items: List<PreviewMenuItemSeed>,
)

/**
 * 菜品 preview 种子。
 *
 * @property itemId 菜品 ID。
 * @property name 菜品名称。
 * @property description 菜品描述。
 * @property amountMinor 价格分。
 * @property badgeLabel 徽章文案。
 */
private data class PreviewMenuItemSeed(
    val itemId: String,
    val name: String,
    val description: String,
    val amountMinor: Int,
    val badgeLabel: String,
)

/**
 * 提供浏览页的本地 preview 分类数据，顺序与 [docs/菜单.md] 保持一致。
 *
 * @return 预览分类列表。
 */
internal fun previewMenuCategories(): List<MenuCategory> {
    return PREVIEW_MENU_CATEGORY_SEEDS.map { categorySeed ->
        MenuCategory(
            categoryId = categorySeed.categoryId,
            displayName = categorySeed.displayName,
            subtitle = categorySeed.subtitle,
            sortOrder = categorySeed.sortOrder,
            description = categorySeed.description,
            items = categorySeed.items.map { itemSeed ->
                previewMenuItem(
                    itemId = itemSeed.itemId,
                    name = itemSeed.name,
                    description = itemSeed.description,
                    amountMinor = itemSeed.amountMinor,
                    imageUrl = "",
                    badgeLabel = itemSeed.badgeLabel,
                )
            },
        )
    }
}

/**
 * 构造 preview 菜品对象，减少重复模板代码。
 *
 * @param itemId 菜品 ID。
 * @param name 菜品名称。
 * @param description 菜品描述。
 * @param amountMinor 菜品金额（分）。
 * @param imageUrl 菜品图片 URL。
 * @param badgeLabel 菜品徽章文案。
 * @return 菜品模型。
 */
private fun previewMenuItem(
    itemId: String,
    name: String,
    description: String,
    amountMinor: Int,
    imageUrl: String,
    badgeLabel: String,
): MenuItem {
    return MenuItem(
        itemId = itemId,
        name = name,
        description = description,
        imageUrl = imageUrl,
        priceInfo = PriceInfo(
            currencyCode = "CNY",
            amountMinor = amountMinor,
            originalAmountMinor = amountMinor,
            unitLabel = "份",
        ),
        availabilityWindows = emptyList(),
        displayBadges = listOf(
            DisplayBadge(
                badgeId = "badge-$itemId",
                label = badgeLabel,
                styleKey = "brand",
            ),
        ),
        tags = listOf("poco", "featured"),
    )
}

/**
 * preview 菜单分类种子定义。
 */
private val PREVIEW_MENU_CATEGORY_SEEDS = listOf(
    PreviewMenuCategorySeed(
        categoryId = "hot-stir-fry",
        displayName = "招牌热炒",
        subtitle = "热锅现炒",
        sortOrder = 1,
        description = "锅气、辣香、下饭感最强的一页，先看最能代表湘味火候的现炒菜。",
        items = listOf(
            PreviewMenuItemSeed("hot-tea-chicken", "茶油炒鸡", "茶油爆香，锅气足，越吃越香。", 8_800, "招牌"),
            PreviewMenuItemSeed("hot-beef", "小炒黄牛肉", "鲜辣现炒，肉嫩椒脆，湘味代表。", 7_800, "人气"),
            PreviewMenuItemSeed("hot-liling-pork", "醴陵小炒肉", "辣椒鲜香，肉香直接，家常头牌。", 5_800, "经典"),
            PreviewMenuItemSeed("hot-youxian-pork", "攸县杀猪肉", "土味厚香，口感扎实，越吃越耐。", 6_800, "土菜"),
            PreviewMenuItemSeed("hot-intestine", "生炒肥肠", "爆香重口，肥肠越嚼越香。", 5_800, "重口"),
            PreviewMenuItemSeed("hot-kidney", "爆炒腰花", "脆嫩见火候，香辣利落。", 5_800, "功夫菜"),
            PreviewMenuItemSeed("hot-tripe", "酸辣脆肚", "脆爽酸辣，开胃带劲。", 4_800, "开胃"),
            PreviewMenuItemSeed("hot-gizzard", "酸辣鸡胗", "脆口鲜辣，下饭下酒都稳。", 3_800, "下酒"),
            PreviewMenuItemSeed("hot-seasonal", "时蔬炒肉", "家常热炒，配饭最顺口。", 3_800, "家常"),
        ),
    ),
    PreviewMenuCategorySeed(
        categoryId = "spicy",
        displayName = "香辣口味",
        subtitle = "辣香下饭",
        sortOrder = 2,
        description = "无辣不欢的整页主场，从鸭、蛙、虾到猪脚，辣香层次一路往上走。",
        items = listOf(
            PreviewMenuItemSeed("spicy-tea-oil-duck", "钵钵茶油鸭", "茶油慢煸出香，鸭肉紧实，辣劲后发。", 8_800, "招牌"),
            PreviewMenuItemSeed("spicy-frog", "紫苏牛蛙", "紫苏提香，蛙腿鲜嫩，入口先香后辣。", 7_800, "人气"),
            PreviewMenuItemSeed("spicy-shrimp", "香辣虾", "虾壳挂满辣油，香麻一上桌就抢。", 8_800, "爆款"),
            PreviewMenuItemSeed("spicy-chicken-feet", "香辣鸡爪", "胶质丰厚，辣味慢慢往后追。", 4_600, "下酒"),
            PreviewMenuItemSeed("spicy-chicken-wings", "香辣鸡翅", "外皮焦香，肉汁带辣，越啃越香。", 4_800, "经典"),
            PreviewMenuItemSeed("spicy-pig-tail", "飘香猪尾", "软糯带筋，香辣酱汁裹得很足。", 5_600, "胶质"),
            PreviewMenuItemSeed("spicy-ribs", "手抓排骨", "先卤后炸，骨边肉香，抓着吃最过瘾。", 6_200, "硬菜"),
            PreviewMenuItemSeed("spicy-beef-omasa", "口味牛朵", "脆嫩带嚼劲，重辣重香，口感很立体。", 5_800, "重口"),
            PreviewMenuItemSeed("spicy-braised-trotter", "红烧猪脚", "酱香打底再叠辣味，软糯特别下饭。", 5_800, "下饭"),
        ),
    ),
    PreviewMenuCategorySeed(
        categoryId = "fish",
        displayName = "鱼鲜大菜",
        subtitle = "鲜味主打",
        sortOrder = 3,
        description = "整鱼、鱼片与干锅硬菜都放在这里，先抓鲜味，再叠锅气和麻辣层次。",
        items = listOf(
            PreviewMenuItemSeed("fish-green-pepper", "青花椒鱼片", "鲜麻先起，鱼片嫩滑，越吃越开胃。", 9_600, "鲜麻"),
            PreviewMenuItemSeed("fish-jumping", "跳水鱼", "汤底鲜辣，鱼肉入味，整桌先抢这一锅。", 11_800, "招牌"),
            PreviewMenuItemSeed("fish-boiled", "水煮活鱼", "红油厚香，鱼片滑嫩，辣得很直给。", 12_800, "霸道"),
            PreviewMenuItemSeed("fish-steamed", "清蒸刁子鱼", "火候收得干净，鱼肉细嫩，鲜味很透。", 10_800, "清鲜"),
            PreviewMenuItemSeed("fish-river", "野生河鱼", "河鲜本味足，炖得香浓，越喝越鲜。", 13_800, "河鲜"),
            PreviewMenuItemSeed("fish-squid", "干锅鱿鱼", "鱿鱼弹韧，干锅香气收得很紧。", 8_600, "干香"),
            PreviewMenuItemSeed("fish-brisket", "干锅牛腩", "牛腩酥软入味，锅气和酱香都很重。", 9_800, "浓香"),
        ),
    ),
    PreviewMenuCategorySeed(
        categoryId = "home-style",
        displayName = "家常土菜",
        subtitle = "乡土滋味",
        sortOrder = 4,
        description = "更偏家常和乡土口味的一组，锅气没有收着，但节奏比香辣页更稳更耐吃。",
        items = listOf(
            PreviewMenuItemSeed("home-pickled-pork", "酸菜回锅肉", "酸香提味，五花回锅后更下饭。", 5_200, "家常"),
            PreviewMenuItemSeed("home-dried-tofu-pork", "香干五花肉", "香干吸满肉汁，越嚼越有豆香。", 5_200, "下饭"),
            PreviewMenuItemSeed("home-seasonal-egg", "时蔬炒蛋", "蔬菜清鲜，鸡蛋蓬松，节奏很舒服。", 3_200, "清爽"),
            PreviewMenuItemSeed("home-green-beans", "油渣四季豆", "豆角带脆，油渣增香，朴素但稳。", 3_600, "乡味"),
            PreviewMenuItemSeed("home-tofu", "家常豆腐", "豆腐吸汁不碎，咸鲜里带一点辣。", 3_400, "经典"),
            PreviewMenuItemSeed("home-eggplant-pot", "茄子煲", "茄肉软糯入味，热锅上桌很顶饭。", 3_600, "暖锅"),
            PreviewMenuItemSeed("home-pickled-vermicelli", "酸菜粉皮", "酸菜脆爽，粉皮滑口，开胃不压桌。", 3_200, "开胃"),
            PreviewMenuItemSeed("home-dried-tofu-shreds", "油泼香干丝", "香干丝挂满热油香，越拌越顺口。", 3_000, "爽口"),
            PreviewMenuItemSeed("home-bone-pepper", "筒子骨辣椒", "骨香厚实，辣椒提味，土菜感很足。", 6_200, "土菜"),
            PreviewMenuItemSeed("home-preserved-egg-pepper", "皮蛋擂辣椒", "辣椒擂香，皮蛋绵密，是白饭杀手。", 2_800, "必点"),
        ),
    ),
    PreviewMenuCategorySeed(
        categoryId = "side",
        displayName = "风味小菜",
        subtitle = "佐餐小味",
        sortOrder = 5,
        description = "冷菜、汤品和几道收口小味集中在这里，用来调节整桌的辣度与节奏。",
        items = listOf(
            PreviewMenuItemSeed("side-white-cut-chicken", "白切鸡", "鸡皮弹嫩，蘸一点料汁就很舒服。", 5_800, "冷盘"),
            PreviewMenuItemSeed("side-lamb-offal", "原味羊杂", "原汤原味，羊香干净，越喝越暖。", 5_800, "原味"),
            PreviewMenuItemSeed("side-stir-lamb", "生炒羊肉", "羊肉现炒，鲜香干脆，辣度点到为止。", 6_800, "鲜香"),
            PreviewMenuItemSeed("side-cold-beef", "凉拌牛肉", "牛肉紧实，拌汁酸辣，越嚼越香。", 5_600, "凉菜"),
            PreviewMenuItemSeed("side-sausage", "腊肠", "腊香偏甜，切片上桌最适合压辣。", 4_200, "腊味"),
            PreviewMenuItemSeed("side-tripe-bamboo", "牛百叶笋", "百叶脆弹，笋香清爽，口感很利落。", 4_800, "脆口"),
            PreviewMenuItemSeed("side-meatball-soup", "肉丸红枣枸杞汤", "汤头温润，肉丸扎实，适合收尾。", 4_200, "汤品"),
            PreviewMenuItemSeed("side-steamed-egg", "水蒸蛋", "蛋羹细滑，口味轻，老人小孩都稳。", 1_800, "软嫩"),
            PreviewMenuItemSeed("side-house-roll", "自制包圆", "手作口感扎实，淡淡米香很特别。", 2_600, "手作"),
        ),
    ),
)
