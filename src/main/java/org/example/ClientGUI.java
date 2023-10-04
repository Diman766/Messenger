package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class ClientGUI extends JFrame implements TCPConnectionListener {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    private final JTextArea log = new JTextArea();

    private final JTextField tfLogin = new JFormattedTextField("ivan_igorevich");

    private final JTextField tfMessage = new JTextField();

    private TCPConnection connection;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
    }

    ClientGUI() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        setTitle("Chat Client");

        JPanel panelTop = new JPanel(new GridLayout(2, 3));
        JTextField tfIPAddress = new JFormattedTextField("127.0.0.1");
        panelTop.add(tfIPAddress);
        JTextField tfPort = new JFormattedTextField("8189");
        panelTop.add(tfPort);
        panelTop.add(tfLogin);
        JTextField tfPassword = new JFormattedTextField("123456");
        panelTop.add(tfPassword);
        JButton btnLogin = new JButton("Login");
        panelTop.add(btnLogin);
        add(panelTop, BorderLayout.NORTH);

        JPanel panelBottom = new JPanel(new BorderLayout());
        panelBottom.add(tfMessage, BorderLayout.CENTER);
        JButton btnSend = new JButton("Send");
        panelBottom.add(btnSend, BorderLayout.EAST);
        add(panelBottom, BorderLayout.SOUTH);

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = tfMessage.getText();
                if (!msg.isEmpty()) {
                    tfMessage.setText("");
                    connection.sendString(tfLogin.getText() + ": " + msg);
                }
            }
        });


        log.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog);

        setVisible(true);

        try {
            connection = new TCPConnection(this, tfIPAddress.getText(), 8189);
        } catch (IOException e) {
            printMsg("Connection exception: " + e);
        }
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready !");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMsg(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close !");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: " + e);
    }

    private synchronized void printMsg(String msg) {
        log.append(msg + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }
}
