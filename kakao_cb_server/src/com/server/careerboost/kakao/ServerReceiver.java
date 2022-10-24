package com.server.careerboost.kakao;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class ServerReceiver extends Thread {

    private final Socket socket;
    private String name;
    private OutputStream os;

    public ServerReceiver(Socket socket, int num) {
        this.socket = socket;
        this.name = "client " + num;
        try {
            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean rename(String newName) {
        // 이미 등록되어 있는 지 확인
        if (Server.clients.get(newName) != null) return false;
        // 글자수 20 확인
        if (newName.length() > 20) return false;

        System.out.println(name + " -> " + newName);
        Server.clients.remove(name);
        name = newName;
        Server.clients.put(name, socket);
        return true;
    }

    public void sendMessage(String msg) throws IOException {

        if (msg.length() > 100) {
            byte[] bytes = "메시지가 너무 깁니다. (최대 100자)".getBytes(StandardCharsets.UTF_8);
            os.write(bytes);
            os.flush();
            return;
        }
        msg = "[" + name + "] : " + msg;
        System.out.println(msg);

        Iterator<String> it = Server.clients.keySet().iterator();
        while (it.hasNext()) {
            try {
                String sName = it.next();
                if (sName.equals(name)) continue;
                byte[] bytes;
                OutputStream os = Server.clients.get(sName).getOutputStream();
                bytes = msg.getBytes(StandardCharsets.UTF_8);
                os.write(bytes);
                os.flush();
                //os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            InputStream is = socket.getInputStream();
            byte[] bytes;
            String msg;
            while (is != null) {
                bytes = new byte[100];
                int readByteCount = is.read(bytes);
                msg = new String(bytes, 0, readByteCount, StandardCharsets.UTF_8);
                //System.out.println(msg);

                if (msg.length() > 6 && msg.startsWith("/name")) {
                    String newName = msg.substring(6);
                    if (!rename(newName)) {
                        System.out.println(name + " 이름 변경 실패");
                        bytes = "중복되었거나 20글자를 초과하는 닉네임입니다.".getBytes(StandardCharsets.UTF_8);
                        os.write(bytes);
                        os.flush();
                    }
                } else {
                    sendMessage(msg);
                }
            }
        }catch (SocketException e) {
            System.out.println(name + " 연결이 끊어졌습니다.");
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                sendMessage("퇴장합니다.");
                Server.clients.remove(name);
                System.out.println("현재 client 인원수 : " + Server.clients.size());
                os.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
