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
    private JComboBox<String> godzinyPracy;
    private JButton OKButton;
    private JButton zarzadzajGodzinamiButton;
    private JButton zarzadzajUzytkownikamiButton;
    private JTextField wynik;
    private JButton listaRejestacjiButton;
    private JComboBox<String> dniTygodnia;

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private GodzinaListener listener;

    public void setGodzinaListener(GodzinaListener listener) {
        this.listener = listener;
    }

    public Rejestracja() {
        setTitle("Rejestracja");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        loadUsersAndHours();

        // Dodanie dni tygodnia do JComboBox
        String[] dni = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela"};
        dniTygodnia.setModel(new DefaultComboBoxModel<>(dni));

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedUser = (String) userComboBox.getSelectedItem();
                String selectedHour = (String) godzinyPracy.getSelectedItem();
                String selectDay = (String) dniTygodnia.getSelectedItem();

                if (selectedUser == null || selectedHour == null || selectDay == null) {
                    wynik.setText("Wszystkie pola muszą być wypełnione!");
                } else {
                    registerUser(selectedUser, selectedHour, selectDay);
                }
            }
        });

        zarzadzajGodzinamiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ZarzadzanieGodzinami oknoGodzin = new ZarzadzanieGodzinami();
                oknoGodzin.setGodzinaListener(new GodzinaListener() {
                    @Override
                    public void onGodzinaDodana() {
                        loadUsersAndHours();  // Po dodaniu godziny odśwież listę godzin
                    }

                    @Override
                    public void onGodzinaUsunieta() {
                        loadUsersAndHours();  // Po usunięciu godziny odśwież listę godzin
                    }
                });
                oknoGodzin.setVisible(true);
            }
        });

        zarzadzajUzytkownikamiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DodawanieUzytkownika oknoDodawania = new DodawanieUzytkownika();
                oknoDodawania.setUzytkownikListener(() -> loadUsersAndHours()); // Po dodaniu użytkownika odśwież listę
                oknoDodawania.setVisible(true);
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

    private void registerUser(String userName, String selectedHour, String selectDay) {
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

            // Sprawdzanie, czy ktoś inny jest już zapisany na ten termin i dzień
            String checkQuery = "SELECT ID FROM rejestracje WHERE TERMIN_ID = ? AND DZIEN = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, hourId);
            checkStmt.setString(2, selectDay);  // Sprawdzamy tylko termin i dzień
            ResultSet rsCheck = checkStmt.executeQuery();

            if (rsCheck.next()) {
                wynik.setText("Na ten termin i dzień jest już ktoś zapisany!");
                return;  // Przerwanie rejestracji, jeśli ktoś inny jest już zapisany
            }

            // Rejestracja użytkownika na nowy termin
            String insertQuery = "INSERT INTO rejestracje (UZYTKOWNIK_ID, TERMIN_ID, DZIEN) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, hourId);
            insertStmt.setString(3, selectDay);  // Przechowujemy dzień w bazie
            int rowsInserted = insertStmt.executeUpdate();

            if (rowsInserted > 0) {
                wynik.setText("Rejestracja zakończona!");
                loadUsersAndHours();  // Odśwież listy
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
