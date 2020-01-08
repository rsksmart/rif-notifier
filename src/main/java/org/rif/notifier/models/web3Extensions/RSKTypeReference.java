package org.rif.notifier.models.web3Extensions;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;

/*
    This class was created to have a create method that can call the constructor with the indexed value
    Typereference dont have a method to set the indexed value, or a solution for it.
 */
public class RSKTypeReference extends TypeReference {
    public static <T extends Type> TypeReference<T> createWithIndexed(final Class<T> cls, boolean indexed) {
        return new TypeReference<T>(indexed) {
            public java.lang.reflect.Type getType() {
                return cls;
            }
        };
    }
}
