package client;

import jdk.internal.util.xml.impl.Input;
import tool.ByteTool;

import java.io.*;
import java.net.InterfaceAddress;
import java.net.Socket;

/**
 * Created by renmingxu on 2017/2/27.
 */
public class FtpClientDemo {
    private String host;
    private int port;
    private String user;
    private String pass;
    private String charsetName;
    private Socket cmdSocket;
    private InputStream cmdInputStream;
    private OutputStream cmdOutputStream;

    public FtpClientDemo(String host, int port, String user, String pass) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.charsetName = "GBK";
    }
    public boolean login() {
        try {
            byte[] tmp = new byte[10240];
            cmdSocket = new Socket(host, port);
            cmdInputStream = cmdSocket.getInputStream();
            cmdOutputStream = cmdSocket.getOutputStream();
            System.out.println(readResponse());
            String u = sendCommand("USER " + user + "\r\n");
            System.out.println(u);
            String p = sendCommand("PASS " + pass + "\r\n");
            System.out.println(p);
            if ("230".equals(p.split(" ")[0])) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }
    public Socket pasv() {
        try {
            byte[] tmp = new byte[10240];
            String s = sendCommand("PASV\r\n");
            String[] slist = s.split(" ");
            if ("227".equals(slist[0])) {
                slist = s.split("\\(")[1].split("\\)")[0].split(",");
                String dataSocketAddress = slist[0] + "." + slist[1] + "." + slist[2] + "." + slist[3];
                int dataSocketPort = Integer.valueOf(slist[4]) * 256 + Integer.valueOf(slist[5]);
                Socket dataSocket = new Socket(dataSocketAddress, dataSocketPort);
                return dataSocket;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public synchronized String sendCommand(String cmd) {
        byte[] tmp = new byte[10240];
        int len = 0;
        try {
            cmdOutputStream.write((cmd + "\r\n").getBytes(charsetName));
            len = cmdInputStream.read(tmp);
            String result = new String(ByteTool.subByte(tmp, 0, len), charsetName);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public synchronized String readResponse() {
        byte[] tmp = new byte[10240];
        int len = 0;
        try {
            len = cmdInputStream.read(tmp);
            String result = new String(ByteTool.subByte(tmp, 0, len), charsetName);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String list() {
            try {
                byte[] tmp;
                Socket dataSocket = pasv();
                if (dataSocket == null){
                    System.out.println("Error");
                    return "";
                }
                InputStream dataInputStream = dataSocket.getInputStream();
                OutputStream dataOutputStream = dataSocket.getOutputStream();
                tmp = new byte[10240];
                String ss = sendCommand("LIST\r\n");
                String result = "";
                while (true) {
                    tmp = new byte[102400];
                    int len = dataInputStream.read(tmp);
                    if (len <= 0) {
                        break;
                    }
                    result += new String(ByteTool.subByte(tmp, 0, len), "GBK");
                }
                dataSocket.close();
                String sss = readResponse();
                if ("226".equals(sss.split(" ")[0])) {
                    return result;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
    }
    public int size(String filename) {
        String result =  sendCommand("SIZE " + filename + "\r\n");
        if ("213".equals(result.split(" ")[0])) {
            return Integer.valueOf(result.split(" ")[1].split("\r\n")[0]);
        }
        return -1;
    }
    public String quit() {
        try {
            byte[] tmp;
            tmp = new byte[10240];
            synchronized (cmdSocket) {
                cmdOutputStream.write(("QUIT\r\n").getBytes());
                cmdInputStream.read(tmp);
            }
            String result =  new String(tmp, "GBK");
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    public boolean retr(String filename, String localFilename) {
        try {
            byte[] tmp;
            Socket dataSocket = pasv();
            if (dataSocket == null){
                return false;
            }
            InputStream dataInputStream = dataSocket.getInputStream();
            OutputStream dataOutputStream = dataSocket.getOutputStream();
            String ss = sendCommand("RETR " + filename + "\r\n");
            if (! "150".equals(ss.split(" ")[0])) {
                return false;
            }
            File localFile = new File(localFilename);
            FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
            while (true) {
                tmp = new byte[102400];
                int len = dataInputStream.read(tmp);
                if (len <= 0) {
                    break;
                }
                localFileOutputStream.write(ByteTool.subByte(tmp, 0, len));
            }
            dataSocket.close();
            localFileOutputStream.close();
            String sss = readResponse();
            if ("226".equals(sss.split(" ")[0])) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean stor(String filename, String localFilename) {
        try {
            byte[] tmp;
            Socket dataSocket = pasv();
            if (dataSocket == null){
                System.out.println("Error");
                return false;
            }
            InputStream dataInputStream = dataSocket.getInputStream();
            OutputStream dataOutputStream = dataSocket.getOutputStream();
            String s = sendCommand("STOR " + filename + "\r\n");
            File localFile = new File(localFilename);
            FileInputStream localFileInputStream = new FileInputStream(localFile);
            while (true) {
                tmp = new byte[102400];
                int len = localFileInputStream.read(tmp);
                if (len <= 0) {
                    break;
                }
                dataOutputStream.write(ByteTool.subByte(tmp, 0, len));
            }
            dataSocket.close();
            String ss = readResponse();
            if ("226".equals(ss.split(" ")[0])) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public String cwd(String directoryName) {
        String result =  sendCommand("CWD " + directoryName + "\r\n");
        if ("250".equals(result.split(" ")[0])) {
            return result.substring(4);
        }
        return "";
    }
}
