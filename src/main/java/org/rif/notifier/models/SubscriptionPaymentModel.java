package org.rif.notifier.models;

import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.models.entities.Currency;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;

import java.math.BigInteger;
import java.util.List;

public class SubscriptionPaymentModel {
    private static final int EXPECTED_EVENT_SIZE = 4;
    private BigInteger amount;
    private Address currencyAddress;
    private String hash;
    private Address provider;
    private Currency currency;

    public SubscriptionPaymentModel(String hash, Address provider, BigInteger amount, Address currencyAddress) {
        this.amount = amount;
        this.currencyAddress = currencyAddress;
        this.hash = hash;
        this.provider = provider;
    }

    private SubscriptionPaymentModel(List<Type> values)   {
        this((String)values.get(0).getValue(), //hash
                (Address)values.get(1), //provider
                (BigInteger)values.get(2).getValue(), //amount
                (Address)values.get(3)); //currency
    }

    public static SubscriptionPaymentModel fromEventValues(List<Type> eventValues)  {
        if(eventValues.size() != EXPECTED_EVENT_SIZE)  {
            throw new ValidationException("Invalid payment event values received");
        }
        try {
            return new SubscriptionPaymentModel(eventValues);
        }catch(Exception e) {
            throw new ValidationException("Invalid payment event values received. " + e.getMessage(), e);
        }
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public Address getCurrencyAddress() {
        return currencyAddress;
    }

    public void setCurrencyAddress(Address currencyAddress) {
        this.currencyAddress = currencyAddress;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Address getProvider() {
        return provider;
    }

    public void setProvider(Address provider) {
        this.provider = provider;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
