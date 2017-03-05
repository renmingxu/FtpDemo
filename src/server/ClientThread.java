package server;

/**
 * Created by renmingxu on 2017/3/1.
 */
public class ClientThread extends Thread {
    private Client client;

    public ClientThread(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        this.client.mainLoop();
        this.client.close();
    }
}
