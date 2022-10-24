package com.server.careerboost.kakao;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Server {

    public static Map<String, Socket> clients;
    private static ServerSocket serverSocket;
    private static int counter = 1;

    public static void main(String[] args) {

        clients = Collections.synchronizedMap(new HashMap<>());
        int socketPort = Integer.parseInt(args[0]);
        try {
            serverSocket = new ServerSocket(socketPort);
            System.out.println("Server start");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                // 최대 100명 제한
                if (clients.size() < 100) {
                    clients.put("client " + counter, socket);
                    System.out.println("client " + counter + " 입장했습니다.");
                    ServerReceiver serverReceiver = new ServerReceiver(socket, counter++);
                    serverReceiver.start();
                } else {
                    OutputStream os = socket.getOutputStream();
                    byte[] bytes = "정원이 초과되어 입장할 수 없습니다".getBytes(StandardCharsets.UTF_8);
                    os.write(bytes);
                    os.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
