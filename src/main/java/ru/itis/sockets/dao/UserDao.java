package ru.itis.sockets.dao;


import ru.itis.sockets.model.User;

import java.util.Optional;

public interface UserDao extends CrudDao<User>{
    Optional<User> findByName(String login);
}
