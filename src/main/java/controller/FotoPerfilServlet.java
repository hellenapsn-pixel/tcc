package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/foto-perfil")
public class FotoPerfilServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String arquivo = request.getParameter("arquivo");

        if (arquivo == null || arquivo.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        arquivo = new File(arquivo).getName();

        String pasta =
                System.getProperty("java.io.tmpdir")
                + File.separator
                + "inventory-perfis";

        File foto = new File(pasta, arquivo);

        // Compatibilidade com fotos antigas salvas na aplicação
        if (!foto.exists() || !foto.isFile()) {
            String pastaAntiga =
                    request.getServletContext()
                    .getRealPath("/uploads/perfis");

            if (pastaAntiga != null) {
                foto = new File(pastaAntiga, arquivo);
            }
        }

        if (!foto.exists() || !foto.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String nome = foto.getName().toLowerCase();

        if (nome.endsWith(".png")) {
            response.setContentType("image/png");
        } else if (nome.endsWith(".jpg") || nome.endsWith(".jpeg")) {
            response.setContentType("image/jpeg");
        } else if (nome.endsWith(".webp")) {
            response.setContentType("image/webp");
        } else if (nome.endsWith(".gif")) {
            response.setContentType("image/gif");
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentLengthLong(foto.length());

        try (FileInputStream entrada = new FileInputStream(foto);
             OutputStream saida = response.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int lidos;

            while ((lidos = entrada.read(buffer)) != -1) {
                saida.write(buffer, 0, lidos);
            }
        }
    }
}
