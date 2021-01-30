import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(7773)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new Connectionhandler(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
