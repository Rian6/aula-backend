import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpApp {

    public static void main(String[] args) {
        int porta = 8080;

        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("Servidor rodando em http://localhost:" + porta);

            while (true) {
                Socket socket = serverSocket.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream out = socket.getOutputStream();

                String primeiraLinha = in.readLine();
                System.out.println("---- REQUEST RECEBIDA ----");
                System.out.println(primeiraLinha);

                String linha;
                while ((linha = in.readLine()) != null && !linha.isEmpty()) {
                    System.out.println(linha);
                }

                /*String path = "";

                if (primeiraLinha != null && !primeiraLinha.isEmpty()) {
                    String[] partes = primeiraLinha.split(" ");
                    String urlCompleta = partes[1];
                    path = urlCompleta;
                }
*/
                StringBuilder html = new StringBuilder();

                html.append("<html>");
                html.append("<head><title>Servidor Java</title></head>");
                html.append("<body>");
                html.append("<h1>Servidor Rodando...</h1>");

                //html.append("<p><b>Path:</b> ").append(path).append("</p>");

                html.append("</body>");
                html.append("</html>");

                String response =
                        "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html; charset=UTF-8\r\n" +
                        "Content-Length: " + html.toString().getBytes().length + "\r\n" +
                        "\r\n" +
                        html.toString();

                out.write(response.getBytes());
                out.flush();

                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}