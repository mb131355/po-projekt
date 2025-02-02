package com.company;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListaRezerwacji extends JFrame {
    private JPanel panel1;
    private JTable rezerwacjeTable;
    private JButton closeButton;
    private JButton usunRezerwacjeButton;
    private JButton edytujRezerwacjeButton;

    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public ListaRezerwacji() {
        setTitle("Lista Rezerwacji");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        loadReservations();

        closeButton.addActionListener(e -> dispose());

        usunRezerwacjeButton.addActionListener(e -> {
            int selectedRow = rezerwacjeTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Wybierz rezerwację do usunięcia!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String pesel = (String) rezerwacjeTable.getValueAt(selectedRow, 2);
            String godzina = (String) rezerwacjeTable.getValueAt(selectedRow, 3);

            int confirm = JOptionPane.showConfirmDialog(this, "Czy na pewno chcesz usunąć tę rezerwację?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteReservation(pesel, godzina);
                loadReservations();
            }
        });

        edytujRezerwacjeButton.addActionListener(e -> {
            int selectedRow = rezerwacjeTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Wybierz rezerwację do edytowania!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String pesel = (String) rezerwacjeTable.getValueAt(selectedRow, 2);
            String godzina = (String) rezerwacjeTable.getValueAt(selectedRow, 3);
            String dzien = (String) rezerwacjeTable.getValueAt(selectedRow, 4);

            List<String> godzinyLista = pobierzGodzinyZBazy();
            String[] godzinyArray = godzinyLista.toArray(new String[0]);
            String[] dniArray = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek"};

            JComboBox<String> godzinyComboBox = new JComboBox<>(godzinyArray);
            JComboBox<String> dniComboBox = new JComboBox<>(dniArray);
            godzinyComboBox.setSelectedItem(godzina);
            dniComboBox.setSelectedItem(dzien);

            JPanel panel = new JPanel();
            panel.add(new JLabel("Nowa godzina:"));
            panel.add(godzinyComboBox);
            panel.add(new JLabel("Nowy dzień:"));
            panel.add(dniComboBox);

            int result = JOptionPane.showConfirmDialog(this, panel, "Edytuj rezerwację", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String newHour = (String) godzinyComboBox.getSelectedItem();
                String newDay = (String) dniComboBox.getSelectedItem();
                editReservation(pesel, godzina, newHour, dzien, newDay);
                loadReservations();
            }
        });
    }

    private void loadReservations() {
        List<Object[]> reservations = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT u.IMIE, u.NAZWISKO, u.PESEL, t.GODZINY, r.DZIEN FROM rejestracje r " +
                    "JOIN uzytkownicy u ON r.UZYTKOWNIK_ID = u.ID " +
                    "JOIN terminy t ON r.TERMIN_ID = t.ID";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                reservations.add(new Object[]{rs.getString("IMIE"), rs.getString("NAZWISKO"), rs.getString("PESEL"), rs.getString("GODZINY"), rs.getString("DZIEN")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        rezerwacjeTable.setModel(new DefaultTableModel(reservations.toArray(new Object[0][0]), new String[]{"Imię", "Nazwisko", "PESEL", "Godzina", "Dzień"}));
    }

    private void deleteReservation(String pesel, String godzina) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "DELETE FROM rejestracje WHERE UZYTKOWNIK_ID = (SELECT ID FROM uzytkownicy WHERE PESEL = ?) " +
                    "AND TERMIN_ID = (SELECT ID FROM terminy WHERE GODZINY = ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, pesel);
            stmt.setString(2, godzina);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Rezerwacja została usunięta!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editReservation(String pesel, String oldHour, String newHour, String oldDay, String newDay) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String checkQuery = "SELECT COUNT(*) FROM rejestracje WHERE TERMIN_ID = (SELECT ID FROM terminy WHERE GODZINY = ?) AND DZIEN = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, newHour);
            checkStmt.setString(2, newDay);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Wybrana godzina i dzień są już zajęte!", "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String updateQuery = "UPDATE rejestracje SET TERMIN_ID = (SELECT ID FROM terminy WHERE GODZINY = ?), DZIEN = ? " +
                    "WHERE UZYTKOWNIK_ID = (SELECT ID FROM uzytkownicy WHERE PESEL = ?) AND TERMIN_ID = (SELECT ID FROM terminy WHERE GODZINY = ?) AND DZIEN = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, newHour);
            updateStmt.setString(2, newDay);
            updateStmt.setString(3, pesel);
            updateStmt.setString(4, oldHour);
            updateStmt.setString(5, oldDay);
            updateStmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Rezerwacja została zaktualizowana!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private List<String> pobierzGodzinyZBazy() {
        List<String> godziny = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT DISTINCT GODZINY FROM terminy ORDER BY GODZINY";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                godziny.add(rs.getString("GODZINY"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return godziny;
    }

    public static void main(String[] args) {
        new ListaRezerwacji().setVisible(true);
    }
}
