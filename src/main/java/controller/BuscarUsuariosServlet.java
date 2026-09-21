package controller;

import dao.Conexao;
import model.Usuario;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/buscar-usuarios")
public class BuscarUsuariosServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // =====================================================
        // VERIFICAR LOGIN
        // =====================================================

        HttpSession sessao =
                request.getSession(false);

        if (sessao == null ||
                sessao.getAttribute("usuario") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login.html"
            );

            return;
        }

        // =====================================================
        // TERMO DA BUSCA
        // =====================================================

        String busca =
                request.getParameter("busca");

        if (busca == null) {
            busca = "";
        }

        busca = busca.trim();

        // Remove @ do começo
        if (busca.startsWith("@")) {

            busca =
                    busca.substring(1).trim();
        }

        ArrayList<Usuario> usuarios =
                new ArrayList<Usuario>();

        String erro = "";

        // =====================================================
        // BUSCAR NO BANCO
        // =====================================================

        if (!busca.isEmpty()) {

            Connection conexao = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {

                conexao =
                        Conexao.conectar();

                if (conexao == null) {

                    erro =
                            "Não foi possível conectar ao banco.";

                } else {

                    String sql =
                            "SELECT " +
                            "id, " +
                            "nome, " +
                            "username, " +
                            "foto " +
                            "FROM usuario " +
                            "WHERE " +
                            "LOWER(COALESCE(username,'')) LIKE LOWER(?) " +
                            "OR " +
                            "LOWER(COALESCE(nome,'')) LIKE LOWER(?) " +
                            "ORDER BY nome";

                    stmt =
                            conexao.prepareStatement(sql);

                    String termo =
                            "%" + busca + "%";

                    stmt.setString(
                            1,
                            termo
                    );

                    stmt.setString(
                            2,
                            termo
                    );

                    rs =
                            stmt.executeQuery();

                    while (rs.next()) {

                        Usuario usuario =
                                new Usuario();

                        usuario.setId(
                                rs.getInt("id")
                        );

                        usuario.setNome(
                                rs.getString("nome")
                        );

                        usuario.setUsername(
                                rs.getString("username")
                        );

                        usuario.setFoto(
                                rs.getString("foto")
                        );

                        usuarios.add(usuario);
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();

                erro =
                        "Não foi possível realizar a busca.";

            } finally {

                try {

                    if (rs != null) {
                        rs.close();
                    }

                } catch (Exception e) {
                }

                try {

                    if (stmt != null) {
                        stmt.close();
                    }

                } catch (Exception e) {
                }

                try {

                    if (conexao != null) {
                        conexao.close();
                    }

                } catch (Exception e) {
                }
            }
        }

        // =====================================================
        // HTML
        // =====================================================

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        StringBuilder html =
                new StringBuilder();

        html.append(
                "<!DOCTYPE html>"
        );

        html.append(
                "<html lang='pt-BR'>"
        );

        html.append("<head>");

        html.append(
                "<meta charset='UTF-8'>"
        );

        html.append(
                "<meta name='viewport' " +
                "content='width=device-width, " +
                "initial-scale=1.0'>"
        );

        html.append(
                "<title>Buscar usuários - Inventory</title>"
        );

        html.append(
                "<link rel='stylesheet' " +
                "href='style.css'>"
        );

        // =====================================================
        // CSS
        // =====================================================

        html.append("<style>");

        html.append(
                "body{" +
                "margin:0;" +
                "background:" +
                "radial-gradient(" +
                "circle at top," +
                "#35105f," +
                "#12091b 55%," +
                "#09050d" +
                ");" +
                "min-height:100vh;" +
                "color:white;" +
                "}"
        );

        html.append(
                ".busca-usuarios{" +
                "max-width:900px;" +
                "margin:45px auto;" +
                "padding:20px;" +
                "}"
        );

        html.append(
                ".caixa-busca{" +
                "background:#181020;" +
                "border:1px solid #4b2370;" +
                "border-radius:20px;" +
                "padding:30px;" +
                "}"
        );

        html.append(
                ".titulo-busca{" +
                "text-align:center;" +
                "margin-bottom:10px;" +
                "font-size:35px;" +
                "color:#b66cff;" +
                "}"
        );

        html.append(
                ".subtitulo-busca{" +
                "text-align:center;" +
                "color:#aaa;" +
                "margin-bottom:25px;" +
                "}"
        );

        // =====================================================
        // FORMULÁRIO
        // =====================================================

        html.append(
                ".form-busca{" +
                "display:flex;" +
                "gap:10px;" +
                "margin-top:20px;" +
                "}"
        );

        html.append(
                ".campo-busca{" +
                "flex:1;" +
                "padding:14px;" +
                "background:#100b15;" +
                "border:1px solid #47305a;" +
                "border-radius:9px;" +
                "color:white;" +
                "font-size:16px;" +
                "outline:none;" +
                "box-sizing:border-box;" +
                "}"
        );

        html.append(
                ".campo-busca:focus{" +
                "border-color:#8b5cf6;" +
                "}"
        );

        html.append(
                ".campo-busca::placeholder{" +
                "color:#777;" +
                "}"
        );

        html.append(
                ".botao-busca{" +
                "padding:14px 25px;" +
                "border:none;" +
                "border-radius:9px;" +
                "background:#7c3aed;" +
                "color:white;" +
                "font-weight:bold;" +
                "cursor:pointer;" +
                "font-size:15px;" +
                "}"
        );

        html.append(
                ".botao-busca:hover{" +
                "background:#8b5cf6;" +
                "}"
        );

        // =====================================================
        // RESULTADOS
        // =====================================================

        html.append(
                ".resultados{" +
                "display:grid;" +
                "grid-template-columns:" +
                "repeat(auto-fill,minmax(220px,1fr));" +
                "gap:15px;" +
                "margin-top:25px;" +
                "}"
        );

        html.append(
                ".usuario-card{" +
                "display:block;" +
                "background:#160d1e;" +
                "border:1px solid #352044;" +
                "border-radius:14px;" +
                "padding:18px;" +
                "color:white;" +
                "text-decoration:none;" +
                "transition:.25s;" +
                "text-align:center;" +
                "}"
        );

        html.append(
                ".usuario-card:hover{" +
                "transform:translateY(-4px);" +
                "border-color:#8b5cf6;" +
                "box-shadow:" +
                "0 10px 25px rgba(124,58,237,.2);" +
                "}"
        );

        // =====================================================
        // FOTO
        // =====================================================

        html.append(
                ".foto-usuario{" +
                "width:80px;" +
                "height:80px;" +
                "border-radius:50%;" +
                "object-fit:cover;" +
                "border:3px solid #7c3aed;" +
                "margin-bottom:10px;" +
                "}"
        );

        html.append(
                ".sem-foto{" +
                "width:80px;" +
                "height:80px;" +
                "border-radius:50%;" +
                "display:flex;" +
                "align-items:center;" +
                "justify-content:center;" +
                "background:#281833;" +
                "color:#999;" +
                "margin:0 auto 10px;" +
                "}"
        );

        html.append(
                ".username{" +
                "color:#b98be8;" +
                "margin-top:5px;" +
                "}"
        );

        // =====================================================
        // MENSAGENS
        // =====================================================

        html.append(
                ".nenhum{" +
                "margin-top:25px;" +
                "padding:25px;" +
                "text-align:center;" +
                "background:#160d1e;" +
                "border:1px dashed #493254;" +
                "border-radius:13px;" +
                "color:#999;" +
                "}"
        );

        html.append(
                ".erro{" +
                "margin-top:25px;" +
                "padding:25px;" +
                "text-align:center;" +
                "background:#210f19;" +
                "border:1px solid #7f1d3b;" +
                "border-radius:13px;" +
                "color:#ff6b91;" +
                "}"
        );
        html.append(
        "<link rel='icon' " +
        "type='image/png' " +
        "href='favicon.png'>"
);

        // =====================================================
        // RESPONSIVO
        // =====================================================

        html.append(
                "@media(max-width:600px){" +
                ".busca-usuarios{" +
                "margin:20px auto;" +
                "padding:12px;" +
                "}" +
                ".caixa-busca{" +
                "padding:20px;" +
                "}" +
                ".titulo-busca{" +
                "font-size:28px;" +
                "}" +
                ".form-busca{" +
                "flex-direction:column;" +
                "}" +
                ".botao-busca{" +
                "width:100%;" +
                "}" +
                "}"
        );

        html.append("</style>");

        html.append("</head>");

        html.append("<body>");

        // =====================================================
        // HEADER
        // =====================================================

        html.append("<header>");

        html.append(
                "<h1>Inventory</h1>"
        );

        html.append("<nav>");

        html.append(
                "<a href='index.html'>" +
                "Início" +
                "</a>"
        );

        html.append(
                "<a href='jogos'>" +
                "Jogos" +
                "</a>"
        );

        html.append(
                "<a href='biblioteca'>" +
                "Biblioteca" +
                "</a>"
        );

        html.append(
                "<a href='buscar-usuarios'>" +
                "Buscar usuários" +
                "</a>"
        );

        html.append(
                "<a href='listas'>" +
                "Listas" +
                "</a>"
        );

        html.append(
                "<a href='perfil'>" +
                "Meu Perfil" +
                "</a>"
        );

        html.append(
                "<a href='logout'>" +
                "Sair" +
                "</a>"
        );

        html.append("</nav>");

        html.append("</header>");

        // =====================================================
        // CONTEÚDO
        // =====================================================

        html.append(
                "<main class='busca-usuarios'>"
        );

        html.append(
                "<div class='caixa-busca'>"
        );

        html.append(
                "<h2 class='titulo-busca'>" +
                "🔎 Buscar usuários" +
                "</h2>"
        );

        html.append(
                "<p class='subtitulo-busca'>" +
                "Encontre outros jogadores do Inventory." +
                "</p>"
        );

        // =====================================================
        // FORMULÁRIO
        // =====================================================

        html.append(
                "<form " +
                "class='form-busca' " +
                "method='GET' " +
                "action='" +
                request.getContextPath() +
                "/buscar-usuarios'>"
        );

        html.append(
                "<input " +
                "class='campo-busca' " +
                "type='text' " +
                "name='busca' " +
                "value='" +
                escapar(busca) +
                "' " +
                "placeholder='Nome ou @username' " +
                "autocomplete='off'>"
        );

        html.append(
                "<button " +
                "class='botao-busca' " +
                "type='submit'>" +
                "Buscar" +
                "</button>"
        );

        html.append("</form>");

        // =====================================================
        // ERRO
        // =====================================================

        if (!erro.isEmpty()) {

            html.append(
                    "<div class='erro'>" +
                    "❌ " +
                    escapar(erro) +
                    "</div>"
            );
        }

        // =====================================================
        // RESULTADOS
        // =====================================================

        if (!busca.isEmpty() &&
                erro.isEmpty()) {

            if (usuarios.isEmpty()) {

                html.append(
                        "<div class='nenhum'>" +
                        "Nenhum usuário encontrado." +
                        "</div>"
                );

            } else {

                html.append(
                        "<div class='resultados'>"
                );

                for (Usuario usuario :
                        usuarios) {

                    html.append(
                            "<a " +
                            "class='usuario-card' " +
                            "href='" +
                            request.getContextPath() +
                            "/perfil-usuario?id=" +
                            usuario.getId() +
                            "'>"
                    );

                    // =================================================
                    // FOTO
                    // =================================================

                    String foto =
                            usuario.getFoto();

                    if (foto != null &&
                            !foto.trim().isEmpty()) {

                        String caminho =
                                foto.trim();

                        if (!caminho.startsWith("http://") &&
                                !caminho.startsWith("https://")) {

                            while (
                                    caminho.startsWith("/")
                            ) {

                                caminho =
                                        caminho.substring(1);
                            }

                            caminho =
                                    request.getContextPath() +
                                    "/foto-perfil?arquivo=" +
                                    URLEncoder.encode(
                                            caminho,
                                            "UTF-8"
                                    );
                        }

                        html.append(
                                "<img " +
                                "class='foto-usuario' " +
                                "src='" +
                                escapar(caminho) +
                                "' " +
                                "alt='Foto do usuário'>"
                        );

                    } else {

                        html.append(
                                "<div class='sem-foto'>" +
                                "👤" +
                                "</div>"
                        );
                    }

                    // =================================================
                    // NOME
                    // =================================================

                    html.append(
                            "<h3>" +
                            escapar(
                                    usuario.getNome()
                            ) +
                            "</h3>"
                    );

                    // =================================================
                    // USERNAME
                    // =================================================

                    String username =
                            usuario.getUsername();

                    if (username == null ||
                            username.trim().isEmpty()) {

                        username =
                                "usuario" +
                                usuario.getId();
                    }

                    html.append(
                            "<div class='username'>" +
                            "@" +
                            escapar(username) +
                            "</div>"
                    );

                    html.append("</a>");
                }

                html.append("</div>");
            }
        }

        html.append("</div>");

        html.append("</main>");

        html.append("</body>");

        html.append("</html>");

        response.getWriter().println(
                html.toString()
        );
    }

    // =====================================================
    // ESCAPAR HTML
    // =====================================================

    private String escapar(
            String texto) {

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