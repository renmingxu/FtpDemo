package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by renmingxu on 2017/3/1.
 */
public class FtpServerDemo {
    private int port;
    String rootDirectory;
    private String user;
    private String pass;

    private ArrayList<Client> clients;

    private ServerSocket serverSocket;



    public FtpServerDemo(int port, String rootDirectory, String user, String pass) {
        this.port = port;
        this.rootDirectory = rootDirectory;
        this.user = user;
        this.pass = pass;
        this.clients = new ArrayList<>();
    }
    public boolean start() {
        try {
            this.serverSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = this.serverSocket.accept();
                System.out.println(clientSocket.getInetAddress().toString().replace('.',',').substring(1));
                Client client = new Client(clientSocket, this);
                this.clients.add(client);
                client.start();
            }
        } catch (IOException e) {
            return false;
        }
    }
    public boolean login(String user, String pass){
        if (this.user.equals(user) && this.pass.equals(pass)){
            return true;
        }
        return false;
    }

}
