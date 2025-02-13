package com.company;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DodawanieUzytkownika extends JFrame {
    private JPanel mainPanel;
    private JTextField imieField;
    private JTextField nazwiskoField;
    private JTextField peselField;
    private JButton dodajButton;
    private JButton usunButton;
    private JButton edytujUżytkownikaButton;
    private JTextField komunikatField;
    private JTable uzytkownicyTable;
    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private UzytkownikListener listener;

    public void setUzytkownikListener(UzytkownikListener listener) {
        this.listener = listener;
    }

    public DodawanieUzytkownika() {
        setTitle("Zarządzanie klientami");
        setContentPane(mainPanel);
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Jeżeli tabela nie została jeszcze utworzona, utwórz ją oraz opakuj w JScrollPane
        if (uzytkownicyTable == null) {
            uzytkownicyTable = new JTable();
        }
        if (uzytkownicyTable.getParent() == null) {
            JScrollPane scrollPane = new JScrollPane(uzytkownicyTable);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
        }
        loadUsers();

        // Dodawanie nowego użytkownika
        dodajButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String imie = imieField.getText().trim();
                String nazwisko = nazwiskoField.getText().trim();
                String pesel = peselField.getText().trim();
                if (imie.isEmpty() || nazwisko.isEmpty() || pesel.isEmpty()) {
                    JOptionPane.showMessageDialog(DodawanieUzytkownika.this,
                            "Wszystkie pola muszą być wypełnione!", "Błąd", JOptionPane.ERROR_MESSAGE);
                } else if (!pesel.matches("\\d{11}")) {
                    JOptionPane.showMessageDialog(DodawanieUzytkownika.this,
                            "PESEL musi składać się z dokładnie 11 cyfr!", "Błąd", JOptionPane.ERROR_MESSAGE);
                } else {
                    addUserToDatabase(imie, nazwisko, pesel);
                }
            }
        });

        // Usuwanie użytkownika
        usunButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = uzytkownicyTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(DodawanieUzytkownika.this,
                            "Wybierz użytkownika z tabeli do usunięcia!", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String pesel = (String) uzytkownicyTable.getValueAt(selectedRow, 2);
                int confirm = JOptionPane.showConfirmDialog(DodawanieUzytkownika.this,
                        "Czy na pewno chcesz usunąć wybranego użytkownika?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteUserFromDatabase(pesel);
                }
            }
        });

        // Edycja użytkownika – otwieramy okienko z polami tekstowymi
        edytujUżytkownikaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = uzytkownicyTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(DodawanieUzytkownika.this,
                            "Wybierz użytkownika do edycji!", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String currentImie = (String) uzytkownicyTable.getValueAt(selectedRow, 0);
                String currentNazwisko = (String) uzytkownicyTable.getValueAt(selectedRow, 1);
                String currentPesel = (String) uzytkownicyTable.getValueAt(selectedRow, 2);

                // Utwórz pola do edycji, wstępnie wypełnione aktualnymi danymi
                JTextField editImie = new JTextField(currentImie, 20);
                JTextField editNazwisko = new JTextField(currentNazwisko, 20);
                JTextField editPesel = new JTextField(currentPesel, 20);

                JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
                panel.add(new JLabel("Imię:"));
                panel.add(editImie);
                panel.add(new JLabel("Nazwisko:"));
                panel.add(editNazwisko);
                panel.add(new JLabel("PESEL:"));
                panel.add(editPesel);

                int result = JOptionPane.showConfirmDialog(DodawanieUzytkownika.this,
                        panel, "Edytuj użytkownika", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String newImie = editImie.getText().trim();
                    String newNazwisko = editNazwisko.getText().trim();
                    String newPesel = editPesel.getText().trim();
                    if (newImie.isEmpty() || newNazwisko.isEmpty() || newPesel.isEmpty()) {
                        JOptionPane.showMessageDialog(DodawanieUzytkownika.this,
                                "Wszystkie pola muszą być wypełnione!", "Błąd", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (!newPesel.matches("\\d{11}")) {
                        JOptionPane.showMessageDialog(DodawanieUzytkownika.this,
                                "PESEL musi składać się z dokładnie 11 cyfr!", "Błąd", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    updateUserInDatabase(currentPesel, newImie, newNazwisko, newPesel);
                }
            }
        });
    }

    // Metoda ładująca użytkowników z bazy i wyświetlająca ich w tabeli
    private void loadUsers() {
        List<Object[]> users = new ArrayList<>();
        String[] columnNames = {"Imię", "Nazwisko", "PESEL"};
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT IMIE, NAZWISKO, PESEL FROM uzytkownicy ORDER BY NAZWISKO, IMIE";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                users.add(new Object[]{
                        rs.getString("IMIE"),
                        rs.getString("NAZWISKO"),
                        rs.getString("PESEL")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (komunikatField != null) {
                komunikatField.setText("Błąd ładowania danych: " + ex.getMessage());
            }
        }
        DefaultTableModel model = new DefaultTableModel(
                users.toArray(new Object[0][]), columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        uzytkownicyTable.setModel(model);
    }

    // Metoda dodająca użytkownika do bazy
    private void addUserToDatabase(String imie, String nazwisko, String pesel) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String checkQuery = "SELECT COUNT(*) FROM uzytkownicy WHERE PESEL = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, pesel);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    if (komunikatField != null) {
                        komunikatField.setText("Użytkownik z tym PESEL już istnieje!");
                    }
                    return;
                }
            }
            String query = "INSERT INTO uzytkownicy (IMIE, NAZWISKO, PESEL) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, imie);
                stmt.setString(2, nazwisko);
                stmt.setString(3, pesel);
                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    if (komunikatField != null) {
                        komunikatField.setText("Użytkownik został dodany!");
                    }
                    if (listener != null) {
                        listener.onUzytkownikDodany();
                    }
                    loadUsers();
                } else {
                    if (komunikatField != null) {
                        komunikatField.setText("Nie udało się dodać użytkownika.");
                    }
                }
            }
        } catch (SQLException e) {
            if (komunikatField != null) {
                komunikatField.setText("Błąd zapisu do bazy: " + e.getMessage());
            }
        }
    }

    // Metoda usuwająca użytkownika z bazy
    private void deleteUserFromDatabase(String pesel) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "DELETE FROM uzytkownicy WHERE PESEL = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, pesel);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                if (komunikatField != null) {
                    komunikatField.setText("Użytkownik został usunięty!");
                }
                if (listener != null) {
                    listener.onUzytkownikDodany();
                }
                loadUsers();
            } else {
                if (komunikatField != null) {
                    komunikatField.setText("Nie znaleziono użytkownika o podanym PESEL.");
                }
            }
        } catch (SQLException e) {
            if (komunikatField != null) {
                komunikatField.setText("Błąd podczas usuwania: " + e.getMessage());
            }
        }
    }

    // Metoda aktualizująca dane użytkownika w bazie
    private void updateUserInDatabase(String originalPesel, String newImie, String newNazwisko, String newPesel) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Jeśli PESEL został zmieniony, upewnij się, że nowy nie istnieje już w bazie
            if (!originalPesel.equals(newPesel)) {
                String checkQuery = "SELECT COUNT(*) FROM uzytkownicy WHERE PESEL = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setString(1, newPesel);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Użytkownik z tym PESEL już istnieje!", "Błąd", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            String updateQuery = "UPDATE uzytkownicy SET IMIE = ?, NAZWISKO = ?, PESEL = ? WHERE PESEL = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setString(1, newImie);
                stmt.setString(2, newNazwisko);
                stmt.setString(3, newPesel);
                stmt.setString(4, originalPesel);
                int updatedRows = stmt.executeUpdate();
                if (updatedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Dane użytkownika zostały zaktualizowane!");
                    if (listener != null) {
                        listener.onUzytkownikDodany();  // <-- wywołanie callbacka
                    }
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Nie udało się zaktualizować danych użytkownika!", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Błąd podczas aktualizacji danych: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DodawanieUzytkownika frame = new DodawanieUzytkownika();
            frame.setVisible(true);
        });
    }
}
