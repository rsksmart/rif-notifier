package org.rif.notifier.models;

import org.rif.notifier.exception.ValidationException;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;

import java.math.BigInteger;
import java.util.List;

public class SubscriptionPaymentModel {
    private static final int EXPECTED = 4;
    private BigInteger amount;
    private String currency;
    private String hash;
    private Address provider;

    public SubscriptionPaymentModel(String hash, Address provider, BigInteger amount, String currency) {
        this.amount = amount;
        this.currency = currency;
        this.hash = hash;
        this.provider = provider;
    }

    private SubscriptionPaymentModel(List<Type> values)   {
        this((String)values.get(0).getValue(), //hash
                (Address)values.get(1), //provider
                (BigInteger)values.get(2).getValue(), //amount
                (String)values.get(3).getValue()); //currency
    }

    public static SubscriptionPaymentModel fromEventValues(List<Type> eventValues)  {
        if(eventValues.size() != EXPECTED)  {
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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
}
