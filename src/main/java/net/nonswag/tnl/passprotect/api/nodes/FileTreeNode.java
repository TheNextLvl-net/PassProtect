package net.nonswag.tnl.passprotect.api.nodes;

import net.nonswag.tnl.passprotect.api.entry.File;
import net.nonswag.tnl.passprotect.api.renderer.TreeIconRenderer;

import javax.annotation.Nonnull;

public class FileTreeNode extends EntryTreeNode<File> {

    public FileTreeNode(@Nonnull File file) {
        super(file);
    }

    @Nonnull
    @Override
    public String type() {
        return "file";
    }

    @Nonnull
    @Override
    public Style style(boolean selected) {
        return new Style("<span style='color:gray;text-decoration: %s;'>%s</span>".formatted(
                selected ? "underline" : "none", getEntry().getName()
        ), TreeIconRenderer.Logo.FILE_OPEN, TreeIconRenderer.Logo.FILE_CLOSED);
    }
}
