package net.thenextlvl.passprotect.api.nodes;

import net.thenextlvl.passprotect.api.entry.Category;
import net.thenextlvl.passprotect.api.renderer.TreeIconRenderer;

import javax.annotation.Nonnull;

public class CategoryTreeNode extends EntryTreeNode<Category> {

    public CategoryTreeNode(Category category) {
        super(category);
    }

    @Nonnull
    @Override
    public String type() {
        return "category";
    }

    @Nonnull
    @Override
    public Style style(boolean selected) {
        return new Style("<span style='color:gray;text-decoration: %s;'>%s (%s)</span>".formatted(
                selected ? "underline" : "none", getEntry().getName(), getEntry().size()
        ), TreeIconRenderer.Logo.DIRECTORY_OPEN, TreeIconRenderer.Logo.DIRECTORY_CLOSED);
    }
}
