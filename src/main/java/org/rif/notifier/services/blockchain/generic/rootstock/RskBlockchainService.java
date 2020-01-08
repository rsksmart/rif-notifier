package org.rif.notifier.services.blockchain.generic.rootstock;

import org.rif.notifier.services.blockchain.generic.ethereum.EthereumBasedService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Service
public class RskBlockchainService extends EthereumBasedService {

    @Value("${rsk.blockchain.endpoint}")
    private String rskBlockchainEndpoint;

    @Override
    public void buildWeb3() {
        web3j = Web3j.build(new HttpService(rskBlockchainEndpoint));
    }
}
