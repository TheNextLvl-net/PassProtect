package net.nonswag.tnl.passprotect.api.nodes;

import lombok.Getter;
import net.nonswag.tnl.passprotect.api.entry.Entry;
import net.nonswag.tnl.passprotect.api.renderer.TreeIconRenderer;

import javax.annotation.Nonnull;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Arrays;

@Getter
public abstract class EntryTreeNode<E extends Entry> extends DefaultMutableTreeNode {

    @Nonnull
    private final E entry;

    public EntryTreeNode(@Nonnull E entry) {
        super(entry.getName());
        this.entry = entry;
    }

    @Nonnull
    public abstract String type();

    @Nonnull
    public String path() {
        return Arrays.toString(getPath());
    }

    @Nonnull
    @Override
    public String toString() {
        return getEntry().getName();
    }

    @Nonnull
    public abstract Style style(boolean selected);

    public record Style(@Nonnull String title, @Nonnull TreeIconRenderer.Logo expanded, @Nonnull TreeIconRenderer.Logo collapsed) {
    }
}