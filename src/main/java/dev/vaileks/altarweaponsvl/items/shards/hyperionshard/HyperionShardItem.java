package dev.vaileks.altarweaponsvl.items.shards.hyperionshard;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.utils.TextUtils;
import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HyperionShardItem {
    private static String tr(String ru, String en) {
        return AltarWeaponsVL.getInstance().tr(ru, en);
    }

    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(Integer.valueOf(3));
            meta.setTooltipStyle(NamespacedKey.minecraft((String)"gold"));
            MiniMessage mm = MiniMessage.miniMessage();
            meta.displayName(TextUtils.shadow(mm.deserialize("<!italic><gold>" + HyperionShardItem.tr("Гиперионовый Осколок", "ʜʏᴘᴇʀɪᴏɴ sʜᴀʀᴅ") + "</gold>")));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><yellow>" + HyperionShardItem.tr("Сияющий осколок, наполненный", "A radiant shard filled with") + "</yellow>"));
            lore.add(mm.deserialize("<!italic><yellow>" + HyperionShardItem.tr("священной и божественной силой.", "sacred and divine power.") + "</yellow>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><white>" + HyperionShardItem.tr("Применение:", "Use:") + "</white>"));
            lore.add(mm.deserialize("<!italic><dark_gray>•</dark_gray> <gray>" + HyperionShardItem.tr("Используется для создания", "Used to create") + "</gray>"));
            lore.add(mm.deserialize("<!italic><gray>" + HyperionShardItem.tr("легендарного оружия.", "legendary weapons.") + "</gray>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><dark_gray><i>" + HyperionShardItem.tr("(( Материал для крафта ))", "(( Crafting material ))") + "</i></dark_gray>"));
            meta.lore(lore.stream().map(TextUtils::shadow).toList());
            dev.vaileks.altarweaponsvl.integration.CommonItems.tag(meta, "hyperion_shard");
        item.setItemMeta(meta);
        }
        return item;
    }
}

