package com.company;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class PrzegladRejestracji extends JFrame {
    private JTextArea rejestracjeArea;

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public PrzegladRejestracji() {
        super("Przegląd Rejestracji");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());

        rejestracjeArea = new JTextArea();
        rejestracjeArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(rejestracjeArea);
        panel1.add(scrollPane, BorderLayout.CENTER);

        this.setContentPane(panel1);
        this.setSize(500, 300);
        this.setVisible(true);

        loadRejestracje();
    }

    private void loadRejestracje() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String query = "SELECT u.IMIE, u.NAZWISKO, r.DATA, gp.UNIKALNA_KOMBINACJA " +
                    "FROM rejestracje r " +
                    "JOIN uzytkownicy u ON r.UZYTKOWNIK_ID = u.ID " +
                    "JOIN godziny_pracy gp ON r.GODZINY_PRACY_ID = gp.ID";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(String.format("Imię: %s, Nazwisko: %s, Data: %s, Godzina: %s\n",
                        rs.getString("IMIE"), rs.getString("NAZWISKO"), rs.getString("DATA"), rs.getString("UNIKALNA_KOMBINACJA")));
            }
            rejestracjeArea.setText(sb.toString());
            conn.close();
        } catch (SQLException e) {
            rejestracjeArea.setText("Błąd ładowania danych.");
        }
    }

    public static void main(String[] args) {
        new PrzegladRejestracji();
    }
}
