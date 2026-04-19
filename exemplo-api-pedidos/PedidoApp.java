import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class PedidoApp {

    static Map<Integer, String> pedidos = new HashMap<>();
    static int sequence = 1;

    public static void main(String[] args) {
        int porta = 8080;

        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("Servidor rodando em http://localhost:" + porta);

            while (true) {
                Socket socket = serverSocket.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream out = socket.getOutputStream();

                String primeiraLinha = in.readLine();
                if (primeiraLinha == null) continue;

                String method = primeiraLinha.split(" ")[0];
                String path = primeiraLinha.split(" ")[1];

                String linha;
                int contentLength = 0;
                while ((linha = in.readLine()) != null && !linha.isEmpty()) {
                    if (linha.toLowerCase().startsWith("content-length")) {
                        contentLength = Integer.parseInt(linha.split(":")[1].trim());
                    }
                }

                char[] bodyChars = new char[contentLength];
                if (contentLength > 0) {
                    in.read(bodyChars, 0, contentLength);
                }
                String body = new String(bodyChars).trim();

                String responseBody = "";
                int status = 200;

                try {

                    // GET
                    if (method.equals("GET") && path.equals("/pedidos")) {
                        responseBody = listarPedidosJson();
                    }

                    // POST
                    else if (method.equals("POST") && path.equals("/pedidos")) {
                        String descricao = extrairDescricaoJson(body);

                        int id = sequence++;
                        pedidos.put(id, descricao);

                        responseBody = "{\"mensagem\":\"Pedido criado\",\"id\":" + id + "}";
                    }

                    // PUT
                    else if (method.equals("PUT") && path.startsWith("/pedidos")) {
                        int id = extrairId(path);
                        String descricao = extrairDescricaoJson(body);

                        if (pedidos.containsKey(id)) {
                            pedidos.put(id, descricao);
                            responseBody = "{\"mensagem\":\"Pedido atualizado\"}";
                        } else {
                            status = 404;
                            responseBody = "{\"erro\":\"Pedido não encontrado\"}";
                        }
                    }

                    // DELETE
                    else if (method.equals("DELETE") && path.startsWith("/pedidos")) {
                        int id = extrairId(path);

                        if (pedidos.containsKey(id)) {
                            pedidos.remove(id);
                            responseBody = "{\"mensagem\":\"Pedido removido\"}";
                        } else {
                            status = 404;
                            responseBody = "{\"erro\":\"Pedido não encontrado\"}";
                        }
                    }

                    else {
                        status = 404;
                        responseBody = "{\"erro\":\"Endpoint não encontrado\"}";
                    }

                } catch (Exception e) {
                    status = 500;
                    responseBody = "{\"erro\":\"Erro interno\"}";
                }

                String statusText = (status == 200) ? "OK" : (status == 404) ? "Not Found" : "Error";

                String response =
                        "HTTP/1.1 " + status + " " + statusText + "\r\n" +
                        "Content-Type: application/json; charset=UTF-8\r\n" +
                        "Access-Control-Allow-Origin: *\r\n" +
                        "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n" +
                        "Access-Control-Allow-Headers: Content-Type\r\n" +
                        "Content-Length: " + responseBody.getBytes().length + "\r\n\r\n" +
                        responseBody;

                out.write(response.getBytes());
                socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String listarPedidosJson() {
        StringBuilder sb = new StringBuilder("[");
        for (Map.Entry<Integer, String> entry : pedidos.entrySet()) {
            sb.append("{\"id\":").append(entry.getKey())
              .append(",\"descricao\":\"").append(entry.getValue()).append("\"},");
        }
        if (sb.length() > 1) sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    private static int extrairId(String path) {
        return Integer.parseInt(path.split("=")[1]);
    }

    private static String extrairDescricaoJson(String json) {
        return json.replace("{", "")
                   .replace("}", "")
                   .replace("\"", "")
                   .split(":")[1];
    }
}