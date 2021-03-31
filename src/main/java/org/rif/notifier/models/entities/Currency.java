package org.rif.notifier.models.entities;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.rif.notifier.util.JsonUtil;
import org.web3j.abi.datatypes.Address;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;

@Entity
@Table(name = "currency")
public class Currency {

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;

    @NotBlank
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "address")
    private org.web3j.abi.datatypes.Address address;

    public Currency()   { }

    public Currency(String name) {
       this.name = name;
    }

    public Currency(String name, Address address) {
        this.name = name ;
        this.address = address;
    }

    public org.web3j.abi.datatypes.Address getAddress() {
        return address;
    }

    public void setAddress(org.web3j.abi.datatypes.Address address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Currency currency = (Currency) o;

        return new EqualsBuilder()
                .append(name, currency.name)
                .append(address, currency.address)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(address)
                .toHashCode();
    }

    @Override
    public String toString() {
        HashMap<String, Object> map = new HashMap<>(2);
        map.put("name", name);
        map.put("address", address);
        return JsonUtil.writeValueAsString(map) ;
    }
}
