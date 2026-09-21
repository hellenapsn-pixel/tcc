package controller;

import dao.Conexao;
import model.Usuario;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/editar-perfil")
public class EditarPerfilServlet extends HttpServlet {

    // =========================================================
    // GET - MOSTRAR FORMULÁRIO
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao =
                request.getSession(false);

        if (sessao == null ||
                sessao.getAttribute("usuario") == null) {

            response.sendRedirect("login.html");
            return;
        }

        Usuario usuario =
                (Usuario) sessao.getAttribute("usuario");

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        String foto = usuario.getFoto();

        String caminhoFoto =
                (foto != null &&
                 !foto.trim().isEmpty())
                ? foto
                : "icon.png";

        StringBuilder html =
                new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='pt-BR'>");

        html.append("<head>");

        html.append("<meta charset='UTF-8'>");

        html.append(
                "<meta name='viewport' " +
                "content='width=device-width, initial-scale=1.0'>"
        );

        html.append(
                "<title>Editar perfil - Inventory</title>"
        );

        html.append(
                "<link rel='icon' " +
                "type='image/png' " +
                "href='icon.png'>"
        );

        html.append(
                "<link rel='stylesheet' " +
                "href='style.css'>"
        );

        html.append("<style>");

        html.append(
                ".editar-container {" +
                "max-width:650px;" +
                "margin:40px auto;" +
                "background:#202830;" +
                "padding:35px;" +
                "border-radius:15px;" +
                "color:white;" +
                "}"
        );

        html.append(
                ".editar-container h2 {" +
                "text-align:center;" +
                "margin-bottom:30px;" +
                "}"
        );

        html.append(
                ".foto-atual {" +
                "display:block;" +
                "width:120px;" +
                "height:120px;" +
                "border-radius:50%;" +
                "object-fit:cover;" +
                "margin:0 auto 25px;" +
                "border:3px solid #6300c0;" +
                "}"
        );

        html.append(
                ".campo {" +
                "margin-bottom:20px;" +
                "}"
        );

        html.append(
                ".campo label {" +
                "display:block;" +
                "margin-bottom:8px;" +
                "font-weight:bold;" +
                "color:#ddd;" +
                "}"
        );

        html.append(
                ".campo input," +
                ".campo textarea," +
                ".campo select {" +
                "width:100%;" +
                "box-sizing:border-box;" +
                "padding:12px;" +
                "background:#14181c;" +
                "color:white;" +
                "border:1px solid #444;" +
                "border-radius:8px;" +
                "font-size:15px;" +
                "}"
        );

        html.append(
                ".campo textarea {" +
                "height:120px;" +
                "resize:vertical;" +
                "font-family:Arial;" +
                "}"
        );

        html.append(
                ".botoes {" +
                "display:flex;" +
                "gap:10px;" +
                "margin-top:25px;" +
                "}"
        );

        html.append(
                ".botao-salvar {" +
                "flex:1;" +
                "padding:13px;" +
                "border:none;" +
                "border-radius:8px;" +
                "background:#6300c0;" +
                "color:white;" +
                "font-weight:bold;" +
                "font-size:16px;" +
                "cursor:pointer;" +
                "}"
        );

        html.append(
                ".botao-salvar:hover {" +
                "background:#7d00ef;" +
                "}"
        );

        html.append(
                ".botao-cancelar {" +
                "flex:1;" +
                "padding:13px;" +
                "border-radius:8px;" +
                "background:#444;" +
                "color:white;" +
                "text-align:center;" +
                "text-decoration:none;" +
                "font-weight:bold;" +
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
                "<div class='logo-area'>"
        );

        html.append(
                "<img src='icon.png' " +
                "alt='Logo Inventory' " +
                "class='logo-header'>"
        );

        html.append("<h1>Inventory</h1>");

        html.append("</div>");

        html.append("<nav>");

        html.append(
                "<a href='home'>Início</a>"
        );

        html.append(
                "<a href='jogos'>Jogos</a>"
        );

        html.append(
                "<a href='biblioteca'>Biblioteca</a>"
        );

        html.append(
                "<a href='perfil'>Meu Perfil</a>"
        );

        html.append(
                "<a href='logout'>Sair</a>"
        );

        html.append("</nav>");

        html.append("</header>");

        // =====================================================
        // FORMULÁRIO
        // =====================================================

        html.append(
                "<main class='editar-container'>"
        );

        html.append("<h2>Editar perfil</h2>");

