package dev.vaileks.altarweaponsvl.items.weaponshandle;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.utils.TextUtils;
import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WeaponsHandleItem {
    private static String tr(String ru, String en) {
        return AltarWeaponsVL.getInstance().tr(ru, en);
    }

    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(Integer.valueOf(9));
            meta.setTooltipStyle(NamespacedKey.minecraft((String)"shard"));
            MiniMessage mm = MiniMessage.miniMessage();
            meta.displayName(TextUtils.shadow(mm.deserialize("<!italic><gradient:#ff5555:#aa0000:#ff5555>" + WeaponsHandleItem.tr("Рукоятка Оружия", "ᴡᴇᴀᴘᴏɴ ʜᴀɴᴅʟᴇ") + "</gradient>")));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><#ff5555>" + WeaponsHandleItem.tr("Прочная рукоять, выкованная из", "A sturdy handle forged from") + "</#ff5555>"));
            lore.add(mm.deserialize("<!italic><#ff5555>" + WeaponsHandleItem.tr("незерита и энергии звезды незера.", "netherite and the energy of a nether star.") + "</#ff5555>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><white>" + WeaponsHandleItem.tr("Как добыть:", "How to obtain:") + "</white>"));
            lore.add(mm.deserialize("<!italic><dark_gray>•</dark_gray> <gray>" + WeaponsHandleItem.tr("Создается на", "Crafted at a") + "</gray> <gold>" + WeaponsHandleItem.tr("Верстаке", "crafting table") + "</gold>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><white>" + WeaponsHandleItem.tr("Применение:", "Use:") + "</white>"));
            lore.add(mm.deserialize("<!italic><dark_gray>•</dark_gray> <gray>" + WeaponsHandleItem.tr("Служит идеальной основой для", "Serves as the perfect base for") + "</gray>"));
            lore.add(mm.deserialize("<!italic><gray>" + WeaponsHandleItem.tr("создания легендарных оружий.", "creating legendary weapons.") + "</gray>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><dark_gray><i>" + WeaponsHandleItem.tr("(( Материал для крафта ))", "(( Crafting material ))") + "</i></dark_gray>"));
            meta.lore(lore.stream().map(TextUtils::shadow).toList());
            dev.vaileks.altarweaponsvl.integration.CommonItems.tag(meta, "weapons_handle");
        item.setItemMeta(meta);
        }
        return item;
    }
}

