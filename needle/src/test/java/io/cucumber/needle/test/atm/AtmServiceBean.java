package io.cucumber.needle.test.atm;

import io.cucumber.needle.test.injectionprovider.ValueGetter;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class AtmServiceBean implements AtmService {

    private int account = 0;

    @EJB
    private BicGetter bicGetter;

    @Inject
    private ValueGetter valueGetter;

    @Override
    public void withdraw(final int amount) {
        if (amount > account) {
            throw new IllegalArgumentException("max amount is: " + account);
        }
        account = account - amount;
    }

    @Override
    public int getAmount() {
        return account;
    }

    @Override
    public void deposit(final int amount) {
        account = account + amount;
    }

    @Override
    public String getInfo() {
        return "BIC: " + bicGetter.getBic() + " and VALUE: " + valueGetter.getValue();
    }

}
