package server;

import jdk.nashorn.internal.runtime.ECMAException;
import tool.ByteTool;
import tool.DateFormatTool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Created by renmingxu on 2017/3/1.
 */
public class Client {
    private Socket clientSocket;
    private InputStream cmdInputStream;
    private OutputStream cmdOutputStream;
    private Socket pasvSocket;
    private InputStream pasvInputStream;
    private OutputStream pasvOutputStream;
    private boolean pasved;
    private String charsetName;
    private boolean loginStatus;
    private ClientThread clientThread;
    private FtpServerDemo server;
    private String pwd;
    private String rootDirectory;
    private String user;

    public Client(Socket clientSocket,FtpServerDemo server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.charsetName = "UTF8";
        this.loginStatus = false;
        this.rootDirectory = server.rootDirectory;
        this.pwd = "/";
        this.pasved = false;
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

    private synchronized byte[] readline() {
        byte[] b = new byte[1024000];
        try {
            int i = 0;
            byte tmp;
            for (;; i++) {
                tmp = (byte) cmdInputStream.read();
                if (tmp == -1) {
                    return null;
                }
                b[i] = tmp;
                if (i >= 1) {
                    if (b[i - 1] == 13 && b[i] == 10){
                        break;
                    }
                }
            }
            if (i >= 1) {
                return ByteTool.subByte(b, 0, i - 1);
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    public synchronized String readReponse() {
        try {
            byte[] tmp = readline();
            if (tmp == null) {
                this.close();
                return null;
            }
            return new String(tmp, charsetName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private synchronized boolean pasv() {
        int a = 200,b = 0;
        ServerSocket pasvServerSocket = null;
        while(true) {
            try {
                pasvServerSocket = new ServerSocket(a * 256 + b);
                System.out.println(a * 256 + b);
                break;
            } catch (IOException e) {
                if (b < 255) {
                    b++;
                } else {
                    b = 0;
                    a ++;
                }
                if (a * 256 + b > 65535) {
                    return false;
                }
            }
        }

        sendCommand("227 Entering Passive Mode (" +
                clientSocket.getLocalAddress().toString().replace('.',',').substring(1)
                + "," + String.valueOf(a) +"," + String.valueOf(b) +").");
        try {
            pasvSocket = pasvServerSocket.accept();
            pasvServerSocket.close();
            pasvInputStream = pasvSocket.getInputStream();
            pasvOutputStream = pasvSocket.getOutputStream();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void close(){
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean size(String tmp) {
        String filename = this.rootDirectory + this.pwd + tmp.split(" ")[1];
        File file = new File(filename);
        if (file.exists()) {
            if (!file.isDirectory()) {
                sendCommand("213 " + file.length());
                return true;
            }
        }
        sendCommand("550 Could get file size.");
        return false;
    }

    public boolean retr(String tmp) {
        if (!this.pasved) {
            sendCommand("425 Use PORT or PASV first.");
            return false;
        }
        String dir = this.rootDirectory + this.pwd;
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        String filename =  dir + tmp.substring(tmp.indexOf(tmp.split(" ")[1]));
        System.out.println(filename);
        File file = new File(filename);
        if (file.exists()) {
            if (!file.isDirectory()) {
                try {
                    sendCommand("150 Opening BINARY mode data connection for" + filename + "(" + file.length() + "bytes).");
                    FileInputStream fileInputStream = new FileInputStream(file);
                    while (true) {
                        byte[] b = new byte[102400];
                        int len = fileInputStream.read(b);
                        if (len <=0 ){
                            break;
                        }
                        b = ByteTool.subByte(b, 0, len);
                        this.pasvOutputStream.write(b);
                    }
                    fileInputStream.close();
                    this.pasvSocket.close();
                    sendCommand("226 Transfer complete.");
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        sendCommand("550 fail to open file.");
        return false;
    }

    public boolean stor(String tmp) {
        if (!this.pasved) {
            sendCommand("425 Use PORT or PASV first.");
            return false;
        }
        String dir = this.rootDirectory + this.pwd;
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        String filename =  dir + tmp.substring(tmp.indexOf(tmp.split(" ")[1]));
        System.out.println(filename);
        File file = new File(filename);
        try {
            sendCommand("150 Opening BINARY mode data connection for" + filename + "(" + file.length() + "bytes).");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            while (true) {
                byte[] b = new byte[102400];
                int len = this.pasvInputStream.read(b);
                if (len <=0 ){
                    break;
                }
                b = ByteTool.subByte(b, 0, len);
                fileOutputStream.write(b);
            }
            fileOutputStream.close();
            this.pasvSocket.close();
            sendCommand("426 Socket closed.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendCommand("550 fail to open file.");
        return false;
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
                if (loginStatus) {
                    switch (tmp.split(" ")[0]) {
                        case "PASV":
                            if (!pasv())
                                return;
                            this.pasved = true;
                            break;
                        case "LIST":
                            list();
                            break;
                        case "PWD":
                            sendCommand("257 \"" + pwd + "\" is the current directory");
                            break;
                        case "CWD":
                            cwd(tmp);
                            break;
                        case "SIZE":
                            size(tmp);
                            break;
                        case "RETR":
                            retr(tmp);
                            this.pasved = false;
                            break;
                        case "STOR":
                            stor(tmp);
                            this.pasved = false;
                            break;
                        case "RNFR":
                            rnfr(tmp);
                            break;
                        case "MKD":
                            mkd(tmp);
                            break;
                        case "DELE":
                            dele(tmp);
                            break;
                        case "RMD":
                            rmd(tmp);
                            break;
                        case "TYPE":
                            sendCommand("200 set to mode.");
                            break;
                        case "noop":
                            sendCommand("200 OK.");
                            break;
                        case "NOOP":
                            sendCommand("200 OK.");
                            break;
                        case "opts":
                            opts(tmp);
                            break;
                        case "QUIT":
                            return;
                        default:
                            sendCommand("502 command not implement");
                    }
                } else if ("USER".equals(tmp.split(" ")[0])) {
                    loginStatus = login(tmp);
                } else {
                    sendCommand("502 command not implement");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean rnfr(String tmp) {
        String dir = this.rootDirectory + this.pwd;
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        String oldFilename =  dir + tmp.substring(tmp.indexOf(tmp.split(" ")[1]));
        System.out.println(oldFilename);
        File oldFile = new File(oldFilename);
        if (oldFile.exists()){
            sendCommand("350 Ready for RNTO.");
            String t = readReponse();
            System.out.println(t);
            if ("RNTO".equals(t.split(" ")[0])) {
                String newFilename = dir + t.substring(t.indexOf(t.split(" ")[1]));
                System.out.println(newFilename);
                File newFile = new File(newFilename);
                if (oldFile.renameTo(newFile)){
                    System.out.println("Success");
                    sendCommand("250 Rename successful.");
                    return true;
                }
                System.out.println("Fail");
            }
        }
        sendCommand("530 Rename failed.");
        return false;
    }

    public boolean mkd(String tmp) {
        String dir = this.rootDirectory + this.pwd;
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        String filename =  dir + tmp.substring(tmp.indexOf(tmp.split(" ")[1]));
        File file = new File(filename);
        if (!file.exists()) {
            if (file.mkdir()) {
                sendCommand("257 created");
                return true;
            }
        }
        sendCommand("550 Create directory operation failed.");
        return false;
    }

    public boolean rmd(String tmp) {
        String dir = this.rootDirectory + this.pwd;
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        String filename =  dir + tmp.substring(tmp.indexOf(tmp.split(" ")[1]));
        File file = new File(filename);
        if (file.exists() && file.isDirectory()) {
            if (file.delete()) {
                sendCommand("250 Remove directory successful.");
                return true;
            }
        }
        sendCommand("550 Remove directory failed.");
        return false;
    }

    public boolean dele(String tmp) {
        String dir = this.rootDirectory + this.pwd;
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        String filename =  dir + tmp.substring(tmp.indexOf(tmp.split(" ")[1]));
        File file = new File(filename);
        if (file.exists() && !file.isDirectory()) {
            if (file.delete()) {
                sendCommand("250 Delete operation successful.");
                return true;
            }else {
                System.out.println("Error");
            }
        }
        sendCommand("550 Delete operation failed.");
        return false;
    }

    public boolean list() {
        if (!this.pasved) {
            sendCommand("425 Use PORT or PASV first.");
            return false;
        }
        sendCommand("150 Here comes the directory listing.");
        File file = new File(this.rootDirectory + this.pwd);
        File [] files = file.listFiles();
        for (File f : files) {
            String filetype = "";
            if (f.isDirectory()) {
                filetype += "d";
            } else {
                filetype += "-";
            }
            filetype += "rwx------";
            String result = filetype + " " +
                    this.user + " " +
                    this.user + " " +
                    f.length() + " " +
                    DateFormatTool.dateString(new Date(file.lastModified())) + " " +
                    f.getName() + "\r\n";
            try {
                pasvOutputStream.write(result.getBytes());
            } catch (IOException e) {
            }
        }
        try {
            pasvSocket.close();
        } catch (IOException e) {
        }
        this.pasved = false;
        sendCommand("226 Directory send OK.");
        return true;
    }

    public boolean opts(String tmp) {
        if ("opts utf8 on".equals(tmp)) {
            sendCommand("200 Always in UTF8 mode.");
            return true;
        } else {
            sendCommand("503 command not implement");
            return true;
        }
    }

    public boolean cwd(String tmp) {
        try {
            String newPwd = tmp.substring(tmp.indexOf(tmp.split(" ")[1]));
            if ("../".equals(newPwd) || "..".equals(newPwd)) {
                if (!"/".equals(pwd)) {
                    this.pwd = this.pwd.split("[^/]*$")[0];
                    if (this.pwd.endsWith("/") && !"/".equals(this.pwd)) {
                        this.pwd = this.pwd.substring(0, this.pwd.length() - 1);
                    }
                }
                sendCommand("250 Directory successfully changed.");
                return true;
            } else if (newPwd.contains("../")) {
                sendCommand("550 Failed to change directory.");
                return false;
            }
            if (newPwd.endsWith("/") && !"/".equals(newPwd)) {
                newPwd = newPwd.split("/$")[0];
            }
            if (!newPwd.startsWith("/")) {
                if ("/".equals(this.pwd)){
                    newPwd = this.pwd + newPwd;
                } else {
                    newPwd = this.pwd + "/" + newPwd;
                }
            }
            File f  = new File(this.rootDirectory + newPwd);
            if (f.exists() && f.isDirectory()) {
                this.pwd = newPwd;
                sendCommand("250 Directory successfully changed.");
            } else {
                sendCommand("550 Failed to change directory.");
                return false;
            }
            if (!pwd.startsWith("/")) {
                pwd = "/" + pwd;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean login(String tmp){
        String user;
        String pass;
        if (tmp.split(" ").length == 2) {
            user = tmp.split(" ")[1];
            System.out.println("asdf");
            this.sendCommand("331 Please specify the password.");
            String t = this.readReponse();
            if ("PASS".equals(t.split(" ")[0]) && t.split(" ").length == 2) {
                pass = t.split(" ")[1];
                System.out.println("|" + user + "|" + pass + "|");
                if (this.server.login(user, pass)) {
                    this.sendCommand("230 Login successful.");
                    this.user = user;
                    return true;
                } else {
                    this.sendCommand("530 Not logged in, user or password incorrect!");
                }
            }
        }
        return false;
    }

}
