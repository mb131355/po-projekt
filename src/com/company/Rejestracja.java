package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Rejestracja extends JFrame {
    private JPanel panel1;
    private JComboBox<String> userComboBox;
    private JTextField nazwisko;
    private JTextField pesel;
    private JComboBox<String> godzinyPracy;
    private JTextField dataRezerwacji;
    private JButton OKButton;
    private JButton zarzadzajGodzinamiButton;
    private JButton zarzadzajUzytkownikamiButton;
    private JTextField wynik;
    private JButton listaRejestacjiButton;
    private JTextField formatDaty;

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public Rejestracja() {
        setTitle("Rejestracja");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        loadUsersAndHours();

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedUser = (String) userComboBox.getSelectedItem();
                String selectedHour = (String) godzinyPracy.getSelectedItem();

                if (selectedUser == null || selectedHour.isEmpty()) {
                    wynik.setText("Wszystkie pola muszą być wypełnione!");
                } else {
                    registerUser(selectedUser, selectedHour);
                }
            }
        });

        zarzadzajGodzinamiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ZarzadzanieGodzinami().setVisible(true);
            }
        });

        zarzadzajUzytkownikamiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new DodawanieUzytkownika().setVisible(true);
            }
        });

        listaRejestacjiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ListaRezerwacji().setVisible(true);
            }
        });

    }

    private void loadUsersAndHours() {
        List<String> users = new ArrayList<>();
        List<String> hours = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String queryUsers = "SELECT IMIE, NAZWISKO FROM uzytkownicy";
            Statement stmtUsers = conn.createStatement();
            ResultSet rsUsers = stmtUsers.executeQuery(queryUsers);
            while (rsUsers.next()) {
                String user = rsUsers.getString("IMIE") + " " + rsUsers.getString("NAZWISKO");
                users.add(user);
            }

            String queryHours = "SELECT GODZINY FROM terminy";
            Statement stmtHours = conn.createStatement();
            ResultSet rsHours = stmtHours.executeQuery(queryHours);
            while (rsHours.next()) {
                String hour = rsHours.getString("GODZINY");
                hours.add(hour);
            }

        } catch (SQLException e) {
            wynik.setText("Błąd połączenia z bazą danych: " + e.getMessage());
        }

        userComboBox.setModel(new DefaultComboBoxModel<>(users.toArray(new String[0])));
        godzinyPracy.setModel(new DefaultComboBoxModel<>(hours.toArray(new String[0])));
    }

    private void registerUser(String userName, String selectedHour) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            // Sprawdzenie, czy użytkownik istnieje
            String userQuery = "SELECT ID FROM uzytkownicy WHERE CONCAT(IMIE, ' ', NAZWISKO) = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, userName);
            ResultSet rsUser = userStmt.executeQuery();
            if (!rsUser.next()) {
                wynik.setText("Nie znaleziono użytkownika!");
                return;
            }
            int userId = rsUser.getInt("ID");

            // Sprawdzenie, czy termin istnieje
            String hourQuery = "SELECT ID FROM terminy WHERE GODZINY = ?";
            PreparedStatement hourStmt = conn.prepareStatement(hourQuery);
            hourStmt.setString(1, selectedHour);
            ResultSet rsHour = hourStmt.executeQuery();
            if (!rsHour.next()) {
                wynik.setText("Nie znaleziono terminu!");
                return;
            }
            int hourId = rsHour.getInt("ID");

            // Sprawdzenie, czy użytkownik już jest zapisany na ten termin
            String checkQuery = "SELECT ID FROM rejestracje WHERE UZYTKOWNIK_ID = ? AND TERMIN_ID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, hourId);
            ResultSet rsCheck = checkStmt.executeQuery();
            if (rsCheck.next()) {
                wynik.setText("Użytkownik już zapisany na ten termin!");
                return;
            }

            // Rejestracja użytkownika
            String insertQuery = "INSERT INTO rejestracje (UZYTKOWNIK_ID, TERMIN_ID) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, hourId);
            int rowsInserted = insertStmt.executeUpdate();
            if (rowsInserted > 0) {
                wynik.setText("Rejestracja zakończona!");
                loadUsersAndHours();
            } else {
                wynik.setText("Nie udało się zarejestrować.");
            }

        } catch (SQLException e) {
            wynik.setText("Błąd zapisu do bazy: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        JFrame frame = new Rejestracja();
        frame.setVisible(true);
    }
}
