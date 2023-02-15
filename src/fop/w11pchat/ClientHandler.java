package fop.w11pchat;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.util.*;

public class ClientHandler implements Runnable {

    private static final Map<ClientHandler,LocalTime> penguins = new HashMap<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String penguinUsername;
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.penguinUsername = bufferedReader.readLine();
            penguins.put(this,LocalTime.now());

            broadCastMessage(penguinUsername + " has entered the chat" + ThreadColor.ANSI_BLUE);
        } catch (IOException e) {
            closeEveryThing(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {

        String messageFromClient;
        while (socket.isConnected())
            try {

                messageFromClient = bufferedReader.readLine();

                if(messageFromClient == null || messageFromClient.toLowerCase().contains("logout")) { // works
                    throw new IOException();
                }
                else if (messageFromClient.toLowerCase().contains("whois")) // works
                    whois();
                else if (messageFromClient.toLowerCase().contains("pingu")) // works
                    pingu();
                else if (messageFromClient.toLowerCase().contains("@")) {   // works
                    String[] textInfo = messageFromClient.split(" ",3);
                    dmingPingu(
                            textInfo[1].replace("@",""),
                            textInfo[2]
                    );
                } else
                    broadCastMessage(messageFromClient); // works

            } catch (IOException e) {
                closeEveryThing(socket, bufferedReader, bufferedWriter);
                return;
            }
    }
    private void dmingPingu(String penguinUsername, String message) throws IOException {
        for(ClientHandler penguin : penguins.keySet())
            if(penguin.penguinUsername.equals(penguinUsername)) {
                try {
                    penguin.bufferedWriter.write(LocalTime.now() + " " + message + ThreadColor.ANSI_PURPLE);
                    penguin.bufferedWriter.newLine();
                    penguin.bufferedWriter.flush();
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        this.bufferedWriter.write(LocalTime.now() +  " " + "Penguin with username '" + penguinUsername + "' wasn't found\n" +
                "\tuse command 'whois' to see who's active" + ThreadColor.ANSI_BLUE);
        this.bufferedWriter.newLine();
        this.bufferedWriter.flush();
    }
    private void whois() throws IOException {
        this.bufferedWriter.write("Active clients at " + LocalTime.now() + "\n" + ThreadColor.ANSI_BLUE);
        penguins.forEach((penguin, joinedTime) -> {
            try {
                if (penguin.penguinUsername.equals(this.penguinUsername))
                    this.bufferedWriter.write(this.penguinUsername + " (ME) " + joinedTime + ThreadColor.ANSI_BLUE);
                else
                    this.bufferedWriter.write(penguin.penguinUsername + " at -> " + joinedTime + ThreadColor.ANSI_BLUE);

                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();

            } catch (IOException e) {
                closeEveryThing(socket, bufferedReader, bufferedWriter);
            }
        });
    }
    private void pingu() {
        Random random = new Random();
        switch (random.nextInt(5)) {
            case 1 -> printing("Gentoo Penguins are the fastest of all penguin species! These penguins can swim at speeds of up to 36km/h!");
            case 2 -> printing("The oldest penguin fossils are 62 million years old.");
            case 3 -> printing("Penguins poop every 20 minutes.");
            case 4 -> printing("A penguins black and white colouring is called counter-shading");
            default -> printing("Penguins are used");
        }
    }
    private void printing(String fact) {
        penguins.forEach((penguin, joinedTime) -> {
            try {
                penguin.bufferedWriter.write(LocalTime.now() + " | " + fact + " | " +ThreadColor.ANSI_BLUE);
                penguin.bufferedWriter.newLine();
                penguin.bufferedWriter.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void broadCastMessage(String messageToSend) {

        penguins.forEach((penguin, joinedTime) -> {
            try {
                if (!penguin.penguinUsername.equals(this.penguinUsername)) {

                    penguin.bufferedWriter.write(LocalTime.now() + " | " + messageToSend + " | " + ThreadColor.ANSI_BLUE);
                    penguin.bufferedWriter.newLine();
                    penguin.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEveryThing(socket, bufferedReader, bufferedWriter);
            }
        });
    }
    private void removePenguin() {
        penguins.remove(this);
        broadCastMessage(penguinUsername + " has left the chat" + ThreadColor.ANSI_BLUE);
    }
    private void closeEveryThing(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removePenguin();
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
