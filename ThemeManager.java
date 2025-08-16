
import java.awt.*;
import javax.swing.*;

public class ThemeManager {
    private final JTextArea textArea;
    private final JLabel statusLabel;

    public ThemeManager(JTextArea textArea, JLabel statusLabel) {
        this.textArea = textArea;
        this.statusLabel = statusLabel;
    }

    public void setDarkTheme() {
        textArea.setBackground(new Color(40, 44, 52));
        textArea.setForeground(Color.WHITE);
        statusLabel.setBackground(new Color(60, 63, 65));
        statusLabel.setForeground(Color.LIGHT_GRAY);
    }

    public void setLightTheme() {
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(Color.BLACK);
        statusLabel.setBackground(Color.LIGHT_GRAY);
        statusLabel.setForeground(Color.BLACK);
    }

    public void setSystemTheme() {
        setLightTheme();
    }
}
