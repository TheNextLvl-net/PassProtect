package net.nonswag.tnl.passprotect.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public final class Clipboard {

    public static void setContent(@Nullable String content, @Nonnull ClipboardOwner owner) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content), owner);
    }

    @Nullable
    public static String getContent() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            return null;
        }
    }
}
