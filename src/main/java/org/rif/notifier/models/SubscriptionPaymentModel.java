package org.rif.notifier.models;

import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.models.entities.Currency;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

import static org.rif.notifier.services.blockchain.payment.RskPaymentService.*;

public class SubscriptionPaymentModel {
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

    public static SubscriptionPaymentModel fromEventValues(String eventName, List<Type> eventValues)  {
        try {
            switch (eventName) {
                case EVENT_REFUND:
                case EVENT_WITHDRAWAL:
                    return new SubscriptionPaymentModel(Numeric.toHexString((byte[]) eventValues.get(1).getValue()), //hash
                            (Address)eventValues.get(0), //provider
                            (BigInteger) eventValues.get(2).getValue(), //amount
                            (Address) eventValues.get(3)); //currency
                case EVENT_SUBSCRIPTION_CREATED:
                    return new SubscriptionPaymentModel(Numeric.toHexString((byte[]) eventValues.get(0).getValue()), //hash
                            (Address) eventValues.get(1), //provider
                            (BigInteger) eventValues.get(3).getValue(), //amount
                            (Address) eventValues.get(2)); //currency
                default:
                    throw new ValidationException("Unsupported payment event " + eventName);
            }
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