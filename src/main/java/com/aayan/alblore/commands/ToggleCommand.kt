package com.aayan.alblore.commands

import com.aayan.albcore.commands.CommandUtil
import com.aayan.albcore.utils.MessageUtil
import com.aayan.albcore.utils.PlayerDataUtil
import com.aayan.alblore.manager.ConfigManager
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

object ToggleCommand {

    fun register(plugin: JavaPlugin) {
        CommandUtil.registerCommand(plugin, "toggleitemlore") {
            description = "Toggle the fake lore system on or off"
            playerOnly = true
            playerOnlyMessage = "&cOnly players can use this command!"
            action { sender, _ ->
                val player = sender as Player
                val path = "ALBLore.IsEnable"
                val currentState = PlayerDataUtil.get(player, path) as? Boolean ?: true
                val newState = !currentState

                PlayerDataUtil.set(player, path, newState)

                val message = if (newState) ConfigManager.enabledMessage else ConfigManager.disabledMessage
                MessageUtil.send(sender, message)
                player.updateInventory()
            }
        }
    }
}