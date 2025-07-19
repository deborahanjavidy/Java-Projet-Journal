import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class CalendarPanel extends JPanel {
    private MainFrame mainFrame;
    private JLabel monthLabel;
    private JPanel calendarGrid;
    private LocalDate currentMonth;
    private Map<LocalDate, List<JournalEntry>> notesByDay;
    private boolean dayViewMode = false;
    private LocalDate selectedDay = null;
    private JTextField searchField = null;
    private JPanel dayViewPanel = null;
    private List<JournalEntry> filteredDayNotes = null;
    private JPanel notesListPanel;
    private JPanel searchPanel;

    public CalendarPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        currentMonth = LocalDate.now().withDayOfMonth(1);

    
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 150, 243));
        JButton prevBtn = makeNavButton("◀️");
        JButton nextBtn = makeNavButton("▶️");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        monthLabel.setForeground(Color.WHITE);
        header.add(prevBtn, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

       
        calendarGrid = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarGrid.setBackground(Color.WHITE);
        calendarGrid.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(calendarGrid, BorderLayout.CENTER);

        prevBtn.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            reloadCalendar();
        });
        nextBtn.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            reloadCalendar();
        });
    }

    public void reloadCalendar() {
        notesByDay = new HashMap<>();
        Integer userId = mainFrame.getUserId();
        if (userId == null) return;
        
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id_note, titre, humeur, contenu, date_creation FROM Note WHERE id_utilisateur=? AND date_creation >= ? AND date_creation <= ? ORDER BY date_creation, id_note";
            PreparedStatement stmt = conn.prepareStatement(sql);
            LocalDate firstDay = currentMonth.withDayOfMonth(1);
            LocalDate lastDay = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth());
            stmt.setInt(1, userId);
            stmt.setString(2, firstDay.toString());
            stmt.setString(3, lastDay.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate date = LocalDate.parse(rs.getString("date_creation"));
                String emoji = rs.getString("humeur");
                String titre = rs.getString("titre");
                String contenu = rs.getString("contenu");
                int idNote = rs.getInt("id_note");
                notesByDay.computeIfAbsent(date, d -> new ArrayList<>())
                    .add(new JournalEntry(idNote, emoji, titre, contenu, date));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        updateCalendarGrid();
    }

    private void updateCalendarGrid() {
        removeAll();
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        dayViewMode = false;
        selectedDay = null;
       
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 150, 243));
        JButton prevBtn = makeNavButton("◀️");
        JButton nextBtn = makeNavButton("▶️");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        monthLabel.setForeground(Color.WHITE);
        header.add(prevBtn, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    
        calendarGrid = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarGrid.setBackground(Color.WHITE);
        calendarGrid.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(calendarGrid, BorderLayout.CENTER);
        prevBtn.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            reloadCalendar();
        });
        nextBtn.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            reloadCalendar();
        });
        
        String mois = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        monthLabel.setText(mois.substring(0,1).toUpperCase() + mois.substring(1) + " " + currentMonth.getYear());
       
        String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (String j : jours) {
            JLabel lbl = new JLabel(j, SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
            lbl.setForeground(new Color(33, 150, 243));
            calendarGrid.add(lbl);
        }
        LocalDate firstDay = currentMonth.withDayOfMonth(1);
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue(); 
        int daysInMonth = currentMonth.lengthOfMonth();
    
        for (int i = 1; i < firstDayOfWeek; i++) {
            calendarGrid.add(new JLabel(""));
        }
       
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.withDayOfMonth(day);
            JPanel dayPanel = new JPanel(new BorderLayout());
            dayPanel.setBackground(Color.WHITE);
            dayPanel.setBorder(BorderFactory.createLineBorder(new Color(220,230,245), 1, true));
            JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            dayPanel.add(dayLabel, BorderLayout.NORTH);
            String emoji = getFirstNoteEmojiForDay(date);
            if (emoji != null) {
                JLabel emojiLabel = new JLabel(emoji);
                emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                emojiLabel.setHorizontalAlignment(SwingConstants.CENTER);
                emojiLabel.setVerticalAlignment(SwingConstants.CENTER);
                
                dayPanel.add(emojiLabel, BorderLayout.CENTER);
            }
            dayPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            dayPanel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    showDayView(date);
                }
            });
            calendarGrid.add(dayPanel);
        }
        
        int totalCells = daysInMonth + firstDayOfWeek - 1;
        int rest = 7 - (totalCells % 7);
        if (rest < 7) {
            for (int i = 0; i < rest; i++) calendarGrid.add(new JLabel(""));
        }
        calendarGrid.revalidate();
        calendarGrid.repaint();
        revalidate();
        repaint();
    }

    private void showDayView(LocalDate date) {
       
        if (date == null) return;
        removeAll();
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        dayViewMode = true;
        selectedDay = date;
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
      
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        JButton prevBtn = makeNavButton("◀");
        JButton nextBtn = makeNavButton("▶");
        JLabel dateLabel = new JLabel(date.toString(), SwingConstants.CENTER);
        dateLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        dateLabel.setForeground(new Color(33, 150, 243));
        header.add(prevBtn, BorderLayout.WEST);
        header.add(dateLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        mainPanel.add(header);
        mainPanel.add(Box.createVerticalStrut(8));
      
        searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        searchField = new JTextField();
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        searchField.setBorder(BorderFactory.createTitledBorder("Rechercher une note..."));
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        mainPanel.add(searchPanel);
        mainPanel.add(Box.createVerticalStrut(8));
 
        notesListPanel = new JPanel();
        notesListPanel.setLayout(new BoxLayout(notesListPanel, BoxLayout.Y_AXIS));
        notesListPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(notesListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane);
        add(mainPanel, BorderLayout.CENTER);
        
        prevBtn.addActionListener(e -> showDayView(date.minusDays(1)));
        nextBtn.addActionListener(e -> showDayView(date.plusDays(1)));
        
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterDayNotes(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterDayNotes(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterDayNotes(); }
        });
        filterDayNotes();
        revalidate();
        repaint();
    }

    private void filterDayNotes() {
        notesListPanel.removeAll();
        if (selectedDay == null) return;
        String filter = searchField.getText().toLowerCase();
        List<JournalEntry> entries = notesByDay.get(selectedDay);
        filteredDayNotes = new ArrayList<>();
        if (entries != null) {
            for (JournalEntry entry : entries) {
                String noteText = entry.titre + " " + entry.contenu;
                if (filter.isEmpty() || noteText.toLowerCase().contains(filter)) {
                    filteredDayNotes.add(entry);
                }
            }
        }
        if (filteredDayNotes.isEmpty()) {
            JLabel empty = new JLabel("Aucune note pour ce jour", SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.ITALIC, 16));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            notesListPanel.add(Box.createVerticalGlue());
            notesListPanel.add(empty);
            notesListPanel.add(Box.createVerticalGlue());
        } else {
            for (JournalEntry entry : filteredDayNotes) {
                notesListPanel.add(makeNotePanel(new NoteData(entry.idNote, entry.emoji, entry.titre, entry.contenu, entry.date.toString())));
            }
        }
        notesListPanel.revalidate();
        notesListPanel.repaint();
    }

    private void showDayDetail(LocalDate date) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Notes du " + date, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 0));
      
        JPanel header = new JPanel(new BorderLayout());
        JButton prevBtn = new JButton("◀");
        JButton nextBtn = new JButton("▶");
        JLabel dateLabel = new JLabel(date.toString(), SwingConstants.CENTER);
        dateLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(prevBtn, BorderLayout.WEST);
        header.add(dateLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);
        dialog.add(header, BorderLayout.NORTH);
    
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        List<JournalEntry> entries = notesByDay.get(date);
        if (entries == null || entries.isEmpty()) {
            JLabel empty = new JLabel("Aucune note pour ce jour", SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.ITALIC, 16));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(Box.createVerticalGlue());
            center.add(empty);
            center.add(Box.createVerticalGlue());
        } else {
            for (JournalEntry entry : entries) {
                JPanel notePanel = new JPanel(new BorderLayout());
                JLabel emoji = new JLabel(entry.emoji, SwingConstants.CENTER);
                emoji.setFont(new Font("SansSerif", Font.PLAIN, 22));
                notePanel.add(emoji, BorderLayout.WEST);
                JLabel titre = new JLabel(entry.titre);
                titre.setFont(new Font("SansSerif", Font.BOLD, 16));
                notePanel.add(titre, BorderLayout.CENTER);
                center.add(notePanel);
                center.add(Box.createVerticalStrut(8));
            }
        }
        dialog.add(center, BorderLayout.CENTER);
        
        prevBtn.addActionListener(e -> {
            dialog.dispose();
            showDayDetail(date.minusDays(1));
        });
        nextBtn.addActionListener(e -> {
            dialog.dispose();
            showDayDetail(date.plusDays(1));
        });
        dialog.setVisible(true);
    }

    private void showNotesPopup(LocalDate date) {
        List<JournalEntry> entries = notesByDay.get(date);
        if (entries == null) return;
        StringBuilder sb = new StringBuilder();
        for (JournalEntry entry : entries) {
            sb.append(entry.emoji).append("  ").append(entry.titre).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Journaux du " + date, JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton makeNavButton(String txt) {
        JButton btn = new JButton(txt);
        btn.setFont(new Font("SansSerif", Font.BOLD, 20));
        btn.setBackground(new Color(33, 150, 243));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

 
    private static class JournalEntry {
        int idNote;
        String emoji;
        String titre;
        String contenu;
        LocalDate date;
        JournalEntry(int id, String emoji, String titre, String contenu, LocalDate date) {
            this.idNote = id;
            this.emoji = emoji;
            this.titre = titre;
            this.contenu = contenu;
            this.date = date;
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
    private class NoteListCellRenderer extends JPanel implements ListCellRenderer<NoteData> {
        private JLabel emojiLabel = new JLabel();
        private JLabel titleLabel = new JLabel();
        private JLabel contentLabel = new JLabel();
        private JLabel dateLabel = new JLabel();
        private JButton deleteButton = new JButton("🗑️");
        public NoteListCellRenderer() {
            setLayout(new BorderLayout(8, 0));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
            centerPanel.setOpaque(false);
            emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            centerPanel.add(emojiLabel);
            centerPanel.add(Box.createHorizontalStrut(8));
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            centerPanel.add(titleLabel);
            centerPanel.add(Box.createHorizontalStrut(8));
            contentLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            contentLabel.setForeground(Color.GRAY);
            centerPanel.add(contentLabel);
            add(centerPanel, BorderLayout.CENTER);
            dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            dateLabel.setForeground(new Color(120,120,120));
            add(dateLabel, BorderLayout.EAST);
            deleteButton.setBackground(Color.WHITE);
            deleteButton.setForeground(new Color(220,53,69));
            deleteButton.setFocusPainted(false);
            deleteButton.setBorder(BorderFactory.createEmptyBorder(2,8,2,8));
            deleteButton.setFont(new Font("SansSerif", Font.BOLD, 16));
            add(deleteButton, BorderLayout.LINE_END);
        }
        @Override
        public Component getListCellRendererComponent(JList<? extends NoteData> list, NoteData value, int index, boolean isSelected, boolean cellHasFocus) {
            emojiLabel.setText(value.humeur);
            titleLabel.setText(value.titre);
            String extrait = value.contenu.length() > 30 ? value.contenu.substring(0, 30) + "..." : value.contenu;
            contentLabel.setText(extrait);
            dateLabel.setText(value.date);
            setBackground(isSelected ? new Color(33,150,243,40) : Color.WHITE);
            deleteButton.setVisible(true);
            deleteButton.addActionListener(e -> deleteNote(value.idNote));
            return this;
        }
    }

    private void loadDayNotes(LocalDate date) {
        notesListPanel.removeAll();
        Integer userId = mainFrame.getUserId();
        if (userId == null) return;
        boolean hasNotes = false;
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id_note, titre, contenu, humeur, date_creation FROM Note WHERE id_utilisateur=? AND date_creation=? ORDER BY date_creation DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                hasNotes = true;
                int idNote = rs.getInt("id_note");
                String titre = rs.getString("titre");
                String contenu = rs.getString("contenu");
                String humeur = rs.getString("humeur");
                String dateStr = rs.getString("date_creation");
                notesListPanel.add(makeNotePanel(new NoteData(idNote, humeur, titre, contenu, dateStr)));
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
        emojiLabel.setBorder(BorderFactory.createEmptyBorder(6,0,6,0));
        panel.add(emojiLabel);
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
            int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cette note ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteNote(note.idNote);
                showDayView(selectedDay); }
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

    private void deleteNote(int idNote) {
        Integer userId = mainFrame.getUserId();
        if (userId == null) return;
        try (Connection conn = Database.getConnection()) {
            String sql = "DELETE FROM Note WHERE id_utilisateur=? AND id_note=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, idNote);
            stmt.executeUpdate();
            reloadCalendar();  showDayView(selectedDay);
            } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void openNoteEditor(int idNote) {
        mainFrame.contentPanel.removeAll();
        NoteEditorPanel editor = new NoteEditorPanel(mainFrame.getUserId(), idNote, () -> {
            mainFrame.showPanel("calendar");
        });
        mainFrame.contentPanel.add(editor, BorderLayout.CENTER);
        mainFrame.contentPanel.revalidate();
        mainFrame.contentPanel.repaint();
    }

    private String getFirstNoteEmojiForDay(LocalDate day) {
        Integer userId = mainFrame.getUserId();
        if (userId == null) return null;
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT humeur FROM Note WHERE id_utilisateur=? AND date_creation=? ORDER BY id_note ASC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setDate(2, java.sql.Date.valueOf(day));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("humeur");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
} 