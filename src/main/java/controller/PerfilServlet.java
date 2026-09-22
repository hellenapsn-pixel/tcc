package controller;

import dao.Conexao;
import dao.UsuarioDAO;
import model.Usuario;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

@WebServlet("/perfil")
public class PerfilServlet extends HttpServlet {

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

        try {

            Usuario usuarioSessao =
                    (Usuario) sessao.getAttribute("usuario");

            int idUsuario =
                    usuarioSessao.getId();

            UsuarioDAO dao =
                    new UsuarioDAO();

            Usuario usuario =
                    dao.buscarPorId(idUsuario);

            if (usuario == null) {

                response.sendRedirect("login.html");
                return;
            }

            int totalSeguidores =
                    dao.contarSeguidores(idUsuario);

            int totalSeguindo =
                    dao.contarSeguindo(idUsuario);

            List<Usuario> seguidores =
                    dao.listarSeguidores(idUsuario);

            List<Usuario> seguindo =
                    dao.listarSeguindo(idUsuario);

            List<Jogo> favoritos =
                    carregarFavoritos(idUsuario);

            List<Lista> listas =
                    carregarListas(idUsuario);

            List<String[]> avaliacoes =
                    carregarAvaliacoes(idUsuario);

            boolean especial =
                    usuario.getEmail() != null
                    &&
                    usuario.getEmail().equalsIgnoreCase(
                            "rebecarodriguesduarte2@gmail.com"
                    );

            response.setContentType(
                    "text/html;charset=UTF-8"
            );

            StringBuilder html =
                    new StringBuilder();

            // =====================================================
            // HTML
            // =====================================================

            html.append("<!DOCTYPE html>");
            html.append("<html lang='pt-BR'>");

            html.append("<head>");
            html.append(
        "<link rel='icon' " +
        "type='image/png' " +
        "href='icon.png'>"
);

            html.append("<meta charset='UTF-8'>");

            html.append(
                    "<meta name='viewport' " +
                    "content='width=device-width, initial-scale=1.0'>"
            );

            html.append(
                    "<title>Meu Perfil - Inventory</title>"
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
                    "* {" +
                    "box-sizing:border-box;" +
                    "}"
            );

            html.append(
                    "body {" +
                    "margin:0;" +
                    "background:#14101b;" +
                    "color:#fff;" +
                    "font-family:Arial,Helvetica,sans-serif;" +
                    "}"
            );

            html.append(
                    ".logo-area {" +
                    "display:flex;" +
                    "align-items:center;" +
                    "gap:9px;" +
                    "flex-shrink:0;" +
                    "}"
            );
            

            html.append(
                    ".logo-header {" +
                    "width:40px !important;" +
                    "height:40px !important;" +
                    "max-width:40px !important;" +
                    "max-height:40px !important;" +
                    "object-fit:contain !important;" +
                    "display:block !important;" +
                    "flex-shrink:0;" +
                    "}"
            );

            html.append(
                    ".logo-area h1 {" +
                    "margin:0;" +
                    "padding:0;" +
                    "font-size:30px;" +
                    "font-weight:bold;" +
                    "line-height:1;" +
                    "color:#fff;" +
                    "}"
            );

            // PÁGINA

            html.append(
                    ".perfil-page {" +
                    "max-width:1150px;" +
                    "margin:0 auto;" +
                    "padding:30px 20px 60px;" +
                    "}"
            );

            // CARD PERFIL

            html.append(
                    ".perfil-card {" +
                    "background:linear-gradient(135deg,#24102f,#14101b);" +
                    "border:1px solid #3e2849;" +
                    "border-radius:18px;" +
                    "padding:28px;" +
                    "display:flex;" +
                    "align-items:center;" +
                    "gap:25px;" +
                    "box-shadow:0 12px 35px rgba(0,0,0,.3);" +
                    "}"
            );

            // FOTO

            html.append(
                    ".perfil-foto {" +
                    "width:125px;" +
                    "height:125px;" +
                    "border-radius:50%;" +
                    "object-fit:cover;" +
                    "border:3px solid #7300d1;" +
                    "background:transparent;" +
                    "flex-shrink:0;" +
                    "}"
            );

            html.append(
                    ".perfil-info {" +
                    "flex:1;" +
                    "}"
            );

            html.append(
                    ".perfil-nome {" +
                    "font-size:31px;" +
                    "margin:0;" +
                    "font-weight:bold;" +
                    "}"
            );

            html.append(
                    ".perfil-username {" +
                    "color:#939aa3;" +
                    "margin-top:5px;" +
                    "}"
            );

            html.append(
                    ".perfil-bio {" +
                    "color:#d7d7d7;" +
                    "margin-top:14px;" +
                    "line-height:1.5;" +
                    "}"
            );

            // EMBLEMA

            html.append(
                    ".love-badge {" +
                    "display:inline-block;" +
                    "margin-top:12px;" +
                    "padding:7px 14px;" +
                    "background:#6300c0;" +
                    "border-radius:20px;" +
                    "font-size:13px;" +
                    "font-weight:bold;" +
                    "}"
            );

            // STATS

            html.append(
                    ".perfil-stats {" +
                    "display:flex;" +
                    "gap:30px;" +
                    "margin-top:19px;" +
                    "flex-wrap:wrap;" +
                    "}"
            );

            html.append(
                    ".stat-numero {" +
                    "font-size:24px;" +
                    "font-weight:bold;" +
                    "}"
            );

            html.append(
                    ".stat-texto {" +
                    "font-size:13px;" +
                    "color:#939aa2;" +
                    "margin-top:3px;" +
                    "}"
            );

            // BOTÕES

            html.append(
                    ".perfil-buttons {" +
                    "display:flex;" +
                    "gap:10px;" +
                    "margin-top:18px;" +
                    "flex-wrap:wrap;" +
                    "}"
            );

            html.append(
                    ".perfil-button {" +
                    "display:inline-block;" +
                    "padding:10px 16px;" +
                    "background:#6300c0;" +
                    "border-radius:8px;" +
                    "color:white;" +
                    "text-decoration:none;" +
                    "font-weight:bold;" +
                    "font-size:14px;" +
                    "transition:.2s;" +
                    "}"
            );

            html.append(
                    ".perfil-button:hover {" +
                    "background:#8300ed;" +
                    "}"
            );

            // GRID

            html.append(
                    ".perfil-grid {" +
                    "display:grid;" +
                    "grid-template-columns:2fr 1fr;" +
                    "gap:22px;" +
                    "margin-top:22px;" +
                    "}"
            );

            // SEÇÕES

            html.append(
                    ".secao {" +
                    "background:rgba(18,10,29,.94);" +
                    "border:1px solid #3b2250;" +
                    "border-radius:15px;" +
                    "padding:22px;" +
                    "margin-bottom:22px;" +
                    "}"
            );

            html.append(
                    ".secao-titulo {" +
                    "font-size:22px;" +
                    "font-weight:bold;" +
                    "margin:0 0 18px;" +
                    "}"
            );

            // JOGOS

            html.append(
                    ".jogos-grid {" +
                    "display:grid;" +
                    "grid-template-columns:repeat(auto-fill,minmax(145px,1fr));" +
                    "gap:16px;" +
                    "}"
            );

            html.append(
                    ".jogo-card {" +
                    "background:rgba(12,7,19,.96);" +
                    "border:1px solid #2f1a3f;" +
                    "border-radius:11px;" +
                    "overflow:hidden;" +
                    "transition:.2s;" +
                    "}"
            );

            html.append(
                    ".jogo-card:hover {" +
                    "transform:translateY(-4px);" +
                    "border-color:#7300d1;" +
                    "}"
            );

            html.append(
                    ".capa-container {" +
                    "height:215px;" +
                    "width:100%;" +
                    "overflow:hidden;" +
                    "background:transparent;" +
                    "}"
            );

            html.append(
                    ".jogo-capa {" +
                    "width:100%;" +
                    "height:215px;" +
                    "object-fit:cover;" +
                    "display:block;" +
                    "background:transparent;" +
                    "}"
            );

            html.append(
                    ".jogo-info {" +
                    "padding:11px;" +
                    "}"
            );

            html.append(
                    ".jogo-titulo {" +
                    "font-size:14px;" +
                    "font-weight:bold;" +
                    "line-height:1.35;" +
                    "}"
            );

            // AVALIACOES

            html.append(
                    ".avaliacoes-lista {" +
                    "display:flex;" +
                    "flex-direction:column;" +
                    "gap:14px;" +
                    "}"
            );

            html.append(
                    ".avaliacao-card {" +
                    "display:flex;" +
                    "gap:16px;" +
                    "background:rgba(12,7,19,.96);" +
                    "border:1px solid #2f1a3f;" +
                    "border-radius:11px;" +
                    "padding:14px;" +
                    "transition:.2s;" +
                    "}"
            );

            html.append(
                    ".avaliacao-card:hover {" +
                    "transform:translateY(-3px);" +
                    "border-color:#7300d1;" +
                    "}"
            );

            html.append(
                    ".avaliacao-capa {" +
                    "width:85px;" +
                    "height:120px;" +
                    "object-fit:cover;" +
                    "border-radius:7px;" +
                    "flex-shrink:0;" +
                    "background:#14101b;" +
                    "}"
            );

            html.append(
                    ".avaliacao-info {" +
                    "flex:1;" +
                    "min-width:0;" +
                    "}"
            );

            html.append(
                    ".avaliacao-info h3 {" +
                    "margin:0 0 7px;" +
                    "font-size:17px;" +
                    "}"
            );

            html.append(
                    ".avaliacao-estrelas {" +
                    "color:#ffd700;" +
                    "font-size:19px;" +
                    "letter-spacing:1px;" +
                    "margin-bottom:6px;" +
                    "}"
            );

            html.append(
                    ".avaliacao-nota {" +
                    "color:#c084fc;" +
                    "font-size:13px;" +
                    "font-weight:bold;" +
                    "}"
            );

            html.append(
                    ".avaliacao-horas {" +
                    "color:#8f98a2;" +
                    "font-size:12px;" +
                    "margin-top:6px;" +
                    "}"
            );

            html.append(
                    ".avaliacao-resenha {" +
                    "color:#cfd3d8;" +
                    "font-size:13px;" +
                    "line-height:1.5;" +
                    "margin-top:9px;" +
                    "}"
            );

            html.append(
                    ".botao-editar-avaliacao {" +
                    "display:inline-block;" +
                    "margin-top:10px;" +
                    "padding:7px 11px;" +
                    "background:#6300c0;" +
                    "color:white;" +
                    "border-radius:6px;" +
                    "text-decoration:none;" +
                    "font-size:12px;" +
                    "font-weight:bold;" +
                    "}"
            );

            html.append(
                    "@media(max-width:600px){" +
                    ".avaliacao-card{" +
                    "flex-direction:column;" +
                    "}" +
                    ".avaliacao-capa{" +
                    "width:110px;" +
                    "height:150px;" +
                    "margin:auto;" +
                    "}" +
                    "}"
            );

            // LISTAS

            html.append(
                    ".lista-card {" +
                    "background:rgba(12,7,19,.96);" +
                    "border:1px solid #2f1a3f;" +
                    "border-radius:11px;" +
                    "padding:16px;" +
                    "margin-bottom:14px;" +
                    "}"
            );

            html.append(
                    ".lista-nome {" +
                    "font-size:18px;" +
                    "font-weight:bold;" +
                    "margin-bottom:14px;" +
                    "}"
            );

            html.append(
                    ".lista-jogos {" +
                    "display:grid;" +
                    "grid-template-columns:repeat(auto-fill,minmax(90px,1fr));" +
                    "gap:10px;" +
                    "}"
            );

            html.append(
                    ".lista-capa {" +
                    "width:100%;" +
                    "height:130px;" +
                    "object-fit:cover;" +
                    "display:block;" +
                    "background:transparent;" +
                    "border-radius:7px;" +
                    "}"
            );

            // USUÁRIOS

            html.append(
                    ".usuario-item {" +
                    "display:flex;" +
                    "align-items:center;" +
                    "gap:11px;" +
                    "padding:10px 0;" +
                    "border-bottom:1px solid #2b1938;" +
                    "}"
            );

            html.append(
                    ".usuario-item:last-child {" +
                    "border-bottom:none;" +
                    "}"
            );

            html.append(
                    ".foto-mini {" +
                    "width:42px;" +
                    "height:42px;" +
                    "border-radius:50%;" +
                    "object-fit:cover;" +
                    "background:transparent;" +
                    "}"
            );

            html.append(
                    ".usuario-link {" +
                    "color:#fff;" +
                    "text-decoration:none;" +
                    "font-weight:bold;" +
                    "}"
            );

            // VAZIO

            html.append(
                    ".vazio {" +
                    "text-align:center;" +
                    "padding:22px 5px;" +
                    "color:#7d858d;" +
                    "}"
            );

            // RESPONSIVO

            html.append(
                    "@media(max-width:800px) {" +

                    ".perfil-card {" +
                    "flex-direction:column;" +
                    "text-align:center;" +
                    "}" +

                    ".perfil-stats {" +
                    "justify-content:center;" +
                    "}" +

                    ".perfil-buttons {" +
                    "justify-content:center;" +
                    "}" +

                    ".perfil-grid {" +
                    "grid-template-columns:1fr;" +
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
                    "<div class='logo-area'>" +
                    "<img src='icon.png' " +
                    "alt='Logo Inventory' " +
                    "class='logo-header'>" +
                    "<h1>Inventory</h1>" +
                    "</div>"
            );

            html.append("<nav>");

            html.append(
                    "<a href='index.html'>Início</a>"
            );

            html.append(
                    "<a href='jogos'>Jogos</a>"
            );

            html.append(
                    "<a href='biblioteca'>Biblioteca</a>"
            );

            html.append(
                    "<a href='buscar-usuarios'>" +
                    "Buscar usuários" +
                    "</a>"
            );

            html.append(
                    "<a href='listas'>Listas</a>"
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
            // PÁGINA
            // =====================================================

            html.append(
                    "<main class='perfil-page'>"
            );

            // =====================================================
            // PERFIL
            // =====================================================

            html.append(
                    "<section class='perfil-card'>"
            );

            String foto =
                    prepararFoto(
                            usuario.getFoto(),
                            request
                    );

            if (!foto.isEmpty()) {

                html.append(
                        "<img class='perfil-foto' " +
                        "src='" +
                        escaparHtml(foto) +
                        "' " +
                        "alt='Foto de perfil'>"
                );

            } else {

                html.append(
                        "<div class='perfil-foto' " +
                        "style='display:flex;" +
                        "align-items:center;" +
                        "justify-content:center;" +
                        "font-size:55px;'>👤</div>"
                );
            }

            html.append(
                    "<div class='perfil-info'>"
            );

            html.append(
                    "<h1 class='perfil-nome'>" +
                    escaparHtml(usuario.getNome()) +
                    "</h1>"
            );

            html.append(
                    "<div class='perfil-username'>@" +
                    escaparHtml(usuario.getNome()) +
                    "</div>"
            );

            if (usuario.getBio() != null &&
                    !usuario.getBio().trim().isEmpty()) {

                html.append(
                        "<div class='perfil-bio'>" +
                        escaparHtml(usuario.getBio()) +
                        "</div>"
                );
            }

            if (especial) {

                html.append(
                        "<span class='love-badge'>" +
                        "♡ My Love" +
                        "</span>"
                );
            }

            // STATS

            html.append(
                    "<div class='perfil-stats'>"
            );

            html.append(
                    "<div>" +
                    "<div class='stat-numero'>" +
                    totalSeguidores +
                    "</div>" +
                    "<div class='stat-texto'>Seguidores</div>" +
                    "</div>"
            );

            html.append(
                    "<div>" +
                    "<div class='stat-numero'>" +
                    totalSeguindo +
                    "</div>" +
                    "<div class='stat-texto'>Seguindo</div>" +
                    "</div>"
            );
            html.append(
                    "<div>" +
                    "<div class='stat-numero'>" +
                    favoritos.size() +
                    "</div>" +
                    "<div class='stat-texto'>Favoritos</div>" +
                    "</div>"
            );

            html.append(
                    "<div>" +
                    "<div class='stat-numero'>" +
                    listas.size() +
                    "</div>" +
                    "<div class='stat-texto'>Listas</div>" +
                    "</div>"
            );

            html.append("</div>");

            // BOTÕES

            html.append(
                    "<div class='perfil-buttons'>"
            );

            html.append(
                    "<a class='perfil-button' " +
                    "href='editar-perfil'>" +
                    "✏️ Editar perfil" +
                    "</a>"
            );

            html.append(
                    "<a class='perfil-button' " +
                    "href='listas'>" +
                    "Minhas listas" +
                    "</a>"
            );

            html.append(
                    "<a class='perfil-button' " +
                    "href='buscar-usuarios'>" +
                    "Buscar usuários" +
                    "</a>"
            );

            html.append("</div>");

            html.append("</div>");

            html.append("</section>");

            // =====================================================
            // GRID
            // =====================================================

            html.append(
                    "<div class='perfil-grid'>"
            );

            // =====================================================
            // COLUNA ESQUERDA
            // =====================================================

            html.append("<div>");

            // FAVORITOS

            html.append(
                    "<section class='secao'>"
            );

            html.append(
                    "<h2 class='secao-titulo'>" +
                    "❤️ Favoritos" +
                    "</h2>"
            );

            if (favoritos.isEmpty()) {

                html.append(
                        "<div class='vazio'>" +
                        "Nenhum jogo favorito ainda." +
                        "</div>"
                );

            } else {

                html.append(
                        "<div class='jogos-grid'>"
                );

                for (Jogo jogo :
                        favoritos) {

                    html.append(
                            montarCardJogo(
                                    jogo,
                                    request
                            )
                    );
                }

                html.append("</div>");
            }

            html.append("</section>");

            // =====================================================
            // AVALIACOES
            // =====================================================

            html.append(
                    "<section class='secao'>"
            );

            html.append(
                    "<h2 class='secao-titulo'>" +
                    "⭐ Minhas avaliações" +
                    "</h2>"
            );

            if (avaliacoes.isEmpty()) {

                html.append(
                        "<div class='vazio'>" +
                        "Você ainda não avaliou nenhum jogo." +
                        "</div>"
                );

            } else {

                html.append(
                        "<div class='avaliacoes-lista'>"
                );

                for (String[] avaliacao :
                        avaliacoes) {

                    int idJogo =
                            Integer.parseInt(
                                    avaliacao[0]
                            );

                    String titulo =
                            avaliacao[1];

                    double nota = 0;

                    try {
                        nota =
                                Double.parseDouble(
                                        avaliacao[3]
                                );
                    } catch (Exception erro) {
                        nota = 0;
                    }

                    int estrelas =
                            (int) Math.round(nota);

                    if (estrelas < 0) {
                        estrelas = 0;
                    }

                    if (estrelas > 5) {
                        estrelas = 5;
                    }

                    String capa =
                            request.getContextPath()
                            + "/capa?id="
                            + idJogo;

                    String comentario =
                            avaliacao[4];

                    String horas =
                            avaliacao[5];

                    html.append(
                            "<div class='avaliacao-card'>"
                    );

                    html.append(
                            "<img " +
                            "class='avaliacao-capa' " +
                            "src='" +
                            escaparHtml(capa) +
                            "' " +
                            "alt='Capa de " +
                            escaparHtml(titulo) +
                            "' " +
                            "onerror='this.style.display=\"none\";'>"
                    );

                    html.append(
                            "<div class='avaliacao-info'>"
                    );

                    html.append(
                            "<h3>" +
                            escaparHtml(titulo) +
                            "</h3>"
                    );

                    html.append(
                            "<div class='avaliacao-estrelas'>"
                    );

                    for (int i = 1; i <= 5; i++) {

                        if (i <= estrelas) {
                            html.append("★");
                        } else {
                            html.append("☆");
                        }
                    }

                    html.append(
                            "</div>"
                    );

                    html.append(
                            "<div class='avaliacao-nota'>" +
                            "Nota: " +
                            nota +
                            "/5" +
                            "</div>"
                    );

                    if (horas != null &&
                            !horas.trim().isEmpty() &&
                            !horas.equals("0") &&
                            !horas.equals("0.0")) {

                        html.append(
                                "<div class='avaliacao-horas'>" +
                                "⏱️ " +
                                escaparHtml(horas) +
                                " horas jogadas" +
                                "</div>"
                        );
                    }

                    if (comentario != null &&
                            !comentario.trim().isEmpty()) {

                        html.append(
                                "<div class='avaliacao-resenha'>" +
                                escaparHtml(comentario) +
                                "</div>"
                        );
                    }

                    html.append(
                            "<a class='botao-editar-avaliacao' " +
                            "href='avaliar?id=" +
                            idJogo +
                            "'>" +
                            "✏️ Editar avaliação" +
                            "</a>"
                    );

                    html.append(
                            "</div>"
                    );

                    html.append(
                            "</div>"
                    );
                }

                html.append(
                        "</div>"
                );
            }

            html.append(
                    "</section>"
            );

            // LISTAS

            html.append(
                    "<section class='secao'>"
            );

            html.append(
                    "<h2 class='secao-titulo'>" +
                    "📚 Minhas listas" +
                    "</h2>"
            );

            if (listas.isEmpty()) {

                html.append(
                        "<div class='vazio'>" +
                        "Nenhuma lista criada ainda." +
                        "</div>"
                );

            } else {

                for (Lista lista :
                        listas) {

                    html.append(
                            "<div class='lista-card'>"
                    );

                    html.append(
                            "<div class='lista-nome'>" +
                            escaparHtml(lista.nome) +
                            "</div>"
                    );

                    if (lista.jogos.isEmpty()) {

                        html.append(
                                "<div class='vazio'>" +
                                "Esta lista está vazia." +
                                "</div>"
                        );

                    } else {

                        html.append(
                                "<div class='lista-jogos'>"
                        );

                        for (Jogo jogo :
                                lista.jogos) {

                            String capa =
                                    request.getContextPath()
                                    + "/capa?id="
                                    + jogo.id;

                            html.append(
                                    "<img " +
                                    "class='lista-capa' " +
                                    "src='" +
                                    escaparHtml(capa) +
                                    "' " +
                                    "alt='Capa de " +
                                    escaparHtml(jogo.titulo) +
                                    "' " +
                                    "onerror='this.style.display=\"none\";'>"
                            );
                        }

                        html.append("</div>");
                    }

                    html.append("</div>");
                }
            }

            html.append("</section>");

            html.append("</div>");

            // =====================================================
            // COLUNA DIREITA
            // =====================================================

            html.append("<div>");

            // SEGUIDORES

            html.append(
                    "<section class='secao'>"
            );

            html.append(
                    "<h2 class='secao-titulo'>" +
                    "👥 Seguidores" +
                    "</h2>"
            );

            if (seguidores.isEmpty()) {

                html.append(
                        "<div class='vazio'>" +
                        "Nenhum seguidor ainda." +
                        "</div>"
                );

            } else {

                for (Usuario u :
                        seguidores) {

                    html.append(
                            montarUsuario(
                                    u,
                                    request
                            )
                    );
                }
            }

            html.append("</section>");

            // SEGUINDO

            html.append(
                    "<section class='secao'>"
            );

            html.append(
                    "<h2 class='secao-titulo'>" +
                    "➕ Seguindo" +
                    "</h2>"
            );

            if (seguindo.isEmpty()) {

                html.append(
                        "<div class='vazio'>" +
                        "Você ainda não segue ninguém." +
                        "</div>"
                );

            } else {

                for (Usuario u :
                        seguindo) {

                    html.append(
                            montarUsuario(
                                    u,
                                    request
                            )
                    );
                }
            }

            html.append("</section>");

            html.append("</div>");

            html.append("</div>");

            html.append("</main>");

            html.append("</body>");

            html.append("</html>");

            response.getWriter().println(
                    html.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect("index.html");
        }
    }

    // =========================================================
    // AVALIACOES
    // =========================================================

    private List<String[]> carregarAvaliacoes(
            int idUsuario) {

        List<String[]> avaliacoes =
                new ArrayList<>();

        Connection conexao = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {

            conexao =
                    Conexao.conectar();

            String sql =
                    "SELECT " +
                    "a.id_jogo, " +
                    "j.titulo, " +
                    "j.capa, " +
                    "a.nota, " +
                    "a.comentario, " +
                    "a.horas_jogadas " +
                    "FROM avaliacao a " +
                    "INNER JOIN jogo j " +
                    "ON a.id_jogo = j.id " +
                    "WHERE a.id_usuario = ? " +
                    "ORDER BY a.data_avaliacao DESC";

            stmt =
                    conexao.prepareStatement(sql);

            stmt.setInt(1, idUsuario);

            rs =
                    stmt.executeQuery();

            while (rs.next()) {

                avaliacoes.add(
                        new String[]{
                                String.valueOf(
                                        rs.getInt("id_jogo")
                                ),
                                rs.getString("titulo"),
                                rs.getString("capa"),
                                String.valueOf(
                                        rs.getDouble("nota")
                                ),
                                rs.getString("comentario"),
                                String.valueOf(
                                        rs.getDouble("horas_jogadas")
                                )
                        }
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            try {
                if (rs != null) rs.close();
            } catch (Exception ignored) {
            }

            try {
                if (stmt != null) stmt.close();
            } catch (Exception ignored) {
            }

            try {
                if (conexao != null) conexao.close();
            } catch (Exception ignored) {
            }
        }

        return avaliacoes;
    }

    // =========================================================
    // FAVORITOS
    // =========================================================

    private List<Jogo> carregarFavoritos(
            int idUsuario)
            throws Exception {

        List<Jogo> jogos =
                new ArrayList<>();

        Connection conexao =
                Conexao.conectar();

        PreparedStatement stmt =
                conexao.prepareStatement(
                        "SELECT j.id,j.titulo,j.capa " +
                        "FROM favorito f " +
                        "INNER JOIN jogo j " +
                        "ON j.id=f.id_jogo " +
                        "WHERE f.id_usuario=? " +
                        "ORDER BY f.id DESC"
                );

        stmt.setInt(1, idUsuario);

        ResultSet rs =
                stmt.executeQuery();

        while (rs.next()) {

            Jogo jogo =
                    new Jogo();

            jogo.id =
                    rs.getInt("id");

            jogo.titulo =
                    rs.getString("titulo");

            jogo.capa =
                    rs.getString("capa");

            jogos.add(jogo);
        }

        rs.close();
        stmt.close();
        conexao.close();

        return jogos;
    }

    // =========================================================
    // LISTAS
    // =========================================================

    private List<Lista> carregarListas(
            int idUsuario)
            throws Exception {

        List<Lista> listas =
                new ArrayList<>();

        Connection conexao =
                Conexao.conectar();

        PreparedStatement stmt =
                conexao.prepareStatement(
                        "SELECT id,nome " +
                        "FROM lista " +
                        "WHERE id_usuario=? " +
                        "ORDER BY id DESC"
                );

        stmt.setInt(1, idUsuario);

        ResultSet rs =
                stmt.executeQuery();

        while (rs.next()) {

            Lista lista =
                    new Lista();

            lista.id =
                    rs.getInt("id");

            lista.nome =
                    rs.getString("nome");

            lista.jogos =
                    carregarJogosLista(
                            lista.id
                    );

            listas.add(lista);
        }

        rs.close();
        stmt.close();
        conexao.close();

        return listas;
    }

    // =========================================================
    // JOGOS DA LISTA
    // =========================================================

    private List<Jogo> carregarJogosLista(
            int idLista)
            throws Exception {

        List<Jogo> jogos =
                new ArrayList<>();

        Connection conexao =
                Conexao.conectar();

        PreparedStatement stmt =
                conexao.prepareStatement(
                        "SELECT j.id,j.titulo,j.capa " +
                        "FROM lista_jogo lj " +
                        "INNER JOIN jogo j " +
                        "ON j.id=lj.id_jogo " +
                        "WHERE lj.id_lista=? " +
                        "ORDER BY lj.id ASC"
                );

        stmt.setInt(1, idLista);

        ResultSet rs =
                stmt.executeQuery();

        while (rs.next()) {

            Jogo jogo =
                    new Jogo();

            jogo.id =
                    rs.getInt("id");

            jogo.titulo =
                    rs.getString("titulo");

            jogo.capa =
                    rs.getString("capa");

            jogos.add(jogo);
        }

        rs.close();
        stmt.close();
        conexao.close();

        return jogos;
    }

    // =========================================================
    // CARD FAVORITO
    // =========================================================

    private String montarCardJogo(
            Jogo jogo,
            HttpServletRequest request) {

        StringBuilder html =
                new StringBuilder();

        String capa =
                request.getContextPath()
                + "/capa?id="
                + jogo.id;

        html.append(
                "<div class='jogo-card'>"
        );

        html.append(
                "<div class='capa-container'>"
        );

        html.append(
                "<img " +
                "class='jogo-capa' " +
                "src='" +
                escaparHtml(capa) +
                "' " +
                "alt='Capa de " +
                escaparHtml(jogo.titulo) +
                "' " +
                "onerror='this.style.display=\"none\";'>"
        );

        html.append("</div>");

        html.append(
                "<div class='jogo-info'>"
        );

        html.append(
                "<div class='jogo-titulo'>" +
                escaparHtml(jogo.titulo) +
                "</div>"
        );

        html.append("</div>");

        html.append("</div>");

        return html.toString();
    }

    // =========================================================
    // USUARIO
    // =========================================================

    private String montarUsuario(
            Usuario usuario,
            HttpServletRequest request) {

        StringBuilder html =
                new StringBuilder();

        String foto =
                prepararFoto(
                        usuario.getFoto(),
                        request
                );

        html.append(
                "<div class='usuario-item'>"
        );

        if (!foto.isEmpty()) {

            html.append(
                    "<img " +
                    "class='foto-mini' " +
                    "src='" +
                    escaparHtml(foto) +
                    "' " +
                    "alt='Foto'>"
            );

        } else {

            html.append(
                    "<div class='foto-mini' " +
                    "style='display:flex;" +
                    "align-items:center;" +
                    "justify-content:center;" +
                    "font-size:21px;'>👤</div>"
            );
        }

        html.append(
                "<a class='usuario-link' " +
                "href='perfil-usuario?id=" +
                usuario.getId() +
                "'>" +
                escaparHtml(usuario.getNome()) +
                "</a>"
        );

        html.append("</div>");

        return html.toString();
    }

    // =========================================================
    // FOTO
    // =========================================================

    private String prepararFoto(
            String foto,
            HttpServletRequest request) {

        if (foto == null ||
                foto.trim().isEmpty()) {

            return "";
        }

        foto =
                foto.trim();

        if (foto.startsWith("http://") ||
                foto.startsWith("https://")) {

            return foto;
        }

        if (foto.startsWith("/")) {

            return
                    request.getContextPath()
                    + foto;
        }

        return
                request.getContextPath()
                + "/foto-perfil?arquivo="
                + foto;
    }

    // =========================================================
    // ESCAPAR HTML
    // =========================================================

    private String escaparHtml(
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

    // =========================================================
    // CLASSES
    // =========================================================

    private static class Jogo {

        int id;

        String titulo;

        String capa;
    }

    private static class Lista {

        int id;

        String nome;

        List<Jogo> jogos =
                new ArrayList<>();
    }
}