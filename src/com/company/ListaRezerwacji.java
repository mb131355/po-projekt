package com.company;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListaRezerwacji extends JFrame {
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
        setTitle("Lista Rezerwacji");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Tworzymy połączenie do bazy tylko raz
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z bazą!", "Błąd", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        // Tworzymy tabelę z nagłówkami
        String[] columnNames = {"Imię", "Nazwisko", "PESEL", "Godzina", "Dzień"};
        DefaultTableModel model = new DefaultTableModel(null, columnNames);
        rezerwacjeTable = new JTable(model);

        // Poprawa wyglądu tabeli
        stylizujTabele();

        // Dodajemy JScrollPane, aby dodać przewijanie
        JScrollPane scrollPane = new JScrollPane(rezerwacjeTable);
        panel1 = new JPanel(new BorderLayout());
        panel1.add(scrollPane, BorderLayout.CENTER);

        // Tworzenie przycisków
        JPanel buttonPanel = new JPanel();
        closeButton = new JButton("Zamknij");
        usunRezerwacjeButton = new JButton("Usuń rezerwację");
        edytujRezerwacjeButton = new JButton("Edytuj rezerwację");

        buttonPanel.add(usunRezerwacjeButton);
        buttonPanel.add(edytujRezerwacjeButton);
        buttonPanel.add(closeButton);

        panel1.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(panel1);

        // Ładowanie danych do tabeli
        loadReservations();

        // Obsługa przycisków
        closeButton.addActionListener(e -> dispose());

        usunRezerwacjeButton.addActionListener(e -> usunRezerwacje());
        edytujRezerwacjeButton.addActionListener(e -> edytujRezerwacje());
    }

    private void loadReservations() {
        List<Object[]> reservations = new ArrayList<>();
        String[] columnNames = {"Imię", "Nazwisko", "PESEL", "Godzina", "Dzień"};

        try {
            String query = "SELECT u.IMIE, u.NAZWISKO, u.PESEL, t.GODZINY, r.DZIEN FROM rejestracje r " +
                    "JOIN uzytkownicy u ON r.UZYTKOWNIK_ID = u.ID " +
                    "JOIN terminy t ON r.TERMIN_ID = t.ID";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                reservations.add(new Object[]{
                        rs.getString("IMIE"),
                        rs.getString("NAZWISKO"),
                        rs.getString("PESEL"),
                        rs.getString("GODZINY"),
                        rs.getString("DZIEN")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        DefaultTableModel model = new DefaultTableModel(reservations.toArray(new Object[0][0]), columnNames);
        rezerwacjeTable.setModel(model);
        stylizujTabele();
    }

    private void stylizujTabele() {
        rezerwacjeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        rezerwacjeTable.getTableHeader().setReorderingAllowed(false);
        rezerwacjeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        rezerwacjeTable.getTableHeader().setBackground(Color.LIGHT_GRAY);
        rezerwacjeTable.getTableHeader().setForeground(Color.BLACK);
        rezerwacjeTable.setRowHeight(25);

        // Centrowanie tekstu w wybranych kolumnach
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        rezerwacjeTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        rezerwacjeTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        rezerwacjeTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
    }

    private void usunRezerwacje() {
        int selectedRow = rezerwacjeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Wybierz rezerwację do usunięcia!", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String pesel = (String) rezerwacjeTable.getValueAt(selectedRow, 2);
        String godzina = (String) rezerwacjeTable.getValueAt(selectedRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this, "Czy na pewno chcesz usunąć tę rezerwację?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM rejestracje WHERE UZYTKOWNIK_ID = (SELECT ID FROM uzytkownicy WHERE PESEL = ?) " +
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

        int confirm = JOptionPane.showConfirmDialog(this, "Czy na pewno chcesz edytować tę rezerwację?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Pobranie aktualnych danych
        String pesel = (String) rezerwacjeTable.getValueAt(selectedRow, 2);
        String oldHour = (String) rezerwacjeTable.getValueAt(selectedRow, 3);
        String oldDay = (String) rezerwacjeTable.getValueAt(selectedRow, 4);

        // Pobranie listy dostępnych godzin
        List<String> godzinyLista = pobierzGodzinyZBazy();
        String[] godzinyArray = godzinyLista.toArray(new String[0]);

        String[] dniArray = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek"};

        JComboBox<String> godzinyComboBox = new JComboBox<>(godzinyArray);
        JComboBox<String> dniComboBox = new JComboBox<>(dniArray);
        godzinyComboBox.setSelectedItem(oldHour);
        dniComboBox.setSelectedItem(oldDay);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Nowa godzina:"));
        panel.add(godzinyComboBox);
        panel.add(new JLabel("Nowy dzień:"));
        panel.add(dniComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edytuj rezerwację", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newHour = (String) godzinyComboBox.getSelectedItem();
            String newDay = (String) dniComboBox.getSelectedItem();

            if (newHour.equals(oldHour) && newDay.equals(oldDay)) {
                JOptionPane.showMessageDialog(this, "Nie dokonano żadnych zmian.", "Informacja", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Aktualizacja w bazie danych
            editReservation(pesel, oldHour, newHour, oldDay, newDay);
        }
    }

    private void editReservation(String pesel, String oldHour, String newHour, String oldDay, String newDay) {
        try {
            // Sprawdzenie, czy nowy termin jest wolny
            String checkQuery = "SELECT COUNT(*) FROM rejestracje WHERE TERMIN_ID = (SELECT ID FROM terminy WHERE GODZINY = ?) AND DZIEN = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, newHour);
            checkStmt.setString(2, newDay);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Wybrana godzina i dzień są już zajęte!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Aktualizacja rezerwacji w bazie
            String updateQuery = "UPDATE rejestracje SET TERMIN_ID = (SELECT ID FROM terminy WHERE GODZINY = ?), DZIEN = ? " +
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
                loadReservations(); // Odświeżenie tabeli
            } else {
                JOptionPane.showMessageDialog(this, "Nie udało się zaktualizować rezerwacji!", "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas aktualizacji rezerwacji!", "Błąd", JOptionPane.ERROR_MESSAGE);
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
