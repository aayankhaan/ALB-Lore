package com.aayan.alblore.listener

import com.aayan.albcore.utils.AveragePrice
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
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material


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

        val material = Material.matchMaterial(item.type.name.key) ?: run {
            println("Material not found: ${item.type.name}")
            return
        }

        println("Material: $material")

        if (!AveragePrice.hasPrice(material)) {
            val noPrice = mutableListOf<Component>()
            noPrice.add(Component.text("").decoration(TextDecoration.ITALIC, false))
            noPrice.add(Component.text("Market").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false))
            noPrice.add(Component.text("No price set").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))

            val existingLore = item.getComponent(ComponentTypes.LORE).orElse(null)
            if (existingLore != null && existingLore.getLines().isNotEmpty()) {
                val lines = existingLore.getLines().toMutableList()
                lines.addAll(noPrice)
                existingLore.setLines(lines)
                item.setComponent(ComponentTypes.LORE, existingLore)
            } else {
                item.setComponent(ComponentTypes.LORE, ItemLore(noPrice))
            }
            return
        }

        val avg = AveragePrice.getAverage(material, player) ?: run {
            println("No average for: $material")
            return
        }

        println("Avg: $avg")
        val breakdown = AveragePrice.getPrices(material, player)

        val newLines = mutableListOf<Component>()
        newLines.add(Component.text("") .decoration(TextDecoration.ITALIC, false))
        newLines.add(Component.text("Market").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false))
        newLines.add(
            Component.text("Average Price: ")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text("$${NumberUtil.formatNumber(avg)}").color(NamedTextColor.WHITE))
        )
        breakdown.forEach { (pluginName, price) ->
            newLines.add(
                Component.text("  $pluginName: ")
                    .color(NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("$${NumberUtil.formatNumber(price)}").color(NamedTextColor.GRAY))
            )
        }

        val existingLore = item.getComponent(ComponentTypes.LORE).orElse(null)

        if (existingLore != null && existingLore.getLines().isNotEmpty()) {
            val lines = existingLore.getLines().toMutableList()

            val alreadyApplied = lines.any {
                plainText.serialize(it).contains("Average Price:")
            }
            if (alreadyApplied) return

            lines.addAll(newLines)

            existingLore.setLines(lines)
            item.setComponent(ComponentTypes.LORE, existingLore)
        } else {
            item.setComponent(
                ComponentTypes.LORE,
                ItemLore(newLines)
            )
        }
    }
}