package com.aayan.alblore.commands

import com.aayan.albcore.commands.ArgType
import com.aayan.albcore.commands.CommandUtil
import com.aayan.albcore.utils.MessageUtil
import com.aayan.alblore.manager.ConfigManager
import org.bukkit.plugin.java.JavaPlugin

object AdminCommand {

    fun register(plugin: JavaPlugin) {
        CommandUtil.registerCommand(plugin, "alblore") {
            description = "Main admin command"
            permission = "alblore.admin"

            onUnknownSubcommand { sender, _ ->
                MessageUtil.send(sender, "&cUsage: /alblore reload")
            }

            subcommand("reload") {
                action { sender, _ ->
                    ConfigManager.reload(plugin)
                    MessageUtil.send(sender, "&aReloaded!")
                }
            }


        }
    }
}