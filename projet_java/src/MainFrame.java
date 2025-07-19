import javax.swing.*;
import java.awt.*;
import java.io.*;

public class MainFrame extends JFrame {
    private Integer idUtilisateur = null;
    public HomePanel homePanel;
    private SettingsPanel settingsPanel;
    public JPanel contentPanel;
    private CalendarPanel calendarPanel;
    private JPanel navBar;

    public MainFrame() {
        setTitle("Journal d'Humeur");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

      
        contentPanel = new JPanel(new BorderLayout());
        homePanel = new HomePanel(this);
        settingsPanel = new SettingsPanel(this);
        calendarPanel = new CalendarPanel(this);
        contentPanel.add(homePanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

     
        navBar = new JPanel(new GridLayout(1, 4, 40, 0));
        navBar.setBackground(Color.WHITE);
        navBar.setBorder(BorderFactory.createEmptyBorder(8, 32, 24, 32));
        JButton settingsButton = makeRoundIconButton("👤", "Profil utilisateur");
        JButton homeButton = makeRoundIconButton("📖", "Accueil/Livre");
        JButton pencilButton = makeRoundIconButton("🖊️", "Nouveau");
        JButton calendarButton = makeRoundIconButton("📅", "Calendrier");
        navBar.add(settingsButton);
        navBar.add(homeButton);
        navBar.add(pencilButton);
        navBar.add(calendarButton);
        add(navBar, BorderLayout.SOUTH);

      
        settingsButton.addActionListener(e -> showPanel("settings"));
        homeButton.addActionListener(e -> showPanel("home"));
        pencilButton.addActionListener(e -> {
            if (idUtilisateur != null) {
                contentPanel.removeAll();
                NoteEditorPanel editor = new NoteEditorPanel(idUtilisateur, null, () -> {
                    showPanel("home");
                });
                contentPanel.add(editor, BorderLayout.CENTER);
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });
        calendarButton.addActionListener(e -> showPanel("calendar"));

      
        this.idUtilisateur = SessionManager.loadUserId();
        if (this.idUtilisateur != null) {
            
            try (java.sql.Connection conn = Database.getConnection()) {
                String sql = "SELECT COUNT(*) FROM utilisateur WHERE id_utilisateur=?";
                java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, this.idUtilisateur);
                java.sql.ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showPanel("home");
                } else {
                    this.idUtilisateur = null;
                    SessionManager.clearSession();
                    showPanel("welcome");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                this.idUtilisateur = null;
                SessionManager.clearSession();
                showPanel("welcome");
            }
        } else {
            showPanel("welcome");
        }
        setVisible(true);
    }

    public void setUserId(Integer id) {
        this.idUtilisateur = id;
        if (id != null) {
            SessionManager.saveUserId(id);
        } else {
            SessionManager.clearSession();
        }
    }
    public Integer getUserId() {
        return idUtilisateur;
    }

    public void showPanel(String name) {
        contentPanel.removeAll();
        if (idUtilisateur == null) {
            navBar.setVisible(false);
            if (name.equals("settings")) {
               
                contentPanel.add(new LoginPanel(this), BorderLayout.CENTER);
            } else if (name.equals("login")) {
                contentPanel.add(new LoginPanel(this), BorderLayout.CENTER);
            } else if (name.equals("signup")) {
                contentPanel.add(new SignUpPanel(this), BorderLayout.CENTER);
            } else {
                contentPanel.add(new WelcomePanel(this), BorderLayout.CENTER);
            }
        } else {
            navBar.setVisible(true);
            if (name.equals("home")) {
                homePanel.loadNotes();
                contentPanel.add(homePanel, BorderLayout.CENTER);
            } else if (name.equals("settings")) {
                settingsPanel.reloadUserInfo();
                contentPanel.add(settingsPanel, BorderLayout.CENTER);
            } else if (name.equals("calendar")) {
                calendarPanel.reloadCalendar();
                contentPanel.add(calendarPanel, BorderLayout.CENTER);
            }
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void updateCalendarPanel() {
        calendarPanel.reloadCalendar();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void openNoteEditor(int noteId) {
        contentPanel.removeAll();
        NoteEditorPanel editor = new NoteEditorPanel(getUserId(), noteId, () -> {
            showPanel("home");
        });
        contentPanel.add(editor, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JButton makeRoundIconButton(String emoji, String tooltip) {
        JButton btn = new JButton(emoji);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 28));
        btn.setPreferredSize(new Dimension(48, 48));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(220, 230, 245));
                btn.setOpaque(true);
                btn.setBorder(BorderFactory.createLineBorder(new Color(33, 150, 243), 2, true));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
                btn.setOpaque(false);
                btn.setBorder(BorderFactory.createEmptyBorder());
            }
        });
        return btn;
    }
} 