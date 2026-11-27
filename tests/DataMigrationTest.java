package test;

import dev.vaileks.altarweaponsvl.DataMigration;
import java.nio.file.Files;
import java.util.Comparator;

public final class DataMigrationTest {
    public static void main(String[] args) throws Exception {
        var root = Files.createTempDirectory("altarvl-migration-");
        try {
            var old = root.resolve("RunesAltarV");
            var updated = root.resolve("AltarWeaponsVL");
            Files.createDirectories(old.resolve("data"));
            Files.createDirectories(old.resolve("items"));
            Files.writeString(old.resolve("config.yml"), "lang: ru_RU");
            Files.writeString(old.resolve("data/altars.yml"), "saved-recipe-and-uses");
            Files.writeString(old.resolve("items/bloodlust.yml"), "custom-ability-settings");
            DataMigration.copyMissing(old, updated);
            if (!Files.readString(updated.resolve("data/altars.yml")).equals("saved-recipe-and-uses")) throw new AssertionError("Saved altars lost");
            if (!Files.readString(updated.resolve("items/bloodlust.yml")).equals("custom-ability-settings")) throw new AssertionError("Weapon settings lost");
            Files.writeString(updated.resolve("config.yml"), "lang: en_US");
            DataMigration.copyMissing(old, updated);
            if (!Files.readString(updated.resolve("config.yml")).equals("lang: en_US")) throw new AssertionError("New settings overwritten");
            if (!Files.exists(old.resolve("data/altars.yml"))) throw new AssertionError("Legacy backup removed");
            DataMigration.copyMissing(root.resolve("missing"), updated);
            System.out.println("DATA_MIGRATION_PASS");
        } finally {
            try (var paths = Files.walk(root)) {
                for (var p : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(p);
            }
        }
    }
}
