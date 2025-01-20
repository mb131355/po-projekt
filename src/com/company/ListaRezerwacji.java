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
    private JLabel headerImie;
    private JLabel headerNazwisko;
    private JLabel headerPesel;
    private JLabel headerDataGodzina;

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
    }

    private void loadReservations() {
        List<Object[]> reservations = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT u.IMIE, u.NAZWISKO, u.PESEL, t.DATA_I_GODZINA FROM rejestracje r " +
                    "JOIN uzytkownicy u ON r.UZYTKOWNIK_ID = u.ID " +
                    "JOIN terminy t ON r.TERMIN_ID = t.ID";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String imie = rs.getString("IMIE");
                String nazwisko = rs.getString("NAZWISKO");
                String pesel = rs.getString("PESEL");
                String dataGodzina = rs.getString("DATA_I_GODZINA");

                reservations.add(new Object[]{imie, nazwisko, pesel, dataGodzina});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        DefaultTableModel model = new DefaultTableModel(
                reservations.toArray(new Object[0][0]),
                new String[]{"", "", "", ""}
        );
        rezerwacjeTable.setModel(model);
    }

    public static void main(String[] args) {
        JFrame frame = new ListaRezerwacji();
        frame.setVisible(true);
    }
}
