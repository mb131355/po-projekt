package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DodawanieUzytkownika extends JFrame {
    private JPanel mainPanel;  // Panel główny
    private JTextField imieField;  // Pole do wprowadzenia imienia
    private JTextField nazwiskoField;  // Pole do wprowadzenia nazwiska
    private JTextField peselField;  // Pole do wprowadzenia PESEL
    private JButton dodajButton;  // Przycisk dodawania użytkownika
    private JTextField komunikatField;  // Pole do wyświetlania komunikatów

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";  // Adres bazy danych
    private static final String USER = "root";  // Użytkownik bazy danych
    private static final String PASSWORD = "";  // Hasło do bazy danych

    public DodawanieUzytkownika() {
        setTitle("Dodawanie Użytkownika");
        setContentPane(mainPanel);  // Powiązanie z głównym panelem
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        dodajButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String imie = imieField.getText();  // Pobranie tekstu z pola imienia
                String nazwisko = nazwiskoField.getText();  // Pobranie tekstu z pola nazwiska
                String pesel = peselField.getText();  // Pobranie tekstu z pola PESEL

                if (imie.isEmpty() || nazwisko.isEmpty() || pesel.isEmpty()) {
                    komunikatField.setText("Wszystkie pola muszą być wypełnione!");
                } else {
                    addUserToDatabase(imie, nazwisko, pesel);
                }
            }
        });
    }

    private void addUserToDatabase(String imie, String nazwisko, String pesel) {
        // Połączenie z bazą danych i zapisanie użytkownika
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Przygotowanie zapytania SQL
            String query = "INSERT INTO uzytkownicy (IMIE, NAZWISKO, PESEL) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, imie);  // Ustawienie wartości dla IMIE
            stmt.setString(2, nazwisko);  // Ustawienie wartości dla NAZWISKO
            stmt.setString(3, pesel);  // Ustawienie wartości dla PESEL

            // Wykonanie zapytania
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                komunikatField.setText("Użytkownik został dodany!");
            } else {
                komunikatField.setText("Nie udało się dodać użytkownika.");
            }
        } catch (SQLException e) {
            komunikatField.setText("Błąd zapisu do bazy: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        JFrame frame = new DodawanieUzytkownika();
        frame.setVisible(true);
    }
}
