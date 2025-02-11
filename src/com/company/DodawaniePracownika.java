package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DodawaniePracownika extends JFrame {
    private JPanel mainPanel;
    private JTextField imieField;
    private JTextField nazwiskoField;
    private JButton dodajButton;
    private JButton usunButton;
    private JTextField komunikatField;

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private PracownikListener listener;

    public void setPracownikListener(PracownikListener listener) {
        this.listener = listener;
    }

    public DodawaniePracownika() {
        setTitle("Zarządzanie pracownikami");
        setContentPane(mainPanel);
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        dodajButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String imie = imieField.getText();
                String nazwisko = nazwiskoField.getText();

                if (imie.isEmpty() || nazwisko.isEmpty()) {
                    JOptionPane.showMessageDialog(DodawaniePracownika.this, "Wszystkie pola muszą być wypełnione!", "Błąd", JOptionPane.ERROR_MESSAGE);
                } else {
                    addPracownikToDatabase(imie, nazwisko);
                }
            }
        });

        usunButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String imie = imieField.getText();
                String nazwisko = nazwiskoField.getText();

                if (imie.isEmpty() || nazwisko.isEmpty()) {
                    JOptionPane.showMessageDialog(DodawaniePracownika.this, "Podaj imię i nazwisko pracownika do usunięcia!", "Błąd", JOptionPane.ERROR_MESSAGE);
                } else {
                    deletePracownikFromDatabase(imie, nazwisko);
                }
            }
        });
    }

    private void addPracownikToDatabase(String imie, String nazwisko) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "INSERT INTO pracownicy (IMIE, NAZWISKO) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, imie);
            stmt.setString(2, nazwisko);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                komunikatField.setText("Pracownik został dodany!");
                if (listener != null) {
                    listener.onPracownikDodany();
                }
            } else {
                komunikatField.setText("Nie udało się dodać pracownika.");
            }
        } catch (SQLException e) {
            komunikatField.setText("Błąd zapisu do bazy: " + e.getMessage());
        }
    }


    private void deletePracownikFromDatabase(String imie, String nazwisko) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "DELETE FROM pracownicy WHERE IMIE = ? AND NAZWISKO = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, imie);
            stmt.setString(2, nazwisko);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                komunikatField.setText("Pracownik został usunięty!");
            } else {
                komunikatField.setText("Nie znaleziono pracownika o podanym imieniu i nazwisku.");
            }
        } catch (SQLException e) {
            komunikatField.setText("Błąd podczas usuwania: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        JFrame frame = new DodawaniePracownika();
        frame.setVisible(true);
    }
}
