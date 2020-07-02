package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.ChainAddressEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ChainAddressRepository extends JpaRepository<ChainAddressEvent, String> {
    List<ChainAddressEvent> findAllByNodehashAndEventNameIn(String nodehash, Set<String> eventName, Pageable pageable);

    List<ChainAddressEvent> findAllByNodehash(String nodehash, Pageable pageable);

    List<ChainAddressEvent> findAllByEventNameIn(Set<String> eventName, Pageable pageable);
}
