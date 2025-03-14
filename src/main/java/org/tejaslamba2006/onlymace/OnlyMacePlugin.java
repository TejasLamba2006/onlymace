package org.tejaslamba2006.onlymace;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.language.replacer.Replacer;
import com.github.sirblobman.api.plugin.ConfigurablePlugin;
import com.github.sirblobman.api.shaded.adventure.text.minimessage.MiniMessage;

import lombok.Getter;
import net.kyori.adventure.text.Component;


import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.tejaslamba2006.onlymace.command.onlymace.OnlyMaceCommand;
import org.tejaslamba2006.onlymace.event.GameEvents;

import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

@Getter
public final class OnlyMacePlugin extends ConfigurablePlugin {
    public static OnlyMacePlugin plugin;
    public boolean maceCrafted;
    private final NamespacedKey maceKey = new NamespacedKey(this, "maceTracker");
    public static Component mmString(String string) {
        String text = MiniMessage.miniMessage().serialize(MiniMessage.miniMessage().deserialize(string));
        return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(text);
    }
    public static Component mm(CommandSender audience, String key) {
        String text = MiniMessage.miniMessage().serialize(plugin.getLanguageManager().getMessageWithPrefix(audience, key));
        return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(text);
    }
    public static Component mm(CommandSender audience, String key, Replacer... replacer) {
        String text = MiniMessage.miniMessage().serialize(plugin.getLanguageManager().getMessageWithPrefix(audience, key, replacer));
        return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(text);
    }
    private YamlConfiguration mainConfig;
    @Override
    public void onLoad() {
        this.loadConfig();
    }

