package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.managers.NotificationManager;
import org.rif.notifier.models.entities.ChainAddressEvent;
import org.rif.notifier.repositories.ChainAddressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ChainAddressManager {
    @Autowired
    private ChainAddressRepository chainAddressRepository;

    private static final Logger logger = LoggerFactory.getLogger(NotificationManager.class);

    @Value("${notifier.notifications.maxquerylimit}")
    private int MAX_LIMIT_QUERY;

    public ChainAddressEvent insert(String nodehash, String eventName, String chain, String address, int hashcode, BigInteger block) {
        ChainAddressEvent evnt = new ChainAddressEvent(nodehash, eventName, chain, address, hashcode, block);
        ChainAddressEvent result = chainAddressRepository.save(evnt);
        return result;
    }

    public List<ChainAddressEvent> getChainAddressesByNodehashAndEventname(String nodehash, Set<String> eventName){
        Pageable pageable = PageRequest.of(0, MAX_LIMIT_QUERY, Sort.by("id").ascending());
        return new ArrayList<>(chainAddressRepository.findAllByNodehashAndEventNameIn(nodehash, eventName, pageable));
    }

    public List<ChainAddressEvent> getChainAddressesByNodehash(String nodehash){
        Pageable pageable = PageRequest.of(0, MAX_LIMIT_QUERY, Sort.by("id").ascending());
        return new ArrayList<>(chainAddressRepository.findAllByNodehash(nodehash, pageable));
    }

    public List<ChainAddressEvent> getChainAddressesByEventname(Set<String> eventName){
        Pageable pageable = PageRequest.of(0, MAX_LIMIT_QUERY, Sort.by("id").ascending());
        return new ArrayList<>(chainAddressRepository.findAllByEventNameIn(eventName, pageable));
    }

    public List<ChainAddressEvent> getChainAddresses(){
        return new ArrayList<>(chainAddressRepository.findAll());
    }

    public ChainAddressEvent getChainAddressEventByHashcode(int hashCode){
        return chainAddressRepository.findByRowhashcode(hashCode);
    }
}
