package org.rif.notifier.services.blockchain.lumino;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class LuminoInvoice {
    private String luminoInvoice;
    LuminoInvoice(@Value("${rsk.blockchain.luminoinvoice}") String luminoInvoice) {
       this.luminoInvoice = luminoInvoice;
    }
    public String generateInvoice(String address){
        return luminoInvoice;
    }
}
