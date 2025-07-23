package com.project.service;

import com.project.dto.UpdateUserRequest;
import com.project.entity.User;

import java.util.List;

public interface UserService {
    void createUser(User user) throws UserException;
    boolean loginUser(String email, String password) throws UserException;
    void deleteUser(Integer id) throws UserException;
    void updateUser(Integer id, String name, String password, String role) throws UserException;
    User findByEmail(String email) throws UserException;
    User findById(Integer id) throws UserException;
    List<User> getAllUsers();
}

