package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.Currency;
import org.rif.notifier.repositories.CurrencyRepository;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;

import java.math.BigInteger;
import java.util.Optional;

@Service
public class CurrencyManager {
    private CurrencyRepository currencyRepository;

    public CurrencyManager(CurrencyRepository currencyRepository)   {
        this.currencyRepository = currencyRepository;
    }

    public Optional<Currency> getCurrencyByAddress(Address address){
        return currencyRepository.findByAddress(address);
    }

    public Optional<Currency> getCurrencyByName(String name){
        return currencyRepository.findByName(name);
    }

    public Currency saveCurrency(Currency currency) {
       return currencyRepository.save(currency);
    }
}
