package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.managers.DbManagerFacade;
import org.rif.notifier.models.entities.RawData;
import org.rif.notifier.repositories.RawDataRepositorty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
public class RawDataManager {

    private static final Logger logger = LoggerFactory.getLogger(RawDataManager.class);

    @Autowired
    private RawDataRepositorty rawDataRepositorty;

    public RawData insert(String type, String data, boolean processed, BigInteger block, int idTopic, int hashcode){
        RawData rd = new RawData(type, data, processed, block, idTopic, hashcode);
        RawData result = rawDataRepositorty.save(rd);
        return result;
    }

    public RawData update(String id, String type, String data, boolean processed, BigInteger block, int idTopic, int hashcode){
        RawData rd = new RawData(id, type, data, processed, block, idTopic, hashcode);
        RawData result = rawDataRepositorty.save(rd);
        return result;
    }

    public List<RawData> getAllRawData(){
        List<RawData> lst = new ArrayList<>();
        rawDataRepositorty.findAll().forEach(lst::add);
        return lst;
    }

    public List<RawData> getRawDataByType(String type){
        List<RawData> lst = new ArrayList<>();
        rawDataRepositorty.findByType(type).forEach(lst::add);
        return lst;
    }

    public List<RawData> getRawDataByTypeAndProcessed(String type, boolean processed){
        List<RawData> lst = new ArrayList<>();
        rawDataRepositorty.findByTypeAndProcessed(type, processed).forEach(lst::add);
        return lst;
    }

    public List<RawData> getRawDataByProcessed(boolean processed){
        return rawDataRepositorty.findByProcessed(processed);
    }

    public RawData getRawdataByHashcode(int hashCode){
        return rawDataRepositorty.findByRowhashcode(hashCode);
    }
}
