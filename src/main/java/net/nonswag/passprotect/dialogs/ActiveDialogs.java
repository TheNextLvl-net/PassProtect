package net.nonswag.passprotect.dialogs;

import lombok.Getter;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.HashMap;

public class ActiveDialogs extends HashMap<String, JDialog> {

    @Getter
    @Nonnull
    private static final ActiveDialogs instance = new ActiveDialogs();

}
