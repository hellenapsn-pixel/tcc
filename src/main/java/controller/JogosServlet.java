package controller;

import dao.Conexao;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/jogos")
public class JogosServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Define o tipo de resposta como HTML em UTF-8
        response.setContentType("text/html;charset=UTF-8");
        
        // Garante que a tabela exista antes de consultar
        criarTabelaSeNaoExistir();

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang='pt-br'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>GameBoxd - Lista de Jogos</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; background-color: #14181c; color: #ffffff; padding: 20px; }");
            out.println("h1 { text-align: center; color: #00e054; }");
            out.println(".grid-jogos { display: flex; flex-wrap: wrap; gap: 20px; justify-content: center; margin-top: 30px; }");
            out.println(".card-jogo { background: #1f2833; border-radius: 8px; width: 200px; padding: 15px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.3); }");
            out.println(".card-jogo img { width: 100%; height: 280px; object-fit: cover; border-radius: 6px; }");
            out.println(".card-jogo h3 { font-size: 1.1em; margin: 10px 0 5px; color: #ffffff; }");
            out.println(".card-jogo p { margin: 3px 0; color: #9ab; font-size: 0.9em; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Meus Jogos</h1>");
            out.println("<div class='grid-jogos'>");

            try (Connection conn = Conexao.conectar()) {
                if (conn != null) {
                    String sql = "SELECT * FROM jogo";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();

                    boolean encontrou = false;
                    while (rs.next()) {
                        encontrou = true;
                        String nome = rs.getString("nome");
                        String ano = rs.getString("ano");
                        String genero = rs.getString("genero");
                        String capa = rs.getString("capa");

                        // Se a capa for nula ou vazia, define uma imagem padrão
                        if (capa == null || capa.trim().isEmpty()) {
                            capa = "https://via.placeholder.com/200x280?text=Sem+Capa";
                        }

                        out.println("<div class='card-jogo'>");
                        out.println("  <img src='" + capa + "' alt='" + nome + "'>");
                        out.println("  <h3>" + nome + "</h3>");
                        out.println("  <p><strong>Ano:</strong> " + ano + "</p>");
                        out.println("  <p><strong>Gênero:</strong> " + genero + "</p>");
                        out.println("</div>");
                    }

                    if (!encontrou) {
                        out.println("<p>Nenhum jogo encontrado no banco de dados.</p>");
                    }
                } else {
                    out.println("<p style='color: red;'>Erro de conexão com o banco de dados.</p>");
                }
            } catch (SQLException e) {
                out.println("<p style='color: red;'>Erro no banco de dados: " + e.getMessage() + "</p>");
            }

            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    private void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS jogo (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "nome TEXT NOT NULL, " +
                     "ano TEXT, " +
                     "genero TEXT, " +
                     "capa TEXT);";
        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}