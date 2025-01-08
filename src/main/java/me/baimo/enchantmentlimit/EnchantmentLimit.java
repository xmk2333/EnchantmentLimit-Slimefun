package me.baimo.enchantmentlimit;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import me.baimo.enchantmentlimit.commands.EnchantmentLimitCommand;
import me.baimo.enchantmentlimit.listeners.AutoDisenchantListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;

public class EnchantmentLimit extends JavaPlugin implements SlimefunAddon {

    @Override
    public void onEnable() {
        // 首先保存和加载配置
        saveDefaultConfig();
        reloadConfig();
        
        // 检查配置文件
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
        
        // 确保 Slimefun 已加载
        if (!getServer().getPluginManager().isPluginEnabled("Slimefun")) {
            getLogger().severe("未找到 Slimefun！插件将被禁用。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 验证配置文件
        if (!validateConfig()) {
            getLogger().severe("配置文件验证失败！插件将被禁用。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new AutoDisenchantListener(this), this);
        
        // 注册命令
        if (getCommand("sfenchantmentlimit") != null) {
            getCommand("sfenchantmentlimit").setExecutor(new EnchantmentLimitCommand(this));
        } else {
            getLogger().warning(getConfig().getString("messages.commandRegisterFail", "命令注册失败！"));
        }

        // 注册玩家加入监听器
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent event) {
                Player player = event.getPlayer();
                if (player.isOp()) {
                    player.sendMessage("");
                    player.sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "[ " + 
                                     ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "EnchantmentLimit" + 
                                     ChatColor.GRAY.toString() + ChatColor.BOLD + " ]");
                    player.sendMessage(ChatColor.WHITE + "感谢使用 " + 
                                     ChatColor.LIGHT_PURPLE + "白陌" + 
                                     ChatColor.WHITE + " 开发的附魔限制插件");
                    player.sendMessage(ChatColor.WHITE + "欢迎加入插件社区 " + 
                                     ChatColor.AQUA + "QQ群: " + 
                                     ChatColor.YELLOW + "528651839");
                    player.sendMessage(ChatColor.WHITE + "获取更多插件资讯与技术支持");
                    player.sendMessage("");
                }
            }
        }, this);

        // 显示启动消息
        String prefix = "\u001B[38;2;85;85;85m"; // 深灰色
        String pink = "\u001B[38;2;255;105;180m"; // 粉色
        String white = "\u001B[38;2;255;255;255m"; // 白色
        String skyBlue = "\u001B[38;2;135;206;235m"; // 天蓝色
        String yellow = "\u001B[38;2;255;255;0m"; // 黄色
        String reset = "\u001B[0m";

        getLogger().info("");
        getLogger().info(prefix + "[ " + pink + "EnchantmentLimit" + prefix + " ]" + reset);
        getLogger().info(white + "欢迎加入插件社区 " + skyBlue + "QQ群: " + yellow + "528651839" + reset);
        getLogger().info(white + "获取更多插件资讯与技术支持" + reset);
        getLogger().info("");

        getLogger().info(getConfig().getString("messages.pluginEnabled", "附魔限制插件已启用！"));
    }

    private boolean validateConfig() {
        try {
            // 检查基本配置节点
            if (!getConfig().isConfigurationSection("enchantments")) {
                getLogger().warning("配置文件中缺少 'enchantments' 节点！");
                return false;
            }
            
            if (!getConfig().isConfigurationSection("messages")) {
                getLogger().warning("配置文件中缺少 'messages' 节点！");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            getLogger().severe("配置文件验证时发生错误：" + e.getMessage());
            return false;
        }
    }

    @Override
    public void onDisable() {
        getLogger().info(getConfig().getString("messages.pluginDisabled", "附魔限制插件已禁用！"));
    }

    @Nonnull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Nonnull
    @Override
    public String getBugTrackerURL() {
        return "https://github.com/xmk2333";
    }
}
