package org.rif.notifier.repositories;

import org.rif.notifier.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserRepository extends JpaRepository<User, String> {
    public User findByApiKey(String apiKey);

    public User findByAddress(String address);
}
