package dev.vaileks.altarweaponsvl.items.shards.vampireshard;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.utils.TextUtils;
import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class VampireShardItem {
    private static String tr(String ru, String en) {
        return AltarWeaponsVL.getInstance().tr(ru, en);
    }

    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(Integer.valueOf(1));
            meta.setTooltipStyle(NamespacedKey.minecraft((String)"shard"));
            MiniMessage mm = MiniMessage.miniMessage();
            meta.displayName(TextUtils.shadow(mm.deserialize("<!italic><red>" + VampireShardItem.tr("Вампировый Осколок", "ᴠᴀᴍᴘɪʀᴇ sʜᴀʀᴅ") + "</red>")));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><dark_red>" + VampireShardItem.tr("Осколок, пропитанный жаждой", "A shard soaked in a thirst for") + "</dark_red>"));
            lore.add(mm.deserialize("<!italic><dark_red>" + VampireShardItem.tr("чужой крови и жизненной силы.", "blood and stolen life force.") + "</dark_red>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><white>" + VampireShardItem.tr("Применение:", "Use:") + "</white>"));
            lore.add(mm.deserialize("<!italic><dark_gray>•</dark_gray> <gray>" + VampireShardItem.tr("Используется для создания", "Used to create") + "</gray>"));
            lore.add(mm.deserialize("<!italic><gray>" + VampireShardItem.tr("легендарного оружия.", "legendary weapons.") + "</gray>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><dark_gray><i>" + VampireShardItem.tr("(( Материал для крафта ))", "(( Crafting material ))") + "</i></dark_gray>"));
            meta.lore(lore.stream().map(TextUtils::shadow).toList());
            dev.vaileks.altarweaponsvl.integration.CommonItems.tag(meta, "vampire_shard");
        item.setItemMeta(meta);
        }
        return item;
    }
}

