package com.company;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class PrzegladRejestracji extends JFrame {
    private JTextArea rejestracjeArea; // Obszar tekstowy do wyświetlania danych rejestracji

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public PrzegladRejestracji() {
        // Tworzymy okno aplikacji
        super("Przegląd Rejestracji");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Tworzymy panel i ustawiamy jego układ
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());

        // Tworzymy obszar tekstowy do wyświetlania rejestracji
        rejestracjeArea = new JTextArea();
        rejestracjeArea.setEditable(false); // Nie pozwalamy na edytowanie tekstu

        // Dodajemy obszar tekstowy do panelu
        JScrollPane scrollPane = new JScrollPane(rejestracjeArea);
        panel1.add(scrollPane, BorderLayout.CENTER);

        // Ustawiamy zawartość okna na panel
        this.setContentPane(panel1);
        this.setSize(500, 300); // Ustalamy rozmiar okna
        this.setVisible(true); // Ustawiamy widoczność okna

        loadRejestracje(); // Ładujemy dane rejestracji z bazy danych
    }

    private void loadRejestracje() {
        try {
            // Łączenie z bazą danych
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String query = "SELECT u.IMIE, u.NAZWISKO, r.DATA, gp.UNIKALNA_KOMBINACJA " +
                    "FROM rejestracje r " +
                    "JOIN uzytkownicy u ON r.UZYTKOWNIK_ID = u.ID " +
                    "JOIN godziny_pracy gp ON r.GODZINY_PRACY_ID = gp.ID";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Zbieranie danych z bazy
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(String.format("Imię: %s, Nazwisko: %s, Data: %s, Godzina: %s\n",
                        rs.getString("IMIE"), rs.getString("NAZWISKO"), rs.getString("DATA"), rs.getString("UNIKALNA_KOMBINACJA")));
            }
            // Wyświetlamy dane w JTextArea
            rejestracjeArea.setText(sb.toString());
            conn.close(); // Zamykamy połączenie z bazą
        } catch (SQLException e) {
            rejestracjeArea.setText("Błąd ładowania danych.");
        }
    }

    public static void main(String[] args) {
        // Uruchamiamy aplikację
        new PrzegladRejestracji();
    }
}
