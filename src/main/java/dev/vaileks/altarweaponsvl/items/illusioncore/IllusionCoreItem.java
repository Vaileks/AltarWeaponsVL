package dev.vaileks.altarweaponsvl.items.illusioncore;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.utils.TextUtils;
import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class IllusionCoreItem {
    private static String tr(String ru, String en) {
        return AltarWeaponsVL.getInstance().tr(ru, en);
    }

    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(Integer.valueOf(7));
            meta.setTooltipStyle(NamespacedKey.minecraft((String)"purple"));
            MiniMessage mm = MiniMessage.miniMessage();
            meta.displayName(TextUtils.shadow(mm.deserialize("<!italic><gradient:#e64ce6:#990099:#e64ce6>" + IllusionCoreItem.tr("Ядро Иллюзий", "ɪʟʟᴜsɪᴏɴ ᴄᴏʀᴇ") + "</gradient>")));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><#d142f5>" + IllusionCoreItem.tr("Таинственное ядро, искажающее", "A mysterious core that twists") + "</#d142f5>"));
            lore.add(mm.deserialize("<!italic><#d142f5>" + IllusionCoreItem.tr("пространство и разум вокруг себя.", "space and the mind around it.") + "</#d142f5>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><white>" + IllusionCoreItem.tr("Как добыть:", "How to obtain:") + "</white>"));
            lore.add(mm.deserialize("<!italic><dark_gray>•</dark_gray> <gray>" + IllusionCoreItem.tr("Создается на", "Crafted at a") + "</gray> <gold>" + IllusionCoreItem.tr("Верстаке", "crafting table") + "</gold>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><white>" + IllusionCoreItem.tr("Применение:", "Use:") + "</white>"));
            lore.add(mm.deserialize("<!italic><dark_gray>•</dark_gray> <gray>" + IllusionCoreItem.tr("Наполняет легендарное оружие", "Fills legendary weapons with") + "</gray>"));
            lore.add(mm.deserialize("<!italic><gray>" + IllusionCoreItem.tr("мистической силой иллюзий.", "the mystical power of illusions.") + "</gray>"));
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><dark_gray><i>" + IllusionCoreItem.tr("(( Материал для крафта ))", "(( Crafting material ))") + "</i></dark_gray>"));
            meta.lore(lore.stream().map(TextUtils::shadow).toList());
            dev.vaileks.altarweaponsvl.integration.CommonItems.tag(meta, "illusion_core");
        item.setItemMeta(meta);
        }
        return item;
    }
}

