package dev.vaileks.altarweaponsvl;
import dev.vaileks.altarweaponsvl.integration.AltarTuning;

import dev.vaileks.altarweaponsvl.commands.AltarLegendaryCommand;
import dev.vaileks.altarweaponsvl.commands.FriendListCommand;
import dev.vaileks.altarweaponsvl.commands.FriendListTabCompleter;
import dev.vaileks.altarweaponsvl.integrations.worldguard.WorldGuardAbilityGate;
import dev.vaileks.altarweaponsvl.integrations.worldguard.WorldGuardManager;
import dev.vaileks.altarweaponsvl.listeners.ItemProtectionListener;
import dev.vaileks.altarweaponsvl.managers.FriendManager;
import dev.vaileks.altarweaponsvl.managers.VisualCleanupManager;
import dev.vaileks.altarweaponsvl.utils.CleanDamageManager;
import dev.vaileks.altarweaponsvl.utils.SpeedBuffUtils;
import dev.vaileks.altarweaponsvl.utils.TextUtils;
import dev.vaileks.altarweaponsvl.utils.WeaponFactory;
import dev.vaileks.altarweaponsvl.weapons.bloodlust.listeners.BloodLustListener;
import dev.vaileks.altarweaponsvl.weapons.bloodlust.managers.BloodAbilityManager;
import dev.vaileks.altarweaponsvl.weapons.bloodlust.managers.BloodBossBarManager;
import dev.vaileks.altarweaponsvl.weapons.bloodlust.tasks.PassiveAuraTask;
import dev.vaileks.altarweaponsvl.weapons.boneblade.listeners.BladeListener;
import dev.vaileks.altarweaponsvl.weapons.boneblade.managers.BoneCooldownManager;
import dev.vaileks.altarweaponsvl.weapons.crazyslots.listeners.CrazySlotsListener;
import dev.vaileks.altarweaponsvl.weapons.crazyslots.managers.CrazySlotsManager;
import dev.vaileks.altarweaponsvl.weapons.cutlass.listeners.CutlassListener;
import dev.vaileks.altarweaponsvl.weapons.cutlass.managers.CutlassManager;
import dev.vaileks.altarweaponsvl.weapons.earthgauntlet.listeners.EarthGauntletListener;
import dev.vaileks.altarweaponsvl.weapons.earthgauntlet.managers.EarthGauntletManager;
import dev.vaileks.altarweaponsvl.weapons.frostscythe.listeners.FrostListener;
import dev.vaileks.altarweaponsvl.weapons.frostscythe.managers.FrostAbilityManager;
import dev.vaileks.altarweaponsvl.weapons.frostscythe.managers.FrostBossBarManager;
import dev.vaileks.altarweaponsvl.weapons.hyperion.listeners.HyperionListener;
import dev.vaileks.altarweaponsvl.weapons.hyperion.managers.HyperionCooldownManager;
import dev.vaileks.altarweaponsvl.weapons.knightfall.listeners.KnightfallListener;
import dev.vaileks.altarweaponsvl.weapons.knightfall.managers.KnightfallManager;
import dev.vaileks.altarweaponsvl.weapons.knightfall.tasks.KnightfallPassiveTask;
import dev.vaileks.altarweaponsvl.weapons.nightpiercer.listeners.NightAbilityListener;
import dev.vaileks.altarweaponsvl.weapons.nightpiercer.managers.NightBossBarCooldown;
import dev.vaileks.altarweaponsvl.weapons.nightpiercer.managers.NightHealthStealManager;
import dev.vaileks.altarweaponsvl.weapons.nightpiercer.tasks.NightPassiveTask;
import dev.vaileks.altarweaponsvl.weapons.paladinsbattleaxe.listeners.PaladinsBattleAxeListener;
import dev.vaileks.altarweaponsvl.weapons.paladinsbattleaxe.managers.PaladinsBattleAxeManager;
import dev.vaileks.altarweaponsvl.weapons.palegun.listeners.PaleGunListener;
import dev.vaileks.altarweaponsvl.weapons.palegun.managers.PaleGunAbilityManager;
import dev.vaileks.altarweaponsvl.weapons.pureblade.listeners.PureBladeListener;
import dev.vaileks.altarweaponsvl.weapons.pureblade.managers.PureBladeManager;
import dev.vaileks.altarweaponsvl.weapons.pureblade.managers.PureBossBarManager;
import dev.vaileks.altarweaponsvl.weapons.shadowblade.listeners.ShadowBladeListener;
import dev.vaileks.altarweaponsvl.weapons.shadowblade.managers.ShadowBladeManager;
import dev.vaileks.altarweaponsvl.weapons.shadowblade.tasks.ShadowBladePassiveTask;
import dev.vaileks.altarweaponsvl.weapons.vulcan.listeners.VulcanHitListener;
import dev.vaileks.altarweaponsvl.weapons.vulcan.listeners.VulcanShootListener;
import dev.vaileks.altarweaponsvl.weapons.vulcan.managers.VulcanChargeManager;
import dev.vaileks.altarweaponsvl.weapons.vulcan.managers.VulcanCooldownManager;
import dev.vaileks.altarweaponsvl.weapons.windweaver.listeners.WindWeaverListener;
import dev.vaileks.altarweaponsvl.weapons.witherblade.listeners.WitherBladeListener;
import dev.vaileks.altarweaponsvl.weapons.witherblade.managers.WitherManager;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AltarWeaponsVL extends JavaPlugin {
   private static AltarWeaponsVL instance;
   private NamespacedKey killsKey;
   private NamespacedKey vulcanKey;
   private NamespacedKey arrowKey;
   private NamespacedKey paleGunKey;
   private NamespacedKey hyperionKey;
   private NamespacedKey witherKey;
   private NamespacedKey paladinsBattleAxeKey;
   private NamespacedKey knightfallTierKey;
   private PaladinsBattleAxeManager paladinsBattleAxeManager;
   private PaladinsBattleAxeListener paladinsBattleAxeListener;
   private CrazySlotsManager crazySlotsManager;
   private CrazySlotsListener crazySlotsListener;
   private WorldGuardManager worldGuardManager;
   private VisualCleanupManager visualCleanupManager;
   private FriendManager friendManager;
   private CleanDamageManager cleanDamageManager;
   private EarthGauntletManager earthGauntletManager;
   private CutlassManager cutlassManager;
   private BoneCooldownManager boneCooldownManager;
   private BloodAbilityManager bloodAbilityManager;
   private BloodBossBarManager bloodBossBarManager;
   private NightAbilityListener nightAbilityListener;
   private NightHealthStealManager nightHealthManager;
   private FrostBossBarManager frostBossBarManager;
   private FrostAbilityManager frostAbilityManager;
   private PaleGunAbilityManager paleGunAbilityManager;
   private KnightfallManager knightfallManager;
   private PureBossBarManager pureBossBarManager;
   private PureBladeManager pureBladeManager;
   private ShadowBladeManager shadowBladeManager;
   private WindWeaverListener windWeaverListener;
   private WitherManager witherManager;
   private dev.vaileks.altarweaponsvl.altars.AltarManager altarManager;
   private dev.vaileks.altarweaponsvl.altars.AltarLanguageManager altarLanguageManager;
   private dev.vaileks.altarweaponsvl.altars.commands.EditAltarCommand editAltarCommand;
   private dev.vaileks.altarweaponsvl.items.copperpickaxe.listeners.CopperPickaxeListener copperPickaxeListener;
   private FileConfiguration altarsConfig;
   private FileConfiguration itemsConfig;
   private FileConfiguration weaponsConfig;
   private FileConfiguration modelsConfig;

   @Override
   public void onLoad() {
      WorldGuardManager.registerFlag();
   }

   @Override
   public void onEnable() {
      instance = this;
      migrateLegacyData();
      this.saveDefaultConfig();
      dev.vaileks.altarweaponsvl.integration.AltarPresets.load(this);
      this.saveDefaultWeaponsConfig();
      this.saveDefaultModelsConfig();
      this.killsKey = new NamespacedKey("runesaltarv", "bloodlust_kills");
      this.vulcanKey = new NamespacedKey("runesaltarv", "vulcan_crossbow");
      this.arrowKey = new NamespacedKey("runesaltarv", "vulcan_arrow");
      this.paleGunKey = new NamespacedKey("runesaltarv", "pale_gun");
      this.hyperionKey = new NamespacedKey("runesaltarv", "hyperion_sword");
      this.witherKey = new NamespacedKey("runesaltarv", "wither_blade");
      this.knightfallTierKey = new NamespacedKey("runesaltarv", "knightfall_tier");
      this.visualCleanupManager = new VisualCleanupManager(this);
      this.worldGuardManager = new WorldGuardManager(this);
      this.getServer().getPluginManager().registerEvents(new WorldGuardAbilityGate(this, this.worldGuardManager), this);
      this.paladinsBattleAxeKey = new NamespacedKey("runesaltarv", "paladins_battle_axe");
      this.paladinsBattleAxeManager = new PaladinsBattleAxeManager(this);
      this.paladinsBattleAxeListener = new PaladinsBattleAxeListener(this, this.paladinsBattleAxeManager);
      this.getServer().getPluginManager().registerEvents(this.paladinsBattleAxeListener, this);
      this.crazySlotsManager = new CrazySlotsManager(this);
      this.crazySlotsListener = new CrazySlotsListener(this, this.crazySlotsManager);
      this.getServer().getPluginManager().registerEvents(this.crazySlotsListener, this);
      this.friendManager = new FriendManager(this);
      this.cleanDamageManager = new CleanDamageManager(this);
      this.earthGauntletManager = new EarthGauntletManager(this);
      this.cutlassManager = new CutlassManager(this);
      this.boneCooldownManager = new BoneCooldownManager(this);
      this.bloodBossBarManager = new BloodBossBarManager(this);
      this.bloodAbilityManager = new BloodAbilityManager(this, this.bloodBossBarManager);
      this.nightAbilityListener = new NightAbilityListener(this);
      this.nightHealthManager = new NightHealthStealManager(this);
      this.knightfallManager = new KnightfallManager(this);
      this.shadowBladeManager = new ShadowBladeManager(this);
      this.witherManager = new WitherManager(this);
      this.windWeaverListener = new WindWeaverListener(this);
      this.frostBossBarManager = new FrostBossBarManager(this);
      this.frostAbilityManager = new FrostAbilityManager(this, this.frostBossBarManager);
      this.paleGunAbilityManager = new PaleGunAbilityManager(this);
      this.pureBossBarManager = new PureBossBarManager(this);
      this.pureBladeManager = new PureBladeManager(this, this.pureBossBarManager);
      PluginCommand cmd = this.getCommand("altarweaponsvl");
      if (cmd != null) {
         var commands = new AltarLegendaryCommand(this);
         cmd.setExecutor(commands);
         cmd.setTabCompleter(commands);
         this.getServer().getPluginManager().registerEvents(commands, this);
      }

      PluginCommand friendCmd = this.getCommand("friendlist");
      if (friendCmd != null) {
         friendCmd.setExecutor(new FriendListCommand(this));
         friendCmd.setTabCompleter(new FriendListTabCompleter(this));
      }

      this.getServer().getPluginManager().registerEvents(new BladeListener(this), this);
      this.getServer().getPluginManager().registerEvents(new BloodLustListener(this, this.bloodAbilityManager), this);
      new PassiveAuraTask(this).runTaskTimer(this, 0L, 1L);
      this.getServer().getPluginManager().registerEvents(this.nightAbilityListener, this);
      new NightPassiveTask().runTaskTimer(this, 0L, 20L);
      this.getServer().getPluginManager().registerEvents(new VulcanShootListener(), this);
      this.getServer().getPluginManager().registerEvents(new VulcanHitListener(), this);
      this.getServer().getPluginManager().registerEvents(new PaleGunListener(this, this.paleGunAbilityManager), this);
      this.getServer().getPluginManager().registerEvents(new ItemProtectionListener(this), this);
      this.getServer().getPluginManager().registerEvents(new FrostListener(this.frostAbilityManager), this);
      this.getServer().getPluginManager().registerEvents(new PureBladeListener(this.pureBladeManager), this);
      this.getServer().getPluginManager().registerEvents(new KnightfallListener(this, this.knightfallManager), this);
      new KnightfallPassiveTask(this).runTaskTimer(this, 0L, 1L);
      this.getServer().getPluginManager().registerEvents(new ShadowBladeListener(this, this.shadowBladeManager), this);
      new ShadowBladePassiveTask().runTaskTimer(this, 0L, 1L);
      this.getServer().getPluginManager().registerEvents(this.windWeaverListener, this);
      this.getServer().getPluginManager().registerEvents(new HyperionListener(), this);
      this.getServer().getPluginManager().registerEvents(new WitherBladeListener(this, this.witherManager), this);
      this.getServer().getPluginManager().registerEvents(new EarthGauntletListener(this, this.earthGauntletManager), this);
      this.getServer().getPluginManager().registerEvents(new CutlassListener(this.cutlassManager), this);
      this.getServer().getPluginManager().registerEvents(new SpeedBuffUtils(), this);
      (new BukkitRunnable() {
            @Override
            public void run() {
               for (Player p : AltarWeaponsVL.this.getServer().getOnlinePlayers()) {
                  ItemStack item = p.getInventory().getItemInMainHand();
                  if (WeaponFactory.isAltarWeapon(item, "knightfall")) {
                     int kills = item.getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(AltarWeaponsVL.this.killsKey, PersistentDataType.INTEGER, Integer.valueOf(0));
                     int cmd = item.getItemMeta().hasCustomModelData() ? item.getItemMeta().getCustomModelData() : 0;
                     boolean hasDensity = item.getItemMeta().hasEnchant(Enchantment.DENSITY);
                     int tier = item.getItemMeta()
                        .getPersistentDataContainer()
                        .getOrDefault(
                           AltarWeaponsVL.this.knightfallTierKey,
                           PersistentDataType.INTEGER,
                           Integer.valueOf(cmd >= 3003 || cmd == 3 ? 3 : (hasDensity ? 2 : (cmd < 3002 && cmd != 2 ? 0 : 1)))
                        );
                     if (kills >= AltarTuning.integer("knightfall","abilities.progression.tier-1-kills",4,1,1000) && tier < 1 || kills >= AltarTuning.integer("knightfall","abilities.progression.tier-2-kills",8,1,1000) && tier < 2 || kills >= AltarTuning.integer("knightfall","abilities.progression.tier-3-kills",10,1,1000) && tier < 3) {
                        TextUtils.sendActionBar(
                           p,
                           TextUtils.legacy(
                              AltarWeaponsVL.this.tr("&aНажмите &2[Смена руки] &aчтобы улучшить оружие.", "&aPress &2[OffHand] &ato upgrade the weapon.")
                           )
                        );
                     }
                  }
               }
            }
         })
         .runTaskTimer(this, 0L, 20L);
      dev.vaileks.altarweaponsvl.integration.AltarIntegration.enable(this);
      enableCrafting();
      this.getLogger().info("AltarWeaponsVL enabled");
   }

   public void saveDefaultWeaponsConfig() {
      this.weaponsConfig=dev.vaileks.altarweaponsvl.integration.AltarPresets.mechanics();
   }

   public void reloadWeaponsConfig() {
      this.weaponsConfig=dev.vaileks.altarweaponsvl.integration.AltarPresets.mechanics();
   }

   public FileConfiguration getWeaponsConfig() {
      if (this.weaponsConfig == null) {
         this.reloadWeaponsConfig();
      }

      return this.weaponsConfig;
   }

   @Override
   public void onDisable() {
      if (altarManager != null) { altarManager.saveAltars(); altarManager.cleanupEntities(); }
      if (copperPickaxeListener != null) copperPickaxeListener.cleanup();
      for (String recipe : List.of("weapons_handle", "illusion_core", "vulcan_skull")) Bukkit.removeRecipe(new NamespacedKey("runesaltarv", recipe));
      dev.vaileks.altarweaponsvl.integration.AltarIntegration.disable();
      if (this.boneCooldownManager != null) {
         this.boneCooldownManager.removeAllBars();
      }

      if (this.bloodBossBarManager != null) {
         this.bloodBossBarManager.clearAll();
      }

      if (this.bloodAbilityManager != null) {
         this.bloodAbilityManager.cleanup();
      }

      if (this.nightHealthManager != null) {
         this.nightHealthManager.revertAll();
      }

      NightBossBarCooldown.clearAll();
      if (this.pureBossBarManager != null) {
         this.pureBossBarManager.clearAll();
      }

      if (this.frostBossBarManager != null) {
         this.frostBossBarManager.clearAll();
      }

      if (this.paleGunAbilityManager != null) {
         this.paleGunAbilityManager.clearAll();
      }

      if (this.windWeaverListener != null) {
         this.windWeaverListener.removeAllBars();
      }

      if (this.knightfallManager != null) {
         this.knightfallManager.clearAllBars();
      }

      if (this.shadowBladeManager != null) {
         this.shadowBladeManager.clearAllBars();
      }

      if (this.witherManager != null) {
         this.witherManager.clearAllBars();
      }

      if (this.earthGauntletManager != null) {
         this.earthGauntletManager.cleanupAll();
      }

      if (this.friendManager != null) {
         this.friendManager.saveFriends();
      }

      if (this.cutlassManager != null) {
         this.cutlassManager.cleanupAll();
      }

      if (this.visualCleanupManager != null) {
         this.visualCleanupManager.cleanupAll();
      }

      if (this.paladinsBattleAxeManager != null) {
         this.paladinsBattleAxeManager.clearAllBars();
      }

      if (this.crazySlotsManager != null) {
         this.crazySlotsManager.forceRevertAll();
      }

      if (this.cleanDamageManager != null) {
         this.cleanDamageManager.clear();
      }

      SpeedBuffUtils.cleanupAll();
      TextUtils.clearBossBars();
      VulcanCooldownManager.clearAllBars();
      VulcanChargeManager.cancelAll();
      HyperionCooldownManager.clearAllBars();

      this.getLogger().info("AltarWeaponsVL выключен!");
   }

   public static AltarWeaponsVL getInstance() {
      return instance;
   }

   public NamespacedKey getKillsKey() {
      return this.killsKey;
   }

   public NamespacedKey getVulcanKey() {
      return this.vulcanKey;
   }

   public NamespacedKey getArrowKey() {
      return this.arrowKey;
   }

   public NamespacedKey getPaleGunKey() {
      return this.paleGunKey;
   }

   public NamespacedKey getHyperionKey() {
      return this.hyperionKey;
   }

   public NamespacedKey getWitherKey() {
      return this.witherKey;
   }

   public NamespacedKey getKnightfallTierKey() {
      return this.knightfallTierKey;
   }

   public FriendManager getFriendManager() {
      return this.friendManager;
   }

   public CleanDamageManager getCleanDamageManager() {
      return this.cleanDamageManager;
   }

   public EarthGauntletManager getEarthGauntletManager() {
      return this.earthGauntletManager;
   }

   public CutlassManager getCutlassManager() {
      return this.cutlassManager;
   }

   public BoneCooldownManager getBoneCooldownManager() {
      return this.boneCooldownManager;
   }

   public BloodAbilityManager getBloodAbilityManager() {
      return this.bloodAbilityManager;
   }

   public BloodBossBarManager getBloodBossBarManager() {
      return this.bloodBossBarManager;
   }

   public NightAbilityListener getNightAbilityListener() {
      return this.nightAbilityListener;
   }

   public NightHealthStealManager getNightHealthManager() {
      return this.nightHealthManager;
   }

   public PureBladeManager getPureBladeManager() {
      return this.pureBladeManager;
   }

   public WindWeaverListener getWindWeaverListener() {
      return this.windWeaverListener;
   }

   public boolean isEnglish() {
      return "en_US".equalsIgnoreCase(this.getConfig().getString("lang", "en_US"));
   }

   public String tr(String ru, String en) {
      return this.isEnglish() ? en : ru;
   }

   public boolean isLimitsEnabled() {
      return this.getConfig().getBoolean("limits.enabled", false);
   }

   public double getLegendaryMaxY() {
      return this.getConfig().getDouble("limits.max-y-height", 200.0);
   }

   public boolean isAboveLegendaryHeight(Player player) {
      return this.isLimitsEnabled() && player.getLocation().getY() > this.getLegendaryMaxY();
   }

   public void resetAllCooldowns(Player player) {
      dev.vaileks.altarweaponsvl.integration.AltarCooldowns.clear(player);
      if (this.boneCooldownManager != null) {
         this.boneCooldownManager.resetPlayer(player);
      }

      if (this.bloodBossBarManager != null) {
         this.bloodBossBarManager.resetPlayer(player);
      }

      if (this.nightAbilityListener != null) {
         this.nightAbilityListener.resetCooldowns(player);
      }

      NightBossBarCooldown.resetPlayer(player);
      if (this.frostBossBarManager != null) {
         this.frostBossBarManager.resetPlayer(player);
      }

      if (this.paleGunAbilityManager != null) {
         this.paleGunAbilityManager.resetPlayer(player);
      }

      if (this.pureBladeManager != null) {
         this.pureBladeManager.resetCooldowns(player);
      }

      if (this.knightfallManager != null) {
         this.knightfallManager.resetCooldowns(player);
      }

      if (this.shadowBladeManager != null) {
         this.shadowBladeManager.resetCooldowns(player);
      }

      if (this.witherManager != null) {
         this.witherManager.resetCooldowns(player);
      }

      if (this.earthGauntletManager != null) {
         this.earthGauntletManager.resetCooldowns(player);
      }

      if (this.cutlassManager != null) {
         this.cutlassManager.resetCooldowns(player);
      }

      if (this.windWeaverListener != null) {
         this.windWeaverListener.resetPlayer(player);
      }

      VulcanCooldownManager.resetPlayer(player);
      HyperionCooldownManager.resetPlayer(player);
      if (this.paladinsBattleAxeManager != null) {
         this.paladinsBattleAxeManager.resetCooldowns(player);
      }

      if (this.crazySlotsManager != null) {
         this.crazySlotsManager.resetPlayer(player);
      }

      TextUtils.removePlayerFromAllBossBars(player);
   }

   public void saveDefaultModelsConfig() { reloadModelsConfig(); }
   public void reloadModelsConfig() {
      this.modelsConfig = new YamlConfiguration();
      this.modelsConfig.set("symbols.crouch-rmb", "Shift + RMB");
      this.modelsConfig.set("symbols.charges-icon", "●");
      dev.vaileks.altarweaponsvl.integration.AltarPresets.models(this.modelsConfig);
   }
   public FileConfiguration getModelsConfig() {
      if (this.modelsConfig == null) {
         this.reloadModelsConfig();
      }

      return this.modelsConfig;
   }

   public NamespacedKey getPaladinsBattleAxeKey() {
      return this.paladinsBattleAxeKey;
   }

   public PaladinsBattleAxeManager getPaladinsBattleAxeManager() {
      return this.paladinsBattleAxeManager;
   }

   public CrazySlotsManager getCrazySlotsManager() {
      return this.crazySlotsManager;
   }

   public int getKnightfallCharges(Player player){return this.knightfallManager.getCharges(player);}

   public WorldGuardManager getWorldGuardManager() {
      return this.worldGuardManager;
   }

   public VisualCleanupManager getVisualCleanupManager() {
      return this.visualCleanupManager;
   }

   public PaleGunAbilityManager getPaleGunAbilityManager() {
      return this.paleGunAbilityManager;
   }

   private void migrateLegacyData() {
      java.nio.file.Path legacy = getDataFolder().toPath().resolveSibling("RunesAltarV");
      try {
         DataMigration.copyMissing(legacy, getDataFolder().toPath());
      } catch (java.io.IOException e) {
         throw new IllegalStateException("Cannot migrate RunesAltarV data; startup stopped to protect saved configuration", e);
      }
   }

   private FileConfiguration loadSettings(String name) {
      File file = new File(getDataFolder(), name);
      if (!file.exists()) saveResource(name, false);
      var config = YamlConfiguration.loadConfiguration(file);
      try (var reader = new java.io.InputStreamReader(java.util.Objects.requireNonNull(getResource(name)), java.nio.charset.StandardCharsets.UTF_8)) {
         config.setDefaults(YamlConfiguration.loadConfiguration(reader));
      } catch (java.io.IOException e) { throw new IllegalStateException("Cannot load " + name, e); }
      return config;
   }

   public void reloadCrafting() {
      reloadAltarsConfig(); itemsConfig = loadSettings("crafting.yml");
      altarLanguageManager.reload(); registerRecipes();
      for (int id : altarManager.getAltarsIds()) altarManager.getAltar(id).spawnEntities();
   }

   private void enableCrafting() {
      reloadAltarsConfig(); itemsConfig = loadSettings("crafting.yml");
      altarLanguageManager = new dev.vaileks.altarweaponsvl.altars.AltarLanguageManager(this);
      altarLanguageManager.reload();
      altarManager = new dev.vaileks.altarweaponsvl.altars.AltarManager(this);
      altarManager.loadAltars();
      editAltarCommand = new dev.vaileks.altarweaponsvl.altars.commands.EditAltarCommand(this);
      getServer().getPluginManager().registerEvents(new dev.vaileks.altarweaponsvl.altars.listeners.AltarListener(this), this);
      getServer().getPluginManager().registerEvents(new dev.vaileks.altarweaponsvl.altars.listeners.AltarBlockListener(this), this);
      getServer().getPluginManager().registerEvents(new dev.vaileks.altarweaponsvl.altars.listeners.AltarChunkListener(this), this);
      getServer().getPluginManager().registerEvents(new dev.vaileks.altarweaponsvl.items.wardenheart.listeners.WardenHeartDropListener(this), this);
      copperPickaxeListener = new dev.vaileks.altarweaponsvl.items.copperpickaxe.listeners.CopperPickaxeListener(this);
      getServer().getPluginManager().registerEvents(copperPickaxeListener, this);
      registerRecipes();
      Map<Integer, Boolean> cooling = new HashMap<>();
      Bukkit.getScheduler().runTaskTimer(this, () -> {
         for (int id : altarManager.getAltarsIds()) {
            var altar = altarManager.getAltar(id);
            if (!altar.isChunkLoaded()) continue;
            boolean cooldown = altar.getCooldownRemaining() > 0;
            if (cooldown) altar.updateRecipeDisplay();
            else if (cooling.getOrDefault(id, false)) {
               altar.spawnEntities();
               var loc = altar.getLocation();
               var message = altarLanguageManager.component("altar.ready_prefix")
                   .append(Component.text(loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ()))
                   .append(altarLanguageManager.component("altar.ready_suffix"));
               Bukkit.broadcast(message);
            }
            cooling.put(id, cooldown);
         }
         cooling.keySet().retainAll(altarManager.getAltarsIds());
      }, 20, 20);
      Bukkit.getScheduler().runTaskTimer(this, () -> {
         for (int id : altarManager.getAltarsIds()) altarManager.getAltar(id).rotateItemDisplay();
      }, 2, 2);
   }

   public dev.vaileks.altarweaponsvl.altars.AltarManager getAltarManager() { return altarManager; }
   public dev.vaileks.altarweaponsvl.altars.AltarLanguageManager getAltarLanguageManager() { return altarLanguageManager; }
   public dev.vaileks.altarweaponsvl.altars.commands.EditAltarCommand getEditAltarCommand() { return editAltarCommand; }
   public FileConfiguration getAltarsConfig() { return altarsConfig; }
   public FileConfiguration getItemsConfig() { return itemsConfig; }
   public void reloadAltarsConfig() { altarsConfig = loadSettings("altars.yml"); }
   public void saveAltarsConfig() {
      try { altarsConfig.save(new File(getDataFolder(), "altars.yml")); }
      catch (java.io.IOException e) { throw new IllegalStateException("Cannot save altars.yml", e); }
   }

   public void registerRecipes() {
      registerConfiguredRecipe("weapons_handle", dev.vaileks.altarweaponsvl.items.weaponshandle.WeaponsHandleItem.create(), "weapon-handle");
      registerConfiguredRecipe("illusion_core", dev.vaileks.altarweaponsvl.items.illusioncore.IllusionCoreItem.create(), "illusion-core");
      registerConfiguredRecipe("vulcan_skull", dev.vaileks.altarweaponsvl.items.vulcanskull.VulcanSkullItem.create(), "vulcan-skull");
   }

   private void registerConfiguredRecipe(String id, ItemStack result, String setting) {
      NamespacedKey key = new NamespacedKey("runesaltarv", id);
      Bukkit.removeRecipe(key);
      String path = "items.recipes."+setting;
      if (!itemsConfig.getBoolean(path+".enabled", true)) return;
      try {
         ShapedRecipe recipe = new ShapedRecipe(key, result);
         recipe.shape(itemsConfig.getStringList(path+".shape").toArray(String[]::new));
         var ingredients = java.util.Objects.requireNonNull(itemsConfig.getConfigurationSection(path+".ingredients"));
         for (String symbol : ingredients.getKeys(false)) {
            Material material = Material.matchMaterial(ingredients.getString(symbol, ""));
            if (symbol.length()!=1 || material==null || material.isAir()) throw new IllegalArgumentException("Invalid ingredient: "+symbol);
            recipe.setIngredient(symbol.charAt(0), material);
         }
         Bukkit.addRecipe(recipe);
      } catch (RuntimeException e) { getLogger().log(java.util.logging.Level.WARNING, "Invalid recipe "+id, e); }
   }
}