        html.append(
                "<img " +
                "class='foto-atual' " +
                "src='" +
                escaparHtml(caminhoFoto) +
                "' " +
                "alt='Foto de perfil'>"
        );

        html.append(
                "<form method='POST' " +
                "action='editar-perfil'>"
        );

        // NOME

        html.append(
                "<div class='campo'>"
        );

        html.append(
                "<label for='nome'>Nome</label>"
        );

        html.append(
                "<input " +
                "type='text' " +
                "id='nome' " +
                "name='nome' " +
                "value='" +
                escaparHtml(valor(usuario.getNome())) +
                "' " +
                "required>"
        );

        html.append("</div>");

        // USERNAME

        html.append(
                "<div class='campo'>"
        );

        html.append(
                "<label for='username'>Username</label>"
        );

        html.append(
                "<input " +
                "type='text' " +
                "id='username' " +
                "name='username' " +
                "value='" +
                escaparHtml(valor(usuario.getUsername())) +
                "' " +
                "required>"
        );

        html.append("</div>");

        // EMAIL

        html.append(
                "<div class='campo'>"
        );

        html.append(
                "<label for='email'>E-mail</label>"
        );

        html.append(
                "<input " +
                "type='email' " +
                "id='email' " +
                "name='email' " +
                "value='" +
                escaparHtml(valor(usuario.getEmail())) +
                "' " +
                "required>"
        );

        html.append("</div>");

        // DATA DE NASCIMENTO

        html.append(
                "<div class='campo'>"
        );

        html.append(
                "<label for='dataNascimento'>" +
                "Data de nascimento" +
                "</label>"
        );

        html.append(
                "<input " +
                "type='date' " +
                "id='dataNascimento' " +
                "name='dataNascimento' " +
                "value='" +
                escaparHtml(
                        valor(
                                usuario.getDataNascimento()
                        )
                ) +
                "'>"
        );

        html.append("</div>");

        // PAÍS

        html.append(
                "<div class='campo'>"
        );

        html.append(
                "<label for='pais'>País</label>"
        );

        html.append(
                "<input " +
                "type='text' " +
                "id='pais' " +
                "name='pais' " +
                "value='" +
                escaparHtml(valor(usuario.getPais())) +
                "'>"
        );

        html.append("</div>");

        // PLATAFORMA

        html.append(
                "<div class='campo'>"
        );

        html.append(
                "<label for='plataformaFavorita'>" +
                "Plataforma favorita" +
                "</label>"
        );

        html.append(
                "<input " +
                "type='text' " +
                "id='plataformaFavorita' " +
                "name='plataformaFavorita' " +
                "placeholder='Ex: PC, PlayStation, Xbox' " +
                "value='" +
                escaparHtml(
                        valor(
                                usuario.getPlataformaFavorita()
                        )
                ) +
                "'>"
        );

        html.append("</div>");

        // FOTO

        html.append(
                "<div class='campo'>"
        );

        html.append(
                "<label for='foto'>" +
                "URL da foto" +
                "</label>"
        );

        html.append(
                "<input " +
                "type='url' " +
                "id='foto' " +
                "name='foto' " +
                "placeholder='https://...' " +
                "value='" +
                escaparHtml(valor(usuario.getFoto())) +
                "'>"
        );

        html.append("</div>");

        // BIO

        html.append(
                "<div class='campo'>"
        );

        html.append(
                "<label for='bio'>Biografia</label>"
        );

        html.append(
                "<textarea " +
                "id='bio' " +
                "name='bio' " +
                "placeholder='Fale um pouco sobre você...'>" +
                escaparHtml(valor(usuario.getBio())) +
                "</textarea>"
        );

        html.append("</div>");

        // BOTÕES

        html.append(
                "<div class='botoes'>"
        );

        html.append(
                "<a " +
                "class='botao-cancelar' " +
                "href='perfil'>" +
                "Cancelar" +
                "</a>"
        );

        html.append(
                "<button " +
                "class='botao-salvar' " +
                "type='submit'>" +
                "Salvar alterações" +
                "</button>"
        );

        html.append("</div>");

        html.append("</form>");

        html.append("</main>");

        html.append("</body>");

        html.append("</html>");

