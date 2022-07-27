package app;

import javax.swing.*;
import java.net.Socket;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Login extends JFrame{
    private int port = 7000;

    private JFrame frame;
    private JLabel lblUserName;
    private JTextField txtUserName;
    private JButton loginBtn;

    public Login() {
        frame = new JFrame();
        frame.setTitle("Login");
        frame.setBounds(450, 200, 500, 300);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        lblUserName = new JLabel("Username");
        lblUserName.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        lblUserName.setHorizontalAlignment(SwingConstants.RIGHT);
        lblUserName.setBounds(50, 50, 110, 40);
        lblUserName.setFont(new Font("Times New Roman", Font.PLAIN, 17));

        txtUserName = new JTextField();
        txtUserName.setBounds(200, 50, 250, 40);
        txtUserName.setFont(new Font("Times New Roman", Font.PLAIN, 17));

        loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        loginBtn.setBounds(200, 140, 120, 40);
        loginBtn.setFont(new Font("Times New Roman", Font.PLAIN, 17));

        frame.getContentPane().add(lblUserName);
        frame.getContentPane().add(txtUserName);
        frame.getContentPane().add(loginBtn);

        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String userName = txtUserName.getText();
                    Socket socket = new Socket("localhost", port);

                    DataInputStream reader = new DataInputStream(socket.getInputStream());
                    DataOutputStream writer = new DataOutputStream(socket.getOutputStream());

//                    send username to server
                    writer.writeUTF(userName);

//                    receive result from server
                    String result = new DataInputStream(socket.getInputStream()).readUTF();

//                    check input of textfield
                    if (userName.equals("")) {
                        JOptionPane.showMessageDialog(frame,  "Please enter username\n");
                    }
                    else {
                        if(result.equals("Username is used")) {
                            JOptionPane.showMessageDialog(frame,  "Username is used\n");
                        }
                        else {
                            Client client = new Client(userName, socket);
                            frame.dispose();
                        }
                    }
                }
                catch(Exception exception) {
                    System.err.println(exception.getMessage());
                }
            }
        });
    }

    public static void main(String[] args) throws Exception{
        Login login = new Login();
        login.frame.setVisible(true);
    }
}
