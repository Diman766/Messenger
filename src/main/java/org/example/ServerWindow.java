package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class ServerWindow extends JFrame implements TCPConnectionListener {
    private static final int POS_X = 500;
    private static final int POS_Y = 550;
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    private final JTextArea log = new JTextArea();
    private boolean isServerWorking;

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    protected ServerWindow() {

        JButton btnStop = new JButton("Stop");
        btnStop.addActionListener(e -> {
            if (!isServerWorking) {
                log.append("Server was stopped !\n");
            } else {
                isServerWorking = false;
                log.append("Server stopped !\n");
            }
        });

        JButton btnStart = new JButton("Start");
        btnStart.addActionListener(e -> {
            if (isServerWorking) {
                log.append("Server is already running !\n");
            } else {
                isServerWorking = true;
                log.append("Server started !\n");
//                serverRun();

            }
        });


        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setResizable(false);
        setTitle("Chat server");
        setAlwaysOnTop(true);
        log.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog);

        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(btnStart);
        panel.add(btnStop);
        add(panel, BorderLayout.SOUTH);

        setVisible(true);

//        serverRun();

        String filePath = "DataBase";
        try (BufferedReader reader =
                     new BufferedReader(new FileReader(filePath))) {
            reader.lines().forEach(s -> log.append(s + "\n"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void serverRun() {

        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                log.append("Server started !\n");
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    log.append("TCPConnection exception: " + e + "\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        log.append("Client connected: " + tcpConnection + "\n");
        sendToAllConnections("Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        log.append(value + "\n");
        sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        log.append("Client disconnected: " + tcpConnection + "\n");
        sendToAllConnections("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        log.append("TCPConnection exception: " + e + "\n");
    }

    private void sendToAllConnections(String value) {
        Path path = Paths.get("DataBase");
        String text = log.getText();
        try {
            Files.write(path,
                    text.getBytes(),
                    StandardOpenOption.CREATE);
        } catch (IOException o) {
            System.out.println("Не удалось записать в файл !");
        }
        final int cnt = connections.size();
        for (int i = 0; i < cnt; i++) {
            connections.get(i).sendString(value);
        }
    }


}


