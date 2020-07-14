package org.rif.notifier.repositories;


import org.rif.notifier.models.entities.RawData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RawDataRepositorty extends JpaRepository<RawData, String> {
    public List<RawData> findByType(String type);

    public List<RawData> findByTypeAndProcessed(String type, boolean processed);

    public List<RawData> findByProcessed(boolean processed);

    public RawData findByRowhashcode(int hashCode);
}

