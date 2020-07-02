package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.DataFetcherEntity;
import org.rif.notifier.repositories.DataFetcherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
public class DataFetcherManager {
    @Autowired
    private DataFetcherRepository dataFetcherRepository;

    public DataFetcherEntity insert(BigInteger lastBlock){
        DataFetcherEntity dt;
        List<DataFetcherEntity> lst = dataFetcherRepository.findAll();
        if(lst.size() > 0)
            dt = lst.get(0);
        else
            dt = new DataFetcherEntity();
        dt.setLastBlock(lastBlock);
        return dataFetcherRepository.save(dt);
    }

    public DataFetcherEntity insertLastBlockChainAddress(BigInteger lastBlock){
        DataFetcherEntity dt;
        List<DataFetcherEntity> lst = dataFetcherRepository.findAll();
        if(lst.size() > 1)
            dt = lst.get(1);
        else
            dt = new DataFetcherEntity();
        dt.setLastBlock(lastBlock);
        return dataFetcherRepository.save(dt);
    }

    public BigInteger get(){
        List<DataFetcherEntity> lst = dataFetcherRepository.findAll();
        if(lst.size() > 0)
            return lst.get(0).getLastBlock();
        else
            return new BigInteger("0");
    }

    public BigInteger getBlockChainAddresses(){
        List<DataFetcherEntity> lst = dataFetcherRepository.findAll();
        if(lst.size() > 1)
            return lst.get(1).getLastBlock();
        else
            return new BigInteger("0");
    }
}