    @Override
    public void onEnable() {
        plugin = this;
        this.loadLanguages();
        this.registerCommands();
        this.registerListeners();
        this.maceCrafted = getConfig().getBoolean("isMaceMade", false);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!this.doesMaceExist()) {
                this.resetMaceCrafting();
                this.getConfig().set("offlinePlayerData", null);
                this.saveConfig();
            } else {
                this.removeAllMaceRecipes();
                this.getLogger().info("Mace already crafted. Recipes removed.");
            }

        }, 40L);
        getLogger().info("OnlyMace plugin has been enabled.");
    }

    public void reload() {
        this.loadConfig();
        this.loadLanguages();
        this.registerCommands();
        this.registerListeners();
        getLogger().info("OnlyMace plugin has been reloaded.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving Ender Chest data before shutdown...");
        Player player;
        boolean hasMace;
        for(Iterator<? extends Player> var1 = Bukkit.getOnlinePlayers().iterator(); var1.hasNext(); getConfig().set("offlinePlayerData." + player.getUniqueId(), hasMace)) {
            player = var1.next();
            hasMace = false;
            ItemStack[] var4 = player.getInventory().getContents();
            int var5 = var4.length;

            int var6;
            ItemStack item;
            for(var6 = 0; var6 < var5; ++var6) {
                item = var4[var6];
                if (isMace(item)) {
                    hasMace = true;
                    break;
                }
            }

            var4 = player.getEnderChest().getContents();
            var5 = var4.length;

            for(var6 = 0; var6 < var5; ++var6) {
                item = var4[var6];
                if (isMace(item)) {
                    hasMace = true;
                    getLogger().info("Player " + player.getName() + " had the Mace in their Ender Chest before shutdown.");
                    break;
                }
            }
        }

        saveConfig();
        getLogger().info("Plugin disabled! Ender Chest data saved.");
    }

    private void loadConfig() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.saveDefault("config.yml");
        configurationManager.reload("config.yml");
        mainConfig = configurationManager.get("config.yml");
        getLogger().info("Configuration loaded");
    }

    private void loadLanguages() {
        LanguageManager languageManager = getLanguageManager();
        languageManager.saveDefaultLanguageFiles();
        languageManager.reloadLanguages();
        languageManager.onPluginEnable();
        getLogger().info("Languages loaded");
    }

    private void registerCommands() {
        new OnlyMaceCommand(this).register();
        getLogger().info("Commands registered");
    }
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new GameEvents(), this);
        getLogger().info("Events registered");
    }
    public boolean isMace(ItemStack item) {
        if (item != null && item.getType() == Material.MACE && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                PersistentDataContainer data = meta.getPersistentDataContainer();
                return data.has(this.maceKey, PersistentDataType.STRING);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public void removeAllMaceRecipes() {
        NamespacedKey vanillaMaceKey = NamespacedKey.minecraft("mace");
        if (Bukkit.getRecipe(vanillaMaceKey) != null) {
            Bukkit.removeRecipe(vanillaMaceKey);
            getLogger().info("Removed vanilla Mace recipe.");
        }

    }
    public void markMace(ItemStack mace) {
        ItemMeta meta = mace.getItemMeta();
        if (meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(this.maceKey, PersistentDataType.STRING, "true");
            mace.setItemMeta(meta);
        }

    }
    public boolean doesMaceExist() {
        this.getLogger().info("Checking if Mace exists...");
        Iterator var2 = Bukkit.getOnlinePlayers().iterator();

        int var5;
        int var6;
        while(var2.hasNext()) {
            Player player = (Player)var2.next();
            ItemStack[] var4 = player.getInventory().getContents();
            var5 = var4.length;

            ItemStack item;
            for(var6 = 0; var6 < var5; ++var6) {
                item = var4[var6];
                if (this.isMace(item) || this.isMaceInsideShulker(item)) {
                    this.getLogger().info("Mace found in " + player.getName() + "'s inventory.");
                    return true;
                }
            }

            var4 = player.getEnderChest().getContents();
            var5 = var4.length;

            for(var6 = 0; var6 < var5; ++var6) {
                item = var4[var6];
                if (this.isMace(item) || this.isMaceInsideShulker(item)) {
                    this.getLogger().info("Mace found in " + player.getName() + "'s Ender Chest.");
                    return true;
                }
            }
        }

        var2 = Bukkit.getWorlds().iterator();

        World world;
        Iterator var20;
        Entity entity;
        while(var2.hasNext()) {
            world = (World)var2.next();
            var20 = world.getEntities().iterator();

            while(var20.hasNext()) {
                entity = (Entity)var20.next();
                if (entity instanceof Item item) {
                    if (this.isMace(item.getItemStack()) || this.isMaceInsideShulker(item.getItemStack())) {
                        this.getLogger().info("Mace found as a dropped item in world: " + world.getName());
                        return true;
                    }
                }
            }
        }

        var2 = Bukkit.getWorlds().iterator();

        int var9;
        while(var2.hasNext()) {
            world = (World)var2.next();
            Chunk[] var21 = world.getLoadedChunks();
            var5 = var21.length;

            for(var6 = 0; var6 < var5; ++var6) {
                Chunk chunk = var21[var6];
                BlockState[] var8 = chunk.getTileEntities();
                var9 = var8.length;

                for(int var10 = 0; var10 < var9; ++var10) {
                    BlockState state = var8[var10];
                    if (state instanceof Container container) {
                        Inventory inv = container.getInventory();
                        ItemStack[] var14 = inv.getContents();
                        for (ItemStack item : var14) {
                            if (this.isMace(item) || this.isMaceInsideShulker(item)) {
                                this.getLogger().info("Mace found inside a container at " + state.getLocation());
                                return true;
                            }
                        }
                    }
                }
            }
        }

        var2 = Bukkit.getWorlds().iterator();

        while(var2.hasNext()) {
            world = (World)var2.next();
            var20 = world.getEntities().iterator();

            while(var20.hasNext()) {
                entity = (Entity)var20.next();
                ItemStack[] var25;
                int var27;
                ItemStack item;
                if (entity instanceof AbstractHorse horse) {
                    var25 = horse.getInventory().getContents();
                    var27 = var25.length;

                    for(var9 = 0; var9 < var27; ++var9) {
                        item = var25[var9];
                        if (this.isMace(item) || this.isMaceInsideShulker(item)) {
                            this.getLogger().info("Mace found in a horse inventory!");
                            return true;
                        }
                    }
                }

                if (entity instanceof StorageMinecart minecart) {
                    var25 = minecart.getInventory().getContents();
                    var27 = var25.length;

                    for(var9 = 0; var9 < var27; ++var9) {
                        item = var25[var9];
                        if (this.isMace(item) || this.isMaceInsideShulker(item)) {
                            this.getLogger().info("Mace found in a storage minecart!");
                            return true;
                        }
                    }
                }

                if (entity instanceof ChestBoat chestBoat) {
                    var25 = chestBoat.getInventory().getContents();
                    var27 = var25.length;

                    for(var9 = 0; var9 < var27; ++var9) {
                        item = var25[var9];
                        if (this.isMace(item) || this.isMaceInsideShulker(item)) {
                            int var10001 = entity.getLocation().getBlockX();
                            getLogger().info("Mace found in a Chest Boat at X: " + var10001 + " Y: " + entity.getLocation().getBlockY() + " Z: " + entity.getLocation().getBlockZ() + " in world " + entity.getWorld().getName());
                            return true;
                        }
                    }
                }
            }
        }

        if (this.getConfig().isConfigurationSection("offlinePlayerData")) {
            var2 = Objects.requireNonNull(this.getConfig().getConfigurationSection("offlinePlayerData")).getKeys(true).iterator();

            while(var2.hasNext()) {
                String uuid = (String)var2.next();
                if (this.getConfig().getBoolean("offlinePlayerData." + uuid, true)) {
                    this.getLogger().info("Mace is in an offline player's inventory (UUID: " + uuid + ").");
                    return true;
                }
            }
        }

        this.getLogger().info("Mace does NOT exist! Crafting can be re-enabled.");
        this.getConfig().set("offlinePlayerData", null);
        this.saveConfig();
        return false;
    }
    public boolean isMaceInsideShulker(ItemStack item) {
        if (item != null && item.getType() == Material.SHULKER_BOX) {
            BlockStateMeta meta = (BlockStateMeta)item.getItemMeta();
            if (meta != null) {
                BlockState var4 = meta.getBlockState();
                if (var4 instanceof ShulkerBox shulkerBox) {
                    Inventory var9 = shulkerBox.getInventory();
                    ItemStack[] var5 = var9.getContents();
                    for (ItemStack storedItem : var5) {
                        if (this.isMace(storedItem) || this.isMaceInsideShulker(storedItem)) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        return false;
    }
    public void saveMaceOwner(UUID ownerUUID) {
        if (ownerUUID == null) {
            this.getConfig().set("MaceHolder", (Object)null);
        } else {
            this.getConfig().set("MaceHolder", ownerUUID.toString());
        }

        this.saveConfig();
    }

    public UUID getMaceOwner() {
        String ownerUUID = this.getConfig().getString("MaceHolder");
        return ownerUUID != null ? UUID.fromString(ownerUUID) : null;
    }

    public boolean isMaceOwner(UUID playerUUID) {
        UUID maceOwner = this.getMaceOwner();
        return maceOwner != null && maceOwner.equals(playerUUID);
    }
    public void resetMaceCrafting() {
        this.maceCrafted = false;
        this.getConfig().set("isMaceMade", false);
        this.getConfig().set("offlinePlayerData", null);
        this.saveConfig();
        this.addVanillaMaceRecipe();
        this.getLogger().info("No Mace found. Crafting is re-enabled.");
        if (this.getConfig().getBoolean("settings.announce-mace-messages", true)) {
            Bukkit.broadcastMessage("§b[OnlyMace] The Mace has been lost! It can now be crafted again.");
        }

    }

    private void addVanillaMaceRecipe() {
        NamespacedKey vanillaMaceKey = NamespacedKey.minecraft("mace");
        if (Bukkit.getRecipe(vanillaMaceKey) == null) {
            Bukkit.reloadData();
            this.getLogger().info("Vanilla Mace recipe has been restored.");
        }
    }
    public boolean isAnyMace(ItemStack item) {
        return item != null && item.getType() == Material.MACE;
    }
}