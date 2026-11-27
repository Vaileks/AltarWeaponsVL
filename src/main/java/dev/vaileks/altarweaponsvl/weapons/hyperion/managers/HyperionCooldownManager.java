package dev.vaileks.altarweaponsvl.weapons.hyperion.managers;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.utils.TextUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class HyperionCooldownManager {
   private static final Map<UUID, Long> scorchingCooldowns = new dev.vaileks.altarweaponsvl.integration.ObservedCooldownMap<>("hyperion","scorching");
   private static final Map<UUID, Long> holyLanceCooldowns = new dev.vaileks.altarweaponsvl.integration.ObservedCooldownMap<>("hyperion","holyLance");
   private static final Map<UUID, BossBar> scorchingBars = new HashMap<>();
   private static final Map<UUID, BossBar> lanceBars = new HashMap<>();

   public static boolean isOnScorchingCooldown(Player p) {
      return scorchingCooldowns.containsKey(p.getUniqueId()) && scorchingCooldowns.get(p.getUniqueId()) > System.currentTimeMillis();
   }

   public static void setScorchingCooldown(Player p) {
      long timeMs = Math.max(1,AltarWeaponsVL.getInstance().getWeaponsConfig().getInt("hyperion.scorching-blade.cooldown",30))*1000L;
      scorchingCooldowns.put(p.getUniqueId(), System.currentTimeMillis() + timeMs);
      startBossBar(p, "scorching", AltarWeaponsVL.getInstance().tr("§eОбжигающий клинок", "§e sᴄᴏʀᴄʜɪɴɢ ʙʟᴀᴅᴇ"), timeMs);
   }

   public static boolean isOnHolyLanceCooldown(Player p) {
      return holyLanceCooldowns.containsKey(p.getUniqueId()) && holyLanceCooldowns.get(p.getUniqueId()) > System.currentTimeMillis();
   }

   public static void setHolyLanceCooldown(Player p) {
      long timeMs = Math.max(1,AltarWeaponsVL.getInstance().getWeaponsConfig().getInt("hyperion.holy-lance.cooldown",60))*1000L;
      holyLanceCooldowns.put(p.getUniqueId(), System.currentTimeMillis() + timeMs);
      startBossBar(p, "lance", AltarWeaponsVL.getInstance().tr("§eСвятое Копьё", "§e ʜᴏʟʏ ʟᴀɴᴄᴇ"), timeMs);
   }

   private static void startBossBar(final Player p, String type, String title, final long maxTimeMs) {
      final UUID id = p.getUniqueId();
      final Map<UUID, BossBar> activeBars = type.equals("scorching") ? scorchingBars : lanceBars;
      if (activeBars.containsKey(id)) {
         activeBars.get(id).removeAll();
         activeBars.remove(id);
      }

      final BossBar bar = dev.vaileks.altarweaponsvl.integration.AltarCooldowns.cooldownBar(ChatColor.YELLOW + ChatColor.stripColor(title), BarColor.YELLOW, BarStyle.SOLID);
      bar.addPlayer(p);
      activeBars.put(id, bar);
      long startTime = System.currentTimeMillis();
      final long endTime = startTime + maxTimeMs;
      (new BukkitRunnable() {
         @Override
         public void run() {
            if (p.isOnline() && activeBars.containsKey(id) && activeBars.get(id) == bar) {
               long now = System.currentTimeMillis();
               if (now >= endTime) {
                  bar.removeAll();
                  activeBars.remove(id);
                  this.cancel();
               } else {
                  double progress = (double)(endTime - now) / (double)maxTimeMs;
                  bar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
               }
            } else {
               bar.removeAll();
               activeBars.remove(id, bar);
               this.cancel();
            }
         }
      }).runTaskTimer(AltarWeaponsVL.getInstance(), 0L, 2L);
   }

   public static void clearAllBars() {
      for (BossBar bar : scorchingBars.values()) {
         bar.removeAll();
      }

      for (BossBar bar : lanceBars.values()) {
         bar.removeAll();
      }

      scorchingBars.clear();
      lanceBars.clear();
   }

   public static void resetPlayer(Player player) {
      UUID uuid = player.getUniqueId();
      scorchingCooldowns.remove(uuid);
      holyLanceCooldowns.remove(uuid);
      BossBar scorchingBar = scorchingBars.remove(uuid);
      BossBar lanceBar = lanceBars.remove(uuid);
      if (scorchingBar != null) {
         scorchingBar.removeAll();
      }

      if (lanceBar != null) {
         lanceBar.removeAll();
      }
   }
}
