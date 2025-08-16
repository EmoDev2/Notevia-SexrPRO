
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class Notevia extends JFrame {
    private final JTextArea textArea = new JTextArea();
    private final JLabel statusLabel = new JLabel();
    private final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final DecimalFormat df = new DecimalFormat("#.##");
    private final ThemeManager themeManager;

    private File currentFile = null;
    private Timer autoSaveTimer;

    public Notevia() {
        setTitle("Notevia (Notepad Anti-Overload Notepad) SexrPRO");
        ImageIcon icon = new ImageIcon("favicon.png");
        setIconImage(icon.getImage());
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));

        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        createTopPanel();
        setupContextMenu();
        setupStatusUpdater();
        setupAutoSave();
        setupWindowCloseConfirmation();

        themeManager = new ThemeManager(textArea, statusLabel);
        themeManager.setSystemTheme();
    }

    private void createTopPanel() {
        JButton fileBtn = new JButton("📁 Archivo");
        JButton themeBtn = new JButton("🎨 Tema");
        JButton configBtn = new JButton("⚙️ Configuración");
        JButton searchBtn = new JButton("🔍 Buscar/Reemplazar");
        JButton helpBtn = new JButton("❓ Ayuda");

        fileBtn.addActionListener(e -> showFileMenu(fileBtn));
        themeBtn.addActionListener(e -> showThemeMenu(themeBtn));
        configBtn.addActionListener(e -> showFontConfig());
        searchBtn.addActionListener(e -> showSearchDialog());
        helpBtn.addActionListener(e -> showHelp());

        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.add(fileBtn);
        topPanel.add(themeBtn);
        topPanel.add(configBtn);
        topPanel.add(searchBtn);
        topPanel.add(helpBtn);

        add(topPanel, BorderLayout.NORTH);
    }

    private void showFileMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem open = new JMenuItem("Abrir");
        JMenuItem save = new JMenuItem("Guardar");
        JMenuItem saveAs = new JMenuItem("Guardar como");
        JMenuItem exit = new JMenuItem("Salir");

        open.addActionListener(e -> openFile());
        save.addActionListener(e -> saveFile());
        saveAs.addActionListener(e -> saveFileAs());
        exit.addActionListener(e -> exitApp());

        


        menu.add(open);
        menu.add(save);
        menu.add(saveAs);
        menu.addSeparator();
        menu.add(exit);

        menu.show(invoker, 0, invoker.getHeight());
    }

    private void showThemeMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem light = new JMenuItem("Claro");
        JMenuItem dark = new JMenuItem("Oscuro");
        JMenuItem system = new JMenuItem("Sistema");

        light.addActionListener(e -> themeManager.setLightTheme());
        dark.addActionListener(e -> themeManager.setDarkTheme());
        system.addActionListener(e -> themeManager.setSystemTheme());

        menu.add(light);
        menu.add(dark);
        menu.add(system);
        menu.show(invoker, 0, invoker.getHeight());
    }

    private void setupContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem cut = new JMenuItem("Cortar");
        JMenuItem copy = new JMenuItem("Copiar");
        JMenuItem paste = new JMenuItem("Pegar");
        JMenuItem selectAll = new JMenuItem("Seleccionar todo");
        JMenuItem search = new JMenuItem("Buscar");
        JMenuItem replace = new JMenuItem("Reemplazar");

        cut.addActionListener(e -> textArea.cut());
        copy.addActionListener(e -> textArea.copy());
        paste.addActionListener(e -> textArea.paste());
        selectAll.addActionListener(e -> textArea.selectAll());
        search.addActionListener(e -> showSearchDialog());
        replace.addActionListener(e -> showReplaceDialog());

        contextMenu.add(cut);
        contextMenu.add(copy);
        contextMenu.add(paste);
        contextMenu.addSeparator();
        contextMenu.add(search);
        contextMenu.add(replace);
        contextMenu.addSeparator();
        contextMenu.add(selectAll);

        textArea.setComponentPopupMenu(contextMenu);

        contextMenu.addPopupMenuListener(new PopupMenuListener() {
    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        try {
            Transferable clipboard = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            boolean canPaste = clipboard != null && clipboard.isDataFlavorSupported(DataFlavor.stringFlavor);
            paste.setEnabled(canPaste);
        } catch (Exception ex) {
            paste.setEnabled(false);
        }
    }
    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
    @Override
    public void popupMenuCanceled(PopupMenuEvent e) { }
});

    }

    private void showSearchDialog() {
        String query = JOptionPane.showInputDialog(this, "Buscar:");
        if (query != null && !query.isEmpty()) {
            String text = textArea.getText();
            int index = text.indexOf(query);
            if (index != -1) {
                textArea.requestFocus();
                textArea.select(index, index + query.length());
            } else {
                showMessage("Texto no encontrado.");
            }
        }
    }

    private void showReplaceDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JTextField searchField = new JTextField();
        JTextField replaceField = new JTextField();

        panel.add(new JLabel("Buscar:"));
        panel.add(searchField);
        panel.add(new JLabel("Reemplazar con:"));
        panel.add(replaceField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Buscar y Reemplazar", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String search = searchField.getText();
            String replace = replaceField.getText();
            if (!search.isEmpty()) {
                textArea.setText(textArea.getText().replace(search, replace));
            }
        }
    }

    private void setupStatusUpdater() {
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateStatus(); }
            public void removeUpdate(DocumentEvent e) { updateStatus(); }
            public void changedUpdate(DocumentEvent e) { updateStatus(); }
        });
        updateStatus();
    }

    private void updateStatus() {
        int lines = textArea.getLineCount();
        int chars = textArea.getText().length();
        String size = formatSize(chars);
        String fileName = currentFile != null ? currentFile.getName() : "[Sin nombre]";
        statusLabel.setText("Líneas: " + lines + " | Tamaño: " + size + " | Archivo: " + fileName);
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        return kb < 1024 ? df.format(kb) + " KB" : df.format(kb / 1024) + " MB";
    }

    private void setupAutoSave() {
        autoSaveTimer = new Timer();
        autoSaveTimer.schedule(new TimerTask() {
            public void run() {
                if (currentFile != null) {
                    saveFile();
                    System.out.println("Autoguardado...");
                }
            }
        }, 10000, 10000);
    }

    private void setupWindowCloseConfirmation() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Object[] options = {"Guardar", "Guardar como", "Cancelar"};
                int result = JOptionPane.showOptionDialog(
                        Notevia.this,
                        "¿Deseas guardar los cambios antes de salir?",
                        "Salir",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (result == 0) saveFile();
                else if (result == 1) saveFileAs();
                else return;

                dispose();
                System.exit(0);
            }
        });
    }

    private void showFontConfig() {
        JDialog dialog = new JDialog(this, "Configuración de fuente", true);
        dialog.setSize(400, 150);
        dialog.setLayout(new FlowLayout());

        JComboBox<String> fonts = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        JSpinner size = new JSpinner(new SpinnerNumberModel(14, 8, 72, 1));
        JButton apply = new JButton("Aplicar");

        apply.addActionListener(e -> {
            String fontName = (String) fonts.getSelectedItem();
            int fontSize = (int) size.getValue();
            textArea.setFont(new Font(fontName, Font.PLAIN, fontSize));
            dialog.dispose();
        });

        dialog.add(new JLabel("Fuente:"));
        dialog.add(fonts);
        dialog.add(new JLabel("Tamaño:"));
        dialog.add(size);
        dialog.add(apply);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                textArea.setText("");
                String line;
                while ((line = reader.readLine()) != null) {
                    textArea.append(line + "\n");
                }
                updateStatus();
            } catch (IOException e) {
                showError("Error al abrir archivo.");
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            writer.write(textArea.getText());
            updateStatus();
        } catch (IOException e) {
            showError("Error al guardar archivo.");
        }
    }

    private void saveFileAs() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            saveFile();
        }
    }

    private void exitApp() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void showHelp() {
        try {
            Desktop.getDesktop().browse(new java.net.URI("https://www.example.com/ayuda"));
        } catch (Exception e) {
            showMessage("No se pudo abrir la página de ayuda.");
        }
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Mensaje", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
