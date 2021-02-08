package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.constants.BlockTypes;
import org.rif.notifier.models.entities.DataFetcherEntity;
import org.rif.notifier.repositories.DataFetcherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class DataFetcherManager {
    private static final BigInteger DEFAULT_INITIAL_LAST_BLOCK = new BigInteger("0");
    @Autowired
    private DataFetcherRepository dataFetcherRepository;

    private DataFetcherEntity saveOrUpdate(BigInteger lastBlock, BlockTypes type){
        DataFetcherEntity dt = dataFetcherRepository.findByBlockType(type);
        if (dt == null) {
            dt = new DataFetcherEntity();
            dt.setBlockType(type);
        }
        dt.setLastBlock(lastBlock);
        return dataFetcherRepository.save(dt);
    }

    private BigInteger getLastBlockForType(BlockTypes type){
        DataFetcherEntity dataFetcherEntity = dataFetcherRepository.findByBlockType(type);
        return dataFetcherEntity != null ? dataFetcherEntity.getLastBlock() :
                DEFAULT_INITIAL_LAST_BLOCK;
    }

    public BigInteger getLastRSKBlock(){
        return getLastBlockForType(BlockTypes.RSK_BLOCK) ;
    }

    public BigInteger getLastRSKChainAddrBlock(){
        return getLastBlockForType(BlockTypes.RSK_CHAINADDR_BLOCK) ;
    }

    public BigInteger getLastPaymentBlock(){
        return getLastBlockForType(BlockTypes.RSK_BLOCK_PAYMENT) ;
    }

    public DataFetcherEntity saveOrUpdate(BigInteger lastBlock){
        return saveOrUpdate(lastBlock, BlockTypes.RSK_BLOCK);
    }

    public DataFetcherEntity saveOrUpdateBlockChainAddress(BigInteger lastBlock){
        return saveOrUpdate(lastBlock, BlockTypes.RSK_CHAINADDR_BLOCK);
    }

    public DataFetcherEntity saveOrUpdateBlockPayment(BigInteger lastBlock){
        return saveOrUpdate(lastBlock, BlockTypes.RSK_BLOCK_PAYMENT);
    }


}
