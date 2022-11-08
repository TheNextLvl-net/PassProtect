package net.nonswag.passprotect.api.entry;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.nonswag.core.api.object.Pair;
import net.nonswag.passprotect.api.dictionary.Dictionary;
import net.nonswag.passprotect.api.nodes.PasswordTreeNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Getter
public class Password implements Entry {

    @Nonnull
    private final String name;
    @Nullable
    private final String description;
    @Nonnull
    private final byte[] password;

    public Password(@Nonnull String name, @Nullable String description, @Nonnull byte[] password) {
        this.name = name;
        this.description = description;
        this.password = password;
    }

    public Password(@Nonnull String name, @Nonnull byte[] password) {
        this(name, null, password);
    }

    @Nonnull
    @Override
    public JsonObject parse() {
        JsonObject object = new JsonObject();
        object.addProperty("name", getName());
        object.addProperty("password", Base64.getEncoder().encodeToString(password));
        object.addProperty("description", description);
        return object;
    }

    @Nonnull
    @Override
    public PasswordTreeNode tree() {
        return new PasswordTreeNode(this);
    }

    @Override
    public int getType() {
        return PASSWORD;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        return o instanceof Password password && name.equals(password.name)
                && Objects.equals(description, password.description)
                && Arrays.equals(this.password, password.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(@Nonnull Entry entry) {
        return equals(entry) ? 0 : entry instanceof Password password ? name.compareTo(password.name) : 1;
    }

    public static class Lookup {

        @Nonnull
        public static Pair<Integer, String> test(@Nonnull String password) {
            if (password.isEmpty()) return new Pair<>(0, null);
            int entropy = 100;
            List<String> messages = new ArrayList<>();
            if (Dictionary.isWord(password)) {
                messages.add("Crackable with a dictionary attack");
                entropy -= 50;
            }
            if (password.equals(password.toLowerCase())) {
                messages.add("No uppercase letters");
                entropy -= 15;
            }
            if (password.equals(password.toUpperCase())) {
                messages.add("No lowercase letters");
                entropy -= 15;
            }
            if (password.length() < 3) {
                entropy = password.length() < 2 ? 0 : entropy - 80;
                messages.add("Instant crackable with a bruteforce attack");
            } else if (password.length() < 4) {
                entropy -= 60;
                messages.add("Fast crackable with a bruteforce attack");
            } else if (password.length() < 8) entropy -= 40;
            else if (password.length() < 16) entropy -= 20;
            if (password.length() < 16) messages.add("Could be longer");
            if (!containsNumber(password)) {
                entropy -= 10;
                messages.add("Does not contain a number");
            }
            if (messages.isEmpty()) return new Pair<>(entropy, null);
            return new Pair<>(entropy, "<html>%s</html>".formatted(String.join("<br>", messages)));
        }

        private static boolean containsNumber(@Nonnull String password) {
            for (int i = 0; i < 10; i++) if (password.contains(String.valueOf(i))) return true;
            return false;
        }
    }
}
