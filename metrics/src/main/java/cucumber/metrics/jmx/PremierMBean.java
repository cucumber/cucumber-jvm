package cucumber.metrics.jmx;

public interface PremierMBean {

    public String getNom();

    public int getValeur();

    public void setValeur(int valeur);

    public void rafraichir();

}
