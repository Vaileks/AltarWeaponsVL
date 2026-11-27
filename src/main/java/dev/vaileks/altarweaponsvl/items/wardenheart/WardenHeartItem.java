package dev.vaileks.altarweaponsvl.items.wardenheart;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.utils.TextUtils;
import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WardenHeartItem {
    private static String tr(String ru, String en) {
        return AltarWeaponsVL.getInstance().tr(ru, en);
    }

    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(Integer.valueOf(1));
            meta.setTooltipStyle(NamespacedKey.minecraft((String)"ancient"));
            MiniMessage mm = MiniMessage.miniMessage();
            meta.displayName(TextUtils.shadow(mm.deserialize("<!italic><gradient:#0b4d53:#23c7c8:#0b4d53>" + WardenHeartItem.tr("Сердце Вардена", "ᴡᴀʀᴅᴇɴ ʜᴇᴀʀᴛ") + "</gradient>")));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><#169c9d>" + WardenHeartItem.tr("Редкий артефакт, пульсирующий", "A rare artifact pulsing with") + "</#169c9d>"));
            lore.add(mm.deserialize("<!italic><#169c9d>" + WardenHeartItem.tr("темной энергией скалка.", "the dark energy of the deep stone.") + "</#169c9d>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><white>" + WardenHeartItem.tr("Как добыть:", "How to obtain:") + "</white>"));
            lore.add(mm.deserialize("<!italic><dark_gray>•</dark_gray> <gray>" + WardenHeartItem.tr("Выпадает при убийстве", "Drops when killing the") + "</gray> <dark_aqua>" + WardenHeartItem.tr("Вардена", "warden") + "</dark_aqua>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><white>" + WardenHeartItem.tr("Применение:", "Use:") + "</white>"));
            lore.add(mm.deserialize("<!italic><dark_gray>•</dark_gray> <gray>" + WardenHeartItem.tr("Используется для создания", "Used to create") + "</gray>"));
            lore.add(mm.deserialize("<!italic><gray>" + WardenHeartItem.tr("других легендарных оружий.", "other legendary weapons.") + "</gray>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><dark_gray><i>" + WardenHeartItem.tr("(( Материал для крафта ))", "(( Crafting material ))") + "</i></dark_gray>"));
            meta.lore(lore.stream().map(TextUtils::shadow).toList());
            dev.vaileks.altarweaponsvl.integration.CommonItems.tag(meta, "warden_heart");
        item.setItemMeta(meta);
        }
        return item;
    }
}

