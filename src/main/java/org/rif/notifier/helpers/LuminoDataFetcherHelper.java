package org.rif.notifier.helpers;

import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.models.listenable.EthereumBasedListenableTypes;
import org.rif.notifier.scheduled.LuminoDataFetchingJob;
import org.rif.notifier.services.LuminoEventServices;
import org.rif.notifier.services.blockchain.generic.rootstock.RskBlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Bytes4;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class LuminoDataFetcherHelper {
    private static final Logger logger = LoggerFactory.getLogger(LuminoDataFetcherHelper.class);

    @Value("${rsk.blockchain.multichaincontract}")
    private String multiChainContract;

    @Autowired
    private RskBlockchainService rskBlockchainService;

    @Autowired
    private LuminoEventServices luminoEventServices;

    @Value("${rsk.blockchain.tokennetworkregistry}")
    private String tokenNetworkRegistry;

    /**
     * Creates a EthereumBasedListeneable to get all events AddrChanged
     * @return EthereumBasedListenable to get event AddrChanged
     */
    public EthereumBasedListenable getAddrChanged(){
        return new EthereumBasedListenable(multiChainContract, EthereumBasedListenableTypes.CONTRACT_EVENT, Arrays.asList(
                new TypeReference<Bytes32>(true) {},
                new TypeReference<Address>(false) {}
        ), "AddrChanged", -2);
    }

    /**
     * Creates a EthereumBasedListeneable to get all events chainAddrChanged
     * @return EthereumBasedListenable to get event chainAddrChanged
     */
    public EthereumBasedListenable getChainAddrChanged(){
        return new EthereumBasedListenable(multiChainContract, EthereumBasedListenableTypes.CONTRACT_EVENT, Arrays.asList(
                new TypeReference<Bytes32>(true) {},
                new TypeReference<Bytes4>(false) {},
                new TypeReference<Utf8String>(false) {}
        ), "ChainAddrChanged", -3);
    }

    public boolean fetchTokens(long start, BigInteger toBlock) throws Exception {
        List<CompletableFuture<List<FetchedEvent>>> tokenTasks = new ArrayList<>();
        tokenTasks.add(rskBlockchainService.getContractEvents(getTokensNetwork(), new BigInteger("0"), toBlock));
        tokenTasks.forEach(listCompletableFuture -> {
            listCompletableFuture.whenComplete((fetchedEvents, throwable) -> {
                long end = System.currentTimeMillis();
                logger.info(Thread.currentThread().getId() + " - End fetching tokens = " + (end - start));
                logger.info(Thread.currentThread().getId() + " - Completed fetching tokens: " + fetchedEvents);
                for (FetchedEvent fetchedEvent : fetchedEvents) {
                    luminoEventServices.addToken(fetchedEvent.getValues().get(1).getValue().toString());
                }
            });
        });
        if (luminoEventServices.getTokens().size() > 0) {
            //fetchedTokens = true;
            return true;
        }
        return false;
    }

    /**
     * Creates a EthereumBasedListeneable to get all tokens registered in the blockchain and returns that object
     * @return EthereumBasedListenable to get tokens
     */
    public EthereumBasedListenable getTokensNetwork(){
        return new EthereumBasedListenable(tokenNetworkRegistry, EthereumBasedListenableTypes.CONTRACT_EVENT, Arrays.asList(
                new TypeReference<Address>(true) {},
                new TypeReference<Address>(true) {}
        ), "TokenNetworkCreated", -1);
    }
}
