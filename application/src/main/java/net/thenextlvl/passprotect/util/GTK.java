package net.thenextlvl.passprotect.util;

import ch.bailu.gtk.gtk.ListBox;

public class GTK {
    public static ListBox createListBox() {
        var list = new ListBox();
        list.setMarginTop(32);
        list.setMarginEnd(32);
        list.setMarginBottom(32);
        list.setMarginStart(32);
        list.addCssClass("boxed-list");
        return list;
    }
}
