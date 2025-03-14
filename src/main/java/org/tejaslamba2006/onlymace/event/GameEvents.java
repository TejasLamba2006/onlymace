package org.tejaslamba2006.onlymace.event;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tejaslamba2006.onlymace.OnlyMacePlugin;

import java.util.Objects;
import java.util.UUID;


public class GameEvents implements Listener {
    private final OnlyMacePlugin plugin = OnlyMacePlugin.getPlugin(OnlyMacePlugin.class);
    @Getter
    private final YamlConfiguration mainConfig  =  plugin.getConfigurationManager().get("config.yml");

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        boolean hasMace = false;
        ItemStack[] var4 = player.getInventory().getContents();
        int var5 = var4.length;

        int var6;
        ItemStack item;
        for(var6 = 0; var6 < var5; ++var6) {
            item = var4[var6];
            if (plugin.isMace(item)) {
                hasMace = true;
                break;
            }
        }

        var4 = player.getEnderChest().getContents();
        var5 = var4.length;

        for(var6 = 0; var6 < var5; ++var6) {
            item = var4[var6];
            if (plugin.isMace(item)) {
                hasMace = true;
                plugin.getLogger().info("Player " + player.getName() + " logged out with the Mace in their Ender Chest.");
                break;
            }
        }

        plugin.getConfig().set("offlinePlayerData." + player.getUniqueId(), hasMace);
        plugin.saveConfig();
    }

    @EventHandler
    public void onMaceDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item droppedItem = event.getItemDrop();
        if (plugin.isMace(droppedItem.getItemStack())) {
            plugin.saveMaceOwner((UUID)null);
            plugin.getLogger().info("Mace ownership cleared due to drop.");
            plugin.getConfig().set("offlinePlayerData." + player.getUniqueId(), false);
            plugin.saveConfig();
        }

    }

    @EventHandler
    public void onMacePickup(EntityPickupItemEvent event) {
        ItemStack pickedItem = event.getItem().getItemStack();
        if (plugin.isMace(pickedItem)) {
            if (event.getEntity() instanceof Player) {
                plugin.saveMaceOwner(event.getEntity().getUniqueId());
            } else {
                plugin.saveMaceOwner((UUID)null);
            }
        }

    }
    @EventHandler
    public void onMaceMove(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            ItemStack cursorItem = event.getCursor();
            ItemStack clickedItem = event.getCurrentItem();
            if (plugin.isMace(cursorItem)) {
                plugin.saveMaceOwner(player.getUniqueId());
                plugin.getConfig().set("offlinePlayerData." + player.getUniqueId().toString(), false);
                plugin.saveConfig();
            } else if (plugin.isMace(clickedItem) && (isStorageContainer(event.getInventory().getType()) || isAnimalStorage(event))) {
                plugin.saveMaceOwner((UUID)null);
            }
        }

    }
    private boolean isStorageContainer(@NotNull InventoryType type) {
        return type == InventoryType.CHEST || type == InventoryType.BARREL || type == InventoryType.DROPPER || type == InventoryType.DISPENSER || type == InventoryType.SHULKER_BOX || type == InventoryType.HOPPER;
    }

    private boolean isAnimalStorage(InventoryClickEvent event) {
        return event.getInventory().getHolder() != null;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        plugin.getConfig().set("offlinePlayerData." + playerUUID, (Object)null);
        plugin.saveConfig();
        if (!plugin.doesMaceExist()) {
            plugin.getLogger().warning("No Mace found! However, it may still be inside an unloaded chunk.");
            plugin.getLogger().warning("Recipe removed. Please manually check if the Mace exists to prevent duplication.");
            plugin.resetMaceCrafting();
        }

    }
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() != null && event.getRecipe().getResult().getType() == Material.MACE && plugin.maceCrafted) {
            event.getInventory().setResult(null);
        }

    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() == Material.MACE) {
            if (plugin.maceCrafted) {
                event.setCancelled(true);
                return;
            }

            plugin.maceCrafted = true;
            plugin.getConfig().set("settings.mace-crafted", true);
            plugin.saveMaceOwner(event.getWhoClicked().getUniqueId());
            plugin.saveConfig();
            ItemStack mace = event.getInventory().getResult();
            if (mace != null) {
                plugin.markMace(mace);
            }

            Bukkit.getScheduler().runTask(plugin, plugin::removeAllMaceRecipes);
            plugin.getLogger().info("Mace crafted! Removing all recipes.");
            if (plugin.getConfig().getBoolean("showMaceAlerts", true)) {
                Bukkit.broadcastMessage("§b[OnlyMace] The Mace has been crafted!");
            }
        }

    }

    @EventHandler
    public void onCrafterCraft(CrafterCraftEvent event) {
        if (event.getRecipe().getResult().getType() == Material.MACE) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onMaceBreak(PlayerItemBreakEvent event) {
        ItemStack brokenItem = event.getBrokenItem();
        if (plugin.isMace(brokenItem)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!plugin.doesMaceExist()) {
                    plugin.resetMaceCrafting();
                }

            }, 50L);
        }

    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (plugin.isMace(event.getEntity().getItemStack())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!plugin.doesMaceExist()) {
                    plugin.resetMaceCrafting();
                }

            }, 50L);
        }

    }
    @EventHandler
    public void onItemRemoved(EntityRemoveFromWorldEvent event) {
        Entity var3 = event.getEntity();
        if (var3 instanceof Item item) {
            if (plugin.isMace(item.getItemStack())) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!plugin.doesMaceExist()) {
                        plugin.resetMaceCrafting();
                    }

                }, 50L);
            }
        }

    }
    @EventHandler
    public void onBundleStore(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack cursorItem = event.getCursor();
        ItemStack clickedItem = event.getCurrentItem();
        if ((clickedInventory != null && clickedInventory.getType() == InventoryType.SHULKER_BOX || Objects.requireNonNull(clickedInventory).getType() == InventoryType.HOPPER) && (plugin.isMace(cursorItem) || plugin.isMace(clickedItem))) {
            event.setCancelled(true);
        }

        if (event.isShiftClick() && plugin.isMace(clickedItem)) {
            Inventory topInventory = event.getView().getTopInventory();
            if (topInventory.getType() == InventoryType.SHULKER_BOX || topInventory.getType() == InventoryType.HOPPER) {
                event.setCancelled(true);
            }
        }

        if (plugin.isMace(cursorItem) && this.isBundle(clickedItem) || this.isBundle(cursorItem) && plugin.isMace(clickedItem)) {
            event.setCancelled(true);
        }

        if (event.isShiftClick() && plugin.isMace(clickedItem) && event.getInventory().getType() == InventoryType.PLAYER) {
            ItemStack[] var9 = event.getWhoClicked().getInventory().getContents();

            for (ItemStack item : var9) {
                if (this.isBundle(item)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();
        if (plugin.isMace(item)) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onHopperPickup(InventoryPickupItemEvent event) {
        Item item = event.getItem();
        if (plugin.isMace(item.getItemStack())) {
            event.setCancelled(true);
        }
    }
    private boolean isBundle(ItemStack item) {
        return item != null && item.getType() == Material.BUNDLE;
    }
}