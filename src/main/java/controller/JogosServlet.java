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
        out.println("<title>Jogos - Inventory</title>");

        out.println("<style>");
        out.println("body{margin:0;background:#0b0d0f;color:white;font-family:Arial,sans-serif;}");
        out.println("header{background:#11151a;padding:20px 40px;border-bottom:1px solid #333;}");
        out.println("header a{color:white;text-decoration:none;margin-right:25px;}");
        out.println(".container{max-width:1200px;margin:40px auto;padding:20px;}");
        out.println("h1{font-size:40px;}");
        out.println(".grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(220px,1fr));gap:20px;}");
        out.println(".card{background:#15191e;border:1px solid #292f36;border-radius:10px;padding:20px;}");
        out.println(".card h2{margin-top:0;}");
        out.println(".info{color:#9da4ad;line-height:1.7;}");
        out.println("</style>");

        out.println("</head>");
        out.println("<body>");

        out.println("<header>");
        out.println("<a href='index.jsp'>INVENTORY</a>");
        out.println("<a href='jogos'>Jogos</a>");
        out.println("</header>");

        out.println("<div class='container'>");
        out.println("<h1>Jogos</h1>");
        out.println("<div class='grid'>");

        try (Connection conn = Conexao.conectar()) {

            String sql = "SELECT id, titulo, genero, plataforma, ano_lancamento, capa "
                    + "FROM jogo ORDER BY titulo";

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                boolean encontrou = false;

                while (rs.next()) {

                    encontrou = true;

                    out.println("<div class='card'>");

                    out.println("<h2>" +
                            escapar(rs.getString("titulo")) +
                            "</h2>");

                    out.println("<div class='info'>");

                    out.println("<strong>Gênero:</strong> " +
                            escapar(rs.getString("genero")) +
                            "<br>");

                    out.println("<strong>Plataforma:</strong> " +
                            escapar(rs.getString("plataforma")) +
                            "<br>");

                    out.println("<strong>Ano:</strong> " +
                            rs.getInt("ano_lancamento"));

                    out.println("</div>");

                    out.println("</div>");
                }

                if (!encontrou) {
                    out.println("<p>Nenhum jogo cadastrado.</p>");
                }
            }

        } catch (Exception e) {

            out.println("<h2>Erro ao carregar os jogos</h2>");
            out.println("<pre>");
            e.printStackTrace(out);
            out.println("</pre>");

            System.out.println("ERRO AO CARREGAR JOGOS");
            e.printStackTrace();
        }

        out.println("</div>");
        out.println("</div>");

        out.println("</body>");
        out.println("</html>");
    }

    private String escapar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}