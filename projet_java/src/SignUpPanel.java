import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SignUpPanel extends JPanel {
    public SignUpPanel(MainFrame mainFrame) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Inscription ✍️", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        add(title);

        JLabel emailLabel = new JLabel("Email :");
        stylizeLabel(emailLabel);
        add(emailLabel);
        JTextField emailField = new JTextField();
        stylizeField(emailField);
        add(emailField);
        add(Box.createVerticalStrut(10));

        JLabel passLabel = new JLabel("Mot de passe :");
        stylizeLabel(passLabel);
        add(passLabel);
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.X_AXIS));
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setMaximumSize(new Dimension(300, 36));
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPasswordField passwordField = new JPasswordField();
        stylizeField(passwordField);
        passwordField.setMaximumSize(new Dimension(260, 36));
        passwordPanel.add(passwordField);
        passwordPanel.add(Box.createHorizontalStrut(6));
        JButton eyeButton = new JButton("\uD83D\uDC41\uFE0F"); // 👁️
        eyeButton.setFocusable(false);
        eyeButton.setBorderPainted(false);
        eyeButton.setContentAreaFilled(false);
        eyeButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        eyeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        passwordPanel.add(eyeButton);
        add(passwordPanel);

        final boolean[] show = {false};
        eyeButton.addActionListener(e -> {
            show[0] = !show[0];
            if (show[0]) {
                passwordField.setEchoChar((char)0);
                eyeButton.setText("\uD83D\uDC41\u200D\uD83D\uDDE8"); // 👁️‍🗨️
            } else {
                passwordField.setEchoChar('•');
                eyeButton.setText("\uD83D\uDC41\uFE0F"); // 👁️
            }
        });
        add(Box.createVerticalStrut(20));

        JButton signupButton = new JButton("✅ S'inscrire");
        stylizeButton(signupButton, new Color(76, 175, 80), Color.WHITE);
        signupButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            try (Connection conn = Database.getConnection()) {
                String checkSql = "SELECT * FROM utilisateur WHERE email=?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(mainFrame, "Email déjà utilisé.");
                    return;
                }
                String sql = "INSERT INTO utilisateur (email, mot_de_passe) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, email);
                stmt.setString(2, password);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(mainFrame, "Inscription réussie ! Vous pouvez vous connecter.");
                mainFrame.showPanel("login");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Erreur lors de l'inscription.");
            }
        });
        add(signupButton);
        add(Box.createVerticalStrut(10));

        JButton backButton = new JButton("Retour ◀️");
        stylizeButton(backButton, new Color(200, 200, 200), Color.BLACK);
        backButton.addActionListener(e -> mainFrame.showPanel("welcome"));
        add(backButton);
    }

    private void stylizeButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 20));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(14, 36, 14, 36));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1, true),
            BorderFactory.createEmptyBorder(14, 36, 14, 36)
        ));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
    }
    private void stylizeLabel(JLabel lbl) {
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
    }
    private void stylizeField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setMaximumSize(new Dimension(300, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 245), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
} 