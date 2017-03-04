package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by renmingxu on 2017/2/27.
 */
public class TcpClientDemo {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 7777);
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        Scanner input = new Scanner(System.in);
        while (true) {
            String s = input.next();
            outputStream.write(s.getBytes());
            byte[] data = new byte[1023000];
            int len = inputStream.read(data);
            System.out.println("Recv from server: (" + len + ")" + new String(data));
            if ("exit".equals(s)) {
                break;
            }
        }
        socket.close();
    }
}
