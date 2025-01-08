package me.baimo.enchantmentlimit.managers;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.enchanting.AutoDisenchanter;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import me.baimo.enchantmentlimit.EnchantmentLimit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DisenchanterManager {
    private final Map<Location, UUID> registeredDisenchanters = new HashMap<>();

    public DisenchanterManager(EnchantmentLimit plugin) {
        // 构造函数保留，以便将来可能需要使用 plugin 实例
    }

    public void registerDisenchanter(Location location) {
        registeredDisenchanters.put(location, UUID.randomUUID());
    }

    public void unregisterDisenchanter(Location location) {
        registeredDisenchanters.remove(location);
    }

    public boolean isRegisteredDisenchanter(Location location) {
        return registeredDisenchanters.containsKey(location);
    }

    public boolean isDisenchanter(Block block) {
        String id = BlockStorage.checkID(block);
        if (id == null) {
            return false;
        }
        
        SlimefunItem item = SlimefunItem.getById(id);
        return item instanceof AutoDisenchanter;
    }

    public boolean isDisenchanter(ItemStack item) {
        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        return sfItem instanceof AutoDisenchanter;
    }
} 