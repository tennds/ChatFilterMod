package chatfilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Storage for the ignored-players list.
 * Saved to config/chatfilter-ignored.txt (one nickname per line).
 */
public final class ChatFilterStore {

    /** All mod service messages start with this prefix so the filter never hides them. */
    public static final String PREFIX = "[ChatFilter]";

    private static final Set<String> NAMES = new LinkedHashSet<>();
    private static final List<Pattern> PATTERNS = new ArrayList<>();
    private static Path file;

    private ChatFilterStore() {}

    public static synchronized void init() {
        Path dir = FabricLoader.getInstance().getConfigDir();
        file = dir.resolve("chatfilter-ignored.txt");
        NAMES.clear();
        if (Files.isRegularFile(file)) {
            try {
                for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    NAMES.add(trimmed);
                }
            } catch (IOException ignored) {
                // no file access — start with an empty list
            }
        }
        rebuildPatterns();
    }

    private static void rebuildPatterns() {
        PATTERNS.clear();
        for (String name : NAMES) {
            PATTERNS.add(Pattern.compile(
                    "(?<![-\\w])" + Pattern.quote(name) + "(?![-\\w])",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS));
        }
    }

    /** @return true if player was added, false if already present */
    public static synchronized boolean add(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        boolean added = NAMES.add(name.trim());
        if (added) {
            rebuildPatterns();
            save();
        }
        return added;
    }

    /** @return true if player was removed from the list */
    public static synchronized boolean remove(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        boolean removed = NAMES.remove(name.trim());
        if (removed) {
            rebuildPatterns();
            save();
        }
        return removed;
    }

    public static synchronized Collection<String> names() {
        return new ArrayList<>(NAMES);
    }

    public static synchronized boolean isEmpty() {
        return NAMES.isEmpty();
    }

    /**
     * Returns true if the message text contains an ignored nickname
     * (case-insensitive, word boundaries). Service messages are never hidden.
     */
    public static synchronized boolean shouldHide(String text) {
        if (text == null || text.isEmpty() || PATTERNS.isEmpty()) {
            return false;
        }
        if (text.startsWith(PREFIX)) {
            return false;
        }
        for (Pattern pattern : PATTERNS) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    private static void save() {
        if (file == null) {
            return;
        }
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, NAMES, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // cannot save — list stays in memory only
        }
    }
}