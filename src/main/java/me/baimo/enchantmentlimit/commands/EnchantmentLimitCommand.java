package me.baimo.enchantmentlimit.commands;

import me.baimo.enchantmentlimit.EnchantmentLimit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EnchantmentLimitCommand implements CommandExecutor {

    private final EnchantmentLimit plugin;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    public EnchantmentLimitCommand(EnchantmentLimit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("enchantmentlimitslimefun.reload")) {
            sender.sendMessage(serializer.deserialize(
                plugin.getConfig().getString("messages.noPermission", "&c你没有权限执行此命令！")));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(serializer.deserialize(
                plugin.getConfig().getString("messages.configReloaded", "&a配置文件已重载！")));
            return true;
        }

        return false;
    }
}
