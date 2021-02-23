package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;

import java.math.BigInteger;
import java.util.Optional;

@Service
public interface CurrencyRepository extends JpaRepository<Currency, Integer> {
    public Optional<Currency> findByAddress(Address address);
    public Optional<Currency> findByName(String name);
}
