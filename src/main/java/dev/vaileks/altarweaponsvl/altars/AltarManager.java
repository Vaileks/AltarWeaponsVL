package dev.vaileks.altarweaponsvl.altars;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.altars.Altar;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class AltarManager {
    private final AltarWeaponsVL plugin;
    private final Map<Integer, Altar> altars = new HashMap<Integer, Altar>();
    private int nextId = 1;
    private File dataFolder;
    private File altarsDataFile;
    private YamlConfiguration altarsDataConfig;
    private final NamespacedKey altarKey;

    public AltarManager(AltarWeaponsVL plugin) {
        this.plugin = plugin;
        this.altarKey = new NamespacedKey("runesaltarv", "is_altar");
        this.initDataFile();
    }

    private void initDataFile() {
        this.dataFolder = new File(this.plugin.getDataFolder(), "data");
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
        this.altarsDataFile = new File(this.dataFolder, "altars.yml");
        if (!this.altarsDataFile.exists()) {
            try {
                this.altarsDataFile.createNewFile();
            }
            catch (IOException e) {
                this.plugin.getLogger().severe("Could not create data/altars.yml: " + e.getMessage());
            }
        }
        this.altarsDataConfig = YamlConfiguration.loadConfiguration((File)this.altarsDataFile);
    }

    public FileConfiguration getDataConfig() {
        if (this.altarsDataConfig == null) {
            this.initDataFile();
        }
        return this.altarsDataConfig;
    }

    public void saveDataConfig() {
        try {
            if (this.altarsDataConfig != null && this.altarsDataFile != null) {
                this.altarsDataConfig.save(this.altarsDataFile);
            }
        }
        catch (IOException e) {
            this.plugin.getLogger().severe("Could not save data/altars.yml: " + e.getMessage());
        }
    }

    public ItemStack createAltarItem() {
        ItemStack altarItem = new ItemStack(Material.STRUCTURE_BLOCK);
        ItemMeta meta = altarItem.getItemMeta();
        if (meta != null) {
            String titleText = this.plugin.getAltarLanguageManager().plain("altar.item_name", new String[0]);
            Component displayName = MiniMessage.miniMessage().deserialize("<gradient:#FFAA00:#FFFFFF:#FFAA00>" + titleText + "</gradient>").decoration(TextDecoration.ITALIC, false);
            meta.displayName(displayName);
            meta.getPersistentDataContainer().set(this.altarKey, PersistentDataType.BYTE, (byte)1);
            altarItem.setItemMeta(meta);
        }
        return altarItem;
    }

    public NamespacedKey getAltarKey() {
        return this.altarKey;
    }

    public Altar createAltar(Location location) {
        if (location == null || location.getWorld() == null || getAltarAt(location) != null) throw new IllegalArgumentException("Altar location is missing or occupied");
        while (this.altars.containsKey(this.nextId)) {
            ++this.nextId;
        }
        Altar altar = new Altar(this.nextId, location, this.plugin);
        this.altars.put(this.nextId, altar);
        this.saveAltars();
        return altar;
    }

    public Altar getAltar(int id) {
        return this.altars.get(id);
    }

    public Set<Integer> getAltarsIds() {
        return Set.copyOf(this.altars.keySet());
    }

    public Altar getAltarAt(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return this.altars.values().stream().filter(a -> a.getLocation() != null && Objects.equals(a.getLocation().getWorld(), location.getWorld()) && a.getLocation().getBlockX() == location.getBlockX() && a.getLocation().getBlockY() == location.getBlockY() && a.getLocation().getBlockZ() == location.getBlockZ()).findFirst().orElse(null);
    }

    public void removeAltar(int id) {
        Altar altar = this.altars.remove(id);
        if (altar != null) {
            altar.removeAllEntities();
            this.getDataConfig().set("altars." + id, null);
            this.saveDataConfig();
        }
    }

    public void loadAltars() {
        cleanupEntities();
        this.altars.clear();
        ConfigurationSection sec = this.getDataConfig().getConfigurationSection("altars");
        if (sec == null) {
            return;
        }
        for (String key : sec.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                Location loc = sec.getLocation(key + ".location");
                if (loc == null) continue;
                Altar altar = new Altar(id, loc, this.plugin);
                if (sec.contains(key + ".result")) {
                    altar.setResultItem(sec.getItemStack(key + ".result"));
                }
                if (sec.contains(key + ".recipe")) {
                    altar.setRecipe(sec.getList(key + ".recipe").stream().filter(ItemStack.class::isInstance).map(ItemStack.class::cast).toList());
                }
                if (sec.contains(key + ".crafted")) {
                    altar.setCrafted(sec.getBoolean(key + ".crafted"));
                }
                altar.setMaxUses(sec.getInt(key + ".max_uses", 1));
                altar.setCurrentUses(sec.getInt(key + ".current_uses", altar.getMaxUses()));
                altar.setCooldownSeconds(sec.getInt(key + ".cooldown", 0));
                altar.setLastUsedTime(sec.getLong(key + ".last_used", 0L));
                for (String type : List.of("title", "recipe", "item")) {
                    for (String axis : List.of("x", "y", "z")) {
                        String scaleKey;
                        String offsetKey = key + ".display." + type + ".offset." + axis;
                        if (sec.contains(offsetKey)) {
                            altar.setCustomOffset(type, axis, sec.getDouble(offsetKey));
                        }
                        if (!sec.contains(scaleKey = key + ".display." + type + ".scale." + axis)) continue;
                        altar.setCustomScale(type, axis, (float)sec.getDouble(scaleKey));
                    }
                }
                this.altars.put(id, altar);
                altar.spawnEntities();
                this.nextId = Math.max(this.nextId, id + 1);
            }
            catch (Exception e) {
                this.plugin.getLogger().warning(this.plugin.getAltarLanguageManager().plain("log.failed_load_altar", "id", key));
            }
        }
    }

    public void saveAltars() {
        FileConfiguration config = this.getDataConfig();
        for (Altar altar : this.altars.values()) {
            String path = "altars." + altar.getId();
            config.set(path + ".location", (Object)altar.getLocation());
            config.set(path + ".result", (Object)altar.getResultItem());
            config.set(path + ".recipe", altar.getRecipe());
            config.set(path + ".crafted", (Object)altar.isCrafted());
            config.set(path + ".max_uses", (Object)altar.getMaxUses());
            config.set(path + ".current_uses", (Object)altar.getCurrentUses());
            config.set(path + ".cooldown", (Object)altar.getCooldownSeconds());
            config.set(path + ".last_used", (Object)altar.getLastUsedTime());
            for (Map.Entry<String, Double> entry : altar.getCustomOffsets().entrySet()) {
                config.set(path + ".display." + entry.getKey(), (Object)entry.getValue());
            }
            for (Map.Entry<String, Float> entry : altar.getCustomScales().entrySet()) {
                config.set(path + ".display." + entry.getKey(), (Object)entry.getValue());
            }
        }
        this.saveDataConfig();
    }

    public void cleanupEntities() {
        this.altars.values().forEach(Altar::removeAllEntities);
    }
}

