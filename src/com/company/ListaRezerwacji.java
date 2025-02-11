package com.company;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListaRezerwacji extends JFrame {
    // Komponenty zostały zadeklarowane i powiązane z XML-em
    private JPanel panel1;
    private JTable rezerwacjeTable;
    private JButton closeButton;
    private JButton usunRezerwacjeButton;
    private JButton edytujRezerwacjeButton;

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection conn;

    public ListaRezerwacji() {
        setTitle("Lista Rejestracji");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Nawiązanie połączenia z bazą
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z bazą!", "Błąd", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        // Ustawiamy główny panel – jest on już skonfigurowany w XML
        setContentPane(panel1);

        // Załadowanie danych do tabeli
        loadReservations();

        // Dodanie listenerów – nie zmieniamy właściwości komponentów
        closeButton.addActionListener(e -> dispose());
        usunRezerwacjeButton.addActionListener(e -> usunRezerwacje());
        edytujRezerwacjeButton.addActionListener(e -> edytujRezerwacje());


    }

    private void loadReservations() {
        List<Object[]> reservations = new ArrayList<>();
        String[] columnNames = {"Imię", "Nazwisko", "PESEL", "Godzina", "Dzień", "Pracownik"};

        try {
            String query = "SELECT u.IMIE, u.NAZWISKO, u.PESEL, t.GODZINY, r.DZIEN, " +
                    "p.IMIE AS PRACOWNIK_IMIE, p.NAZWISKO AS PRACOWNIK_NAZWISKO " +
                    "FROM rejestracje r " +
                    "JOIN uzytkownicy u ON r.UZYTKOWNIK_ID = u.ID " +
                    "JOIN terminy t ON r.TERMIN_ID = t.ID " +
                    "JOIN pracownicy p ON r.PRACOWNIK_ID = p.ID " +
                    "ORDER BY r.DZIEN ASC, t.GODZINY ASC";  // sortowanie według daty, a potem godziny
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String pracownik = rs.getString("PRACOWNIK_IMIE") + " " + rs.getString("PRACOWNIK_NAZWISKO");
                reservations.add(new Object[]{
                        rs.getString("IMIE"),
                        rs.getString("NAZWISKO"),
                        rs.getString("PESEL"),
                        rs.getString("GODZINY"),
                        rs.getString("DZIEN"),
                        pracownik
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        DefaultTableModel model = new DefaultTableModel(reservations.toArray(new Object[0][0]), columnNames);
        rezerwacjeTable.setModel(model);
    }


    private void usunRezerwacje() {
        int selectedRow = rezerwacjeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz rezerwację do usunięcia!", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String pesel = (String) rezerwacjeTable.getValueAt(selectedRow, 2);
        String godzina = (String) rezerwacjeTable.getValueAt(selectedRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this, "Czy na pewno chcesz usunąć tę rezerwację?",
                "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM rejestracje WHERE UZYTKOWNIK_ID = " +
                        "(SELECT ID FROM uzytkownicy WHERE PESEL = ?) " +
                        "AND TERMIN_ID = (SELECT ID FROM terminy WHERE GODZINY = ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, pesel);
                stmt.setString(2, godzina);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Rezerwacja została usunięta!");
                loadReservations();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void edytujRezerwacje() {
        int selectedRow = rezerwacjeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz rezerwację do edytowania!", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Czy na pewno chcesz edytować tę rezerwację?",
                "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Pobranie aktualnych danych rezerwacji
        String pesel = (String) rezerwacjeTable.getValueAt(selectedRow, 2);
        String oldHour = (String) rezerwacjeTable.getValueAt(selectedRow, 3);
        String oldDay = (String) rezerwacjeTable.getValueAt(selectedRow, 4);

        // Pobranie listy dostępnych godzin z bazy
        List<String> godzinyLista = pobierzGodzinyZBazy();
        String[] godzinyArray = godzinyLista.toArray(new String[0]);

        JComboBox<String> godzinyComboBox = new JComboBox<>(godzinyArray);
        JTextField dataTextField = new JTextField(oldDay);

        // Używamy prostego układu wewnętrznego – nie nadpisujemy stylów z XML
        JPanel panel = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Nowa godzina:"));
        panel.add(godzinyComboBox);
        panel.add(new JLabel("Nowa data (yyyy-mm-dd):"));
        panel.add(dataTextField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edytuj rezerwację", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newHour = (String) godzinyComboBox.getSelectedItem();
            String newDay = dataTextField.getText().trim();

            if (newHour.equals(oldHour) && newDay.equals(oldDay)) {
                JOptionPane.showMessageDialog(this, "Nie dokonano żadnych zmian.", "Informacja",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            editReservation(pesel, oldHour, newHour, oldDay, newDay);
        }
    }

    private void editReservation(String pesel, String oldHour, String newHour, String oldDay, String newDay) {
        try {
            String checkQuery = "SELECT COUNT(*) FROM rejestracje WHERE TERMIN_ID = " +
                    "(SELECT ID FROM terminy WHERE GODZINY = ?) AND DZIEN = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, newHour);
            checkStmt.setString(2, newDay);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Wybrana godzina i dzień są już zajęte!",
                        "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String updateQuery = "UPDATE rejestracje SET TERMIN_ID = " +
                    "(SELECT ID FROM terminy WHERE GODZINY = ?), DZIEN = ? " +
                    "WHERE UZYTKOWNIK_ID = (SELECT ID FROM uzytkownicy WHERE PESEL = ?) " +
                    "AND TERMIN_ID = (SELECT ID FROM terminy WHERE GODZINY = ?) AND DZIEN = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, newHour);
            updateStmt.setString(2, newDay);
            updateStmt.setString(3, pesel);
            updateStmt.setString(4, oldHour);
            updateStmt.setString(5, oldDay);

            int updatedRows = updateStmt.executeUpdate();

            if (updatedRows > 0) {
                JOptionPane.showMessageDialog(this, "Rezerwacja została zaktualizowana!");
                loadReservations();
            } else {
                JOptionPane.showMessageDialog(this, "Nie udało się zaktualizować rezerwacji!",
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas aktualizacji rezerwacji!",
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String> pobierzGodzinyZBazy() {
        List<String> godziny = new ArrayList<>();
        try {
            String query = "SELECT DISTINCT GODZINY FROM terminy ORDER BY GODZINY";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                godziny.add(rs.getString("GODZINY"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return godziny;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ListaRezerwacji().setVisible(true));
    }
}
