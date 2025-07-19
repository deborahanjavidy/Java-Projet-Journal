import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class NoteEditorPanel extends JPanel {
    private JTextField titreField;
    private JComboBox<String> humeurBox;
    private JTextArea contenuArea;
    private JButton saveButton, deleteButton, closeButton;
    private Integer noteId; 
    private Integer userId;
    private Runnable onClose;

    public NoteEditorPanel(Integer userId, Integer noteId, Runnable onClose) {
        this.userId = userId;
        this.noteId = noteId;
        this.onClose = onClose;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(32, 48, 32, 48));

      
        JPanel topRow = new JPanel();
        topRow.setLayout(new BoxLayout(topRow, BoxLayout.X_AXIS));
        topRow.setBackground(Color.WHITE);
      
        titreField = new JTextField();
        titreField.setFont(new Font("SansSerif", Font.BOLD, 20));
        titreField.setMaximumSize(new Dimension(220, 40));
        titreField.setPreferredSize(new Dimension(220, 40));
        titreField.setBorder(BorderFactory.createTitledBorder("Titre (obligatoire)"));
        topRow.add(titreField);
        topRow.add(Box.createHorizontalStrut(12));
      
        JPanel humeurPanel = new JPanel();
        humeurPanel.setLayout(new BoxLayout(humeurPanel, BoxLayout.X_AXIS));
        humeurPanel.setBackground(Color.WHITE);
        JLabel humeurLabel = new JLabel("Humeur : ");
        humeurLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        humeurPanel.add(humeurLabel);
        humeurBox = new JComboBox<>(new String[]{"😀", "😐", "😢", "😡", "😍", "😴"});
        humeurBox.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        humeurBox.setPreferredSize(new Dimension(60, 40));
        humeurBox.setMaximumSize(new Dimension(60, 40));
        humeurBox.setBorder(BorderFactory.createEmptyBorder(8,0,0,0));
        humeurPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        humeurPanel.add(humeurBox);
        topRow.add(humeurPanel);
        topRow.add(Box.createHorizontalStrut(16));
        
        saveButton = new JButton("💾 Enregistrer");
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        saveButton.setBackground(new Color(33, 150, 243));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.setBorder(BorderFactory.createLineBorder(new Color(33, 150, 243), 2, true));
        topRow.add(saveButton);
        topRow.add(Box.createHorizontalStrut(8));
        closeButton = new JButton("❌ Fermer");
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        closeButton.setBackground(new Color(230, 230, 230));
        closeButton.setForeground(Color.BLACK);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true));
        topRow.add(closeButton);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(topRow);
        add(Box.createVerticalStrut(16));

      
        contenuArea = new JTextArea(8, 30);
        contenuArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        contenuArea.setLineWrap(true);
        contenuArea.setWrapStyleWord(true);
        contenuArea.setBorder(BorderFactory.createTitledBorder("Contenu"));
        JScrollPane scrollPane = new JScrollPane(contenuArea);
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(scrollPane);
        add(Box.createVerticalStrut(8));
        if (noteId != null) {
            deleteButton = new JButton("🗑️ Supprimer");
            deleteButton.setFont(new Font("SansSerif", Font.BOLD, 16));
            deleteButton.setBackground(new Color(220, 53, 69));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);
            deleteButton.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
            deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            deleteButton.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69), 2, true));
            deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(deleteButton);
            add(Box.createVerticalStrut(8));
            deleteButton.addActionListener(e -> deleteNote());
        }
        saveButton.addActionListener(e -> saveNote());
        closeButton.addActionListener(e -> onClose.run());
        if (noteId != null) loadNote();
    }

    private void stylizeField(JTextField field, String title) {
        field.setFont(new Font("SansSerif", Font.BOLD, 18));
        field.setBorder(BorderFactory.createTitledBorder(title));
        field.setMaximumSize(new Dimension(400, 40));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
    private void stylizeArea(JTextArea area, String title) {
        area.setFont(new Font("SansSerif", Font.PLAIN, 16));
        area.setBorder(BorderFactory.createTitledBorder(title));
        area.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
    private JButton makeIconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(220, 230, 245), 1, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 36));
        return btn;
    }

    private void saveNote() {
        String titre = titreField.getText().trim();
        if (titre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le titre est obligatoire.");
            titreField.requestFocus();
            return;
        }
        String humeur = (String) humeurBox.getSelectedItem();
        String contenu = contenuArea.getText();
        try (Connection conn = Database.getConnection()) {
            if (noteId == null) {
                String sql = "INSERT INTO Note (titre, humeur, contenu, date_creation, id_utilisateur) VALUES (?, ?, ?, CURDATE(), ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, titre);
                stmt.setString(2, humeur);
                stmt.setString(3, contenu);
                stmt.setInt(4, userId);
                stmt.executeUpdate();
            } else {
                String sql = "UPDATE Note SET titre=?, humeur=?, contenu=? WHERE id_note=? AND id_utilisateur=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, titre);
                stmt.setString(2, humeur);
                stmt.setString(3, contenu);
                stmt.setInt(4, noteId);
                stmt.setInt(5, userId);
                stmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Note enregistrée !");
            onClose.run();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement.");
        }
    }

    private void deleteNote() {
        int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cette note ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = Database.getConnection()) {
            String sql = "DELETE FROM Note WHERE id_note=? AND id_utilisateur=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, noteId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Note supprimée !");
            onClose.run();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la suppression.");
        }
    }

    private void loadNote() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT titre, humeur, contenu FROM Note WHERE id_note=? AND id_utilisateur=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, noteId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                titreField.setText(rs.getString("titre"));
                humeurBox.setSelectedItem(rs.getString("humeur"));
                contenuArea.setText(rs.getString("contenu"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
} 