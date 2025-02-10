package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Calendar;

public class Rejestracja extends JFrame {
    private JPanel panel1;
    private JComboBox<String> userComboBox;
    private JComboBox<String> godzinyPracy;
    private JButton OKButton;
    private JButton zarzadzajGodzinamiButton;
    private JButton zarzadzajUzytkownikamiButton;
    private JTextField wynik;
    private JButton listaRejestacjiButton;
    private JButton dodajPracownikaButton;
    private JComboBox wybierzPracownika;
    private JSpinner dateSpinner;

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

        // Konfiguracja JSpinner dla daty
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(),
                null,
                null,
                Calendar.DAY_OF_MONTH);
        dateSpinner.setModel(dateModel);

        // Formatowanie wyglądu daty w JSpinner
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);

        loadUsersAndHours();

        pack();

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedUser = (String) userComboBox.getSelectedItem();
                String selectedHour = (String) godzinyPracy.getSelectedItem();
                Date selectedDate = (Date) dateSpinner.getValue();

                if (selectedUser == null || selectedHour == null || selectedDate == null) {
                    wynik.setText("Wszystkie pola muszą być wypełnione!");
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = sdf.format(selectedDate);
                    registerUser(selectedUser, selectedHour, formattedDate);
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
                        loadUsersAndHours();
                    }

                    @Override
                    public void onGodzinaUsunieta() {
                        loadUsersAndHours();
                    }
                });
                oknoGodzin.setVisible(true);
            }
        });

        zarzadzajUzytkownikamiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DodawanieUzytkownika oknoDodawania = new DodawanieUzytkownika();
                oknoDodawania.setUzytkownikListener(() -> loadUsersAndHours());
                oknoDodawania.setVisible(true);
            }
        });

        dodajPracownikaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DodawaniePracownika oknoPracownika = new DodawaniePracownika();
                oknoPracownika.setVisible(true);
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
            String queryUsers = "SELECT IMIE, NAZWISKO, PESEL FROM uzytkownicy";
            Statement stmtUsers = conn.createStatement();
            ResultSet rsUsers = stmtUsers.executeQuery(queryUsers);
            while (rsUsers.next()) {
                String user = rsUsers.getString("IMIE") + " " + rsUsers.getString("NAZWISKO") + " (Pesel: " + rsUsers.getString("PESEL") + ")";
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

    private void registerUser(String userName, String selectedHour, String selectedDate) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String[] userParts = userName.split(" \\(Pesel:");
            String fullName = userParts[0];

            String userQuery = "SELECT ID FROM uzytkownicy WHERE CONCAT(IMIE, ' ', NAZWISKO) = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, fullName);
            ResultSet rsUser = userStmt.executeQuery();
            if (!rsUser.next()) {
                wynik.setText("Nie znaleziono użytkownika!");
                return;
            }
            int userId = rsUser.getInt("ID");

            String hourQuery = "SELECT ID FROM terminy WHERE GODZINY = ?";
            PreparedStatement hourStmt = conn.prepareStatement(hourQuery);
            hourStmt.setString(1, selectedHour);
            ResultSet rsHour = hourStmt.executeQuery();
            if (!rsHour.next()) {
                wynik.setText("Nie znaleziono terminu!");
                return;
            }
            int hourId = rsHour.getInt("ID");

            String checkQuery = "SELECT ID FROM rejestracje WHERE TERMIN_ID = ? AND DZIEN = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, hourId);
            checkStmt.setString(2, selectedDate);
            ResultSet rsCheck = checkStmt.executeQuery();

            if (rsCheck.next()) {
                wynik.setText("Na ten termin i dzień jest już ktoś zapisany!");
                return;
            }

            String insertQuery = "INSERT INTO rejestracje (UZYTKOWNIK_ID, TERMIN_ID, DZIEN) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, hourId);
            insertStmt.setString(3, selectedDate);
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
        SwingUtilities.invokeLater(() -> {
            new Rejestracja().setVisible(true);
        });
    }
}