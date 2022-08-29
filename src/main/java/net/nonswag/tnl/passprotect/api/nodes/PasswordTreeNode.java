package net.nonswag.tnl.passprotect.api.nodes;

import net.nonswag.tnl.passprotect.api.entry.Password;
import net.nonswag.tnl.passprotect.api.renderer.TreeIconRenderer;

import javax.annotation.Nonnull;

public class PasswordTreeNode extends EntryTreeNode<Password> {

    public PasswordTreeNode(@Nonnull Password password) {
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
