package client;

import java.util.Scanner;

/**
 * Created by renmingxu on 2017/2/27.
 */
public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int loginTimes = 0;
        FtpClientDemo ftpClient;
        while (true) {
            System.out.print("Server Address:");
            String serverAddress = input.next();
            System.out.print("Server Port:");
            int serverPort = input.nextInt();
            System.out.print("User:");
            String user = input.next();
            System.out.print("Pass:");
            String pass = input.next();
            ftpClient = new FtpClientDemo(serverAddress, serverPort, user, pass);
            if (ftpClient.login()) {
                System.out.println("Login success");
                break;
            } else {
                System.out.println("Login fail");
                loginTimes ++;
                if (loginTimes >= 3) {
                    System.out.println("Exit!");
                }
            }
        }
        while (true) {
            System.out.println("\n\n1.list \n" +
                    "2.size\n" +
                    "3.retr\n" +
                    "4.stor\n" +
                    "5.cwd\n" +
                    "exit"
            );
            String cmd = input.next();
            System.out.println(cmd);
            switch (cmd) {
                case "exit":
                    System.out.println(ftpClient.quit());
                    System.exit(0);
                    break;
                case "list":
                    System.out.println(ftpClient.list());
                    break;
                case "pwd":
                    System.out.println(ftpClient.pwd());
                    break;
                case "download":
                    System.out.print("Filename:");
                    String filenamedownload = input.next();
                    System.out.print("Local Filename:");
                    String localFilenamedownload = input.next();
                    System.out.println(ftpClient.download(filenamedownload, localFilenamedownload));
                    break;
                case "retr":
                    System.out.print("Filename:");
                    String filenameRetr = input.next();
                    System.out.print("Local Filename:");
                    String localFilenameRetr = input.next();
                    System.out.println(ftpClient.retr(filenameRetr, localFilenameRetr));
                    break;
                case "stor":
                    System.out.print("Filename:");
                    String filenameStor = input.next();
                    System.out.print("Local Filename:");
                    String localFilenameStor = input.next();
                    System.out.println(ftpClient.stor(filenameStor, localFilenameStor));
                    break;
                case "rename":
                    System.out.print("Old Filename:");
                    String filenameRename = input.next();
                    System.out.print("New Filename:");
                    String newFileRename = input.next();
                    System.out.println(ftpClient.rename(filenameRename, newFileRename));
                    break;
                case "size":
                    String filenameSize = input.next();
                    System.out.println(ftpClient.size(filenameSize));
                    break;
                case "mkd":
                    String filenameMkd = input.next();
                    System.out.println(ftpClient.mkd(filenameMkd));
                    break;
                case "cwd":
                    String directoryName = input.next();
                    System.out.println(ftpClient.cwd(directoryName));
                default:
            }
        }
    }
}
