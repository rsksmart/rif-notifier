package org.rif.notifier.datafetcher;

import org.rif.notifier.exception.RSKBlockChainException;
import org.rif.notifier.models.datafetching.FetchedEvent;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
public class ContractEventEthereumBasedDataFetcher extends EthereumBasedDataFetcher {

    private static final Logger logger = LoggerFactory.getLogger(ContractEventEthereumBasedDataFetcher.class);


    /**
     * Generates a list of FetchedEvents from the blockchain, using the getLogs method
     * Throws completionexception in case, unable to fetch
     * @param ethereumBasedListenable
     * @param from Block number from, to fetch
     * @param to Block number to, to fetch
     * @param web3j Library
     * @return When finished it returns fetched events from the blockchain
     */
    @Async
    @Override
    public CompletableFuture<List<FetchedEvent>> fetch(EthereumBasedListenable ethereumBasedListenable, BigInteger from, BigInteger to, Web3j web3j) {
       return  CompletableFuture.supplyAsync(() -> {
           long start  = System.currentTimeMillis();
           logger.info(Thread.currentThread().getId() + " - Starting reading events for subscription: "+ ethereumBasedListenable);
           List<FetchedEvent> fetchedEventData = new ArrayList<>();
           try {
               fetchedEventData = getLogs(web3j,from, to, ethereumBasedListenable.getAddress(), ethereumBasedListenable.getEventName(), ethereumBasedListenable.getEventFields(), ethereumBasedListenable.getTopicId());
           } catch (Exception e) {
               logger.error(Thread.currentThread().getId() + " - Error fetching contract data for subscription: "+ ethereumBasedListenable, e);
               throw new CompletionException("Error fetching contract data for subscription " + ethereumBasedListenable, e);
               //return new ArrayList<FetchedEvent>();
           }
           long end = System.currentTimeMillis();
           logger.info(Thread.currentThread().getId() + " - End Contract Data fetching time = "+ (end-start));
           return fetchedEventData;

       }).exceptionally(throwable -> {
           logger.error("Error fetching contract data for subscription: "+ ethereumBasedListenable, throwable);
           throw throwable instanceof CompletionException ? (CompletionException)throwable : new CompletionException(throwable);
           //return new ArrayList<>();
       });
    }

    /**
     * Prepares all data to get the log data, applies filters to get the log.
     * The filters to be applied are the params in the method signature
     * @param web3j Instance of Web3j
     * @param from From block number
     * @param to To Block number
     * @param contractAddress ContractAddress filter to be applied
     * @param eventName Event name filter to be applied
     * @param eventFields Need to indicate for Contract Event the params of itself
     * @param topicId Its used to save the fetchedEvent and to make a relationship between the topic that creates this listenable event
     * @return Fetchedevents filtered
     * @throws Exception
     */
    private List<FetchedEvent> getLogs(Web3j web3j, BigInteger from, BigInteger to, String contractAddress, String eventName, List<TypeReference<?>> eventFields, int topicId)
            throws Exception {
        // Create event object to add its signature as a Filter
        Event event =  new Event(eventName, eventFields);

        // Encode event signature
        String encodedEventSignature = EventEncoder.encode(event);

        // Apply filter, retrieve the logs
        EthLog filterLogs = applyFilterForEvent(web3j, encodedEventSignature, contractAddress, from, to);

        List<FetchedEvent> events = new ArrayList<>();

        // Decode event from logs
        for (EthLog.LogResult logResult : filterLogs.getLogs()) {

            Log log = (Log) logResult.get();

            List<String> topics = log.getTopics();

            // The first topic should be the event signature. The next ones the indexed fields
            if (!topics.get(0).equals(encodedEventSignature)) {
                continue;
            }

            // Get indexed values if present
            List<Type> values = new ArrayList<>();
            if (!event.getIndexedParameters().isEmpty()) {
                for (int i = 0; i < event.getIndexedParameters().size(); i++) {
                    Type value =
                            FunctionReturnDecoder.decodeIndexedValue(
                                    topics.get(i + 1), event.getIndexedParameters().get(i));
                    values.add(value);
                }
            }

            // Get non indexed values (Decode data)
            values.addAll(FunctionReturnDecoder.decode(log.getData(), event.getNonIndexedParameters()));

            events.add(new FetchedEvent(eventName, values, log.getBlockNumber(), contractAddress, topicId));
        }
        return events;
    }

    /**
     * Apply filters to event and calls web3j library to get the data filtered, returns a EthLog
     */
    private EthLog applyFilterForEvent(
           Web3j web3j, String encodedEventSignature, String contractAddress, BigInteger from, BigInteger to)
            throws Exception {

        EthFilter ethFilter =
                new EthFilter(
                        from == null ? DefaultBlockParameterName.EARLIEST : DefaultBlockParameter.valueOf(from),
                        to == null ? DefaultBlockParameterName.LATEST : DefaultBlockParameter.valueOf(to),
                        contractAddress);

        ethFilter.addSingleTopic(encodedEventSignature);

        return web3j.ethGetLogs(ethFilter).send();
    }
}
