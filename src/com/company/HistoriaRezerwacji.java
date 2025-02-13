package com.company;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoriaRezerwacji extends JFrame {
    private JPanel panel1;
    private JTable rezerwacjeTable;
    private JButton closeButton;

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection conn;

    public HistoriaRezerwacji() {
        setTitle("Historia Rejestracji");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z bazą!", "Błąd", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        panel1 = new JPanel(new BorderLayout());
        rezerwacjeTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(rezerwacjeTable);
        panel1.add(scrollPane, BorderLayout.CENTER);

        closeButton = new JButton("Zamknij");
        panel1.add(closeButton, BorderLayout.SOUTH);
        setContentPane(panel1);

        loadHistoricalReservations();

        closeButton.addActionListener(e -> dispose());
    }

    private void loadHistoricalReservations() {
        List<Object[]> reservations = new ArrayList<>();
        String[] columnNames = {"Imię", "Nazwisko", "PESEL", "Godzina", "Dzień", "Pracownik"};

        try {
            String query = "SELECT u.IMIE, u.NAZWISKO, u.PESEL, t.GODZINY, r.DZIEN, " +
                    "p.IMIE AS PRACOWNIK_IMIE, p.NAZWISKO AS PRACOWNIK_NAZWISKO " +
                    "FROM rejestracje r " +
                    "JOIN uzytkownicy u ON r.UZYTKOWNIK_ID = u.ID " +
                    "JOIN terminy t ON r.TERMIN_ID = t.ID " +
                    "JOIN pracownicy p ON r.PRACOWNIK_ID = p.ID " +
                    "WHERE r.DZIEN < CURDATE() " +
                    "ORDER BY r.DZIEN DESC, t.GODZINY ASC";
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

        DefaultTableModel model = new DefaultTableModel(reservations.toArray(new Object[0][]), columnNames);
        rezerwacjeTable.setModel(model);
    }
}
