package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='pt-BR'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Jogos - GameBoxd</title>");

        out.println("<style>");
        out.println("body {");
        out.println("    font-family: Arial, sans-serif;");
        out.println("    background: #111;");
        out.println("    color: white;");
        out.println("    margin: 0;");
        out.println("    padding: 30px;");
        out.println("}");

        out.println("h1 {");
        out.println("    text-align: center;");
        out.println("}");

        out.println(".jogos {");
        out.println("    display: grid;");
        out.println("    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));");
        out.println("    gap: 20px;");
        out.println("    max-width: 1200px;");
        out.println("    margin: 30px auto;");
        out.println("}");

        out.println(".jogo {");
        out.println("    background: #222;");
        out.println("    border-radius: 10px;");
        out.println("    padding: 15px;");
        out.println("    box-shadow: 0 0 10px rgba(0,0,0,0.5);");
        out.println("}");

        out.println(".jogo img {");
        out.println("    width: 100%;");
        out.println("    height: 280px;");
        out.println("    object-fit: cover;");
        out.println("    border-radius: 8px;");
        out.println("}");

        out.println(".jogo h2 {");
        out.println("    font-size: 20px;");
        out.println("}");

        out.println(".jogo p {");
        out.println("    color: #ccc;");
        out.println("}");

        out.println("</style>");
        out.println("</head>");

        out.println("<body>");

        out.println("<h1>Jogos</h1>");
        out.println("<div class='jogos'>");

        try (Connection conn = Conexao.conectar()) {

            String sql = "SELECT id, titulo, genero, plataforma, ano_lancamento, capa "
                    + "FROM jogo ORDER BY titulo";

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    String titulo = rs.getString("titulo");
                    String genero = rs.getString("genero");
                    String plataforma = rs.getString("plataforma");
                    int ano = rs.getInt("ano_lancamento");
                    String capa = rs.getString("capa");

                    out.println("<div class='jogo'>");

                    if (capa != null && !capa.isEmpty()) {
                        out.println("<img src='" + capa + "' alt='Capa de " + titulo + "'>");
                    }

                    out.println("<h2>" + titulo + "</h2>");
                    out.println("<p><strong>Gênero:</strong> " + genero + "</p>");
                    out.println("<p><strong>Plataforma:</strong> " + plataforma + "</p>");
                    out.println("<p><strong>Ano:</strong> " + ano + "</p>");

                    out.println("</div>");
                }
            }

        } catch (Exception e) {

            out.println("<div style='text-align:center;'>");
            out.println("<h2>Erro ao carregar os jogos.</h2>");
            out.println("<p>" + e.getMessage() + "</p>");
            out.println("</div>");

            e.printStackTrace();
        }

        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}