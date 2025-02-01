package com.company;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListaRezerwacji extends JFrame {
    private JPanel panel1;
    private JTable rezerwacjeTable;
    private JButton closeButton;
    private JButton usunRezerwacjeButton;
    private JButton edytujRezerwacjeButton; // Przycisk do edytowania rezerwacji

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public ListaRezerwacji() {
        setTitle("Lista Rezerwacji");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        loadReservations();

        closeButton.addActionListener(e -> {
            dispose();
        });

        usunRezerwacjeButton.addActionListener(e -> {
            int selectedRow = rezerwacjeTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Wybierz rezerwację do usunięcia!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String pesel = (String) rezerwacjeTable.getValueAt(selectedRow, 2);
            String godzina = (String) rezerwacjeTable.getValueAt(selectedRow, 3);

            int confirm = JOptionPane.showConfirmDialog(this, "Czy na pewno chcesz usunąć tę rezerwację?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteReservation(pesel, godzina);
                loadReservations();
            }
        });

        // Obsługa edytowania rezerwacji
        edytujRezerwacjeButton.addActionListener(e -> {
            int selectedRow = rezerwacjeTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Wybierz rezerwację do edytowania!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String pesel = (String) rezerwacjeTable.getValueAt(selectedRow, 2);
            String godzina = (String) rezerwacjeTable.getValueAt(selectedRow, 3);
            String dzien = (String) rezerwacjeTable.getValueAt(selectedRow, 4);

            // Okno dialogowe do wyboru, co chcemy edytować (godzinę lub dzień)
            String[] options = {"Godzina", "Dzień", "Oba"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Co chcesz edytować?",
                    "Wybór",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);

            if (choice == 0) { // Edytowanie godziny
                String newHour = JOptionPane.showInputDialog(this, "Wprowadź nową godzinę dla rezerwacji (dotychczasowa: " + godzina + "):");
                if (newHour != null && !newHour.isEmpty()) {
                    editReservation(pesel, godzina, newHour, dzien, null);
                    loadReservations();  // Odśwież tabelę
                }
            } else if (choice == 1) { // Edytowanie dnia
                String newDay = JOptionPane.showInputDialog(this, "Wprowadź nowy dzień dla rezerwacji (dotychczasowy: " + dzien + "):");
                if (newDay != null && !newDay.isEmpty()) {
                    editReservation(pesel, godzina, null, dzien, newDay);
                    loadReservations();  // Odśwież tabelę
                }
            } else if (choice == 2) { // Edytowanie zarówno godziny, jak i dnia
                String newHour = JOptionPane.showInputDialog(this, "Wprowadź nową godzinę dla rezerwacji (dotychczasowa: " + godzina + "):");
                String newDay = JOptionPane.showInputDialog(this, "Wprowadź nowy dzień dla rezerwacji (dotychczasowy: " + dzien + "):");
                if (newHour != null && !newHour.isEmpty() && newDay != null && !newDay.isEmpty()) {
                    editReservation(pesel, godzina, newHour, dzien, newDay);
                    loadReservations();  // Odśwież tabelę
                }
            }
        });
    }

    private void loadReservations() {
        List<Object[]> reservations = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT u.IMIE, u.NAZWISKO, u.PESEL, t.GODZINY, r.DZIEN FROM rejestracje r " +
                    "JOIN uzytkownicy u ON r.UZYTKOWNIK_ID = u.ID " +
                    "JOIN terminy t ON r.TERMIN_ID = t.ID";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String imie = rs.getString("IMIE");
                String nazwisko = rs.getString("NAZWISKO");
                String pesel = rs.getString("PESEL");
                String godzina = rs.getString("GODZINY");
                String dzien = rs.getString("DZIEN");

                reservations.add(new Object[]{imie, nazwisko, pesel, godzina, dzien});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        DefaultTableModel model = new DefaultTableModel(
                reservations.toArray(new Object[0][0]),
                new String[]{"Imię", "Nazwisko", "PESEL", "Godzina", "Dzień"}
        );
        rezerwacjeTable.setModel(model);
    }

    private void deleteReservation(String pesel, String godzina) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "DELETE FROM rejestracje WHERE UZYTKOWNIK_ID = (SELECT ID FROM uzytkownicy WHERE PESEL = ?) " +
                    "AND TERMIN_ID = (SELECT ID FROM terminy WHERE GODZINY = ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, pesel);
            stmt.setString(2, godzina);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Rezerwacja została usunięta!");
            } else {
                JOptionPane.showMessageDialog(this, "Nie udało się usunąć rezerwacji.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas usuwania rezerwacji: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Metoda edytująca rezerwację
    private void editReservation(String pesel, String oldHour, String newHour, String oldDay, String newDay) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Pobieramy ID użytkownika
            String userQuery = "SELECT ID FROM uzytkownicy WHERE PESEL = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, pesel);
            ResultSet rsUser = userStmt.executeQuery();
            if (rsUser.next()) {
                int userId = rsUser.getInt("ID");

                // Pobieramy ID starego terminu
                String hourQuery = "SELECT ID FROM terminy WHERE GODZINY = ?";
                PreparedStatement hourStmt = conn.prepareStatement(hourQuery);
                hourStmt.setString(1, oldHour);
                ResultSet rsHour = hourStmt.executeQuery();
                if (rsHour.next()) {
                    int oldHourId = rsHour.getInt("ID");

                    // Pobieramy ID nowego terminu (jeśli godzina została zmieniona)
                    Integer newHourId = null;
                    if (newHour != null) {
                        PreparedStatement newHourStmt = conn.prepareStatement(hourQuery);
                        newHourStmt.setString(1, newHour);
                        ResultSet rsNewHour = newHourStmt.executeQuery();
                        if (rsNewHour.next()) {
                            newHourId = rsNewHour.getInt("ID");
                        }
                    }

                    // Aktualizujemy rejestrację
                    String updateQuery = "UPDATE rejestracje SET TERMIN_ID = ?, DZIEN = ? WHERE UZYTKOWNIK_ID = ? AND TERMIN_ID = ? AND DZIEN = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    if (newHourId != null) {
                        updateStmt.setInt(1, newHourId);
                    } else {
                        updateStmt.setInt(1, oldHourId); // Jeśli godzina nie została zmieniona
                    }
                    updateStmt.setString(2, (newDay != null) ? newDay : oldDay); // Dzień (jeśli zmieniony)
                    updateStmt.setInt(3, userId);
                    updateStmt.setInt(4, oldHourId);
                    updateStmt.setString(5, oldDay);

                    int rowsUpdated = updateStmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(this, "Rezerwacja została zaktualizowana!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Nie udało się zaktualizować rezerwacji.", "Błąd", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas edytowania rezerwacji: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new ListaRezerwacji();
        frame.setVisible(true);
    }
}
