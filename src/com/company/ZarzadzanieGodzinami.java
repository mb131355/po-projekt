package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ZarzadzanieGodzinami extends JFrame {
    private JPanel mainPanel; // Powiązane z XML
    private JTextField godzinaStart; // Powiązane z XML
    private JTextField godzinaKoniec; // Powiązane z XML
    private JButton dodajGodzineButton; // Powiązane z XML
    private JButton usunGodzineButton; // Powiązane z XML
    private JLabel wynik; // Powiązane z XML

    // Dane do połączenia z bazą
    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja"; // Zmień na swoją bazę danych
    private static final String USER = "root"; // Zmień na swoje dane użytkownika
    private static final String PASSWORD = ""; // Zmień na swoje hasło

    public ZarzadzanieGodzinami() {
        // Ustawienia okna
        setTitle("Zarządzanie Godzinami");
        setContentPane(mainPanel); // Załadowanie z pliku XML
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Okno będzie zamknięte po kliknięciu X

        // Obsługa przycisków
        dodajGodzineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addHour();
            }
        });

        usunGodzineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteHour();
            }
        });
    }

    private void addHour() {
        String godzinaStartowa = godzinaStart.getText();
        String godzinaKoncowa = godzinaKoniec.getText();

        // Połącz z bazą danych i zapisz dane
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "INSERT INTO terminy (DATA_I_GODZINA) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, godzinaStartowa + " - " + godzinaKoncowa);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                wynik.setText("Dodano godzinę: " + godzinaStartowa + " - " + godzinaKoncowa);
            } else {
                wynik.setText("Nie udało się dodać godziny.");
            }
        } catch (SQLException e) {
            wynik.setText("Błąd zapisu do bazy: " + e.getMessage());
        }
    }

    private void deleteHour() {
        String godzinaStartowa = godzinaStart.getText();
        String godzinaKoncowa = godzinaKoniec.getText();

        // Połącz z bazą danych i usuń dane
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "DELETE FROM terminy WHERE DATA_I_GODZINA = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, godzinaStartowa + " - " + godzinaKoncowa);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                wynik.setText("Usunięto godzinę: " + godzinaStartowa + " - " + godzinaKoncowa);
            } else {
                wynik.setText("Nie znaleziono godziny do usunięcia.");
            }
        } catch (SQLException e) {
            wynik.setText("Błąd usuwania z bazy: " + e.getMessage());
        }
    }
}
