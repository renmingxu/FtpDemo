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
            String u = sendCommand("USER " + user);
            System.out.println(u);
            String p = sendCommand("PASS " + pass);
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
            String s = sendCommand("PASV");
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
            String result = readResponse();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
    public synchronized String readResponse() {
        byte[] tmp = new byte[10240];
        int len = 0;
        try {
            tmp = readline();
            String result = new String(tmp, charsetName);
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
                String ss = sendCommand("LIST");
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
        String result =  sendCommand("SIZE " + filename);
        if ("213".equals(result.split(" ")[0])) {
            return Integer.valueOf(result.split(" ")[1]);
        }
        return -1;
    }
    public String pwd() {
        String result = sendCommand("PWD");
        if ("257".equals(result.split(" ")[0])) {
            return result.split("\"")[1];
        }
        return "";
    }
    public String quit() {
        try {
            byte[] tmp;
            tmp = new byte[10240];
            synchronized (cmdSocket) {
                cmdOutputStream.write(("QUIT").getBytes());
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
            String ss = sendCommand("RETR " + filename);
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
    private boolean retrdir(String dirname, String localDirname) {
        String oldPwd = pwd();
        cwd(dirname);
        File dir = new File(localDirname);
        if (dir.exists()) {
            return false;
        }
        if (!localDirname.endsWith(File.separator)) {
            localDirname = localDirname + File.separator;
        }
        if (!dir.mkdirs()) {
            return false;
        }
        String ls = list();
        String[] lslist = ls.split("\r\n");
        for (String l :
                lslist) {
            if ("".equals(l))
                continue;
            String[] llist = l.split("( ){1,}");
            String filename = l.substring(l.indexOf(llist[8]));
            String filetype = llist[0].substring(0,1);
            if ("-".equals(filetype)) {
                    retr(filename, localDirname + "\\" + filename);
                } else if ("d".equals(filetype)) {
                    retrdir(filename,localDirname + "\\" + filename);
                } else {
                    return false;
                }
        }
        cwd(oldPwd);
        return true;
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
            String s = sendCommand("STOR " + filename);
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
    public boolean download(String file, String localFile) {
        String ls = list();
        String[] lslist = ls.split("\r\n");
        boolean result = false;
        for (String l :
                lslist) {
            String[] llist = l.split("( ){1,}");
            String filename = l.substring(l.indexOf(llist[8]));
            String filetype = llist[0].substring(0,1);
            if (filename.equals(file)) {
                result = true;
                if ("-".equals(filetype)) {
                    return retr(file, localFile);
                } else if ("d".equals(filetype)) {
                    return retrdir(file, localFile);
                } else {
                    return false;
                }
            }
        }
        return true;
    }
    public String cwd(String directoryName) {
        String result =  sendCommand("CWD " + directoryName);
        if ("250".equals(result.split(" ")[0])) {
            return result.substring(4);
        }
        return "";
    }
    public boolean mkd(String directoryName) {
        String result =  sendCommand("MKD " + directoryName);
        if ("250".equals(result.split(" ")[0])) {
            return true;
        }
        return false;
    }
    public boolean rename(String oldName, String newName) {
        String tmp = sendCommand("RNFR " + oldName);
        if ("350".equals(tmp.split(" ")[0])) {
            tmp = sendCommand("RNTO " + newName);
            if ("250".equals(tmp.split(" ")[0])) {
                return true;
            }
        }
        return false;

    }
}
