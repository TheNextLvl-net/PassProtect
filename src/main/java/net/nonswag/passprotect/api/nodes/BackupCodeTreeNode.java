package net.nonswag.passprotect.api.nodes;

import net.nonswag.passprotect.api.entry.BackupCode;
import net.nonswag.passprotect.api.renderer.TreeIconRenderer;

import javax.annotation.Nonnull;

public class BackupCodeTreeNode extends EntryTreeNode<BackupCode> {

    public BackupCodeTreeNode(@Nonnull BackupCode backupCode) {
        super(backupCode);
    }

    @Nonnull
    @Override
    public String type() {
        return "backup code";
    }

    @Nonnull
    @Override
    public Style style(boolean selected) {
        return new Style("<span style='color:gray;text-decoration: %s;'>%s</span>".formatted(
                selected ? "underline" : "none", getEntry().getName()
        ), TreeIconRenderer.Logo.BACKUP_OPEN, TreeIconRenderer.Logo.BACKUP_CLOSED);
    }
}
