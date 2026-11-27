package dev.vaileks.altarweaponsvl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/** Copies only missing legacy data; administrators' new settings always win. */
public final class DataMigration {
    private DataMigration() {}

    public static void copyMissing(Path legacy, Path destination) throws IOException {
        if (!Files.isDirectory(legacy, LinkOption.NOFOLLOW_LINKS)) return;
        Files.createDirectories(destination);
        try (var entries = Files.walk(legacy)) {
            for (Path source : entries.toList()) {
                if (Files.isSymbolicLink(source)) continue;
                Path target = destination.resolve(legacy.relativize(source));
                if (Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS)) Files.createDirectories(target);
                else if (Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS) && !Files.exists(target, LinkOption.NOFOLLOW_LINKS)) Files.copy(source, target);
            }
        }
    }
}
