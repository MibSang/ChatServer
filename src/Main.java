import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(8081);
        //server.accept();
        Client client = new Client("localhost", 8081);
    }
}

class Server extends Thread{
    int port;
    ServerSocket serverSocket;
    ArrayList<Socket> sockets = new ArrayList<>();
    class ServerThread {
        Socket socket;
        InetSocketAddress isa;
        ServerThread(Socket socket) {
            this.socket = socket;
            this.isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        }

        void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = br.readLine();

                if (message.equals("exit")) {
                    sockets.remove(socket);
                    socket.close();
                    return;
                }

                String toSend = isa+": "+message+"\n";
                System.out.println(toSend);
                for (Socket s: sockets) {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                    bw.write(toSend);
                    bw.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    Server(int port) {
        this.port = port;
    }

    void accept() {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                System.out.println("Waiting for connection...");
                Socket socket = serverSocket.accept();
                InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
                sockets.add(socket);
                System.out.println("\tConnection established: " + isa);
                ServerThread serverThread = new ServerThread(socket);
                serverThread.run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Client {
    int port;
    Socket socket;
    Client(String connect_to, int port) {
        this.port = port;
        try {
            socket = new Socket(connect_to, port);
            System.out.println("Connected to " + socket.getRemoteSocketAddress());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bw.write("hello\n");
            bw.flush();
            System.out.println("sent hello");
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
