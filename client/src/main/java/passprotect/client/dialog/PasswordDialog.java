package passprotect.client.dialog;

import ch.bailu.gtk.adw.PasswordEntryRow;
import ch.bailu.gtk.gtk.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import passprotect.client.util.GTK;

@Getter
@Setter
@Accessors(fluent = true)
public class PasswordDialog extends Dialog {
    private final Window window;
    private final Button confirmButton = new Button();
    private final PasswordEntryRow passwordEntryRow = new PasswordEntryRow();

    public PasswordDialog(Window window) {
        this.window = window;

        var passwordBox = GTK.createListBox();
        var content = new Box(Orientation.VERTICAL, 0);

        passwordEntryRow.setSelectable(false);

        confirmButton.setLabel("Confirm");
        confirmButton.setHasFrame(true);
        confirmButton.setHalign(Align.FILL);
        confirmButton.setMarginBottom(16);
        confirmButton.setMarginStart(32);
        confirmButton.setMarginEnd(32);

        passwordBox.append(passwordEntryRow);
        passwordBox.setMarginBottom(16);

        content.append(passwordBox);
        content.append(confirmButton);

        setChild(content);
        setTransientFor(window);
        setParent(window);
    }
}
