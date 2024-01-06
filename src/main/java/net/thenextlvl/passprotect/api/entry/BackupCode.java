package net.thenextlvl.passprotect.api.entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.thenextlvl.passprotect.api.nodes.BackupCodeTreeNode;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

@Getter
public class BackupCode implements Entry {

    @Nonnull
    private final String name;
    @Nonnull
    private final String[] codes;

    public BackupCode(String name, String... codes) {
        this.name = name;
        this.codes = codes;
    }

    @Nonnull
    public String[] asStringArray() {
        String[] codes = new String[this.codes.length];
        for (int i = 0; i < this.codes.length; i++) codes[i] = String.valueOf(this.codes[i]);
        return codes;
    }

    @Nonnull
    @Override
    public JsonObject parse() {
        JsonObject object = new JsonObject();
        JsonArray codes = new JsonArray();
        for (String code : this.codes) codes.add(code);
        object.addProperty("name", name);
        object.add("codes", codes);
        return object;
    }

    @Nonnull
    @Override
    public BackupCodeTreeNode tree() {
        return new BackupCodeTreeNode(this);
    }

    @Override
    public int getType() {
        return BACKUP_CODE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        return o instanceof BackupCode backupCode && name.equals(backupCode.name) && Arrays.equals(codes, backupCode.codes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Arrays.hashCode(codes));
    }

    @Override
    public int compareTo(Entry entry) {
        return equals(entry) ? 0 : entry instanceof BackupCode backupCode ? name.compareTo(backupCode.name) : 1;
    }
}
