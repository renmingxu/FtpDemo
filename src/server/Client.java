package server;

import tool.ByteTool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by renmingxu on 2017/3/1.
 */
public class Client {
    private Socket clientSocket;
    private InputStream cmdInputStream;
    private OutputStream cmdOutputStream;
    private String charsetName;
    private boolean loginStatus;
    private ClientThread clientThread;
    private FtpServerDemo server;

    public Client(Socket clientSocket,FtpServerDemo server) {
        this.clientSocket = clientSocket;
        this.charsetName = "GBK";
        this.loginStatus = false;
        this.server = server;
    }
    public void start() {
        this.clientThread = new ClientThread(this);
        this.clientThread.start();
    }
    public synchronized boolean sendCommand(String cmd) {
        try {
            cmdOutputStream.write((cmd + "\r\n").getBytes(charsetName));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public synchronized String readReponse() {
        try {
            byte[] tmp = new byte[102400];
            int len = cmdInputStream.read(tmp);
            if (len <= 0) {
                this.close();
                return null;
            }
            return new String(ByteTool.subByte(tmp, 0, len), charsetName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void close(){
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mainLoop() {
        try {
            this.cmdInputStream = this.clientSocket.getInputStream();
            this.cmdOutputStream = this.clientSocket.getOutputStream();
            this.sendCommand("220 Hello!");
            while(true) {
                String tmp = this.readReponse();
                if (tmp == null) {
                    return;
                }
                System.out.println(tmp);
                switch (tmp.split("\r\n")[0].split(" ")[0]) {
                    case "USER":
                        this.login(tmp);
                        break;
                    default:
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public synchronized boolean login(String tmp){
        String user;
        String pass;
        if (tmp.split(" ").length == 2) {
            user = tmp.split(" ")[1].split("\r\n")[0];
            System.out.println("asdf");
            this.sendCommand("331 Please specify the password.");
            String t = this.readReponse();
            if ("PASS".equals(t.split(" ")[0]) && t.split(" ").length == 2) {
                pass = t.split(" ")[1].split("\r\n")[0];
                if (this.server.login(user, pass)) {
                    this.sendCommand("230 Login successful.");
                    return true;
                } else {
                    this.sendCommand("530 Not logged in, user or password incorrect!");
                }
            }
        }
        return false;
    }
}
