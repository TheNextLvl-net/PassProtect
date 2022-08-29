package net.nonswag.tnl.passprotect.panels;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;

public abstract class Panel {

    @Nonnull
    public String getName() {
        return getClass().getSimpleName();
    }

    @Nonnull
    public abstract JPanel getPanel();

    public boolean isResizable() {
        return false;
    }

    public void onFocus() {
    }

    @Nonnull
    public abstract Dimension getPreferredSize();
}
