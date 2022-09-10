package net.nonswag.tnl.passprotect.api.entry;

import com.google.gson.JsonElement;
import net.nonswag.tnl.passprotect.api.nodes.EntryTreeNode;

import javax.annotation.Nonnull;

public interface Entry extends Comparable<Entry> {

    int UNKNOWN = -1;
    int CATEGORY = 0;
    int PASSWORD = 1;
    int BACKUP_CODE = 2;
    int FILE = 3;
    int TOTP = 4;

    @Nonnull
    String getName();

    @Nonnull
    JsonElement parse();

    @Nonnull
    EntryTreeNode<?> tree();

    int getType();
}
