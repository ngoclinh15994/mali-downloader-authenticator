package com.malitool.authentication.repository;

import com.malitool.authentication.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    User findUserByUsername(String username);

    User findUserByUsernameAndPassword(String username, String password);

    User findByEmail(String email);
}
