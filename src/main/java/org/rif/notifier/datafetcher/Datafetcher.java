package org.rif.notifier.datafetcher;

import org.rif.notifier.models.web3Extensions.RSKTypeReference;
import org.rif.notifier.models.entities.Topic;
import org.rif.notifier.models.entities.TopicParams;
import org.rif.notifier.models.listenable.EthereumBasedListenable;
import org.rif.notifier.models.listenable.EthereumBasedListenableTypes;
import org.rif.notifier.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.rif.notifier.constants.TopicParamTypes.*;


public class Datafetcher {

    private static final Logger logger = LoggerFactory.getLogger(Datafetcher.class);

    private static final String PATH_TO_TYPES = "org.web3j.abi.datatypes.";

    /**
     * Creates a EthereumBasedListenable for a contract event from a given Topic, the Topic needs to be validated before calling this method
     * @param tp
     * @return
     * @throws ClassNotFoundException
     */
    private static EthereumBasedListenable CreateContractEventListeneable(Topic tp) throws ClassNotFoundException {
        List<TypeReference<?>> params = new ArrayList<>();
        //Streaming to get the address, eventName and parameters to create the listeneable
        Optional<TopicParams> addressParam = tp.getTopicParams().stream()
                .filter(item -> item.getType().equals(CONTRACT_ADDRESS)).findAny();
        String address = addressParam.isPresent() ? addressParam.get().getValue() : "";
        Optional<TopicParams> eventNameParam = tp.getTopicParams().stream()
                .filter(item -> item.getType().equals(EVENT_NAME)).findFirst();
        String eventName = eventNameParam.isPresent() ? eventNameParam.get().getValue() : "";
        List<TopicParams> topicParams = tp.getTopicParams().stream()
                .filter(item -> item.getType().equals(EVENT_PARAM))
                .collect(Collectors.toList());
        for(TopicParams param : topicParams) {
            String value = param.getValueType();
            boolean indexed = param.getIndexed();
            Class myClass;
            //Get the reflection of the datatype
            if (Utils.isClass(PATH_TO_TYPES + value)) {
                myClass = Class.forName(PATH_TO_TYPES + value);
            } else {
                myClass = Class.forName(PATH_TO_TYPES + "generated." + value);
            }

            TypeReference paramReference = RSKTypeReference.createWithIndexed(myClass, indexed);

            params.add(paramReference);
        }
        if(address.isEmpty() || eventName.isEmpty())
            return null;
        return new EthereumBasedListenable(address, EthereumBasedListenableTypes.CONTRACT_EVENT, params, eventName, tp.getId());
    }

    /**
     * Returns a EthereumBasedListenable of a type of event
     * @param tp
     * @return
     * @throws ClassNotFoundException
     */
    public static EthereumBasedListenable getEthereumBasedListenableFromTopic(Topic tp) throws ClassNotFoundException {
        EthereumBasedListenable rtn = null;
        switch (tp.getType()){
            case CONTRACT_EVENT:
                rtn = CreateContractEventListeneable(tp);
                break;
            case NEW_BLOCK:
                rtn = new EthereumBasedListenable(null, EthereumBasedListenableTypes.NEW_BLOCK, null, null, tp.getId());
                break;
            case NEW_TRANSACTIONS:
                rtn = new EthereumBasedListenable("0x2", EthereumBasedListenableTypes.NEW_TRANSACTIONS, null, null, tp.getId());
                break;
        }
        return rtn;
    }
}
