package org.rif.notifier.services.blockchain.generic.rootstock;

import okhttp3.OkHttpClient;
import org.rif.notifier.services.blockchain.generic.ethereum.EthereumBasedService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.TimeUnit;

@Service
public class RskBlockchainService extends EthereumBasedService {

    @Value("${rsk.blockchain.endpoint}")
    private String rskBlockchainEndpoint;

    @Override
    public void buildWeb3() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(new Long(100), TimeUnit.SECONDS);
        builder.readTimeout(new Long(100), TimeUnit.SECONDS);
        builder.writeTimeout(new Long(100), TimeUnit.SECONDS);
        OkHttpClient client = builder.build();
        web3j = Web3j.build(new HttpService(rskBlockchainEndpoint, client));
    }
}

