import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            Server server = new Server(8081);
            server.accept();
        } else {
            Client client = new Client("3.36.108.241", 8081, args[0]);
        }
    }
}

class SocketIO {
    Socket socket;
    BufferedReader reader;
    BufferedWriter writer;

    SocketIO(Socket socket) throws IOException {
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    public String readLine() throws IOException {
        return reader.readLine();
    }

    public void writeLine(String payload) throws IOException {
        writer.write(payload+"\n");
        writer.flush();
    }
}

class Server extends Thread{
    int port;
    ServerSocket serverSocket;
    ArrayList<SocketIO> sockets = new ArrayList<>();
    ArrayList<String> messages = new ArrayList<>();
    class ServerThread extends Thread {
        SocketIO socket;
        InetSocketAddress isa;

        ServerThread(Socket socket) {
            try {
                this.socket = new SocketIO(socket);
                sockets.add(this.socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        }

        public void run() {
            String message = null;
            for (String s: messages) {
                try {
                    socket.writeLine(s);
                } catch (IOException e) {
                    System.out.println(isa+" disconnected.");
                    messages.add(isa+" disconnected.");
                    sockets.remove(socket);
                    return;
                }
            }
            while (true) {
                try {
                    message = socket.readLine();
                } catch (IOException e) {
                    System.out.println(isa+" disconnected.");
                    messages.add(isa+" disconnected.");
                    sockets.remove(socket);
                    return;
                }
                if (message == null || message.equals("exit") || message.equals("null")) {
                    System.out.println(isa+" disconnected.");
                    messages.add(isa+" disconnected.");
                    sockets.remove(socket);
                    try {
                        socket.socket.close();
                    } catch (IOException ignored) { }
                    return;
                }

                String toSend = message;
                System.out.println("\t" + toSend);
                messages.add(toSend);
                while (messages.size() > 100) {
                    messages.remove(0);
                }
                for (SocketIO s : sockets) {
                    try {
                        s.writeLine(toSend);
                    } catch (IOException e) {
                        System.out.println(isa+" disconnected.");
                        messages.add(isa+" disconnected.");
                        sockets.remove(s);
                        return;
                    }
                }
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
                Socket socket = serverSocket.accept();
                InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
                System.out.println("Connection established: " + isa);
                ServerThread serverThread = new ServerThread(socket);
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Client {
    int port;
    Socket socket;

    BufferedReader br;
    BufferedWriter bw;
    BufferedReader systemRead;
    class ClientReceiver extends Thread {
        public void run() {
            String m;
            while (!socket.isClosed()) {
                try {
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    m = br.readLine();
                    System.out.println(m);
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    Client(String connect_to, int port, String nickname) {
        this.port = port;
        // System.out.println(System.getProperty("file.encoding"));
        try {
            socket = new Socket(connect_to, port);
            System.out.println("Connected to " + socket.getRemoteSocketAddress());
            ClientReceiver clientReceiver = new ClientReceiver();
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            systemRead = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            clientReceiver.start();
            while (!socket.isClosed()) {
                String payload = systemRead.readLine();

                bw.write(nickname+": "+payload+"\n");
                bw.flush();
                if (payload.equals("exit"))
                    break;
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
