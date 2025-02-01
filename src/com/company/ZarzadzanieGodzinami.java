package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ZarzadzanieGodzinami extends JFrame {
    private JPanel mainPanel;
    private JTextField godzinaStart;
    private JTextField godzinaKoniec;
    private JButton dodajGodzineButton;
    private JButton usunGodzineButton;
    private JLabel wynik;

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private GodzinaListener listener;


    public ZarzadzanieGodzinami() {
        setTitle("Zarządzanie Godzinami");
        setContentPane(mainPanel);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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

    public void setGodzinaListener(GodzinaListener listener) {
        this.listener = listener;
    }

    private void addHour() {
        String godzinaStartowa = godzinaStart.getText();
        String godzinaKoncowa = godzinaKoniec.getText();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "INSERT INTO terminy (GODZINY) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, godzinaStartowa + " - " + godzinaKoncowa);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                wynik.setText("Dodano godzinę: " + godzinaStartowa + " - " + godzinaKoncowa);
                if (listener != null) {
                    listener.onGodzinaDodana();
                }
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

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "DELETE FROM terminy WHERE GODZINY = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, godzinaStartowa + " - " + godzinaKoncowa);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                wynik.setText("Usunięto godzinę: " + godzinaStartowa + " - " + godzinaKoncowa);
                if (listener != null) {
                    listener.onGodzinaUsunieta();
                }
            } else {
                wynik.setText("Nie znaleziono godziny do usunięcia.");
            }
        } catch (SQLException e) {
            wynik.setText("Błąd usuwania z bazy: " + e.getMessage());
        }
    }
}

