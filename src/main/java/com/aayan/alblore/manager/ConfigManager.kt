package com.aayan.alblore.manager


import com.aayan.albcore.utils.ColorUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.plugin.java.JavaPlugin

object ConfigManager {

    private lateinit var plugin: JavaPlugin

    var hasPrice: List<String> = listOf("&#0dff00$ &f%price%")
    var noPrice: List<String> = listOf("&cCan't Find Item Price")

    var shulkerEmptyHasPrice: List<String> = listOf("&fWorth: &#0dff00$%price%")
    var shulkerEmptyNoPrice: List<String> = listOf("&cCan't Find Item Price")
    var shulkerHasPrice: List<String> = listOf("&fWorth: &#0dff00$%price%")
    var shulkerNoPrice: List<String> = listOf("&cCan't Find Item Price")
    var shulkerPartialPrice: List<String> = listOf("&7(Some items excluded)")
    var enabledMessage: String = "&7Lore system has been &aenabled&7."
    var disabledMessage: String = "&7Lore system has been &cdisabled&7."

    fun load(plugin: JavaPlugin) {
        this.plugin = plugin
        plugin.saveDefaultConfig()
        plugin.reloadConfig()

        val config = plugin.config

        hasPrice = config.getStringList("lore.has-price").ifEmpty { hasPrice }
        noPrice = config.getStringList("lore.no-price").ifEmpty { noPrice }

        shulkerEmptyHasPrice = config.getStringList("lore.shulker.empty-has-price").ifEmpty { shulkerEmptyHasPrice }
        shulkerEmptyNoPrice = config.getStringList("lore.shulker.empty-no-price").ifEmpty { shulkerEmptyNoPrice }
        shulkerHasPrice = config.getStringList("lore.shulker.has-price").ifEmpty { shulkerHasPrice }
        shulkerNoPrice = config.getStringList("lore.shulker.no-price").ifEmpty { shulkerNoPrice }
        shulkerPartialPrice = config.getStringList("lore.shulker.partial-price").ifEmpty { shulkerPartialPrice }
        enabledMessage = config.getString("messages.enabled") ?: enabledMessage
        disabledMessage = config.getString("messages.disabled") ?: disabledMessage
    }

    fun reload(plugin: JavaPlugin) = load(plugin)

    fun buildLines(template: List<String>, placeholders: Map<String, String> = emptyMap(), marker: String? = null): List<Component> {
        return template.mapIndexed { index, line ->
            var replaced = line
            placeholders.forEach { (key, value) -> replaced = replaced.replace("%$key%", value) }
            var component = ColorUtil.parse(replaced).decoration(TextDecoration.ITALIC, false)

            if (index == 0 && marker != null) {
                component = component.append(Component.text(marker).color(NamedTextColor.BLACK))
            }
            component
        }
    }
}