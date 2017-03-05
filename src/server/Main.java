package server;

/**
 * Created by renmingxu on 2017/3/1.
 */
public class Main {

    public static void main(String[] args) {
        FtpServerDemo ftpServer = new FtpServerDemo(21,"D:\\ftp\\" ,"ren","ren");
        ftpServer.start();
    }
}
