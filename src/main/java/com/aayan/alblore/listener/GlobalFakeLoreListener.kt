package com.aayan.alblore.listener

import com.aayan.albcore.utils.AveragePrice
import com.aayan.albcore.utils.ColorUtil
import com.aayan.albcore.utils.NumberUtil
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCursorItem
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BlockStateMeta


class GlobalFakeLoreListener : PacketListenerAbstract(PacketListenerPriority.NORMAL) {

    override fun onPacketSend(event: PacketSendEvent) {
        val uuid = event.user.uuid ?: return
        val player = Bukkit.getPlayer(uuid) ?: return

        when (event.packetType) {

            PacketType.Play.Server.WINDOW_ITEMS -> {
                val packet = WrapperPlayServerWindowItems(event)

                val newItems = packet.items.map { original ->
                    if (original == null) return@map null

                    val copy = original.copy()
                    applyPlayerLore(copy, player)
                    copy
                }

                packet.items = newItems
            }

            PacketType.Play.Server.SET_SLOT -> {
                val packet = WrapperPlayServerSetSlot(event)

                val original = packet.item
                if (original != null && !original.isEmpty) {
                    val copy = original.copy()
                    applyPlayerLore(copy, player)
                    packet.item = copy
                }
            }

            PacketType.Play.Server.SET_CURSOR_ITEM -> {
                val packet = WrapperPlayServerSetCursorItem(event)

                val original = packet.stack
                if (original != null && !original.isEmpty) {
                    val copy = original.copy()
                    applyPlayerLore(copy, player)
                    packet.stack = copy
                }
            }
        }
    }

    private val plainText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()

    private fun applyPlayerLore(item: ItemStack?, player: org.bukkit.entity.Player) {
        if (item == null || item.isEmpty) return

        val material = Material.matchMaterial(item.type.name.key) ?: return

        if (material.name.contains("SHULKER_BOX")) {
            applyShulkerLore(item, player, material)
            return
        }

        val avg = AveragePrice.getAverage(material, player)
        val totalAvg = if (avg != null) avg * item.amount else 0.0

        val newLines = mutableListOf<Component>()

        if (avg == null) {
            newLines.add(ColorUtil.parse("&r&cCan't Find Item Price")
                .decoration(TextDecoration.ITALIC, false))
        } else {
            newLines.add(ColorUtil.parse("&r&fWorth: &#0dff00$${NumberUtil.formatNumber(totalAvg)}")
                .decoration(TextDecoration.ITALIC, false))

        }

        val existingLore = item.getComponent(ComponentTypes.LORE).orElse(null)

        if (existingLore != null && existingLore.getLines().isNotEmpty()) {
            val lines = existingLore.getLines().toMutableList()

            val alreadyApplied = lines.any { plainText.serialize(it).contains("$") || plainText.serialize(it).contains("Can't Find") }
            if (alreadyApplied) return

            lines.addAll(newLines)
            existingLore.setLines(lines)
            item.setComponent(ComponentTypes.LORE, existingLore)
        } else {
            item.setComponent(ComponentTypes.LORE, ItemLore(newLines))
        }
    }

    private fun applyShulkerLore(item: ItemStack, player: org.bukkit.entity.Player, material: Material) {
        val bukkitItem = SpigotConversionUtil.toBukkitItemStack(item)
        val meta = bukkitItem.itemMeta

        val isShulker = meta is BlockStateMeta && meta.blockState is ShulkerBox
        if (!isShulker) return

        val shulker = (meta as BlockStateMeta).blockState as ShulkerBox
        val contents = shulker.inventory.contents.filterNotNull().filter { it.type != org.bukkit.Material.AIR }

        val newLines = mutableListOf<Component>()

        if (contents.isEmpty()) {
            val avg = AveragePrice.getAverage(material, player)
            if (avg == null) {
                newLines.add(ColorUtil.parse("&r&cCan't Find Item Price").decoration(TextDecoration.ITALIC, false))
            } else {
                newLines.add(ColorUtil.parse("&r&fWorth: &#0dff00$${NumberUtil.formatNumber(avg)}").decoration(TextDecoration.ITALIC, false))
            }
        } else {
            var total = 0.0
            var hasUnpriced = false

            for (itemStack in contents) {
                val avg = AveragePrice.getAverage(itemStack.type, player)
                if (avg == null) {
                    hasUnpriced = true
                } else {
                    total += (avg * itemStack.amount)
                }
            }

            newLines.add(ColorUtil.parse("&r"))

            if (hasUnpriced && total == 0.0) {
                newLines.add(ColorUtil.parse("&r&cCan't Find Item Price").decoration(TextDecoration.ITALIC, false))
            } else {
                newLines.add(ColorUtil.parse("&r&fWorth: &#0dff00$${NumberUtil.formatNumber(total)}").decoration(TextDecoration.ITALIC, false))
                if (hasUnpriced) {
                    newLines.add(ColorUtil.parse("&r&7(Some items excluded)").decoration(TextDecoration.ITALIC, false))
                }
            }
        }

        val existingLore = item.getComponent(ComponentTypes.LORE).orElse(null)
        if (existingLore != null && existingLore.getLines().isNotEmpty()) {
            val lines = existingLore.getLines().toMutableList()
            if (lines.any { plainText.serialize(it).contains("Worth:") || plainText.serialize(it).contains("Can't Find") }) return
            lines.addAll(newLines)
            existingLore.setLines(lines)
            item.setComponent(ComponentTypes.LORE, existingLore)
        } else {
            item.setComponent(ComponentTypes.LORE, ItemLore(newLines))
        }
    }
}