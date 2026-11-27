package dev.vaileks.altarweaponsvl.items.shards.paleshard;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.utils.TextUtils;
import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PaleShardItem {
    private static String tr(String ru, String en) {
        return AltarWeaponsVL.getInstance().tr(ru, en);
    }

    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(Integer.valueOf(2));
            meta.setTooltipStyle(NamespacedKey.minecraft((String)"pure"));
            MiniMessage mm = MiniMessage.miniMessage();
            meta.displayName(TextUtils.shadow(mm.deserialize("<!italic><gray>" + PaleShardItem.tr("Бледный Осколок", "ᴘᴀʟᴇ sʜᴀʀᴅ") + "</gray>")));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><white>" + PaleShardItem.tr("Холодный осколок, хранящий в себе", "A cold shard holding within it") + "</white>"));
            lore.add(mm.deserialize("<!italic><white>" + PaleShardItem.tr("тихую и бледную силу бледного леса.", "the quiet, pale power of pale forest.") + "</white>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><white>" + PaleShardItem.tr("Применение:", "Use:") + "</white>"));
            lore.add(mm.deserialize("<!italic><dark_gray>•</dark_gray> <gray>" + PaleShardItem.tr("Используется для создания", "Used to create") + "</gray>"));
            lore.add(mm.deserialize("<!italic><gray>" + PaleShardItem.tr("легендарного оружия.", "legendary weapons.") + "</gray>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><dark_gray><i>" + PaleShardItem.tr("(( Материал для крафта ))", "(( Crafting material ))") + "</i></dark_gray>"));
            meta.lore(lore.stream().map(TextUtils::shadow).toList());
            dev.vaileks.altarweaponsvl.integration.CommonItems.tag(meta, "pale_shard");
        item.setItemMeta(meta);
        }
        return item;
    }
}

