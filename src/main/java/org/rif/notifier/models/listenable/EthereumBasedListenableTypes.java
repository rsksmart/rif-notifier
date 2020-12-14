package org.rif.notifier.models.listenable;

public enum EthereumBasedListenableTypes {

    NEW_BLOCK("Block"),
    NEW_TRANSACTIONS("Transaction"),
    PENDING_TRANSACTIONS("Pending"),
    CONTRACT_EVENT("ContractAddress");

    private String type;
    EthereumBasedListenableTypes(String type)    {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
