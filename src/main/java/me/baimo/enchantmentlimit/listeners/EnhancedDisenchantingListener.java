package me.baimo.enchantmentlimit.listeners;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.enchanting.AutoDisenchanter;
import me.baimo.enchantmentlimit.EnchantmentLimit;
import me.baimo.enchantmentlimit.managers.DisenchanterManager;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import io.github.thebusybiscuit.slimefun4.core.services.BlockDataService;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

import java.util.HashMap;
import java.util.Map;

public class EnhancedDisenchantingListener implements Listener {
    private final EnchantmentLimit plugin;
    private final DisenchanterManager manager;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    public EnhancedDisenchantingListener(EnchantmentLimit plugin, DisenchanterManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (manager.isDisenchanter(event.getItemInHand())) {
            manager.registerDisenchanter(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (manager.isRegisteredDisenchanter(event.getBlock().getLocation())) {
            manager.unregisterDisenchanter(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlockMenu menu)) {
            return;
        }

        String id = BlockStorage.checkID(menu.getBlock());
        if (id == null) {
            return;
        }
        
        SlimefunItem sfItem = SlimefunItem.getById(id);
        if (!(sfItem instanceof AutoDisenchanter)) {
            return;
        }

        // 检查点击的是否是输出槽
        if (!isOutputSlot(menu, event.getSlot())) {
            return;
        }

        // 获取输入物品
        ItemStack inputItem = menu.getItemInSlot(getInputSlot(menu));
        if (inputItem == null || inputItem.getEnchantments().isEmpty()) {
            return;
        }

        // 检查是否有书
        ItemStack book = menu.getItemInSlot(getBookSlot(menu));
        if (book == null || book.getType() != Material.BOOK) {
            return;
        }

        // 处理祛魔
        handleDisenchanting(event, menu, inputItem, book);
    }

    private void handleDisenchanting(InventoryClickEvent event, BlockMenu menu, ItemStack item, ItemStack book) {
        Map<Enchantment, Integer> enchants = item.getEnchantments();
        Map<Enchantment, Integer> newEnchants = new HashMap<>();
        boolean needModify = false;

        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            Enchantment enchant = entry.getKey();
            int currentLevel = entry.getValue();
            
            String enchantKey = "enchantments.minecraft:" + enchant.getKey().getKey().toLowerCase();
            int maxLevel = plugin.getConfig().getInt(enchantKey, 5);
            
            if (currentLevel > maxLevel) {
                needModify = true;
                newEnchants.put(enchant, maxLevel);
                
                if (plugin.getConfig().getBoolean("settings.enableMessages", true)) {
                    String rawMessage = plugin.getConfig().getString("messages.limitExceeded", 
                        "&c附魔 %enchant% 的等级 %level% 超出限制（最大：%max%）！");
                    Component message = serializer.deserialize(
                        rawMessage
                            .replace("%enchant%", enchant.getKey().getKey())
                            .replace("%level%", String.valueOf(currentLevel))
                            .replace("%max%", String.valueOf(maxLevel))
                    );
                    
                    event.getWhoClicked().sendMessage(message);
                }
            } else {
                newEnchants.put(enchant, currentLevel);
            }
        }

        if (needModify) {
            event.setCancelled(true);
            
            // 创建新的附魔书
            ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) enchantedBook.getItemMeta();
            
            // 添加限制后的附魔
            newEnchants.forEach((enchant, level) -> 
                meta.addStoredEnchant(enchant, level, true));
            
            enchantedBook.setItemMeta(meta);
            
            // 移除原物品的附魔
            ItemStack disenchantedItem = item.clone();
            disenchantedItem.setAmount(1);
            ItemMeta itemMeta = disenchantedItem.getItemMeta();
            enchants.keySet().forEach(itemMeta::removeEnchant);
            disenchantedItem.setItemMeta(itemMeta);
            
            // 更新机器中的物品
            menu.replaceExistingItem(getInputSlot(menu), disenchantedItem);
            menu.replaceExistingItem(getBookSlot(menu), null);
            
            // 尝试放入输出物品
            for (int slot : getOutputSlots(menu)) {
                if (menu.getItemInSlot(slot) == null) {
                    menu.replaceExistingItem(slot, enchantedBook);
                    break;
                }
            }
        }
    }

    private boolean isOutputSlot(BlockMenu menu, int slot) {
        // 实现输出槽检查逻辑
        return slot >= 40 && slot <= 44;
    }

    private int getInputSlot(BlockMenu menu) {
        // 返回输入物品槽位
        return 19;
    }

    private int getBookSlot(BlockMenu menu) {
        // 返回书本槽位
        return 25;
    }

    private int[] getOutputSlots(BlockMenu menu) {
        // 返回输出槽位数组
        return new int[]{40, 41, 42, 43, 44};
    }
} 