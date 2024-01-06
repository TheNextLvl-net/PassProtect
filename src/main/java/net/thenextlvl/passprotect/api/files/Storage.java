package net.thenextlvl.passprotect.api.files;

import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.thenextlvl.core.api.errors.file.FileSaveException;
import net.thenextlvl.core.api.file.formats.TextFile;
import net.thenextlvl.core.api.file.helper.JsonHelper;
import net.thenextlvl.cryptography.AES;
import net.thenextlvl.passprotect.api.entry.*;
import net.thenextlvl.passprotect.util.Compressor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.TreeSet;

public class Storage extends TextFile {

    @Getter
    @Setter
    @Nullable
    private static Storage instance;
    @Nonnull
    protected AES aes;
    @Getter
    @Nonnull
    private final String user;
    @Getter
    @Nonnull
    private final TreeSet<Category> categories = new TreeSet<>();
    @Getter
    @Nonnull
    private byte[] securityKey;

    public Storage(String user, byte[] securityKey) throws IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        super(user, "saves.pp");
        this.aes = getAES(this.securityKey = securityKey);
        this.user = user;
        init(false);
    }

    public void setSecurityKey(byte[] securityKey) throws FileSaveException {
        this.aes = getAES(this.securityKey = securityKey);
        save();
    }

    @Override
    public void save() throws FileSaveException {
        try {
            setContent(new String[]{aes.encode(parse(getCategories()).toString())});
        } catch (Exception e) {
            throw new FileSaveException(e);
        } finally {
            super.save();
        }
    }

    public void init(boolean reload) throws IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (reload) load();
        categories.clear();
        JsonElement root = JsonHelper.parse(aes.decode(String.join("", getContent())));
        if (root.isJsonArray()) categories.addAll(parseCategories(root.getAsJsonArray()));
    }

    @Nullable
    private Entry parse(int type, JsonObject object) {
        return switch (type) {
            case Entry.CATEGORY -> parseCategory(object);
            case Entry.PASSWORD -> parsePassword(object);
            case Entry.BACKUP_CODE -> parseBackupCode(object);
            case Entry.FILE -> parseFile(object);
            case Entry.TOTP -> parseTotp(object);
            default -> null;
        };
    }

    @Nullable
    private Category parseCategory(JsonObject object) {
        return object.has("name") ? new Category(object.get("name").getAsString(), parseEntries(object)) : null;
    }

    @Nullable
    private Password parsePassword(JsonObject object) {
        if (!object.has("name") || !object.has("password")) return null;
        String name = object.get("name").getAsString();
        byte[] decode = Base64.getDecoder().decode(object.get("password").getAsString());
        return new Password(name, object.has("description") && object.get("description").isJsonPrimitive() ? object.get("description").getAsString() : null, decode);
    }

    @Nullable
    private BackupCode parseBackupCode(JsonObject object) {
        if (!object.has("name") || !object.has("codes")) return null;
        if (!object.get("codes").isJsonArray()) return null;
        String name = object.get("name").getAsString();
        JsonArray array = object.getAsJsonArray("codes");
        String[] codes = new String[array.size()];
        for (int i = 0; i < array.size(); i++) codes[i] = array.get(i).getAsString();
        return new BackupCode(name, codes);
    }

    @Nullable
    private File parseFile(JsonObject object) {
        if (!object.has("name") || (!object.has("error") && !object.has("content"))) return null;
        try {
            String name = object.get("name").getAsString();
            boolean error = object.has("error");
            String[] content;
            if (error) {
                byte[] decode = Base64.getDecoder().decode(object.get("error").getAsString().getBytes(StandardCharsets.UTF_8));
                content = new String(decode).split("\n");
            } else content = Compressor.decompress(object.get("content").getAsString()).split("\n");
            return new File(name, content);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    private TOTP parseTotp(JsonObject object) {
        if (!object.has("issuer") || (!object.has("account-name") && !object.has("secret-key"))) return null;
        return new TOTP(object.get("issuer").getAsString(), object.get("account-name").getAsString(), object.get("secret-key").getAsString());
    }

    private int getType(String key) {
        return switch (key.split(";")[0]) {
            case "0" -> Entry.CATEGORY;
            case "1" -> Entry.PASSWORD;
            case "2" -> Entry.BACKUP_CODE;
            case "3" -> Entry.FILE;
            case "4" -> Entry.TOTP;
            default -> Entry.UNKNOWN;
        };
    }

    @Nonnull
    private List<Category> parseCategories(JsonArray array) {
        List<Category> categories = new ArrayList<>();
        array.forEach(entry -> {
            if (!entry.isJsonObject()) return;
            Category category = (Category) parse(Entry.CATEGORY, entry.getAsJsonObject());
            if (category != null) categories.add(category);
        });
        return categories;
    }

    @Nonnull
    private List<Entry> parseEntries(JsonObject object) {
        List<Entry> entries = new ArrayList<>();
        object.entrySet().forEach(entry -> {
            if (!entry.getValue().isJsonObject()) return;
            entries.add(parse(getType(entry.getKey()), entry.getValue().getAsJsonObject()));
        });
        return entries;
    }

    @Nonnull
    private JsonArray parse(TreeSet<Category> categories) {
        JsonArray root = new JsonArray();
        categories.forEach(category -> root.add(category.parse()));
        return root;
    }

    @Nonnull
    private static AES getAES(byte[] securityKey) {
        return new AES(Hashing.sha256().hashBytes(securityKey).asBytes());
    }
}
