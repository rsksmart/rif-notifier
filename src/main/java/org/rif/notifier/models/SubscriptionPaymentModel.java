package org.rif.notifier.models;

import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.models.entities.Currency;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

public class SubscriptionPaymentModel {
    private static final int SUBSCRIPTION_CREATED_EVENT_SIZE = 4;
    private static final int REFUND_WITHDRAW_EVENT_SIZE = 3;
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

    public static SubscriptionPaymentModel fromEventValues(List<Type> eventValues)  {
        if(eventValues.size() < REFUND_WITHDRAW_EVENT_SIZE || eventValues.size() > SUBSCRIPTION_CREATED_EVENT_SIZE)  {
            throw new ValidationException("Invalid payment event values received");
        }
        try {
            if(eventValues.size() == 4) {
                return new SubscriptionPaymentModel(Numeric.toHexString((byte[])eventValues.get(0).getValue()), //hash
                (Address)eventValues.get(1), //provider
                (BigInteger)eventValues.get(3).getValue(), //amount
                (Address)eventValues.get(2)); //currency
            }
            else if (eventValues.size() == 3)   {
                return new SubscriptionPaymentModel(Numeric.toHexString((byte[])eventValues.get(0).getValue()),
                        null,
                        (BigInteger)eventValues.get(1).getValue(), //amount
                        (Address)eventValues.get(2)); //currency
            }
            throw new ValidationException("Invalid payment event values received");
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
