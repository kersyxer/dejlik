package com.project.service;

import com.project.dto.UpdateUserRequest;
import com.project.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    void createUser(User user) throws UserException;
    boolean loginUser(String email, String password) throws UserException;
    void deleteUser(UUID id) throws UserException;
    void updateUser(UUID id, String name, String password, String role) throws UserException;
    User findByEmail(String email) throws UserException;
    User findById(UUID id) throws UserException;
    List<User> getAllUsers();
}

