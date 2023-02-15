package fop.w11pchat;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

public class ChatClient {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the username for the group chat" + ThreadColor.ANSI_BLUE);
        String username = scanner.nextLine();

        printInfo();

        Socket socket = new Socket("localhost",3000);

        ChatClient chatClient = new ChatClient(socket,username);
        chatClient.receivingMessage();
        chatClient.message();
    }
    private static void printInfo() {
        System.out.println(
                """
                        You can just start typing to have conversation or you can use following commands:
                        \t'@username<blank>message' write without quotes to DM someone.
                        \t'WHOIS' to see chat members.
                        \t'LOGOUT' to leave the chat.
                        \t'PINGU' to notify everyone with an interesting fact about penguins""" + ThreadColor.ANSI_BLUE
        );
    }

    private ChatClient(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            close(socket,bufferedWriter, bufferedReader);
        }
    }
    private void message() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while(socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(username + ": " + messageToSend + ThreadColor.ANSI_BLUE);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

        } catch (IOException e) {
            close(socket,bufferedWriter, bufferedReader);
        }
    }
    private void receivingMessage() {
        new Thread(() -> {
           String msgFromGroupChat;
           while (socket.isConnected())
               try {
                   msgFromGroupChat = bufferedReader.readLine();
                   if(msgFromGroupChat == null || msgFromGroupChat.equals("logout"))
                       System.exit(0);
                   System.out.println(msgFromGroupChat);
               } catch (IOException e) {
                   close(socket,bufferedWriter, bufferedReader);
                   return;
               }
        }).start();
    }
    private void close(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        try {
            if(bufferedReader != null)
                bufferedReader.close();
            if(bufferedWriter != null)
                bufferedWriter.close();
            if(socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
