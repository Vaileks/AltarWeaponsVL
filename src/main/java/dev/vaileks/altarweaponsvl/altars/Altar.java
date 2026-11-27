package dev.vaileks.altarweaponsvl.altars;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Altar {
    private final int id;
    private Location location;
    private final AltarWeaponsVL plugin;
    private ItemStack resultItem;
    private List<ItemStack> recipe = new ArrayList<ItemStack>();
    private boolean isCrafted = false;
    private int maxUses = 1;
    private int currentUses = 1;
    private int cooldownSeconds = 0;
    private long lastUsedTime = 0L;
    private UUID itemEntityId;
    private UUID titleEntityId;
    private UUID recipeEntityId;
    private UUID blockDisplayId;
    private float currentRotation = 0.0f;
    private final Map<String, Double> customOffsets = new HashMap<String, Double>();
    private final Map<String, Float> customScales = new HashMap<String, Float>();

    public Altar(int id, Location location, AltarWeaponsVL plugin) {
        this.id = id;
        this.location = location;
        this.plugin = plugin;
    }

    public void spawnEntities() {
        this.removeHolograms();
        if (!isChunkLoaded()) {
            return;
        }
        FileConfiguration config = this.plugin.getAltarsConfig();
        Display.Billboard billboard = Display.Billboard.valueOf((String)config.getString("settings.display.billboard", "VERTICAL"));
        NamespacedKey tagKey = new NamespacedKey("runesaltarv", "altar_entity");
        if (this.blockDisplayId == null || Bukkit.getEntity((UUID)this.blockDisplayId) == null) {
            Location blockLoc = this.location.clone().add(0.0, 0.001, 0.0);
            BlockDisplay blockDisplay = (BlockDisplay)this.location.getWorld().spawn(blockLoc, BlockDisplay.class, display -> { display.setPersistent(false); display.getPersistentDataContainer().set(tagKey, PersistentDataType.INTEGER, this.id); });
            blockDisplay.setBlock(Bukkit.createBlockData((Material)Material.STRUCTURE_BLOCK));
            this.blockDisplayId = blockDisplay.getUniqueId();
        }
        if (this.resultItem == null || this.isCrafted && this.maxUses != -1 && this.currentUses <= 0) {
            return;
        }
        long cd = this.getCooldownRemaining();
        if (cd > 0L) {
            this.updateRecipeDisplay();
            return;
        }
        Location itemLoc = this.location.clone().add(this.getOffset("item", "x", 0.5), this.getOffset("item", "y", 1.5), this.getOffset("item", "z", 0.5));
        ItemDisplay itemDisplay = this.location.getWorld().spawn(itemLoc, ItemDisplay.class, item -> {
            item.setItemStack(this.resultItem.clone());
            item.setPersistent(false);
            item.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
            item.getPersistentDataContainer().set(tagKey, PersistentDataType.INTEGER, this.id);
            applyScale(item, "item");
        });
        this.itemEntityId = itemDisplay.getUniqueId();
        Location titleLoc = this.location.clone().add(this.getOffset("title", "x", 0.5), this.getOffset("title", "y", 3.2), this.getOffset("title", "z", 0.5));
        TextDisplay titleDisplay = (TextDisplay)this.location.getWorld().spawn(titleLoc, TextDisplay.class, display -> { display.setPersistent(false); display.getPersistentDataContainer().set(tagKey, PersistentDataType.INTEGER, this.id); });
        titleDisplay.setBillboard(billboard);
        titleDisplay.text(Altar.getItemDisplayName(this.resultItem));
        titleDisplay.setBackgroundColor(Color.fromARGB((int)0, (int)0, (int)0, (int)0));
        titleDisplay.setShadowed(true);
        this.applyScale((Display)titleDisplay, "title");
        this.titleEntityId = titleDisplay.getUniqueId();
        this.updateRecipeDisplay();
    }

    public void rotateItemDisplay() {
        if (!isChunkLoaded() || this.itemEntityId == null) {
            return;
        }
        Entity entity = Bukkit.getEntity((UUID)this.itemEntityId);
        if (!(entity instanceof ItemDisplay)) {
            return;
        }
        ItemDisplay display = (ItemDisplay)entity;
        this.currentRotation += 0.05f;
        if (this.currentRotation >= (float)Math.PI * 2) {
            this.currentRotation = 0.0f;
        }
        Transformation trans = display.getTransformation();
        Quaternionf rot = new Quaternionf(new AxisAngle4f(this.currentRotation, 0.0f, 1.0f, 0.0f));
        display.setTransformation(new Transformation(trans.getTranslation(), rot, trans.getScale(), trans.getRightRotation()));
    }

    public void updateRecipeDisplay() {
        if (this.recipeEntityId != null) {
            Entity e = Bukkit.getEntity((UUID)this.recipeEntityId);
            if (e != null) {
                e.remove();
            }
            this.recipeEntityId = null;
        }
        if (!isChunkLoaded() || this.isCrafted && this.maxUses != -1 && this.currentUses <= 0) {
            return;
        }
        long cd = this.getCooldownRemaining();
        if (this.recipe.isEmpty() && cd == 0L) {
            return;
        }
        FileConfiguration config = this.plugin.getAltarsConfig();
        Display.Billboard billboard = Display.Billboard.valueOf((String)config.getString("settings.display.billboard", "VERTICAL"));
        NamespacedKey tagKey = new NamespacedKey("runesaltarv", "altar_entity");
        Location recipeLoc = this.location.clone().add(this.getOffset("recipe", "x", 0.5), this.getOffset("recipe", "y", 3.5), this.getOffset("recipe", "z", 0.5));
        TextDisplay recipeDisplay = (TextDisplay)this.location.getWorld().spawn(recipeLoc, TextDisplay.class, display -> { display.setPersistent(false); display.getPersistentDataContainer().set(tagKey, PersistentDataType.INTEGER, this.id); });
        recipeDisplay.setBillboard(billboard);
        recipeDisplay.setBackgroundColor(Color.fromARGB((int)0, (int)0, (int)0, (int)0));
        recipeDisplay.setShadowed(true);
        if (cd > 0L) {
            recipeDisplay.text(this.plugin.getAltarLanguageManager().component("altar.cooldown", "seconds", String.valueOf(cd)));
        } else {
            TextComponent.Builder builder = Component.text();
            for (int i = 0; i < this.recipe.size(); ++i) {
                ItemStack req = this.recipe.get(i);
                ((TextComponent.Builder)builder.append(this.plugin.getAltarLanguageManager().component("altar.recipe_amount", "amount", String.valueOf(req.getAmount())))).append(Altar.getItemDisplayName(req));
                if (i >= this.recipe.size() - 1) continue;
                builder.append((Component)Component.newline());
            }
            recipeDisplay.text((Component)builder.build());
        }
        this.applyScale((Display)recipeDisplay, "recipe");
        this.recipeEntityId = recipeDisplay.getUniqueId();
    }

    public long getCooldownRemaining() {
        if (this.lastUsedTime == 0L) {
            return 0L;
        }
        long end = this.lastUsedTime + (long)this.cooldownSeconds * 1000L;
        long now = System.currentTimeMillis();
        return Math.max(0L, (end - now + 999L) / 1000L);
    }

    public static Component getItemDisplayName(ItemStack item) {
        if (item == null) {
            return Component.empty();
        }
        if (item.hasItemMeta()) {
            SkullMeta skullMeta;
            if (item.getItemMeta().hasDisplayName()) {
                return item.getItemMeta().displayName();
            }
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta instanceof SkullMeta && (skullMeta = (SkullMeta)itemMeta).hasOwner()) {
                String ownerName = skullMeta.getOwningPlayer() != null ? skullMeta.getOwningPlayer().getName() : "Player";
                return Component.text("Голова " + (ownerName != null ? ownerName : "Player"));
            }
        }
        return Component.translatable(item.getType().translationKey());
    }

    public double getOffset(String type, String axis, double def) {
        String key = type + ".offset." + axis;
        if (this.customOffsets.containsKey(key)) {
            return this.customOffsets.get(key);
        }
        return this.plugin.getAltarsConfig().getDouble("settings.display." + type + ".offset." + axis, def);
    }

    public void setCustomOffset(String type, String axis, double value) {
        this.customOffsets.put(type + ".offset." + axis, value);
    }

    public Map<String, Double> getCustomOffsets() {
        return this.customOffsets;
    }

    public float getScale(String type, String axis, double def) {
        String key = type + ".scale." + axis;
        if (this.customScales.containsKey(key)) {
            return this.customScales.get(key).floatValue();
        }
        return (float)this.plugin.getAltarsConfig().getDouble("settings.display." + type + ".scale." + axis, def);
    }

    public void setCustomScale(String type, String axis, float value) {
        this.customScales.put(type + ".scale." + axis, Float.valueOf(value));
    }

    public Map<String, Float> getCustomScales() {
        return this.customScales;
    }

    private void applyScale(Display display, String path) {
        float sx = this.getScale(path, "x", 1.0);
        float sy = this.getScale(path, "y", 1.0);
        float sz = this.getScale(path, "z", 1.0);
        Transformation trans = display.getTransformation();
        trans.getScale().set((Vector3fc)new Vector3f(sx, sy, sz));
        display.setTransformation(trans);
    }

    public void removeAllEntities() {
        this.removeHolograms();
        if (this.blockDisplayId != null) {
            Entity e = Bukkit.getEntity((UUID)this.blockDisplayId);
            if (e != null) {
                e.remove();
            }
            this.blockDisplayId = null;
        }
    }

    public void removeHolograms() {
        UUID[] ids;
        for (UUID uuid : ids = new UUID[]{this.itemEntityId, this.titleEntityId, this.recipeEntityId}) {
            Entity e2;
            if (uuid == null || (e2 = Bukkit.getEntity((UUID)uuid)) == null) continue;
            e2.remove();
        }
        this.itemEntityId = null;
        this.titleEntityId = null;
        this.recipeEntityId = null;
    }

    public boolean isChunkLoaded() {
        return location != null && location.getWorld() != null && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public int getId() {
        return this.id;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ItemStack getResultItem() {
        return this.resultItem;
    }

    public void setResultItem(ItemStack resultItem) {
        this.resultItem = resultItem;
        this.isCrafted = false;
    }

    public List<ItemStack> getRecipe() {
        return this.recipe;
    }

    public void setRecipe(List<ItemStack> recipe) {
        this.recipe = recipe != null ? recipe.stream().filter(java.util.Objects::nonNull).map(ItemStack::clone).collect(java.util.stream.Collectors.toCollection(ArrayList::new)) : new ArrayList<>();
    }

    public void addRecipeItem(ItemStack item) {
        this.recipe.add(item.clone());
    }

    public boolean isCrafted() {
        return this.isCrafted;
    }

    public void setCrafted(boolean crafted) {
        this.isCrafted = crafted;
    }

    public int getMaxUses() {
        return this.maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
        this.currentUses = maxUses;
    }

    public int getCurrentUses() {
        return this.currentUses;
    }

    public void setCurrentUses(int currentUses) {
        this.currentUses = currentUses;
    }

    public int getCooldownSeconds() {
        return this.cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public long getLastUsedTime() {
        return this.lastUsedTime;
    }

    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }
}

