import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SettingsPanel extends JPanel {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton showHideButton;
    private JButton editEmailButton;
    private JButton editPasswordButton;
    private JButton saveButton;
    private boolean passwordVisible = false;
    private boolean emailEditable = false;
    private boolean passwordEditable = false;
    private MainFrame mainFrame;

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("👤 Profil utilisateur", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        add(title);

        
        JPanel emailPanel = new JPanel();
        emailPanel.setLayout(new BoxLayout(emailPanel, BoxLayout.X_AXIS));
        emailPanel.setBackground(Color.WHITE);
        JLabel emailLabel = new JLabel("Email :");
        stylizeLabel(emailLabel);
        emailPanel.add(emailLabel);
        emailField = new JTextField();
        stylizeField(emailField);
        emailField.setEditable(false);
        emailPanel.add(emailField);
        editEmailButton = new JButton("✏️ Modifier");
        stylizeSmallButton(editEmailButton);
        editEmailButton.addActionListener(e -> toggleEmailEdit());
        emailPanel.add(editEmailButton);
        add(emailPanel);
        add(Box.createVerticalStrut(16));

        
        JPanel passPanel = new JPanel();
        passPanel.setLayout(new BoxLayout(passPanel, BoxLayout.X_AXIS));
        passPanel.setBackground(Color.WHITE);
        JLabel passLabel = new JLabel("Mot de passe :");
        stylizeLabel(passLabel);
        passPanel.add(passLabel);
        passwordField = new JPasswordField();
        stylizeField(passwordField);
        passwordField.setEditable(false);
        passwordField.setEchoChar('•');
        passPanel.add(passwordField);
        showHideButton = new JButton("👁️");
        stylizeSmallButton(showHideButton);
        showHideButton.addActionListener(e -> togglePasswordVisibility());
        passPanel.add(showHideButton);
        editPasswordButton = new JButton("✏️ Modifier");
        stylizeSmallButton(editPasswordButton);
        editPasswordButton.addActionListener(e -> togglePasswordEdit());
        passPanel.add(editPasswordButton);
        add(passPanel);
        add(Box.createVerticalStrut(16));

       
        saveButton = new JButton("💾 Enregistrer");
        stylizeButton(saveButton, new Color(33, 150, 243), Color.WHITE);
        saveButton.setVisible(false);
        saveButton.addActionListener(e -> saveProfile());
        add(saveButton);
        add(Box.createVerticalStrut(20));

    
        JButton deleteButton = new JButton("🗑️ Supprimer le compte");
        stylizeButton(deleteButton, new Color(220, 53, 69), Color.WHITE);
        deleteButton.addActionListener(e -> deleteAccount());
        add(deleteButton);
        add(Box.createVerticalStrut(10));

        JButton logoutButton = new JButton("🚪 Déconnexion");
        stylizeButton(logoutButton, new Color(200, 200, 200), Color.BLACK);
        logoutButton.addActionListener(e -> logout());
        add(logoutButton);

        loadUserInfo();
    }

    private void stylizeButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(bg, 2, true));
    }
    private void stylizeSmallButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setBackground(new Color(240, 240, 240));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    private void stylizeLabel(JLabel lbl) {
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 10));
    }
    private void stylizeField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setMaximumSize(new Dimension(300, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 245), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }
    private void togglePasswordVisibility() {
        if (passwordVisible) {
            passwordField.setEchoChar('•');
            showHideButton.setText("👁️");
        } else {
            passwordField.setEchoChar((char) 0);
            showHideButton.setText("🙈");
        }
        passwordVisible = !passwordVisible;
    }
    private void toggleEmailEdit() {
        emailEditable = !emailEditable;
        emailField.setEditable(emailEditable);
        editEmailButton.setText(emailEditable ? "❌ Annuler" : "✏️ Modifier");
        updateSaveButtonVisibility();
    }
    private void togglePasswordEdit() {
        passwordEditable = !passwordEditable;
        passwordField.setEditable(passwordEditable);
        editPasswordButton.setText(passwordEditable ? "❌ Annuler" : "✏️ Modifier");
        updateSaveButtonVisibility();
    }
    private void updateSaveButtonVisibility() {
        saveButton.setVisible(emailEditable || passwordEditable);
    }
    private void loadUserInfo() {
        Integer userId = mainFrame.getUserId();
        if (userId == null) {
            SwingUtilities.invokeLater(() -> mainFrame.showPanel("login"));
            return;
        }
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT email, mot_de_passe FROM utilisateur WHERE id_utilisateur=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                emailField.setText(rs.getString("email"));
                passwordField.setText(rs.getString("mot_de_passe"));
                passwordField.setEchoChar('•');
                passwordVisible = false;
                showHideButton.setText("👁️");
                emailField.setEditable(false);
                passwordField.setEditable(false);
                emailEditable = false;
                passwordEditable = false;
                editEmailButton.setText("✏️ Modifier");
                editPasswordButton.setText("✏️ Modifier");
                saveButton.setVisible(false);
            } else {
               
                mainFrame.setUserId(null);
                SessionManager.clearSession();
                SwingUtilities.invokeLater(() -> mainFrame.showPanel("login"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private void saveProfile() {
        Integer userId = mainFrame.getUserId();
        if (userId == null) return;
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE utilisateur SET email=?, mot_de_passe=? WHERE id_utilisateur=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Profil mis à jour !");
            loadUserInfo();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour.");
        }
    }
    private void deleteAccount() {
        Integer userId = mainFrame.getUserId();
        if (userId == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Supprimer votre compte ? Cette action est irréversible.", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = Database.getConnection()) {
            String sql = "DELETE FROM utilisateur WHERE id_utilisateur=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Compte supprimé.");
            mainFrame.setUserId(null);
            mainFrame.showPanel("home");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la suppression.");
        }
    }
    private void logout() {
        mainFrame.setUserId(null);
        mainFrame.showPanel("login");
    }

    public void reloadUserInfo() {
        loadUserInfo();
    }
}
