package org.rif.notifier.validation;

import org.rif.notifier.boot.configuration.NotifierConfig;
import org.rif.notifier.constants.ResponseConstants;
import org.rif.notifier.exception.ValidationException;
import org.rif.notifier.models.entities.Currency;
import org.rif.notifier.services.CurrencyServices;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrencyValidator extends BaseValidator {
    private CurrencyServices currencyServices;
    private NotifierConfig notifierConfig;

    public CurrencyValidator(CurrencyServices currencyServices, NotifierConfig notifierConfig) {
       this.currencyServices = currencyServices;
       this.notifierConfig = notifierConfig;
    }

    public Optional<Currency> validate(org.web3j.abi.datatypes.Address currencyAddress)    {
        return currencyServices.getCurrencyByAddress(currencyAddress);
    }

    public Currency validate(String currencyName)   {
        if(notifierConfig.getAcceptedCurrencies().stream().noneMatch(currency->currency.equals(currencyName)))   {
            throw new ValidationException(ResponseConstants.INVALID_CURRENCY);
        }
        return currencyServices.getCurrencyByName(currencyName).orElseThrow(()->new ValidationException(ResponseConstants.INVALID_CURRENCY));
    }
}
