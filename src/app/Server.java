package app;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Server {
    private static int port = 7000;

    private JFrame frame;
    private JList listAllUser;
    private JList listOnlineUser;
    private JTextArea txtAreaShow;
    private JLabel lblAllUser;
    private JLabel lblOnlineUser;

    private ServerSocket serverSocket;
//    store difference user
    private static HashSet<String> setOnlineUser = new HashSet<String>();
    private static HashMap<String, Socket> mapAllUser = new HashMap<String, Socket>();
    private DefaultListModel<String> modelAllUser = new DefaultListModel<String>();
    private DefaultListModel<String> modelOnlineUser = new DefaultListModel<String>();

    public Server() {
        frame = new JFrame();
        frame.setTitle("Server");
        frame.setBounds(200, 100, 650, 450);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        txtAreaShow = new JTextArea();
        txtAreaShow.setEditable(false);
        txtAreaShow.setFont(new Font("Times New Roman", Font.PLAIN, 17));
        txtAreaShow.setBounds(10, 30, 400, 330);

        listOnlineUser = new JList();
        listOnlineUser.setEnabled(false);
        listOnlineUser.setBounds(420, 30, 200, 150);
        listOnlineUser.setFont(new Font("Times New Roman", Font.PLAIN, 17));

        listAllUser = new JList();
        listAllUser.setEnabled(false);
        listAllUser.setBounds(420, 210, 200, 150);
        listAllUser.setFont(new Font("Times New Roman", Font.PLAIN, 17));

        lblOnlineUser = new JLabel("Online Users");
        lblOnlineUser.setBounds(420, 0, 90, 30);
        lblOnlineUser.setHorizontalAlignment(SwingConstants.LEFT);
        lblOnlineUser.setFont(new Font("Times New Roman", Font.PLAIN, 17));

        lblAllUser = new JLabel("All Users");
        lblAllUser.setBounds(420, 180, 90, 30);
        lblAllUser.setHorizontalAlignment(SwingConstants.LEFT);
        lblAllUser.setFont(new Font("Times New Roman", Font.PLAIN, 17));

        frame.getContentPane().add(txtAreaShow);
        frame.getContentPane().add(listAllUser);
        frame.getContentPane().add(listOnlineUser);
        frame.getContentPane().add(lblAllUser);
        frame.getContentPane().add(lblOnlineUser);

        try {
            serverSocket = new ServerSocket(port);
            txtAreaShow.append("Server waiting at port : " + port + "\n");

            ReceiveFromClient receiveFromClient = new ReceiveFromClient();
            receiveFromClient.start();
        }
        catch (Exception exception) {
            System.err.println(exception.getMessage());
        }
    }

    public class ReceiveFromClient extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
//                    receive username from login
                    String userName = new DataInputStream(socket.getInputStream()).readUTF();
                    DataOutputStream writer = new DataOutputStream(socket.getOutputStream());

//                    check if username is used
                    if (setOnlineUser != null && setOnlineUser.contains(userName)) {
                        writer.writeUTF("Username is used");
                    }
                    else {
                        mapAllUser.put(userName, socket);
                        setOnlineUser.add(userName);
                        writer.writeUTF("");

                        modelOnlineUser.addElement(userName);

                        if (!modelAllUser.contains(userName)) {
                            modelAllUser.addElement(userName);
                        }

                        listAllUser.setModel(modelAllUser);
                        listOnlineUser.setModel(modelOnlineUser);

                        txtAreaShow.append("Client " + userName + " is connected\n");

                        ReadMessageFromClient readMessageFromClient = new ReadMessageFromClient(socket, userName);
                        readMessageFromClient.start();

                        UpdateListOnlineUser updateListOnlineUser = new UpdateListOnlineUser();
                        updateListOnlineUser.start();
                    }
                }
                catch (Exception exception) {
                    System.err.println(exception.getMessage());
                }
            }
        }
    }

    public class ReadMessageFromClient extends Thread {
        Socket socket;
        String userName;
        private ReadMessageFromClient(Socket socket, String userName) {
            this.socket = socket;
            this.userName = userName;
        }

        @Override
        public void run() {
            while (listAllUser != null && !mapAllUser.isEmpty()) {
                try {
                    String message = new DataInputStream(socket.getInputStream()).readUTF();
                    System.out.println("Receive from client : " + message);

                    String[] elementOfMessage = message.split(":");
                    if (elementOfMessage[0].equals("p2p")) {
                        String[] receiveUserName = elementOfMessage[1].split(",");
                        for (String userName : receiveUserName) {
                            try {
                                if (setOnlineUser.contains(userName)) {
                                    DataOutputStream writer = new DataOutputStream(((Socket) mapAllUser.get(userName)).getOutputStream());
                                    writer.writeUTF("Receive from : " + this.userName + " With content : " + elementOfMessage[2]);
                                }
                            }
                            catch (Exception exception) {
                                System.err.println(exception.getMessage());
                            }
                        }
                    }
                    else if (elementOfMessage[0].equals("exit")) {
                        setOnlineUser.remove(userName);
                        txtAreaShow.append(userName + " is disconnected\n");

//                        only show message for another user
                        Iterator<String> iterator = setOnlineUser.iterator();
                        while (iterator.hasNext()) {
                            String temp = (String) iterator.next();
                            if (!temp.equals(userName)) {
                                try {
                                    DataOutputStream writer = new DataOutputStream(((Socket) mapAllUser.get(temp)).getOutputStream());
                                    writer.writeUTF(userName + " is disconnected");
                                }
                                catch (Exception exception) {
                                    System.err.println(exception.getMessage());
                                }
                            }
                        }
                        UpdateListOnlineUser updateListOnlineUsers = new UpdateListOnlineUser();
                        updateListOnlineUsers.start();

                        modelOnlineUser.removeElement(userName);
                        listOnlineUser.setModel(modelOnlineUser);
                    }
                }
                catch (Exception exception) {
//                    System.err.println(exception.getMessage());
                }
            }
        }
    }

    public class UpdateListOnlineUser extends Thread {
        @Override
        public void run() {
            try {
                String userName = "";
                Iterator iterator = setOnlineUser.iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    userName += key + ",";
                }
                if (userName.length() != 0) {
                    userName = userName.substring(0, userName.length() - 1);
                }

                iterator = setOnlineUser.iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    try {
                        DataOutputStream writer = new DataOutputStream(((Socket) mapAllUser.get(key)).getOutputStream());
                        writer.writeUTF("server:" + userName);
                    }
                    catch (Exception exception) {
                        System.err.println(exception.getMessage());
                    }
                }
            }
            catch (Exception exception) {
                System.err.println(exception.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.frame.setVisible(true);
    }
}
