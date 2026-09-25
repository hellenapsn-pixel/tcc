package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/jogos") // <--- ESTA LINHA É O QUE FAZ O TOMCAT ENCONTRAR A ROTA /jogos
public class JogosServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("JOGOS SERVLET FOI CHAMADO");

        // Usa a classe Conexao para abrir o banco
        try (Connection conn = Conexao.conectar()) {
            
            String sql = "SELECT id, titulo, genero, plataforma, ano_lancamento, capa FROM jogo ORDER BY titulo";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                // Lógica para enviar os jogos para o seu JSP ou resposta JSON
                // Exemplo: request.setAttribute("listaJogos", lista);
            }

        } catch (Exception e) {
            System.out.println("ERRO NO JOGOSSERVLET");
            e.printStackTrace();
        }

        // Redireciona para sua página JSP
        request.getRequestDispatcher("/jogos.jsp").forward(request, response);
    }
}