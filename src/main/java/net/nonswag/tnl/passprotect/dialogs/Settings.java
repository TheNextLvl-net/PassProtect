package net.nonswag.tnl.passprotect.dialogs;

import net.nonswag.tnl.passprotect.Launcher;
import net.nonswag.tnl.passprotect.PassProtect;
import net.nonswag.tnl.passprotect.api.files.Config;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Settings extends JDialog {

    @Nonnull
    private JPanel panel, general, file, selection, settings, appearance;
    @Nonnull
    private JList<JRadioButton> list;

    public Settings(@Nonnull Config config) {
        super(PassProtect.getInstance().getWindow(), "Preferences");
        setModal(true);
        setContentPane(panel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(@Nonnull WindowEvent event) {
                dispose();
            }
        });
        panel.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        setResizable(false);
        setPreferredSize(new Dimension(360, 180));
        pack();
        setLocationRelativeTo(PassProtect.getInstance().getWindow());
        DefaultListModel<JRadioButton> model = new DefaultListModel<>();
        for (int index = 0; index < Launcher.getLookAndFeels().size(); index++) {
            UIManager.LookAndFeelInfo theme = Launcher.getLookAndFeels().get(index);
            JRadioButton button = new JRadioButton(theme.getName()) {
                @Override
                public String toString() {
                    return getText();
                }
            };
            button.setSelected(UIManager.getLookAndFeel().getClass().getName().equals(theme.getClassName()));
            button.addActionListener(actionEvent -> {
                config.setAppearance(theme);
                Launcher.applyAppearance(config);
                PassProtect.getInstance().setLoggedIn(true);
            });
            model.addElement(button);
        }
        list.setModel(model);
        setVisible(true);
        setAlwaysOnTop(true);
    }
}
