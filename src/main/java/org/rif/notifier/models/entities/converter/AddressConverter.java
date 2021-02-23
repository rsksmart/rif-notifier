package org.rif.notifier.models.entities.converter;

import org.web3j.abi.datatypes.Address;
import org.web3j.utils.Numeric;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.math.BigInteger;

/**
 * Converts Web3j Address type to Database BigInteger type and vice versa
 * Applies the conversion to entities that use Address type in the fields
 */
@Converter(autoApply = true)
public class AddressConverter implements AttributeConverter<Address, BigInteger> {
    /**
     * Converts web3j Address type to database BigInteger type
     * @param address
     * @return
     */
    @Override
    public BigInteger convertToDatabaseColumn(Address address) {
        return address.toUint160().getValue();
    }

    /**
     * Converts database type BigInteger to web3j Address type
     * @param address
     * @return
     */
    @Override
    public Address convertToEntityAttribute(BigInteger address) {
        return new Address(address);
    }
}
