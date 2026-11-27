# AltarWeaponsVL

![AltarWeaponsVL](branding/altarweaponsvl.jpg)

16 legendary weapons for Paper 1.21.4–26.3. Maintained by Vaileks.

Install `AltarWeaponsVL.jar` in `plugins/` and restart the server. No other plugins are required. WorldGuard support is optional.

Set `lang: en_US` or `lang: ru_RU` in `plugins/AltarWeaponsVL/config.yml`. Each weapon has one editable file in `plugins/AltarWeaponsVL/items/`, containing its name, descriptions, models and ability settings. Reload with `/awv reload`. Existing administrator settings are preserved.

## Commands

- `/awv` — view the weapon menu; administrators can click an item to receive it.
- `/awv give <player> <weapon_id>` — give a weapon. Tab completion lists all weapon and common item IDs.
- `/awv reload` — reload configuration and refresh held items.
- `/awv cooldown reset <player>` — reset ability cooldowns.
- `/friendlist add <player>`, `/friendlist remove <player>`, `/friendlist list` — manage up to two friends who are protected from your abilities. Aliases: `/fl`, `/trust`.

Permissions: `altarweaponsvl.menu` (everyone), `altarweaponsvl.admin` (operators).

Controls are shown in each weapon's lore. F means the swap-hands key; Shift + F means using it while sneaking. Active effect indicators use the action bar, with chat messages when attempting an ability on cooldown and when its cooldown ends. Passive effects do not announce cooldowns.

## Altars and common items

- `/awv items` — common item menu; administrators can click to receive items.
- `/awv give <player> <item_id>` — includes `warden_heart`, `vulcan_skull`, `illusion_core`, `weapons_handle`, `copper_pickaxe`, `vampire_shard`, `pale_shard`, `hyperion_shard`.
- `/awv altar give <player>` — give a placeable altar pedestal.
- `/awv altar create` — place an altar at your feet (the block must be empty).
- `/awv altar set item <id>` — set the held item as the result and clear the old recipe.
- `/awv altar set recipe <id>` — add the held stack as an ingredient. Repeat for each ingredient.
- `/awv altar set recipe <id> head <amount> [player]` — require generic or named player heads.
- `/awv altar set use <id> <amount|inf>` and `/awv altar set cooldown <id> <seconds>` — set craft limits and reuse delay.
- `/awv altar remove recipe <id> <position>`, `/awv altar remove item <id>` — edit the recipe.
- `/awv altar delete <id|all>` — remove placed altars.
- `/awv altar save <id> <name>` and `/awv altar load <name>` — save and place recipe templates.
- `/awv altar teleport <id>`, `/awv altar teleporthere <id>` — move yourself or an altar.
- `/awv altar set scale <title|recipe|item> <id|all|def> <value>` — display size.

Right-click a configured altar to craft. A rotating item, name and ingredient list appear above its pedestal. Recipes, uses and cooldowns survive restart. `altars.yml` controls display defaults, `crafting.yml` controls crafting-material recipes, Warden Heart drops and protected blocks for the copper pickaxe. Placed altars/templates are stored in `data/altars.yml`. Altar crafting does not require the separate Pedestals plugin. Weapon settings remain in `items/`.

The original Weapon's Handle, Illusion Core and Vulcan's Skull workbench recipes are included. Warden Heart drops from Wardens. Copper Pickaxe keeps its original F toggle and 3x3 mining (two layers deep), respecting block-break cancellation. Player-head drops can be disabled with `player-heads.enabled: false` in `config.yml`. Copper armor is not included.

## Weapons

Bone Blade, Bloodlust, Nightpiercer, Vulcan's Crossbow, Pale Gun, Frost Scythe, Pure Blade, Knightfall, Shadow Blade, Windweaver, Hyperion, Withered Bone Blade, Earth Gauntlet, Cutlass, Paladin's Battle Axe and Crazy Slots.

Weapons have their own persistent tags, so an unrelated item with the same custom model is not treated as legendary. Weapon ability damage follows the server damage pipeline, including absorption, event cancellation and totem resurrection.

## Resource pack

Custom models and tooltip textures require a compatible resource pack, distributed separately through [Discord](https://discord.gg/ssXctkBkQD). No resource pack assets are included in this source repository or JAR. Mechanics work server-side without a client mod. Without the pack, items use vanilla appearances.

## Server compatibility

Paper releases: 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.1, 26.1.2, 26.2 and 26.3. Use Java 21 or newer for 1.21.x and Java 25 for 26.x. There is no stable Paper 26.1 build in the official download catalog.

On versions without the vanilla copper pickaxe, its custom item uses an iron-pickaxe base while retaining its identifier, enchantments and mining ability. Chain effects resolve the material name used by the running server. Use a resource pack made for your Minecraft version.

## Updating from RunesAltarV

Remove the old RunesAltarV JAR before installing AltarWeaponsVL. On first start, missing configuration and saved altar data are copied from `plugins/RunesAltarV/` to `plugins/AltarWeaponsVL/`. Existing files in the new folder are preserved. The old folder remains as a backup. Item and recipe identifiers retain their legacy namespace for compatibility. `/rav`, `/altarv` and `/runesaltarv` remain aliases; existing `runesaltarv.*` permissions are supported.

## Build

JDK 21 or newer and Maven: `mvn package`. The resulting file is `target/AltarWeaponsVL.jar`. Only the provided Paper API and its transitive libraries are needed at compile time.

Alternatively set `ALTAR_COMPILE_CLASSPATH` to the Paper 1.21.4 API and its transitive dependencies and run `python3 build.py`; this produces `AltarWeaponsVL.jar`.

The `tests/` directory contains integration checks intended for a disposable Paper server with this plugin and the test helper. They cover weapon creation, both lore languages, configuration reload, ability cooldown guards, active effect rendering, obstruction checks and native damage/resurrection behavior. They do not replace multiplayer visual testing.

## Weapon configuration

Each weapon file is the single configuration home for its abilities, progression, models, attribute bonuses, visual parameters and sound settings. Existing ability fields are now fully written out instead of relying on hidden Java defaults. The `tuning` section exposes additional effect durations/amplifiers, targeting radii, animation sizes and projectile parameters; `visuals.sounds` configures individual vanilla or resource-pack sounds and relative volume/pitch, and `visuals.particle-density` adjusts particle counts. `item.attack-damage-bonus` and `item.attack-speed-bonus` add main-hand attributes without replacing progression modifiers. Bounds reject non-finite/extreme values in the additional tuning fields. New fields are appended on reload/startup; existing administrator values are preserved.

