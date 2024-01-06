package net.thenextlvl.passprotect.api.nodes;

import net.thenextlvl.passprotect.api.entry.Password;
import net.thenextlvl.passprotect.api.renderer.TreeIconRenderer;

import javax.annotation.Nonnull;

public class PasswordTreeNode extends EntryTreeNode<Password> {

    public PasswordTreeNode(Password password) {
        super(password);
    }

    @Nonnull
    @Override
    public String type() {
        return "password";
    }

    @Nonnull
    @Override
    public Style style(boolean selected) {
        return new Style("<span style='color:gray;text-decoration: %s;'>%s</span>".formatted(
                selected ? "underline" : "none", getEntry().getName()
        ), TreeIconRenderer.Logo.UNLOCKED, TreeIconRenderer.Logo.LOCKED);
    }
}
