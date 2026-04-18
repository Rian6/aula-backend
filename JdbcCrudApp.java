import java.sql.*;
import java.util.Scanner;

public class JdbcCrudApp {

    private static final String URL = "jdbc:sqlite:database.db";

    public static void main(String[] args) {
        inicializarBanco();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== MENU ===");
            System.out.println("1 - Listar produtos");
            System.out.println("2 - Inserir produto");
            System.out.println("3 - Atualizar produto");
            System.out.println("4 - Deletar produto");
            System.out.println("0 - Sair");
            System.out.print("Escolha: ");

            int opcao;
            try {
                opcao = Integer.parseInt(scanner.nextLine());
            } catch (Exception e) {
                System.out.println("Entrada inválida!");
                continue;
            }

            switch (opcao) {
                case 1 -> listar();
                case 2 -> inserir(scanner);
                case 3 -> atualizar(scanner);
                case 4 -> deletar(scanner);
                case 0 -> {
                    System.out.println("Encerrando...");
                    return;
                }
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    // 🔥 CORREÇÃO AQUI
    private static Connection conectar() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC"); // força carregar driver
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver SQLite não encontrado!", e);
        }
        return DriverManager.getConnection(URL);
    }

    private static void inicializarBanco() {
        String sql = """
            CREATE TABLE IF NOT EXISTS produto (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT,
                preco REAL
            )
        """;

        try (
            Connection conn = conectar();
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
            System.out.println("Banco SQLite pronto!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void listar() {
        String sql = "SELECT * FROM produto";

        try (
            Connection conn = conectar();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            System.out.println("\n--- PRODUTOS ---");

            boolean vazio = true;

            while (rs.next()) {
                vazio = false;
                System.out.printf(
                    "%d | %s | %.2f%n",
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getDouble("preco")
                );
            }

            if (vazio) {
                System.out.println("Nenhum produto encontrado.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void inserir(Scanner scanner) {
        String sql = "INSERT INTO produto (nome, preco) VALUES (?, ?)";

        try (
            Connection conn = conectar();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            System.out.print("Nome: ");
            String nome = scanner.nextLine();

            System.out.print("Preço: ");
            double preco = Double.parseDouble(scanner.nextLine());

            stmt.setString(1, nome);
            stmt.setDouble(2, preco);

            stmt.executeUpdate();
            System.out.println("Produto inserido!");

        } catch (Exception e) {
            System.out.println("Erro ao inserir.");
        }
    }

    private static void atualizar(Scanner scanner) {
        String sql = "UPDATE produto SET nome = ?, preco = ? WHERE id = ?";

        try (
            Connection conn = conectar();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            System.out.print("ID: ");
            long id = Long.parseLong(scanner.nextLine());

            System.out.print("Novo nome: ");
            String nome = scanner.nextLine();

            System.out.print("Novo preço: ");
            double preco = Double.parseDouble(scanner.nextLine());

            stmt.setString(1, nome);
            stmt.setDouble(2, preco);
            stmt.setLong(3, id);

            int linhas = stmt.executeUpdate();

            if (linhas > 0) {
                System.out.println("Produto atualizado!");
            } else {
                System.out.println("Produto não encontrado.");
            }

        } catch (Exception e) {
            System.out.println("Erro ao atualizar.");
        }
    }

    private static void deletar(Scanner scanner) {
        String sql = "DELETE FROM produto WHERE id = ?";

        try (
            Connection conn = conectar();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            System.out.print("ID: ");
            long id = Long.parseLong(scanner.nextLine());

            stmt.setLong(1, id); // 🔥 isso faltava no seu código original

            int linhas = stmt.executeUpdate();

            if (linhas > 0) {
                System.out.println("Produto deletado!");
            } else {
                System.out.println("Produto não encontrado.");
            }

        } catch (Exception e) {
            System.out.println("Erro ao deletar.");
        }
    }
}