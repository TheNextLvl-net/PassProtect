package net.thenextlvl.passprotect.api.fields;

import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;

@Getter
public class PasswordField extends JPasswordField {

    @Nonnull
    private static final Color color = new Color(152, 152, 152);

    @Nullable
    private String placeholder = null;
    private boolean passwordVisible = false;

    {
        setPasswordVisible(false);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent event) {
                update();
            }

            @Override
            public void keyPressed(KeyEvent event) {
                update();
            }

            @Override
            public void keyReleased(KeyEvent event) {
                update();
            }

            private void update() {
                if (PasswordField.super.getPassword().length != 0) setPasswordVisible(isPasswordVisible());
            }
        });
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
        if (!String.valueOf(super.getPassword()).equals(getPlaceholder())) return;
        super.setEchoChar('°');
        super.setText("");
        setForeground(Color.GRAY);
    }

    private void setPlaceholder() {
        if (getPlaceholder() == null || hasFocus()) return;
        String password = String.valueOf(super.getPassword());
        if (!password.equals(getPlaceholder()) && !password.isEmpty()) return;
        super.setEchoChar((char) 0);
        super.setText(getPlaceholder());
        setForeground(color);
    }

    private boolean isPlaceholderSet() {
        return Objects.equals(getForeground(), color) && String.valueOf(super.getPassword()).equals(getPlaceholder());
    }

    @Nonnull
    public PasswordField setPlaceholder(@Nullable String placeholder) {
        if (placeholder == null) {
            unsetPlaceholder();
            this.placeholder = null;
        } else {
            this.placeholder = placeholder;
            setPlaceholder();
        }
        return this;
    }

    public void setPasswordVisible(boolean passwordVisible) {
        this.passwordVisible = passwordVisible;
        String text = String.valueOf(super.getPassword() == null ? new char[0] : super.getPassword());
        boolean b = text.isEmpty() || text.equals(getPlaceholder());
        setForeground(b ? color : Color.GRAY);
        setEchoChar(b || isPasswordVisible() ? 0 : '°');
        putClientProperty("JPasswordField.cutCopyAllowed", passwordVisible);
    }

    @Override
    public void setText(@Nullable String text) {
        super.setText(text);
        setPasswordVisible(isPasswordVisible());
    }

    @Override
    public char[] getPassword() {
        return isPlaceholderSet() ? new char[0] : super.getPassword();
    }
}
