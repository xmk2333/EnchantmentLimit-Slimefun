package me.baimo.enchantmentlimit.listeners;

import io.github.thebusybiscuit.slimefun4.api.events.AutoDisenchantEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.baimo.enchantmentlimit.EnchantmentLimit;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class AutoDisenchantListener implements Listener {
    private final EnchantmentLimit plugin;

    public AutoDisenchantListener(EnchantmentLimit plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof BlockMenu menu)) {
            return;
        }

        String id = BlockStorage.checkID(menu.getBlock());
        if (id == null || !id.equals("AUTO_DISENCHANTER")) {
            return;
        }

        // 检查是否是放入物品到输入槽
        if (event.getRawSlot() != 19) { // 输入槽位置
            return;
        }

        // 获取放入的物品
        ItemStack item = event.getCursor();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        // 检查是否是 Slimefun 物品且是否可以祛魔
        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        if (sfItem != null && !sfItem.isDisenchantable()) {
            return;
        }

        Map<Enchantment, Integer> enchants = item.getEnchantments();
        if (enchants.isEmpty()) {
            return;
        }

        // 检查每个附魔是否超出限制
        boolean hasExceededLimit = false;

        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            Enchantment enchant = entry.getKey();
            int currentLevel = entry.getValue();
            
            String enchantKey = "enchantments.minecraft:" + enchant.getKey().getKey().toLowerCase();
            int maxLevel = plugin.getConfig().getInt(enchantKey, 5);
            
            if (currentLevel > maxLevel) {
                hasExceededLimit = true;
                break;
            }
        }

        // 根据模式处理
        String mode = plugin.getConfig().getString("settings.mode", "strict");
        if (hasExceededLimit && mode.equalsIgnoreCase("strict")) {
            event.setCancelled(true);
            event.setCursor(item); // 保持物品在鼠标上
            player.updateInventory(); // 更新玩家物品栏

            // 延迟1tick执行，确保物品被弹出
            new BukkitRunnable() {
                @Override
                public void run() {
                    // 移除输入槽的物品
                    menu.replaceExistingItem(19, null);
                    // 移除书本
                    menu.replaceExistingItem(25, null);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAutoDisenchant(AutoDisenchantEvent event) {
        try {
            // 获取原物品的附魔
            Map<Enchantment, Integer> enchants = event.getItem().getEnchantments();
            if (enchants.isEmpty()) {
                return;
            }

            // 检查是否有超限附魔
            boolean hasExceededLimit = false;
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                Enchantment enchant = entry.getKey();
                int currentLevel = entry.getValue();
                
                String enchantKey = "enchantments.minecraft:" + enchant.getKey().getKey().toLowerCase();
                int maxLevel = plugin.getConfig().getInt(enchantKey, 5);
                
                if (currentLevel > maxLevel) {
                    hasExceededLimit = true;
                    break;
                }
            }

            String mode = plugin.getConfig().getString("settings.mode", "strict");
            if (hasExceededLimit) {
                if (mode.equalsIgnoreCase("strict")) {
                    // 在严格模式下，取消祛魔
                    event.setCancelled(true);
                } else if (mode.equalsIgnoreCase("convert")) {
                    // 在转换模式下，修改附魔等级
                    Map<Enchantment, Integer> limitedEnchants = new HashMap<>();
                    for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                        Enchantment enchant = entry.getKey();
                        int currentLevel = entry.getValue();
                        
                        String enchantKey = "enchantments.minecraft:" + enchant.getKey().getKey().toLowerCase();
                        int maxLevel = plugin.getConfig().getInt(enchantKey, 5);
                        
                        limitedEnchants.put(enchant, Math.min(currentLevel, maxLevel));
                    }

                    // 修改原物品的附魔等级
                    ItemStack item = event.getItem();
                    ItemMeta itemMeta = item.getItemMeta();

                    // 移除所有附魔
                    for (Enchantment enchant : enchants.keySet()) {
                        itemMeta.removeEnchant(enchant);
                    }

                    // 添加限制后的附魔
                    for (Map.Entry<Enchantment, Integer> entry : limitedEnchants.entrySet()) {
                        itemMeta.addEnchant(entry.getKey(), entry.getValue(), true);
                    }

                    // 应用更改
                    item.setItemMeta(itemMeta);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning(ChatColor.translateAlternateColorCodes('&', "处理祛魔事件时发生错误：" + e.getMessage()));
            e.printStackTrace();
        }
    }
}

