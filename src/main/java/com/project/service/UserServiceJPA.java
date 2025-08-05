package com.project.service;

import com.project.dto.UpdateUserRequest;
import com.project.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceJPA implements UserService{
    @PersistenceContext
    private EntityManager em;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceJPA(EntityManager entityManager ,PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.em = entityManager;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void createUser(User user) throws UserException {
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
    public void deleteUser(UUID id) throws UserException {
        User existing = em.find(User.class, id);
        if (existing == null) {
            throw new UserException("User not found with id: " + id);
        }
        em.remove(existing);
    }

    @Override
    public void updateUser(UUID id, String name, String password, String role) throws UserException {
        User existing = em.find(User.class, id);
        if(existing == null){
            throw new UserException("User not found with id: " + id);
        }

        if (name != null && !name.isBlank()) {
            boolean nameTaken = em.createNamedQuery("User.findByName", User.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .anyMatch(u -> !u.getId().equals(id));
            if (nameTaken) {
                throw new UserException("Name already taken: " + name);
            }
            existing.setName(name);
        }
        if (role != null && !role.isBlank()) {
            existing.setRole(role);
        }
        if (password != null && !password.isBlank()) {
            String hashed = passwordEncoder.encode(password);
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

    @Override
    public User findById(UUID id) throws UserException {
        User u = em.find(User.class, id);
        if(u == null){
            throw new UserException("User not found with id: " + id);
        }
        return u;
    }

    @Override
    public List<String> findAllUsernamesByRole(String role) {
        return userRepository.findByRole(role).stream()
                .map(User::getName)
                .collect(Collectors.toList());
    }

    @Override
    public User findByUsername(String username) {
        List<User> users = userRepository.findByName(username); // Повертає список користувачів
        if (users.isEmpty()) {
            throw new UserException("User not found with name: " + username);
        }
        return users.getFirst();
    }
}
