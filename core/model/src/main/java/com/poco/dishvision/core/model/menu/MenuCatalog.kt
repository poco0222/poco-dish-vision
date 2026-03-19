/**
 * @file MenuCatalog.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义菜单目录（Menu Catalog）领域模型与 Instant（时间戳）序列化器。
 */
package com.poco.dishvision.core.model.menu

import java.time.Instant
import java.time.format.DateTimeParseException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 菜单目录（Menu Catalog）根对象，描述完整菜单结构与主题信息。
 *
 * @property schemaVersion schema（模式）版本。
 * @property catalogId 目录唯一标识。
 * @property restaurantName 餐厅名称。
 * @property lastUpdatedAt 最近更新时间（ISO-8601）。
 * @property themeConfig 主题配置。
 * @property categories 菜单分类列表。
 */
@Serializable
data class MenuCatalog(
    val schemaVersion: Int,
    val catalogId: String,
    val restaurantName: String,
    @Serializable(with = InstantIso8601Serializer::class)
    val lastUpdatedAt: Instant,
    val themeConfig: ThemeConfig,
    val categories: List<MenuCategory>,
)

/**
 * Instant（时间戳）最小可行 serializer（序列化器），统一使用 ISO-8601 字符串。
 */
object InstantIso8601Serializer : KSerializer<Instant> {

    // 该 descriptor（描述符）明确声明 Instant 以字符串形式在 JSON 中传输。
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "java.time.Instant",
        kind = PrimitiveKind.STRING,
    )

    /**
     * 将 Instant 序列化为 ISO-8601 字符串。
     *
     * @param encoder Kotlin serialization 编码器。
     * @param value 待编码的 Instant。
     */
    override fun serialize(
        encoder: Encoder,
        value: Instant,
    ) {
        encoder.encodeString(value.toString())
    }

    /**
     * 将 ISO-8601 字符串反序列化为 Instant。
     *
     * @param decoder Kotlin serialization 解码器。
     * @return 解析后的 Instant。
     * @throws SerializationException 当输入字符串不是合法 ISO-8601 格式时抛出。
     */
    override fun deserialize(decoder: Decoder): Instant {
        val rawInstantText = decoder.decodeString()
        return try {
            Instant.parse(rawInstantText)
        } catch (exception: DateTimeParseException) {
            throw SerializationException("Invalid ISO-8601 instant: $rawInstantText", exception)
        }
    }
}
