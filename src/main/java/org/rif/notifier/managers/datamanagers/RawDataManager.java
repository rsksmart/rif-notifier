package org.rif.notifier.managers.datamanagers;

import org.rif.notifier.models.entities.RawData;
import org.rif.notifier.repositories.RawDataRepositorty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
public class RawDataManager {

    @Autowired
    private RawDataRepositorty rawDataRepositorty;

    public RawData insert(String type, String data, boolean processed, BigInteger block, int idTopic){
        RawData rd = new RawData(type, data, processed, block, idTopic);
        RawData result = rawDataRepositorty.save(rd);
        return result;
    }

    public RawData update(String id, String type, String data, boolean processed, BigInteger block, int idTopic){
        RawData rd = new RawData(id, type, data, processed, block, idTopic);
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
        List<RawData> lst = new ArrayList<>();
        rawDataRepositorty.findByProcessed(processed).forEach(lst::add);
        return lst;
    }
}
