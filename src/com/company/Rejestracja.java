package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Rejestracja extends JFrame {
    private JPanel panel1; // Załadowane z pliku .form
    private JTextField imie; // Z pliku .form
    private JTextField nazwisko;
    private JTextField pesel;
    private JComboBox<String> godzinyPracy;
    private JTextField dataRezerwacji;
    private JButton OKButton; // Z pliku .form
    private JButton zarzadzajGodzinamiButton;
    private JButton zarzadzajUzytkownikamiButton;
    private JTextField wynik;
    private JTextField formatDaty;

    public Rejestracja() {
        setTitle("Rejestracja");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        // Dodajemy nasłuchiwacze zdarzeń
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                wynik.setText("Rejestracja zakończona!");
            }
        });

        zarzadzajGodzinamiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ZarzadzanieGodzinami().setVisible(true);
            }
        });

        zarzadzajGodzinamiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new DodawanieUzytkownika().setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new Rejestracja();
        frame.setVisible(true);
    }
}
