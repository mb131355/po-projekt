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

        JOptionPane.showMessageDialog(this, "Edycja jeszcze nie jest zaimplementowana!"); // TODO: Dodać logikę edycji
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ListaRezerwacji().setVisible(true));
    }
}
