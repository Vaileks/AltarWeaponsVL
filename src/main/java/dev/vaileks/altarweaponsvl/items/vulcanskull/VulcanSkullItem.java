package dev.vaileks.altarweaponsvl.items.vulcanskull;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.utils.TextUtils;
import java.util.ArrayList;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class VulcanSkullItem {
    private static String tr(String ru, String en) {
        return AltarWeaponsVL.getInstance().tr(ru, en);
    }

    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.CLAY_BALL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(Integer.valueOf(3));
            meta.setTooltipStyle(NamespacedKey.minecraft((String)"vulcan"));
            MiniMessage mm = MiniMessage.miniMessage();
            meta.displayName(TextUtils.shadow(mm.deserialize("<!italic><gradient:#ff5555:#aa0000:#ff5555>" + VulcanSkullItem.tr("Череп Вулкана", "ᴠᴜʟᴄᴀɴ's sᴋᴜʟʟ") + "</gradient>")));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><#ff5555>" + VulcanSkullItem.tr("Пылающий череп древнего божества,", "The blazing skull of an ancient deity,") + "</#ff5555>"));
            lore.add(mm.deserialize("<!italic><#ff5555>" + VulcanSkullItem.tr("источающий невыносимый жар.", "radiating unbearable heat.") + "</#ff5555>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><white>" + VulcanSkullItem.tr("Как добыть:", "How to obtain:") + "</white>"));
            lore.add(mm.deserialize("<!italic><dark_gray>•</dark_gray> <gray>" + VulcanSkullItem.tr("Создается на", "Crafted at a") + "</gray> <gold>" + VulcanSkullItem.tr("Верстаке", "crafting table") + "</gold>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><white>" + VulcanSkullItem.tr("Применение:", "Use:") + "</white>"));
            lore.add(mm.deserialize("<!italic><dark_gray>•</dark_gray> <gray>" + VulcanSkullItem.tr("Используется для создания", "Used to create") + "</gray>"));
            lore.add(mm.deserialize("<!italic><gray>" + VulcanSkullItem.tr("других легендарных оружий.", "other legendary weapons.") + "</gray>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><dark_gray><i>" + VulcanSkullItem.tr("(( Материал для крафта ))", "(( Crafting material ))") + "</i></dark_gray>"));
            meta.lore(lore.stream().map(TextUtils::shadow).toList());
            NamespacedKey uuidKey = new NamespacedKey("runesaltarv", "unique_uuid");
            meta.getPersistentDataContainer().set(uuidKey, PersistentDataType.STRING, UUID.randomUUID().toString());
            dev.vaileks.altarweaponsvl.integration.CommonItems.tag(meta, "vulcan_skull");
        item.setItemMeta(meta);
        }
        return item;
    }
}

