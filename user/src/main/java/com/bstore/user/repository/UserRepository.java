package com.bstore.user.repository;

import com.bstore.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, QueryByExampleExecutor<User> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

}