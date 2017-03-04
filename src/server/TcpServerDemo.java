package server;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by renmingxu on 2017/2/27.
 */
public class TcpServerDemo {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(7777, 1024);
        while (true) {
            Socket clientSocket =  serverSocket.accept();
            Thread  t = new Thread() {
                @Override
                public void run() {
                    try {
                        System.out.println("accept client : " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                        InputStream inputStream = clientSocket.getInputStream();
                        OutputStream outputStream = clientSocket.getOutputStream();
                        while(true) {
                            byte[] data = new byte[102400];
                            int len = inputStream.read(data);
                            if (len >= 0) {
                                byte[] dataSend = new byte[len];
                                System.arraycopy(data, 0, dataSend,0, len);
                                System.out.println("Recv from client:(" + len + ") " + new String(dataSend));
                                outputStream.write(dataSend);
                                if ("exit".equals(new String(dataSend))) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        System.out.println("client exit : " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                        clientSocket.close();

                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }
            };
            t.start();
        }
    }
}
