package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DodawanieUzytkownika extends JFrame {
    private JPanel mainPanel;
    private JTextField imieField;
    private JTextField nazwiskoField;
    private JTextField peselField;
    private JButton dodajButton;
    private JTextField komunikatField;

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public DodawanieUzytkownika() {
        setTitle("Dodawanie Użytkownika");
        setContentPane(mainPanel);
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        dodajButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String imie = imieField.getText();
                String nazwisko = nazwiskoField.getText();
                String pesel = peselField.getText();

                if (imie.isEmpty() || nazwisko.isEmpty() || pesel.isEmpty()) {
                    komunikatField.setText("Wszystkie pola muszą być wypełnione!");
                } else {
                    addUserToDatabase(imie, nazwisko, pesel);
                }
            }
        });
    }

    private void addUserToDatabase(String imie, String nazwisko, String pesel) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Przygotowanie zapytania SQL
            String query = "INSERT INTO uzytkownicy (IMIE, NAZWISKO, PESEL) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, imie);
            stmt.setString(2, nazwisko);
            stmt.setString(3, pesel);

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
