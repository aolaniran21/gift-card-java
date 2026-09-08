package com.example.auth.infrastructure.persistence;

import com.example.auth.domain.User;
import com.example.auth.domain.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpa;

    public UserRepositoryAdapter(JpaUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    private User toDomain(UserEntity e) {
        return new User(e.getId(), e.getEmail(), e.getPassword(), e.getFirstName(), e.getLastName(), e.getRole());
    }

    private UserEntity toEntity(User u) {
        UserEntity e = new UserEntity(u.getEmail(), u.getPassword(), u.getFirstName(), u.getLastName(), u.getRole());
        if (u.getId() != null) {
            e.setId(u.getId());
        }
        return e;
    }
}
