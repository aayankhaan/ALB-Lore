package com.aayan.alblore

import com.aayan.albcore.hooks.PacketEventsHook
import com.aayan.alblore.commands.AdminCommand
import com.aayan.alblore.commands.ToggleCommand
import com.aayan.alblore.listener.GlobalFakeLoreListener
import com.aayan.alblore.manager.ConfigManager
import com.github.retrooper.packetevents.PacketEvents
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class ALBLore : JavaPlugin() {


    override fun onEnable() {

        val g = "\u001B[32m"
        val r = "\u001B[31m"
        val x = "\u001B[0m"

        if (!PacketEventsHook.isLoaded()) {
            println()
            println("$r══════════════════════════════════════════════════════════════$x")
            println("$r    _      _       ____      _        ___    ____    _____ $x")
            println("$r   / \\    | |     | __ )    | |      / _ \\  |  _ \\  | ____|$x")
            println("$r  / _ \\   | |     |  _ \\    | |     | | | | | |_) | |  _|  $x")
            println("$r / ___ \\  | |___  | |_) |   | |___  | |_| | |  _ <  | |___ $x")
            println("$r/_/   \\_\\ |_____| |____/    |_____|  \\___/  |_| \\_\\ |_____|$x")
            println()
            println("$r Required dependency: PacketEvents")
            println("$r Plugin has been disabled.")
            println("$r══════════════════════════════════════════════════════════════$x")
            println()

            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        println()
        println("$g══════════════════════════════════════════════════════════════$x")
        println("$g    _      _       ____      _        ___    ____    _____ $x")
        println("$g   / \\    | |     | __ )    | |      / _ \\  |  _ \\  | ____|$x")
        println("$g  / _ \\   | |     |  _ \\    | |     | | | | | |_) | |  _|  $x")
        println("$g / ___ \\  | |___  | |_) |   | |___  | |_| | |  _ <  | |___ $x")
        println("$g/_/   \\_\\ |_____| |____/    |_____|  \\___/  |_| \\_\\ |_____|$x")
        println()
        println("$g PacketEvents detected successfully.")
        println("$g Plugin enabled successfully!")
        println("$g══════════════════════════════════════════════════════════════$x")
        println()

        ConfigManager.load(this)
        AdminCommand.register(this)
        ToggleCommand.register(this)
        PacketEvents.getAPI().eventManager.registerListeners(GlobalFakeLoreListener())
    }

    override fun onDisable() {
        PacketEvents.getAPI().eventManager.unregisterAllListeners()
    }
}
