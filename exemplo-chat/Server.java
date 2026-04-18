import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Servidor iniciado na porta 12345...");

        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler client = new ClientHandler(socket);
            synchronized (clients) {
                clients.add(client);
            }
            new Thread(client).start();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String nome;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try {
                // primeira mensagem = nome do usuário
                nome = in.readLine();
                broadcast("🔵 " + nome + " entrou no chat");

                String msg;
                while ((msg = in.readLine()) != null) {
                    broadcast(nome + ": " + msg);
                }
            } catch (IOException e) {
                System.out.println("Cliente desconectado: " + nome);
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}

                synchronized (clients) {
                    clients.remove(this);
                }
                broadcast("🔴 " + nome + " saiu do chat");
            }
        }

        private void broadcast(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.out.println(message);
                }
            }
            System.out.println(message);
        }
    }
}