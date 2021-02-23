package org.rif.notifier.services;

import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.Currency;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;

import java.math.BigInteger;
import java.util.Optional;

@Service
public class CurrencyServices {
    private DbManagerFacade dbManagerFacade;

    public CurrencyServices(DbManagerFacade dbManagerFacade)    {
        this.dbManagerFacade = dbManagerFacade;
    }


    /**
     * Get the currency by given address
     * @param address
     * @return
     */
    public Optional<Currency> getCurrencyByAddress(Address address){
        return dbManagerFacade.getCurrencyByAddress(address);
    }

    /**
     * Get the currency by given  name
     * @param name
     * @return
     */
    public Optional<Currency> getCurrencyByName(String name){
        return dbManagerFacade.getCurrencyByName(name);
    }


}
