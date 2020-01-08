package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.DataFetcherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataFetcherRepository extends JpaRepository<DataFetcherEntity, String> {
    public DataFetcherEntity findById(int id);
}
