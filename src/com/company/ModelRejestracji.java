package com.company;

public class ModelRejestracji {

    private String imie;
    private String nazwisko;
    private String pesel;
    private String dataigodzina;

    public ModelRejestracji(String imie, String nazwisko, String pesel, String dataigodzina) { //parametry konstruktora
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.pesel = pesel;
        this.dataigodzina = dataigodzina;
    }

    public String getImie() {
        return imie;
    }

    public String getNazwisko() {
        return nazwisko;
    }

    public String getPesel() {
        return pesel;
    }

    public String getDataigodzina() {
        return dataigodzina;
    }
}
