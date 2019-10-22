package ru.itis.sockets.programs.servers;

import com.beust.jcommander.JCommander;
import ru.itis.sockets.servers.ChatMultiServer;
import ru.itis.sockets.service.Args;
import ru.itis.sockets.service.PropertiesLoader;

public class ProgramChatMultiServer {
    public static void main(String[] args) {
        Args args1 = new Args();
        JCommander jCommander = new JCommander(args1);
        jCommander.parse(args);
        String[] properties = PropertiesLoader.getProperties(args1.getPath_prop());
        ChatMultiServer server = new ChatMultiServer(properties);
        server.start(Integer.parseInt(args1.getPort()));
    }
}
