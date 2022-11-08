package net.nonswag.passprotect.api.nodes;

import net.nonswag.passprotect.api.entry.TOTP;
import net.nonswag.passprotect.api.renderer.TreeIconRenderer;

import javax.annotation.Nonnull;

public class TOTPTreeNode extends EntryTreeNode<TOTP> {

    public TOTPTreeNode(@Nonnull TOTP totp) {
        super(totp);
    }

    @Nonnull
    @Override
    public String type() {
        return "Time-based auth code";
    }

    @Nonnull
    @Override
    public Style style(boolean selected) {
        return new Style("<span style='color:gray;text-decoration: %s;'>%s</span>".formatted(
                selected ? "underline" : "none", getEntry().getName()
        ), TreeIconRenderer.Logo.TOTP_OPEN, TreeIconRenderer.Logo.TOTP_CLOSED);
    }
}
