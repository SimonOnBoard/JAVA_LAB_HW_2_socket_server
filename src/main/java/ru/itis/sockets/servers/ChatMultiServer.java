package ru.itis.sockets.servers;

import ru.itis.sockets.dao.UserDao;
import ru.itis.sockets.dao.UserDaoImpl;
import ru.itis.sockets.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.DriverManager;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatMultiServer {
    // список клиентов
    private List<ClientHandler> clients;
    private String[] properties;
    public ChatMultiServer(String[] properties) {
        this.properties =  properties;
        // Список для работы с многопоточностью
        clients = new CopyOnWriteArrayList<>();
    }

    public void start(int port) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        // запускаем бесконечный цикл
        while (true) {
            try {
                // запускаем обработчик сообщений для каждого подключаемого клиента
//                ClientHandler handler = new ClientHandler(serverSocket.accept());
//                handler.start();
            new ClientHandler(serverSocket.accept()).start();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private class ClientHandler extends Thread {
        // связь с одним клиентом
        private Socket clientSocket;
        private BufferedReader in;
        private UserDao userDao;
        private User user;
        ClientHandler(Socket socket) {
            this.clientSocket = socket;
            // добавляем текущее подключение в список
            System.out.println("New client");
        }

        public void run() {
            try {
                Thread thread = Thread.currentThread();
                this.userDao = new UserDaoImpl(DriverManager.getConnection(properties[0],properties[1],properties[2]));
                // получем входной поток для конкретного клиента
                in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                checkAuth();
                while ((inputLine = in.readLine()) != null) {
                    if (".".equals(inputLine)) {
                        // бегаем по всем клиентам и обовещаем их о событии
                        for (ClientHandler client : clients) {
                            PrintWriter out = new PrintWriter(client.clientSocket.getOutputStream(), true);
                            out.println("bye");
                        }
                        this.clientSocket.close();
                        clients.remove(this);
                        in.close();
                        this.stop();
                        break;
                    } else {
                        for (ClientHandler client : clients) {
                            PrintWriter out = new PrintWriter(client.clientSocket.getOutputStream(), true);
                            out.println(inputLine);
                        }
                    }
                }
//                in.close();
//                clientSocket.close();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private void checkAuth() {
            try {
                PrintWriter out = new PrintWriter(this.clientSocket.getOutputStream(),true);
                out.println("1 - зарегистрироваться,2 - авторизироваться");
                boolean flag = false;
                while(!flag){
                    String input = in.readLine();
                    switch (input){
                        case "1":
                            registration(out);
                            login(out);
                            flag = true;
                            break;
                        case "2":
                            login(out);
                            flag = true;
                            break;
                        default:
                            out.println("Неверный выбор)) Попробуйте ещё раз");
                    }
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private void login(PrintWriter out) throws IOException {
            out.println("Авторизация");
            out.println("Введите логин");
            String in = this.in.readLine();
            Optional<User> user = userDao.findByName(in);
            if(user.isPresent()){
                out.println("Введите пароль");
                in = this.in.readLine();
                if(user.get().getPassword().equals(in)){
                    this.user = user.get();
                    clients.add(this);
                }
                else{
                    login(out);
                }

            }
            else{
                out.println("Такого пользователя нет, зарегистрируйтесь");
                registration(out);
            }
        }

        private void registration(PrintWriter out) throws IOException {
            out.println("Регистрация");
            out.println("Введите логин");
            String login = this.in.readLine();
            out.println("Введите пароль");
            String password = this.in.readLine();
            userDao.save(new User(login,password));
            login(out);
        }
    }
}
