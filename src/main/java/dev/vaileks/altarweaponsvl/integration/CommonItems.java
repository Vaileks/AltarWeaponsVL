package dev.vaileks.altarweaponsvl.integration;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.items.copperpickaxe.CopperPickaxeItem;
import dev.vaileks.altarweaponsvl.items.illusioncore.IllusionCoreItem;
import dev.vaileks.altarweaponsvl.items.shards.hyperionshard.HyperionShardItem;
import dev.vaileks.altarweaponsvl.items.shards.paleshard.PaleShardItem;
import dev.vaileks.altarweaponsvl.items.shards.vampireshard.VampireShardItem;
import dev.vaileks.altarweaponsvl.items.vulcanskull.VulcanSkullItem;
import dev.vaileks.altarweaponsvl.items.wardenheart.WardenHeartItem;
import dev.vaileks.altarweaponsvl.items.weaponshandle.WeaponsHandleItem;
import java.util.List;
import java.util.function.Supplier;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class CommonItems {
    private CommonItems() {}
    public record Entry(String id, Supplier<ItemStack> factory) {}
    public static void tag(org.bukkit.inventory.meta.ItemMeta meta, String id) {
        meta.getPersistentDataContainer().set(new NamespacedKey("runesaltarv", "common_item"), PersistentDataType.STRING, id);
    }
    public static String id(ItemStack item) {
        return item == null || !item.hasItemMeta() ? null : item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey("runesaltarv", "common_item"), PersistentDataType.STRING);
    }
    public static final List<Entry> ITEMS = List.of(
        new Entry("warden_heart", WardenHeartItem::create),
        new Entry("vulcan_skull", VulcanSkullItem::create),
        new Entry("illusion_core", IllusionCoreItem::create),
        new Entry("weapons_handle", WeaponsHandleItem::create),
        new Entry("copper_pickaxe", CopperPickaxeItem::create),
        new Entry("vampire_shard", VampireShardItem::create),
        new Entry("pale_shard", PaleShardItem::create),
        new Entry("hyperion_shard", HyperionShardItem::create)
    );
}
