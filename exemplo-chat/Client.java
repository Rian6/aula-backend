import java.io.*;
import java.net.*;

public class Client {

    public static void main(String[] args) {
        String serverIP = "172.22.112.1";
        int port = 12345;

        try (
            Socket socket = new Socket(serverIP, port);
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {

            System.out.print("Digite seu nome: ");
            String nome = teclado.readLine();
            out.println(nome);

            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    System.out.println("Conexão encerrada.");
                }
            }).start();

            String msg;
            while ((msg = teclado.readLine()) != null) {
                out.println(msg);
            }

        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}