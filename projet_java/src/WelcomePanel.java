import javax.swing.*;
import java.awt.*;

public class WelcomePanel extends JPanel {
    public WelcomePanel(MainFrame mainFrame) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("👋 Bienvenue sur Journal d'Humeur !", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(32, 0, 8, 0));
        add(title);

        JLabel subtitle = new JLabel("Écrivez chaque jour votre journal et votre humeur.", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 32, 0));
        add(subtitle);

        JButton loginButton = new JButton("🔑 Connexion");
        stylizeButton(loginButton, new Color(33, 150, 243), Color.WHITE);
        loginButton.addActionListener(e -> mainFrame.showPanel("login"));
        add(loginButton);
        add(Box.createVerticalStrut(20));


        
        JButton signupButton = new JButton("✅ Inscription");
        stylizeButton(signupButton, new Color(76, 175, 80), Color.WHITE);
        signupButton.addActionListener(e -> mainFrame.showPanel("signup"));
        add(signupButton);
    }

    private void stylizeButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 20));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(16, 40, 16, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1, true),
            BorderFactory.createEmptyBorder(16, 40, 16, 40)
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
} 