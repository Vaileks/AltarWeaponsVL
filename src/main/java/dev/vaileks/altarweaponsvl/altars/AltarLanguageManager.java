package dev.vaileks.altarweaponsvl.altars;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

public class AltarLanguageManager {
    private static final String DEFAULT_LANGUAGE = "ru_RU";
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("ru_RU", "en_US");
    private final AltarWeaponsVL plugin;
    private YamlConfiguration messages;
    private String language = "ru_RU";

    public AltarLanguageManager(AltarWeaponsVL plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        String configuredLanguage = this.plugin.getConfig().getString("lang", null);
        if (configuredLanguage == null) {
            configuredLanguage = this.plugin.getAltarsConfig().getString("language", DEFAULT_LANGUAGE);
        }
        boolean invalidLanguage = !SUPPORTED_LANGUAGES.contains(configuredLanguage = configuredLanguage.trim());
        this.language = invalidLanguage ? DEFAULT_LANGUAGE : configuredLanguage;
        File messagesFolder = new File(this.plugin.getDataFolder(), "messages");
        if (!messagesFolder.exists()) {
            messagesFolder.mkdirs();
        }
        for (String supportedLanguage : SUPPORTED_LANGUAGES) {
            String resourcePath = "messages/" + supportedLanguage + ".yml";
            File localeFile = new File(this.plugin.getDataFolder(), resourcePath);
            if (localeFile.exists()) continue;
            this.plugin.saveResource(resourcePath, false);
        }
        File localeFile = new File(this.plugin.getDataFolder(), "messages/" + this.language + ".yml");
        this.messages = YamlConfiguration.loadConfiguration((File)localeFile);
        try (InputStream stream = this.plugin.getResource("messages/" + this.language + ".yml");){
            if (stream != null) {
                YamlConfiguration bundledMessages = YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(stream, StandardCharsets.UTF_8));
                this.messages.setDefaults((Configuration)bundledMessages);
            }
        }
        catch (Exception exception) {
            this.plugin.getLogger().warning("Could not load bundled language defaults: " + exception.getMessage());
        }
        if (invalidLanguage) {
            this.plugin.getLogger().warning(this.plain("language.invalid", "language", configuredLanguage, "fallback", DEFAULT_LANGUAGE));
        }
    }

    public Component component(String key, String ... replacements) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(this.replace(this.getRaw(key), replacements));
    }

    public List<Component> components(String key, String ... replacements) {
        ArrayList<Component> result = new ArrayList<Component>();
        List<String> lines = this.messages.getStringList(key);
        if (lines.isEmpty()) {
            lines = List.of("&cMissing message list: " + key);
        }
        for (String string : lines) {
            result.add(LegacyComponentSerializer.legacyAmpersand().deserialize(this.replace(string, replacements)));
        }
        return result;
    }

    public String plain(String key, String ... replacements) {
        return PlainTextComponentSerializer.plainText().serialize(this.component(key, replacements));
    }

    public String getLanguage() {
        return this.language;
    }

    private String getRaw(String key) {
        String message = this.messages.getString(key);
        return message != null ? message : "&cMissing message: " + key;
    }

    private String replace(String message, String ... replacements) {
        String result = message;
        int i = 0;
        while (i + 1 < replacements.length) {
            result = result.replace("{" + replacements[i] + "}", replacements[i + 1]);
            i += 2;
        }
        return result;
    }
}

