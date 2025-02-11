package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class DodawanieUzytkownika extends JFrame {
    private JPanel mainPanel;
    private JTextField imieField;
    private JTextField nazwiskoField;
    private JTextField peselField;
    private JButton dodajButton;
    private JButton usunButton;
    private JTextField komunikatField;

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private UzytkownikListener listener;

    public void setUzytkownikListener(UzytkownikListener listener) {
        this.listener = listener;
    }

    public DodawanieUzytkownika() {
        setTitle("Zarządzanie klientami");
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
                    JOptionPane.showMessageDialog(DodawanieUzytkownika.this, "Wszystkie pola muszą być wypełnione!", "Błąd", JOptionPane.ERROR_MESSAGE);
                } else if (!pesel.matches("\\d{11}")) {
                    JOptionPane.showMessageDialog(DodawanieUzytkownika.this, "PESEL musi składać się z dokładnie 11 cyfr!", "Błąd", JOptionPane.ERROR_MESSAGE);
                } else {
                    addUserToDatabase(imie, nazwisko, pesel);
                }
            }
        });

        usunButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pesel = peselField.getText();

                if (pesel.isEmpty()) {
                    JOptionPane.showMessageDialog(DodawanieUzytkownika.this, "Podaj PESEL użytkownika do usunięcia!", "Błąd", JOptionPane.ERROR_MESSAGE);
                } else if (!pesel.matches("\\d{11}")) {
                    JOptionPane.showMessageDialog(DodawanieUzytkownika.this, "PESEL musi składać się z dokładnie 11 cyfr!", "Błąd", JOptionPane.ERROR_MESSAGE);
                } else {
                    deleteUserFromDatabase(pesel);
                }
            }
        });
    }

    private void addUserToDatabase(String imie, String nazwisko, String pesel) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String checkQuery = "SELECT COUNT(*) FROM uzytkownicy WHERE PESEL = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, pesel);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count > 0) {
                        komunikatField.setText("Użytkownik z tym PESEL już istnieje!");
                        return;
                    }
                }
            }

            String query = "INSERT INTO uzytkownicy (IMIE, NAZWISKO, PESEL) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, imie);
                stmt.setString(2, nazwisko);
                stmt.setString(3, pesel);

                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    komunikatField.setText("Użytkownik został dodany!");

                    if (listener != null) {
                        listener.onUzytkownikDodany();
                    }
                } else {
                    komunikatField.setText("Nie udało się dodać użytkownika.");
                }
            }
        } catch (SQLException e) {
            komunikatField.setText("Błąd zapisu do bazy: " + e.getMessage());
        }
    }

    private void deleteUserFromDatabase(String pesel) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "DELETE FROM uzytkownicy WHERE PESEL = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, pesel);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                komunikatField.setText("Użytkownik został usunięty!");
            } else {
                komunikatField.setText("Nie znaleziono użytkownika o podanym PESEL.");
            }
        } catch (SQLException e) {
            komunikatField.setText("Błąd podczas usuwania: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        JFrame frame = new DodawanieUzytkownika();
        frame.setVisible(true);
    }
}
