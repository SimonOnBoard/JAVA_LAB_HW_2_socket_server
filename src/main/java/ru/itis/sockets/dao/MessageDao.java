package ru.itis.sockets.dao;

import ru.itis.sockets.model.Message;

import java.util.List;

public interface MessageDao extends CrudDao<Message> {
    List<Message> findAllById(Long id,int limit,boolean foreign_key);
}
