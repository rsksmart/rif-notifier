package org.rif.notifier.services.blockchain.lumino;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class LuminoInvoice {
    public static String generateInvoice(String address){
        return "123457A90123457B901234C579012345D79012E345790F12345G790123H45790I";
    }
}
