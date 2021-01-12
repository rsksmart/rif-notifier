package org.rif.notifier.services;

import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.ChainAddressEvent;
import org.rif.notifier.models.entities.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ChainAddressesServices {

    @Autowired
    private DbManagerFacade dbManagerFacade;

    private static final Logger logger = LoggerFactory.getLogger(ChainAddressesServices.class);

    /**
     * Returns a list of chain addresses events, given some filters, or all instead
     * @return List<ChainAddressEvent> with all the events retrieved by the notifier
     */
    public List<ChainAddressEvent> getChainAddresses(String address, String nodehash, Set<String> eventName){
        List<ChainAddressEvent> lst = new ArrayList<>();
        List<Subscription> subs = dbManagerFacade.getActiveSubscriptionByAddress(address);
        subs.forEach(sub->{
            if(sub != null) {
                if (sub.isActive())
                    lst.addAll(dbManagerFacade.getChainAddresses(nodehash, eventName));
            }
        });
        return lst;
    }
}
