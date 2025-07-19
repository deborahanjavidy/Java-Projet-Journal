import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.border.EmptyBorder;

public class HomePanel extends JPanel {
    private MainFrame mainFrame;
    private DefaultListModel<NoteData> notesModel;
    private JList<NoteData> notesList;
    private JPanel centerPanel;
    private JButton addNoteButton;
    private ArrayList<Integer> noteIds;
    private JTextField searchField;
    private JPanel searchPanel;
   private JPanel notesListPanel;

    public HomePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 255));

      
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 150, 243));
        JLabel title = new JLabel("Mon journal", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        
        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(24, 24, 24, 24),
            BorderFactory.createLineBorder(new Color(220, 230, 245), 1, true)
        ));
        notesListPanel = new JPanel();
        notesListPanel.setLayout(new BoxLayout(notesListPanel, BoxLayout.Y_AXIS));
        notesListPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(notesListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

      
        searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setBorder(BorderFactory.createTitledBorder("Rechercher une note..."));
        searchPanel.add(searchField, BorderLayout.CENTER);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterNotes(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterNotes(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterNotes(); }
        });

       
        loadNotes();
    }

    public void loadNotes() {
        notesListPanel.removeAll();
        Integer userId = mainFrame.getUserId();
        if (userId == null) return;
        boolean hasNotes = false;
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id_note, titre, contenu, humeur, date_creation FROM Note WHERE id_utilisateur=? ORDER BY date_creation DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                hasNotes = true;
                int idNote = rs.getInt("id_note");
                String titre = rs.getString("titre");
                String contenu = rs.getString("contenu");
                String humeur = rs.getString("humeur");
                String date = rs.getString("date_creation");
                notesListPanel.add(makeNotePanel(new NoteData(idNote, humeur, titre, contenu, date)));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (!hasNotes) {
            JLabel emptyLabel = new JLabel("Pas encore de note", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 18));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            notesListPanel.add(Box.createVerticalStrut(32));
            notesListPanel.add(emptyLabel);
        }
        notesListPanel.revalidate();
        notesListPanel.repaint();
    }

    private void filterNotes() {
        String filter = searchField.getText().toLowerCase();
        notesListPanel.removeAll();
        Integer userId = mainFrame.getUserId();
        if (userId == null) return;
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id_note, titre, contenu, humeur, date_creation FROM Note WHERE id_utilisateur=? ORDER BY date_creation DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int idNote = rs.getInt("id_note");
                String titre = rs.getString("titre");
                String contenu = rs.getString("contenu");
                String humeur = rs.getString("humeur");
                String date = rs.getString("date_creation");
                String noteText = (date + " - " + titre + ": " + contenu).toLowerCase();
                if (filter.isEmpty() || noteText.contains(filter)) {
                    notesListPanel.add(makeNotePanel(new NoteData(idNote, humeur, titre, contenu, date)));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        notesListPanel.revalidate();
        notesListPanel.repaint();
    }

    public void openNoteEditor(Integer noteId) {
        centerPanel.removeAll();
        NoteEditorPanel editor = new NoteEditorPanel(mainFrame.getUserId(), noteId, () -> {
            centerPanel.removeAll();
            JScrollPane scrollPane = new JScrollPane(notesListPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            centerPanel.add(scrollPane, BorderLayout.CENTER);
            centerPanel.add(searchPanel, BorderLayout.NORTH);
            loadNotes();
            centerPanel.revalidate();
            centerPanel.repaint();
        });
        centerPanel.add(editor, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private static ImageIcon loadIcon(String path, int size) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
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

    private void deleteNote(int noteId) {
        int confirm = JOptionPane.showConfirmDialog(mainFrame, "Voulez-vous vraiment supprimer cette note ?", "Confirmation de suppression", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Database.getConnection()) {
                String sql = "DELETE FROM Note WHERE id_note = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, noteId);
                stmt.executeUpdate();
                loadNotes();mainFrame.updateCalendarPanel();  } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Erreur lors de la suppression de la note.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class NoteData {
        int idNote;
        String humeur;
        String titre;
        String contenu;
        String date;
        public NoteData(int id, String humeur, String titre, String contenu, String date) {
            this.idNote = id;
            this.humeur = humeur;
            this.titre = titre;
            this.contenu = contenu;
            this.date = date;
        }
    }
    private JPanel makeNotePanel(NoteData note) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        panel.setPreferredSize(new Dimension(0, 56));
        JLabel emojiLabel = new JLabel(note.humeur);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        emojiLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        emojiLabel.setBorder(BorderFactory.createEmptyBorder(6,0,6,0)); panel.add(emojiLabel);
        panel.add(Box.createHorizontalStrut(12));
        JLabel titleLabel = new JLabel(note.titre);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createHorizontalStrut(8));
        JLabel contentLabel = new JLabel(note.contenu.length() > 30 ? note.contenu.substring(0, 30) + "..." : note.contenu);
        contentLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        contentLabel.setForeground(new Color(100,100,100));
        contentLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.add(contentLabel);
        panel.add(Box.createHorizontalGlue());
        JLabel dateLabel = new JLabel(note.date);
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(170,170,170));
        dateLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.add(dateLabel);
        panel.add(Box.createHorizontalStrut(12));
        JButton deleteButton = new JButton("🗑️");
        deleteButton.setBackground(new Color(0,0,0,0));
        deleteButton.setOpaque(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setForeground(new Color(220,53,69));
        deleteButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        deleteButton.setAlignmentY(Component.CENTER_ALIGNMENT);
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> {
            deleteNote(note.idNote);
        });
        panel.add(deleteButton);
      
        MouseAdapter editListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() != deleteButton) {
                    openNoteEditor(note.idNote);
                }
            }
        };
        panel.addMouseListener(editListener);
        emojiLabel.addMouseListener(editListener);
        titleLabel.addMouseListener(editListener);
        contentLabel.addMouseListener(editListener);
        dateLabel.addMouseListener(editListener);
        return panel;
    }
} 