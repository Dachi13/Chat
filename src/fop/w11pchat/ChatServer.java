package fop.w11pchat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ChatServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(3000);
        ChatServer chatServer = new ChatServer(serverSocket);
        chatServer.startServer();
    }
    private final ServerSocket serverSocket;

    private ChatServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
    private void startServer() {
        try {
            while(!serverSocket.isClosed()) {

                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected" + ThreadColor.ANSI_BLUE);
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }
    private void closeServerSocket() {
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
    
