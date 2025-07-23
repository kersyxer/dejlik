package com.project.service;

import com.project.dto.UpdateUserRequest;
import com.project.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceJPA implements UserService{
    @PersistenceContext
    private EntityManager em;

    private final PasswordEncoder passwordEncoder;

    public UserServiceJPA(EntityManager entityManager ,PasswordEncoder passwordEncoder) {
        this.em = entityManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void addUser(User user) throws UserException {
        boolean emailExists = em.createNamedQuery("User.findByEmail", User.class)
                .setParameter("email", user.getEmail())
                .getResultStream()
                .findAny()
                .isPresent();
        if (emailExists) {
            throw new UserException("Email already taken: " + user.getEmail());
        }

        boolean nameExists = em.createNamedQuery("User.findByName", User.class)
                .setParameter("name", user.getName())
                .getResultStream()
                .findAny()
                .isPresent();
        if (nameExists) {
            throw new UserException("Name already taken: " + user.getName());
        }

        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed);
        em.persist(user);
    }

    @Override
    public boolean loginUser(String email, String password) throws UserException {
        User user = em
                .createNamedQuery("User.findByEmail", User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new UserException("User not found with email: " + email));

        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public void deleteUser(Integer id) throws UserException {
        int deletedCount = em
                .createNamedQuery("User.deleteById")
                .setParameter("id", id)
                .executeUpdate();
        if (deletedCount == 0) {
            throw new UserException("Cannot delete – user not found, id: " + id);
        }
    }

    @Override
    public void updateUser(UpdateUserRequest req) throws UserException {
        User existing = em.createNamedQuery("User.findByEmail", User.class)
                .setParameter("email", req.getEmail())
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new UserException("User not found: " + req.getEmail()));

        if (req.getName() != null && !req.getName().isBlank()) {
            boolean nameTaken = em.createNamedQuery("User.findByName", User.class)
                    .setParameter("name", req.getName())
                    .getResultStream()
                    .anyMatch(u -> !u.getEmail().equals(req.getEmail()));
            if (nameTaken) {
                throw new UserException("Name already taken: " + req.getName());
            }
            existing.setName(req.getName());
        }

        if (req.getRole() != null && !req.getRole().isBlank()) {
            existing.setRole(req.getRole());
        }

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            String hashed = passwordEncoder.encode(req.getPassword());
            existing.setPassword(hashed);
        }
    }

    @Override
    public User findByEmail(String email) throws UserException {
        return em.createNamedQuery("User.findByEmail", User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new UserException("User not found with email: " + email));
    }

    @Override
    public List<User> getAllUsers() {
        return em.createNamedQuery("User.allUsers", User.class).getResultList();
    }
}
