package dev.vaileks.altarweaponsvl.weapons.nightpiercer.tasks;
import dev.vaileks.altarweaponsvl.integration.AltarTuning;

import dev.vaileks.altarweaponsvl.utils.WeaponFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class NightPassiveTask extends BukkitRunnable {
   private boolean isNightpiercer(ItemStack item) {
      return WeaponFactory.isAltarWeapon(item, "nightpiercer");
   }

   @Override
   public void run() {
      for (Player p : Bukkit.getOnlinePlayers()) {
         if (this.isNightpiercer(p.getInventory().getItemInMainHand())) {
            long time = p.getWorld().getTime();
            if (time >= 13000L && time <= 23000L) {
               p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, AltarTuning.integer("nightpiercer","tuning.night-passive-task.effects.regeneration.duration-ticks",50,1,72000), AltarTuning.integer("nightpiercer","tuning.night-passive-task.effects.regeneration.amplifier",0,0,10), false, false, false));
            }
         }
      }
   }
}
