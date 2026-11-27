package dev.vaileks.altarweaponsvl.items.wardenheart.listeners;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.items.wardenheart.WardenHeartItem;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class WardenHeartDropListener
implements Listener {
    private final AltarWeaponsVL plugin;

    public WardenHeartDropListener(AltarWeaponsVL plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWardenDeath(EntityDeathEvent e) {
        if (e.getEntity().getType() != EntityType.WARDEN) {
            return;
        }
        if (!this.plugin.getItemsConfig().getBoolean("items.warden-heart.drop.enabled", true)) {
            return;
        }
        int amount = Math.max(0, this.plugin.getItemsConfig().getInt("items.warden-heart.drop.amount", 1));
        for (int i = 0; i < amount; ++i) {
            e.getDrops().add(WardenHeartItem.create());
        }
    }
}

