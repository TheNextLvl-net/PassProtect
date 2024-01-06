package net.thenextlvl.passprotect.api.fields;

import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Objects;

@Getter
public class TextField extends JTextField {

    @Nonnull
    private static final Color color = new Color(152, 152, 152);

    @Nullable
    private String placeholder = null;

    public TextField(@Nullable String text) {
        super(text);
    }

    public TextField() {
    }

    {
        setForeground(Color.GRAY);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                unsetPlaceholder();
            }

            @Override
            public void focusLost(FocusEvent event) {
                setPlaceholder();
            }
        });
    }

    private void unsetPlaceholder() {
        if (getPlaceholder() == null) return;
        if (!String.valueOf(super.getText()).equals(getPlaceholder())) return;
        setText("");
        setForeground(Color.GRAY);
    }

    private void setPlaceholder() {
        if (getPlaceholder() == null || hasFocus()) return;
        String text = super.getText();
        if (!text.equals(getPlaceholder()) && !text.isEmpty()) return;
        setText(getPlaceholder());
        setForeground(color);
    }

    private boolean isPlaceholderSet() {
        return Objects.equals(getForeground(), color) && super.getText().equals(getPlaceholder());
    }

    @Nonnull
    public TextField setPlaceholder(@Nullable String placeholder) {
        if (placeholder == null) {
            unsetPlaceholder();
            this.placeholder = null;
        } else {
            this.placeholder = placeholder;
            setPlaceholder();
        }
        return this;
    }

    @Override
    public void setText(@Nullable String text) {
        setForeground(text == null || text.isEmpty() || text.equals(getPlaceholder()) ? color : Color.GRAY);
        super.setText(text);
    }

    @Override
    public String getText() {
        return isPlaceholderSet() ? "" : super.getText();
    }
}
