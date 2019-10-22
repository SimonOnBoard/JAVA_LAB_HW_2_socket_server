package ru.itis.sockets.dao;

import ru.itis.sockets.model.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MessageDaoImpl implements MessageDao {
    private Connection connection;

    public MessageDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Message> find(Integer id) {
        return Optional.empty();
    }

    @Override
    public void save(Message model) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO message (text, date, owner_id) VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS);) {
            statement.setString(1,model.getText());
            statement.setObject(2, LocalDateTime.now());
            statement.setLong(3,model.getOwnerId());
            //Выполняем запрос и сохраняем колличество изменённых строк
            int updRows = statement.executeUpdate();
            if (updRows == 0) {
                //Если ничего не было изменено, значит возникла ошибка
                //Возбуждаем соответсвующее исключений
                throw new SQLException();
            }
            //Достаём созданное Id пользователя
            try (ResultSet set = statement.getGeneratedKeys();) {
                //Если id  существет,обновляем его у подели.
                if (set.next()) {
                    model.setId(set.getLong(1));
                } else {
                    //Модель сохранилась но не удаётся получить сгенерированный id
                    //Возбуждаем соответвующее исключение
                    throw new SQLException();
                }
            }

        } catch (SQLException e) {
            //Если сохранений провалилось, обернём пойманное исключение в непроверяемое и пробросим дальше(best-practise)
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void update(Message model) {

    }

    @Override
    public void delete(Integer id) {

    }

    @Override
    public List<Message> findAll() {
        return null;
    }
}
