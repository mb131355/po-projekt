package com.company;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DodawaniePracownika extends JFrame {
    private JPanel mainPanel;
    private JTextField imieField;
    private JTextField nazwiskoField;
    private JButton dodajButton;
    private JButton usunButton;
    private JTextField komunikatField;
    private JTable pracownicyTable;

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

        if (pracownicyTable == null) {
            pracownicyTable = new JTable();
        }

        if (pracownicyTable.getParent() == null) {
            JScrollPane scrollPane = new JScrollPane(pracownicyTable);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
        }

        loadEmployees();

        dodajButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String imie = imieField.getText().trim();
                String nazwisko = nazwiskoField.getText().trim();

                if (imie.isEmpty() || nazwisko.isEmpty()) {
                    JOptionPane.showMessageDialog(DodawaniePracownika.this,
                            "Wszystkie pola muszą być wypełnione!", "Błąd",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    addPracownikToDatabase(imie, nazwisko);
                }
            }
        });

        usunButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = pracownicyTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(DodawaniePracownika.this,
                            "Wybierz pracownika z tabeli do usunięcia!", "Błąd",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String imie = (String) pracownicyTable.getValueAt(selectedRow, 0);
                String nazwisko = (String) pracownicyTable.getValueAt(selectedRow, 1);

                int confirm = JOptionPane.showConfirmDialog(DodawaniePracownika.this,
                        "Czy na pewno chcesz usunąć tego pracownika?", "Potwierdzenie",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deletePracownikFromDatabase(imie, nazwisko);
                }
            }
        });


    }
    private void loadEmployees() {
        List<Object[]> employees = new ArrayList<>();
        String[] columnNames = {"Imię", "Nazwisko"};

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT IMIE, NAZWISKO FROM pracownicy ORDER BY NAZWISKO, IMIE";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                employees.add(new Object[]{
                        rs.getString("IMIE"),
                        rs.getString("NAZWISKO")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (komunikatField != null) {
                komunikatField.setText("Błąd ładowania danych: " + ex.getMessage());
            }
        }

        DefaultTableModel model = new DefaultTableModel(
                employees.toArray(new Object[0][]), columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pracownicyTable.setModel(model);
    }


    private void addPracownikToDatabase(String imie, String nazwisko) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "INSERT INTO pracownicy (IMIE, NAZWISKO) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, imie);
            stmt.setString(2, nazwisko);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                if (komunikatField != null) {
                    komunikatField.setText("Pracownik został dodany!");
                }
                if (listener != null) {
                    listener.onPracownikDodany();
                }
                loadEmployees();
            } else {
                if (komunikatField != null) {
                    komunikatField.setText("Nie udało się dodać pracownika.");
                }
            }
        } catch (SQLException e) {
            if (komunikatField != null) {
                komunikatField.setText("Błąd zapisu do bazy: " + e.getMessage());
            }
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
                if (komunikatField != null) {
                    komunikatField.setText("Pracownik został usunięty!");
                }
                loadEmployees();
            } else {
                if (komunikatField != null) {
                    komunikatField.setText("Nie znaleziono pracownika o podanym imieniu i nazwisku.");
                }
            }
        } catch (SQLException e) {
            if (komunikatField != null) {
                komunikatField.setText("Błąd podczas usuwania: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new DodawaniePracownika();
        frame.setVisible(true);
    }
}
