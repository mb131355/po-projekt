package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.ParseException;
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
    private JLabel wynikData;
    private JLabel wynikGodzina;

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private GodzinaListener listener;

    public void setGodzinaListener(GodzinaListener listener) {
        this.listener = listener;
    }

    public Rejestracja() {
        setTitle("Rejestracja w salonie manicure");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();

        SpinnerDateModel dateModel = new SpinnerDateModel(today, today, null, Calendar.DAY_OF_MONTH);
        dateSpinner.setModel(dateModel);

        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);

        JFormattedTextField ftf = dateEditor.getTextField();
        ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);

        loadUsersAndHours();
        pack();

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dateText = dateEditor.getTextField().getText();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date selectedDate;
                try {
                    selectedDate = sdf.parse(dateText);
                } catch (ParseException ex) {
                    wynikData.setText("Zły format daty! Użyj formatu yyyy-MM-dd.");
                    return;
                }

                Calendar selectedCal = Calendar.getInstance();
                selectedCal.setTime(selectedDate);
                selectedCal.set(Calendar.HOUR_OF_DAY, 0);
                selectedCal.set(Calendar.MINUTE, 0);
                selectedCal.set(Calendar.SECOND, 0);
                selectedCal.set(Calendar.MILLISECOND, 0);

                Calendar nowCal = Calendar.getInstance();
                nowCal.set(Calendar.HOUR_OF_DAY, 0);
                nowCal.set(Calendar.MINUTE, 0);
                nowCal.set(Calendar.SECOND, 0);
                nowCal.set(Calendar.MILLISECOND, 0);

                if (selectedCal.before(nowCal)) {
                    wynikData.setText("Nie można wybrać przeszłej daty!");
                    return;
                }
                wynikData.setText("");

                String selectedUser = (String) userComboBox.getSelectedItem();
                String selectedHour = (String) godzinyPracy.getSelectedItem();
                String formattedDate = sdf.format(selectedDate);

                registerUser(selectedUser, selectedHour, formattedDate);
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
                oknoPracownika.setPracownikListener(() -> loadUsersAndHours());
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
        List<String> workers = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String queryUsers = "SELECT IMIE, NAZWISKO, PESEL FROM uzytkownicy ORDER BY NAZWISKO ASC";
            Statement stmtUsers = conn.createStatement();
            ResultSet rsUsers = stmtUsers.executeQuery(queryUsers);
            while (rsUsers.next()) {
                String user = rsUsers.getString("IMIE") + " " + rsUsers.getString("NAZWISKO") + " (Pesel: " + rsUsers.getString("PESEL") + ")";
                users.add(user);
            }

            String queryHours = "SELECT GODZINY FROM terminy ORDER BY GODZINY ASC";
            Statement stmtHours = conn.createStatement();
            ResultSet rsHours = stmtHours.executeQuery(queryHours);
            while (rsHours.next()) {
                String hour = rsHours.getString("GODZINY");
                hours.add(hour);
            }

            String queryWorkers = "SELECT IMIE, NAZWISKO FROM pracownicy";
            Statement stmtWorkers = conn.createStatement();
            ResultSet rsWorkers = stmtWorkers.executeQuery(queryWorkers);
            while (rsWorkers.next()) {
                String worker = rsWorkers.getString("IMIE") + " " + rsWorkers.getString("NAZWISKO");
                workers.add(worker);
            }

        } catch (SQLException e) {
            wynik.setText("Błąd połączenia z bazą danych: " + e.getMessage());
        }

        userComboBox.setModel(new DefaultComboBoxModel<>(users.toArray(new String[0])));
        godzinyPracy.setModel(new DefaultComboBoxModel<>(hours.toArray(new String[0])));
        wybierzPracownika.setModel(new DefaultComboBoxModel<>(workers.toArray(new String[0])));
    }


    private void registerUser(String userName, String selectedHour, String selectedDate) {
        String selectedWorker = (String) wybierzPracownika.getSelectedItem();

        if (selectedWorker == null || selectedWorker.isEmpty()) {
            wynik.setText("Musisz wybrać pracownika!");
            return;
        }

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

            String workerQuery = "SELECT ID FROM pracownicy WHERE CONCAT(IMIE, ' ', NAZWISKO) = ?";
            PreparedStatement workerStmt = conn.prepareStatement(workerQuery);
            workerStmt.setString(1, selectedWorker);
            ResultSet rsWorker = workerStmt.executeQuery();
            if (!rsWorker.next()) {
                wynik.setText("Nie znaleziono pracownika!");
                return;
            }
            int workerId = rsWorker.getInt("ID");

            String queryOverlapping = "SELECT t.GODZINY FROM rejestracje r " +
                    "JOIN terminy t ON r.TERMIN_ID = t.ID " +
                    "WHERE r.DZIEN = ? AND r.PRACOWNIK_ID = ?";
            PreparedStatement psOver = conn.prepareStatement(queryOverlapping);
            psOver.setString(1, selectedDate);
            psOver.setInt(2, workerId);
            ResultSet rsOver = psOver.executeQuery();

            String[] selectedParts = selectedHour.split(" - ");
            if (selectedParts.length != 2) {
                wynikGodzina.setText("Błędny format wybranego terminu: " + selectedHour);
                return;
            }
            java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            java.time.LocalTime newStart, newEnd;
            try {
                newStart = java.time.LocalTime.parse(selectedParts[0].trim(), timeFormatter);
                newEnd = java.time.LocalTime.parse(selectedParts[1].trim(), timeFormatter);
            } catch (Exception ex) {
                wynikGodzina.setText("Błędny format wybranego terminu: " + selectedHour);
                return;
            }

            boolean overlappingFound = false;
            while (rsOver.next()) {
                String existingRange = rsOver.getString("GODZINY");
                String[] parts = existingRange.split(" - ");
                if (parts.length != 2) continue;
                java.time.LocalTime existStart, existEnd;
                try {
                    existStart = java.time.LocalTime.parse(parts[0].trim(), timeFormatter);
                    existEnd = java.time.LocalTime.parse(parts[1].trim(), timeFormatter);
                } catch (Exception ex) {
                    continue;
                }

                if (newStart.isBefore(existEnd) && existStart.isBefore(newEnd)) {
                    overlappingFound = true;
                    break;
                }
            }
            if (overlappingFound) {
                wynikGodzina.setText("Wybrany pracownik jest już zajęty w tym terminie!");
                return;
            } else {
                wynikGodzina.setText("");
            }

            String insertQuery = "INSERT INTO rejestracje (UZYTKOWNIK_ID, TERMIN_ID, DZIEN, PRACOWNIK_ID) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, hourId);
            insertStmt.setString(3, selectedDate);
            insertStmt.setInt(4, workerId);
            int rowsInserted = insertStmt.executeUpdate();

            if (rowsInserted > 0) {
                wynik.setText("Rejestracja zakończona!");
                loadUsersAndHours();
                if (ListaRezerwacji.instance != null && ListaRezerwacji.instance.isVisible()) {
                    ListaRezerwacji.instance.loadReservations();
                }
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