package cucumber.runtime.java.test.atm;

public interface AtmService {

    void withdraw(int amount);

    int getAmount();

    void deposit(int amount);

    String getInfo();
}
