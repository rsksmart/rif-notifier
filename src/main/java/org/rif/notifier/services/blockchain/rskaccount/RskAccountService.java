package org.rif.notifier.services.blockchain.rskaccount;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

@Service
@Component
public class RskAccountService {

  @Value("${rif.notifier.account.password}")
  private String notifierAccountPassword;

  @Value("${rif.notifier.account.file}")
  private String notifierApiAccountFile;

  private Credentials rskAccountCredentials;

  @Autowired private ResourceLoader resourceLoader;

  public Credentials getRskAccountCredentials() {
    if (rskAccountCredentials == null) {
      try {
        Resource res = resourceLoader.getResource("classpath:" + notifierAccountPassword);
        rskAccountCredentials =
            WalletUtils.loadCredentials(notifierApiAccountFile, res.getFile());
      } catch (Exception e) {
        return null;
      }
    }
    return rskAccountCredentials;
  }
}
