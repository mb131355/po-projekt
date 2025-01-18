package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Rejestracja extends JFrame {
    private JPanel panel1; // Załadowane z pliku .form
    private JComboBox<String> userComboBox;  // Zaktualizowana zmienna
    private JTextField nazwisko;
    private JTextField pesel;
    private JComboBox<String> godzinyPracy;
    private JTextField dataRezerwacji;
    private JButton OKButton; // Z pliku .form
    private JButton zarzadzajGodzinamiButton;
    private JButton zarzadzajUzytkownikamiButton;
    private JTextField wynik;
    private JTextField formatDaty;

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";  // Adres bazy danych
    private static final String USER = "root";  // Użytkownik bazy danych
    private static final String PASSWORD = "";  // Hasło do bazy danych

    public Rejestracja() {
        setTitle("Rejestracja");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        // Wczytywanie użytkowników i godzin
        loadUsersAndHours();

        // Dodajemy nasłuchiwacze zdarzeń
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedUser = (String) userComboBox.getSelectedItem();  // Używamy userComboBox
                String selectedHour = (String) godzinyPracy.getSelectedItem();
//                String dataRezerwacjiText = dataRezerwacji.getText();

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
    }

    // Ładowanie użytkowników i godzin do ComboBoxów
    private void loadUsersAndHours() {
        List<String> users = new ArrayList<>();
        List<String> hours = new ArrayList<>();

        // Pobieramy użytkowników
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String queryUsers = "SELECT IMIE, NAZWISKO FROM uzytkownicy";
            Statement stmtUsers = conn.createStatement();
            ResultSet rsUsers = stmtUsers.executeQuery(queryUsers);
            while (rsUsers.next()) {
                String user = rsUsers.getString("IMIE") + " " + rsUsers.getString("NAZWISKO");
                users.add(user);
            }

            // Pobieramy dostępne godziny
            String queryHours = "SELECT DATA_I_GODZINA FROM terminy";
            Statement stmtHours = conn.createStatement();
            ResultSet rsHours = stmtHours.executeQuery(queryHours);
            while (rsHours.next()) {
                String hour = rsHours.getString("DATA_I_GODZINA");
                hours.add(hour);
            }

        } catch (SQLException e) {
            wynik.setText("Błąd połączenia z bazą danych: " + e.getMessage());
        }

        // Ustawiamy ComboBoxy
        userComboBox.setModel(new DefaultComboBoxModel<>(users.toArray(new String[0])));
        godzinyPracy.setModel(new DefaultComboBoxModel<>(hours.toArray(new String[0])));
    }

    // Rejestracja użytkownika
    private void registerUser(String userName, String selectedHour) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Pobranie ID użytkownika na podstawie imienia i nazwiska
            String userQuery = "SELECT ID FROM uzytkownicy WHERE CONCAT(IMIE, ' ', NAZWISKO) = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, userName);
            ResultSet rsUser = userStmt.executeQuery();
            int userId = 0;
            if (rsUser.next()) {
                userId = rsUser.getInt("ID");
                System.out.println("User ID: " + userId); // Debugowanie: Sprawdź, czy ID użytkownika jest pobierane poprawnie
            } else {
                System.out.println("Brak użytkownika o nazwisku: " + userName); // Debugowanie: Sprawdź, czy użytkownik istnieje w bazie
            }

            // Pobranie ID terminu na podstawie godziny
            String hourQuery = "SELECT ID FROM terminy WHERE DATA_I_GODZINA = ?";
            PreparedStatement hourStmt = conn.prepareStatement(hourQuery);
            hourStmt.setString(1, selectedHour);
            ResultSet rsHour = hourStmt.executeQuery();
            int hourId = 0;
            if (rsHour.next()) {
                hourId = rsHour.getInt("ID");
                System.out.println("Hour ID: " + hourId); // Debugowanie: Sprawdź, czy ID godziny jest pobierane poprawnie
            } else {
                System.out.println("Brak terminu o godzinie: " + selectedHour); // Debugowanie: Sprawdź, czy godzina istnieje w bazie
            }

            // Upewnijmy się, że obie zmienne są różne od 0
            if (userId == 0 || hourId == 0) {
                wynik.setText("Nie udało się znaleźć użytkownika lub terminu.");
                return;
            }

            // Dodanie rekordu do tabeli rejestracje
            String query = "INSERT INTO rejestracje (UZYTKOWNIK_ID, TERMIN_ID) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setInt(2, hourId);
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                wynik.setText("Rejestracja zakończona!");
                System.out.println("Rejestracja zakończona!"); // Debugowanie: Sprawdź, czy wstawienie do bazy przebiegło poprawnie

//                // Usunięcie godziny po zapisaniu rejestracji
//                String deleteQuery = "DELETE FROM terminy WHERE ID = ?";
//                PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
//                deleteStmt.setInt(1, hourId);
//                deleteStmt.executeUpdate();

                // Opcjonalnie, możesz również odświeżyć listę godzin w GUI po usunięciu godziny
                loadUsersAndHours();
            } else {
                wynik.setText("Nie udało się zarejestrować.");
                System.out.println("Nie udało się zarejestrować."); // Debugowanie: Sprawdź, dlaczego rejestracja się nie udała
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
