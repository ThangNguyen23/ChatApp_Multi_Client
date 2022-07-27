package app;

import java.awt.*;
import javax.swing.*;
import java.net.Socket;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.StringTokenizer;

public class Client extends JFrame {
    private JFrame frame;
    private JTextArea txtAreaShow;
    private JList jListOnline;
    private JTextField txtMessage;
    private JButton exitBtn;
    private JButton sendBtn;
    private JLabel lblOnline;

    private DataInputStream reader;
    private DataOutputStream writer;
    private DefaultListModel<String> modelList;
    private String userName = "";
    private String receiveUserName = "";

    public Client() {
        init();
    }

    public Client(String userName, Socket socket) {
        init();
        this.userName = userName;
        try {
            frame.setTitle("Client " + userName);

//            store online user
            modelList = new DefaultListModel<String>();
            jListOnline.setModel(modelList);

            reader = new DataInputStream(socket.getInputStream());
            writer = new DataOutputStream(socket.getOutputStream());

//            read message from server
            ResultFromServer result = new ResultFromServer();
            result.start();
        }
        catch (Exception exception) {
            System.err.println(exception.getMessage());
        }
    }

    private void init() {
        frame = new JFrame();
        frame.setTitle("Client");
        frame.setBounds(200, 100, 650, 450);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        txtAreaShow = new JTextArea();
        txtAreaShow.setEditable(false);
        txtAreaShow.setFont(new Font("Times New Roman", Font.PLAIN, 17));
        txtAreaShow.setBounds(10, 30, 400, 300);

        txtMessage = new JTextField();
        txtMessage.setBounds(10, 370, 400, 30);
        txtMessage.setFont(new Font("Times New Roman", Font.PLAIN, 17));
        txtMessage.setHorizontalAlignment(SwingConstants.LEFT);

        lblOnline = new JLabel("Roommates");
        lblOnline.setBounds(420, 10, 90, 20);
        lblOnline.setHorizontalAlignment(SwingConstants.LEFT);
        lblOnline.setFont(new Font("Times New Roman", Font.PLAIN, 17));

        jListOnline = new JList();
        jListOnline.setBounds(420, 30, 200, 300);
        jListOnline.setFont(new Font("Times New Roman", Font.PLAIN, 17));

        sendBtn = new JButton("Send");
        sendBtn.setBounds(420, 370, 90, 30);
        sendBtn.setFont(new Font("Times New Roman", Font.PLAIN, 17));

        exitBtn = new JButton("Exit");
        exitBtn.setBackground(Color.red);
        exitBtn.setBounds(530, 370, 90, 30);
        exitBtn.setFont(new Font("Times New Roman", Font.PLAIN, 17));

        frame.getContentPane().add(txtAreaShow);
        frame.getContentPane().add(txtMessage);
        frame.getContentPane().add(lblOnline);
        frame.getContentPane().add(jListOnline);
        frame.getContentPane().add(sendBtn);
        frame.getContentPane().add(exitBtn);

        sendBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                first online user when accept
                jListOnline.setSelectedIndex(0);
                String message = txtMessage.getText();
                if (message != null && !message.isEmpty()) {
                    try {
                        int flag = 0;
                        String msgSendToServer = "";
                        String identityString = "p2p";
                        List<String> listUsers = jListOnline.getSelectedValuesList();

                        if (listUsers.size() == 0) {
                            flag = 1;
                        }

                        for (String user : listUsers) {
                            if (receiveUserName.isEmpty()) {
                                receiveUserName += user;
                            }
                            else {
                                receiveUserName += "," + user;
                                break;
                            }
                        }

                        msgSendToServer = identityString + ":" + receiveUserName + ":" + message;

                        if (identityString.equals("p2p")) {
//                            check again
                            if (flag == 1) {
                                JOptionPane.showMessageDialog(frame, "You need to choose user");
                            }
                            else {
                                writer.writeUTF(msgSendToServer);
                                txtMessage.setText("");
                                txtAreaShow.append("Send to : " + receiveUserName + " With content : " + message + "\n");
                            }
                        }
                        receiveUserName = "";
                    }
                    catch (Exception exception) {
                        System.err.println(exception.getMessage());
                        JOptionPane.showMessageDialog(frame, "User is disconnected !");
                    }
                }
            }
        });

        exitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    writer.writeUTF("exit");
                    txtAreaShow.append("You are disconnected\n");
                    frame.dispose();
                }
                catch (Exception exception) {
                    System.err.println(exception.getMessage());
                }
            }
        });

        jListOnline.setEnabled(false);
        frame.setVisible(true);
    }

    public class ResultFromServer extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    String result = reader.readUTF();
                    System.out.println("Result from server : " + result);
                    if (result.contains("server:")) {
//                        get list online user from server
                        result = result.substring(7);
                        modelList.clear();

                        StringTokenizer token = new StringTokenizer(result, ",", false);
                        while (token.hasMoreTokens()) {
                            String temp = token.nextToken();
                            if (!userName.equals(temp)) {
//                                auto matching
                                int input = JOptionPane.showConfirmDialog(frame, "Do you want to chat with " + temp + " ?", "Select an Option", JOptionPane.YES_NO_OPTION);
                                if (input == 0) {
                                    modelList.addElement(temp);
                                    break;
                                }
                                else {
                                    modelList.clear();
                                }
                            }
                        }
                    }
                    else {
//                        message from another user
                        txtAreaShow.append("" + result + "\n");
                    }
                }
                catch (Exception exception) {
                    System.err.println(exception.getMessage());
                    break;
                }
            }
        }
    }

//    public static void main(String[] args) throws Exception {
//        Client client = new Client();
//        client.frame.setVisible(true);
//    }
}
