package com.aayan.alblore.listener

import com.aayan.albcore.utils.AveragePrice
import com.aayan.albcore.utils.NumberUtil
import com.aayan.albcore.utils.PlayerDataUtil
import com.aayan.alblore.manager.ConfigManager
import com.github.retrooper.packetevents.event.*
import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.*
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BlockStateMeta

class GlobalFakeLoreListener : PacketListenerAbstract(PacketListenerPriority.NORMAL) {
    private val MARKER = "§x§a§l§b§l§o§r§e"
    private val plainText = PlainTextComponentSerializer.plainText()

    override fun onPacketSend(event: PacketSendEvent) {
        val player = event.user.uuid?.let { Bukkit.getPlayer(it) } ?: return

        val isEnable = PlayerDataUtil.get(player,"ALBLore.IsEnable") as? Boolean ?: true
        if (!isEnable) return

        when (event.packetType) {
            PacketType.Play.Server.WINDOW_ITEMS -> WrapperPlayServerWindowItems(event).apply { items = items.map { it?.copy()?.also { i -> applyPlayerLore(i, player) } } }
            PacketType.Play.Server.SET_SLOT -> WrapperPlayServerSetSlot(event).apply { item?.let { item = it.copy().also { i -> applyPlayerLore(i, player) } } }
            PacketType.Play.Server.SET_CURSOR_ITEM -> WrapperPlayServerSetCursorItem(event).apply { stack?.let { stack = it.copy().also { i -> applyPlayerLore(i, player) } } }
        }
    }

    private fun applyPlayerLore(item: ItemStack, player: Player) {
        if (item.isEmpty) return
        val material = Material.matchMaterial(item.type.name.key) ?: return
        val lines = mutableListOf<Component>()
        val isShulker = material.name.contains("SHULKER_BOX")

        if (isShulker) {
            val bukkitItem = SpigotConversionUtil.toBukkitItemStack(item)
            val meta = bukkitItem.itemMeta as? BlockStateMeta
            val shulker = meta?.blockState as? ShulkerBox ?: return
            val contents = shulker.inventory.contents.filterNotNull().filter { it.type != Material.AIR }

            if (contents.isEmpty()) {
                val avg = AveragePrice.getAverage(material, player)
                lines.addAll(if (avg == null) ConfigManager.buildLines(ConfigManager.shulkerEmptyNoPrice)
                else ConfigManager.buildLines(ConfigManager.shulkerEmptyHasPrice, mapOf("price" to NumberUtil.formatNumber(avg))))
            } else {
                var total = 0.0
                var hasUnpriced = false
                contents.forEach {
                    val avg = AveragePrice.getAverage(it.type, player)
                    if (avg == null) hasUnpriced = true else total += avg * it.amount
                }
                if (hasUnpriced && total == 0.0) lines.addAll(ConfigManager.buildLines(ConfigManager.shulkerNoPrice))
                else {
                    lines.addAll(ConfigManager.buildLines(ConfigManager.shulkerHasPrice, mapOf("price" to NumberUtil.formatNumber(total))))
                    if (hasUnpriced) lines.addAll(ConfigManager.buildLines(ConfigManager.shulkerPartialPrice))
                }
            }
        } else {
            val avg = AveragePrice.getAverage(material, player)
            lines.addAll(if (avg == null) ConfigManager.buildLines(ConfigManager.noPrice)
            else ConfigManager.buildLines(ConfigManager.hasPrice, mapOf("price" to NumberUtil.formatNumber(avg * item.amount), "per_item" to NumberUtil.formatNumber(avg))))
        }
        updateItemLore(item, lines, isShulker)
    }

    private fun updateItemLore(item: ItemStack, newLines: List<Component>, isShulker: Boolean) {
        val existing = item.getComponent(ComponentTypes.LORE).orElse(null)
        if (existing?.getLines()?.any { plainText.serialize(it).contains(MARKER) } == true) return
        val finalLines = (existing?.getLines() ?: mutableListOf()).toMutableList()
        if (isShulker) { finalLines.add(Component.empty()) }
        finalLines.addAll(newLines)
        item.setComponent(ComponentTypes.LORE, ItemLore(finalLines))
    }
}