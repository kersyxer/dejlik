package com.project.service;

import com.project.dto.UpdateUserRequest;
import com.project.entity.User;

import java.util.List;

public interface UserService {
    void addUser(User user) throws UserException;
    boolean loginUser(String email, String password) throws UserException;
    void deleteUser(Integer id) throws UserException;
    void updateUser(UpdateUserRequest req) throws UserException;
    User findByEmail(String email) throws UserException;
    List<User> getAllUsers();
}

