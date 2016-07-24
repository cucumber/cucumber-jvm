package cucumber.metric.jmx.impl;

import cucumber.metric.jmx.PremierMBean;

public class Premier implements PremierMBean {

    private static String nom = "PremierMBean";
    private int valeur = 100;

    @Override
    public String getNom() {
        return nom;
    }

    @Override
    public int getValeur() {
        return valeur;
    }

    @Override
    public synchronized void setValeur(int valeur) {
        this.valeur = valeur;
    }

    @Override
    public void rafraichir() {
        System.out.println("Rafraichir les donnees");

    }

    public Premier() {

    }
}