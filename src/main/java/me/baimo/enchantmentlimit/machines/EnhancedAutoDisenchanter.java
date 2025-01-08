package me.baimo.enchantmentlimit.machines;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.enchanting.AutoDisenchanter;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import me.baimo.enchantmentlimit.EnchantmentLimit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

public class EnhancedAutoDisenchanter extends AutoDisenchanter {
    
    private final EnchantmentLimit plugin;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    @ParametersAreNonnullByDefault
    public EnhancedAutoDisenchanter(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, EnchantmentLimit plugin) {
        super(itemGroup, item, recipeType, recipe);
        this.plugin = plugin;
    }

    @Override
    protected MachineRecipe findNextRecipe(BlockMenu menu) {
        // 检查输出槽是否有空间
        for (int outputSlot : getOutputSlots()) {
            if (menu.getItemInSlot(outputSlot) != null) {
                return null;
            }
        }

        // 获取输入物品
        ItemStack item = menu.getItemInSlot(getInputSlots()[0]);
        if (!isDisenchantable(item)) {
            return null;
        }

        // 获取书本
        ItemStack book = menu.getItemInSlot(getInputSlots()[1]);
        if (book == null || book.getType() != Material.BOOK) {
            return null;
        }

        // 处理附魔
        Map<Enchantment, Integer> enchants = item.getEnchantments();
        if (enchants.isEmpty()) {
            return null;
        }

        Map<Enchantment, Integer> allowedEnchants = new HashMap<>();
        boolean needModify = false;

        // 检查每个附魔的等级限制
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            Enchantment enchant = entry.getKey();
            int currentLevel = entry.getValue();
            
            String enchantKey = "enchantments.minecraft:" + enchant.getKey().getKey().toLowerCase();
            int maxLevel = plugin.getConfig().getInt(enchantKey, 5);
            
            if (currentLevel > maxLevel) {
                needModify = true;
                allowedEnchants.put(enchant, maxLevel);
                
                if (plugin.getConfig().getBoolean("settings.enableMessages", true)) {
                    String rawMessage = plugin.getConfig().getString("messages.limitExceeded", 
                        "&c附魔 %enchant% 的等级 %level% 超出限制（最大：%max%）！");
                    Component message = serializer.deserialize(rawMessage
                            .replace("%enchant%", enchant.getKey().getKey())
                            .replace("%level%", String.valueOf(currentLevel))
                            .replace("%max%", String.valueOf(maxLevel)));
                            
                    menu.toInventory().getViewers().forEach(viewer -> viewer.sendMessage(message));
                }
            } else {
                allowedEnchants.put(enchant, currentLevel);
            }
        }

        // 创建输出物品
        ItemStack disenchantedItem = item.clone();
        disenchantedItem.setAmount(1);
        ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) enchantedBook.getItemMeta();

        // 转移附魔
        ItemMeta itemMeta = disenchantedItem.getItemMeta();
        for (Map.Entry<Enchantment, Integer> entry : allowedEnchants.entrySet()) {
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();
            
            itemMeta.removeEnchant(enchant);
            bookMeta.addStoredEnchant(enchant, level, true);
        }

        disenchantedItem.setItemMeta(itemMeta);
        enchantedBook.setItemMeta(bookMeta);

        // 创建配方
        return new MachineRecipe(
            90 * allowedEnchants.size() / getSpeed(),
            new ItemStack[] {item, book},
            new ItemStack[] {disenchantedItem, enchantedBook}
        );
    }

    private boolean isDisenchantable(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir() || item.getType() == Material.BOOK || !item.hasItemMeta()) {
            return false;
        }

        if (!item.getEnchantments().isEmpty()) {
            SlimefunItem sfItem = SlimefunItem.getByItem(item);
            return sfItem == null || sfItem.isDisenchantable();
        }

        return false;
    }

    @Override
    @Nonnull
    public String getMachineIdentifier() {
        return "AUTO_DISENCHANTER";
    }
}
