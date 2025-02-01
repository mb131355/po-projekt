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




    private static final String URL = "jdbc:mysql://localhost:3306/rejestracja";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public ListaRezerwacji() {
        setTitle("Lista Rezerwacji");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        loadReservations();

        closeButton.addActionListener(e -> {
            dispose();
        });

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

    }

    private void loadReservations() {
        List<Object[]> reservations = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT u.IMIE, u.NAZWISKO, u.PESEL, t.GODZINY FROM rejestracje r " +
                    "JOIN uzytkownicy u ON r.UZYTKOWNIK_ID = u.ID " +
                    "JOIN terminy t ON r.TERMIN_ID = t.ID";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String imie = rs.getString("IMIE");
                String nazwisko = rs.getString("NAZWISKO");
                String pesel = rs.getString("PESEL");
                String godzina = rs.getString("GODZINY");

                reservations.add(new Object[]{imie, nazwisko, pesel, godzina});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        DefaultTableModel model = new DefaultTableModel(
                reservations.toArray(new Object[0][0]),
                new String[]{"", "", "", ""}
        );
        rezerwacjeTable.setModel(model);
    }
    private void deleteReservation(String pesel, String godzina) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "DELETE FROM rejestracje WHERE UZYTKOWNIK_ID = (SELECT ID FROM uzytkownicy WHERE PESEL = ?) " +
                    "AND TERMIN_ID = (SELECT ID FROM terminy WHERE GODZINY = ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, pesel);
            stmt.setString(2, godzina);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Rezerwacja została usunięta!");
            } else {
                JOptionPane.showMessageDialog(this, "Nie udało się usunąć rezerwacji.", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas usuwania rezerwacji: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void main(String[] args) {
        JFrame frame = new ListaRezerwacji();
        frame.setVisible(true);
    }
}