        response.getWriter().println(
                html.toString()
        );
    }

    // =========================================================
    // POST - SALVAR
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession sessao =
                request.getSession(false);

        if (sessao == null ||
                sessao.getAttribute("usuario") == null) {

            response.sendRedirect("login.html");
            return;
        }

        try {

            Usuario usuario =
                    (Usuario) sessao.getAttribute("usuario");

            int id =
                    usuario.getId();

            String nome =
                    valor(
                            request.getParameter("nome")
                    );

            String username =
                    valor(
                            request.getParameter("username")
                    );

            String email =
                    valor(
                            request.getParameter("email")
                    );

            String dataNascimento =
                    valor(
                            request.getParameter(
                                    "dataNascimento"
                            )
                    );

            String pais =
                    valor(
                            request.getParameter("pais")
                    );

            String plataformaFavorita =
                    valor(
                            request.getParameter(
                                    "plataformaFavorita"
                            )
                    );

            String bio =
                    valor(
                            request.getParameter("bio")
                    );

            String foto =
                    valor(
                            request.getParameter("foto")
                    );

            if (nome.isEmpty() ||
                    username.isEmpty() ||
                    email.isEmpty()) {

                response.sendRedirect(
                        "editar-perfil?erro=campos"
                );

                return;
            }

            Connection conexao =
                    Conexao.conectar();

            if (conexao == null) {

                throw new Exception(
                        "Não foi possível conectar ao banco."
                );
            }

            // =================================================
            // VERIFICAR SE USERNAME JÁ EXISTE
            // =================================================

            String sqlUsername =
                    "SELECT id FROM usuario " +
                    "WHERE username = ? " +
                    "AND id <> ?";

            PreparedStatement stmtUsername =
                    conexao.prepareStatement(
                            sqlUsername
                    );

            stmtUsername.setString(
                    1,
                    username
            );

            stmtUsername.setInt(
                    2,
                    id
            );

            ResultSet rsUsername =
                    stmtUsername.executeQuery();

            if (rsUsername.next()) {

                rsUsername.close();
                stmtUsername.close();
                conexao.close();

                response.sendRedirect(
                        "editar-perfil?erro=username"
                );

                return;
            }

            rsUsername.close();
            stmtUsername.close();

            // =================================================
            // VERIFICAR SE EMAIL JÁ EXISTE
            // =================================================

            String sqlEmail =
                    "SELECT id FROM usuario " +
                    "WHERE email = ? " +
                    "AND id <> ?";

            PreparedStatement stmtEmail =
                    conexao.prepareStatement(
                            sqlEmail
                    );

            stmtEmail.setString(
                    1,
                    email
            );

            stmtEmail.setInt(
                    2,
                    id
            );

            ResultSet rsEmail =
                    stmtEmail.executeQuery();

            if (rsEmail.next()) {

                rsEmail.close();
                stmtEmail.close();
                conexao.close();

                response.sendRedirect(
                        "editar-perfil?erro=email"
                );

                return;
            }

            rsEmail.close();
            stmtEmail.close();

            // =================================================
            // ATUALIZAR
            // =================================================

            String sql =
                    "UPDATE usuario SET " +
                    "nome = ?, " +
                    "username = ?, " +
                    "email = ?, " +
                    "data_nascimento = ?, " +
                    "pais = ?, " +
                    "plataforma_favorita = ?, " +
                    "bio = ?, " +
                    "foto = ? " +
                    "WHERE id = ?";

            PreparedStatement stmt =
                    conexao.prepareStatement(sql);

            stmt.setString(1, nome);
            stmt.setString(2, username);
            stmt.setString(3, email);
            stmt.setString(4, dataNascimento);
            stmt.setString(5, pais);
            stmt.setString(6, plataformaFavorita);
            stmt.setString(7, bio);
            stmt.setString(8, foto);
            stmt.setInt(9, id);

            stmt.executeUpdate();

            stmt.close();
            conexao.close();

            // =================================================
            // ATUALIZAR USUÁRIO DA SESSÃO
            // =================================================

            usuario.setNome(nome);
            usuario.setUsername(username);
            usuario.setEmail(email);
            usuario.setDataNascimento(
                    dataNascimento
            );
            usuario.setPais(pais);
            usuario.setPlataformaFavorita(
                    plataformaFavorita
            );
            usuario.setBio(bio);
            usuario.setFoto(foto);

            sessao.setAttribute(
                    "usuario",
                    usuario
            );

            // =================================================
            // VOLTAR PARA PERFIL
            // =================================================

            response.sendRedirect("perfil");

        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect(
                    "editar-perfil?erro=erro"
            );
        }
    }

    // =========================================================
    // VALOR
    // =========================================================

    private static String valor(String texto) {

        if (texto == null) {

            return "";
        }

        return texto.trim();
    }

    // =========================================================
    // ESCAPAR HTML
    // =========================================================

    private static String escaparHtml(String texto) {

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