package org.rif.notifier.scheduled;

import org.apache.commons.collections4.CollectionUtils;
import org.rif.notifier.helpers.LuminoDataFetcherHelper;
import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.entities.ChainAddressEvent;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.models.listenable.EthereumBasedListenableTypes;
import org.rif.notifier.services.LuminoEventServices;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Bytes4;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.rif.notifier.constants.CommonConstants.RSK_SLIP_ADDRESS;

@Component
public class LuminoDataFetchingJob {

    private static final Logger logger = LoggerFactory.getLogger(LuminoDataFetchingJob.class);

    @Autowired
    private RskBlockchainService rskBlockchainService;

    @Autowired
    private DbManagerFacade dbManagerFacade;

    @Autowired
    private LuminoEventServices luminoEventServices;

    @Autowired
    private LuminoDataFetcherHelper luminoHelper;

    @Scheduled(fixedDelayString = "${notifier.run.fixedDelayFetchingChainAddresses}", initialDelayString = "${notifier.run.fixedInitialDelayFetchingChainAddresses}")
    public void runChainAddresses() throws Exception {
        // Get latest block for this run
        BigInteger to = rskBlockchainService.getLastBlock();
        BigInteger fromChainAddresses;
        fromChainAddresses = dbManagerFacade.getLastBlockForChainAddresses();
        fromChainAddresses = fromChainAddresses.add(new BigInteger("1"));
        long start = System.currentTimeMillis();
        List<CompletableFuture<List<FetchedEvent>>> chainAddresses = new ArrayList<>();
        chainAddresses.add(rskBlockchainService.getContractEvents(luminoHelper.getAddrChanged(), fromChainAddresses, to));
        chainAddresses.add(rskBlockchainService.getContractEvents(luminoHelper.getChainAddrChanged(), fromChainAddresses, to));

        BigInteger finalFromChainAddresses = fromChainAddresses;
        chainAddresses.forEach(listCompletableFuture -> {
            listCompletableFuture.whenComplete((fetchedEvents, throwable) -> {
                List<ChainAddressEvent> chainsEvents = new ArrayList<>();
                long end = System.currentTimeMillis();
                logger.info(Thread.currentThread().getId() + " - End fetching chainaddres = " + (end - start));
                logger.info(Thread.currentThread().getId() + " - Completed fetching chainaddresses: " + fetchedEvents.size());
                if(throwable != null) {
                    dbManagerFacade.saveLastBlock(finalFromChainAddresses);
                } else {
                    // I need to filter by topics, cause here we have chainaddresses events for RSK and other chains that has different params in the event
                    fetchedEvents.stream().filter(item -> item.getTopicId() == -2).forEach(item -> {
                        // RSK AddrChanged event
                        CollectionUtils.addIgnoreNull(chainsEvents,buildChainAddress(item, true));
                    });
                    fetchedEvents.stream().filter(item -> item.getTopicId() == -3).forEach(item -> {
                        // ChainAddrChanged event
                        CollectionUtils.addIgnoreNull(chainsEvents,buildChainAddress(item, false));
                    });

                    if (!chainsEvents.isEmpty()) {
                        dbManagerFacade.saveChainAddressesEvents(chainsEvents);
                    }
                }
            });
        });
        dbManagerFacade.saveLastBlockChainAdddresses(to);
    }

    private ChainAddressEvent buildChainAddress(FetchedEvent item, boolean addrChanged)    {
        String nodehash = Numeric.toHexString((byte[]) item.getValues().get(0).getValue());
        String eventName = addrChanged ? "AddrChanged" : "ChainAddrChanged";
        String address = addrChanged ? item.getValues().get(1).getValue().toString() : item.getValues().get(2).getValue().toString();
        String chain = addrChanged ? RSK_SLIP_ADDRESS : Numeric.toHexString((byte[]) item.getValues().get(1).getValue());
        ChainAddressEvent chainAddr = new ChainAddressEvent(nodehash, eventName, chain, address, item.getBlockNumber());
        chainAddr.setRowhashcode(chainAddr.hashCode());
        if (dbManagerFacade.getChainAddressEventByHashcode(chainAddr.getRowhashcode()) == null) {
            return chainAddr;
        }
        return null;
    }


}
