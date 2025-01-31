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
            String userQuery = "SELECT ID FROM uzytkownicy WHERE CONCAT(IMIE, ' ', NAZWISKO) = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, userName);
            ResultSet rsUser = userStmt.executeQuery();
            int userId = 0;
            if (rsUser.next()) {
                userId = rsUser.getInt("ID");
                System.out.println("User ID: " + userId);
            } else {
                System.out.println("Brak użytkownika o nazwisku: " + userName);
            }

            String hourQuery = "SELECT ID FROM terminy WHERE GODZINY = ?";
            PreparedStatement hourStmt = conn.prepareStatement(hourQuery);
            hourStmt.setString(1, selectedHour);
            ResultSet rsHour = hourStmt.executeQuery();
            int hourId = 0;
            if (rsHour.next()) {
                hourId = rsHour.getInt("ID");
                System.out.println("Hour ID: " + hourId);
            } else {
                System.out.println("Brak terminu o godzinie: " + selectedHour);
            }

            if (userId == 0 || hourId == 0) {
                wynik.setText("Nie udało się znaleźć użytkownika lub terminu.");
                return;
            }

            String query = "INSERT INTO rejestracje (UZYTKOWNIK_ID, TERMIN_ID) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setInt(2, hourId);
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                wynik.setText("Rejestracja zakończona!");
                System.out.println("Rejestracja zakończona!");


                loadUsersAndHours();
            } else {
                wynik.setText("Nie udało się zarejestrować.");
                System.out.println("Nie udało się zarejestrować."); 
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
