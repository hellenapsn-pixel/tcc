package controller;

import dao.Conexao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/jogos")
public class JogosServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("=================================");
        System.out.println("JOGOS SERVLET FOI CHAMADO");
        System.out.println("URL: " + request.getRequestURI());
        System.out.println("=================================");

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        // =====================================================
        // FILTRO
        // =====================================================

        String generoFiltro =
                request.getParameter("genero");

        if (generoFiltro == null) {
            generoFiltro = "";
        }

        generoFiltro =
                generoFiltro.trim();

        System.out.println(
                "FILTRO DE GENERO: [" + generoFiltro + "]"
        );

        // =====================================================
        // USUARIO LOGADO
        // =====================================================

        HttpSession sessao =
                request.getSession(false);

        int idUsuario = -1;

        if (sessao != null &&
                sessao.getAttribute("usuario") != null) {

            try {

                model.Usuario usuario =
                        (model.Usuario)
                        sessao.getAttribute("usuario");

                idUsuario =
                        usuario.getId();

                System.out.println(
                        "USUARIO LOGADO ID: "
                        + idUsuario
                );

            } catch (Exception e) {

                System.out.println(
                        "ERRO AO PEGAR USUARIO DA SESSAO"
                );

                e.printStackTrace();

                idUsuario = -1;
            }

        } else {

            System.out.println(
                    "NENHUM USUARIO LOGADO"
            );
        }

        // =====================================================
        // HTML
        // =====================================================

        StringBuilder html =
                new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='pt-BR'>");

        html.append("<head>");

        html.append(
                "<meta charset='UTF-8'>"
        );

        html.append(
                "<meta name='viewport' "
                + "content='width=device-width, "
                + "initial-scale=1.0'>"
        );

        html.append(
                "<title>Jogos - Inventory</title>"
        );

        html.append(
                "<link rel='stylesheet' "
                + "href='style.css'>"
        );

        // =====================================================
        // CSS
        // =====================================================

        html.append("<style>");

        html.append(
                "body{"
                + "background:"
                + "linear-gradient("
                + "135deg,#0d0714,#160b24,#0d0714"
                + ");"
                + "min-height:100vh;"
                + "}"
        );

        html.append(
                ".jogos-container{"
                + "max-width:1200px;"
                + "margin:45px auto;"
                + "padding:20px;"
                + "}"
        );

        html.append(
                ".titulo-jogos{"
                + "text-align:center;"
                + "margin-bottom:10px;"
                + "font-size:38px;"
                + "font-weight:bold;"
                + "color:#c084fc;"
                + "}"
        );

        html.append(
                ".subtitulo-jogos{"
                + "text-align:center;"
                + "color:#aaa;"
                + "font-size:16px;"
                + "margin-bottom:30px;"
                + "}"
        );

        html.append(
                ".filtro-genero{"
                + "display:flex;"
                + "justify-content:center;"
                + "align-items:center;"
                + "gap:12px;"
                + "margin-bottom:35px;"
                + "flex-wrap:wrap;"
                + "}"
        );

        html.append(
                ".filtro-genero label{"
                + "color:#ddd;"
                + "font-weight:bold;"
                + "font-size:16px;"
                + "}"
        );

        html.append(
                ".filtro-genero select{"
                + "background:#21152d;"
                + "color:#fff;"
                + "border:1px solid #7c3aed;"
                + "border-radius:9px;"
                + "padding:11px 16px;"
                + "font-size:15px;"
                + "cursor:pointer;"
                + "outline:none;"
                + "}"
        );

        // =====================================================
        // GRID
        // =====================================================

        html.append(
                ".catalogo-jogos{"
                + "display:grid;"
                + "grid-template-columns:"
                + "repeat(auto-fill,minmax(210px,1fr));"
                + "gap:28px;"
                + "}"
        );

        // =====================================================
        // CARD
        // =====================================================

        html.append(
                ".card-jogo{"
                + "position:relative;"
                + "background:"
                + "linear-gradient("
                + "145deg,#21152d,#17101f"
                + ");"
                + "border:1px solid #38204d;"
                + "padding:12px;"
                + "border-radius:16px;"
                + "text-align:center;"
                + "overflow:hidden;"
                + "transition:.3s;"
                + "box-shadow:"
                + "0 8px 25px rgba(0,0,0,.35);"
                + "}"
        );

        html.append(
                ".card-jogo:hover{"
                + "transform:translateY(-8px);"
                + "border-color:#8b5cf6;"
                + "box-shadow:"
                + "0 15px 35px rgba(124,58,237,.35);"
                + "}"
        );

        // =====================================================
        // CAPA
        // =====================================================

        html.append(
                ".capa-jogo{"
                + "width:100%;"
                + "height:285px;"
                + "object-fit:cover;"
                + "border-radius:12px;"
                + "display:block;"
                + "background:#120d18;"
                + "}"
        );

        html.append(
                ".sem-capa{"
                + "width:100%;"
                + "height:285px;"
                + "display:flex;"
                + "align-items:center;"
                + "justify-content:center;"
                + "text-align:center;"
                + "padding:20px;"
                + "box-sizing:border-box;"
                + "background:"
                + "linear-gradient("
                + "135deg,#21152d,#54227d"
                + ");"
                + "border-radius:12px;"
                + "color:#fff;"
                + "font-size:18px;"
                + "font-weight:bold;"
                + "}"
        );

        html.append(
                ".sem-capa span{"
                + "max-width:180px;"
                + "line-height:1.4;"
                + "}"
        );

        // =====================================================
        // TITULO
        // =====================================================

        html.append(
                ".card-jogo h3{"
                + "font-size:18px;"
                + "margin:15px 5px 8px;"
                + "color:#fff;"
                + "min-height:44px;"
                + "}"
        );

        // =====================================================
        // INFORMACOES
        // =====================================================

        html.append(
                ".info-jogo{"
                + "display:flex;"
                + "justify-content:center;"
                + "flex-wrap:wrap;"
                + "gap:7px;"
                + "margin-bottom:12px;"
                + "}"
        );

        html.append(
                ".tag-jogo{"
                + "background:#2d183e;"
                + "border:1px solid #4c2670;"
                + "color:#c084fc;"
                + "padding:5px 9px;"
                + "border-radius:20px;"
                + "font-size:12px;"
                + "}"
        );

        // =====================================================
        // FAVORITO
        // =====================================================

        html.append(
                ".botao-favorito{"
                + "width:100%;"
                + "padding:11px;"
                + "margin-top:10px;"
                + "border:1px solid #8b5cf6;"
                + "border-radius:9px;"
                + "background:#241434;"
                + "color:#f3e8ff;"
                + "font-weight:bold;"
                + "font-size:14px;"
                + "cursor:pointer;"
                + "transition:.25s;"
                + "}"
        );

        html.append(
                ".botao-favorito:hover{"
                + "background:#6d28d9;"
                + "transform:scale(1.02);"
                + "}"
        );

        html.append(
                ".botao-favorito.ativo{"
                + "background:"
                + "linear-gradient("
                + "135deg,#7c3aed,#a855f7"
                + ");"
                + "color:#fff;"
                + "}"
        );

        // =====================================================
        // BIBLIOTECA
        // =====================================================

        html.append(
                ".botao-biblioteca{"
                + "display:block;"
                + "margin-top:10px;"
                + "padding:12px;"
                + "background:"
                + "linear-gradient("
                + "135deg,#7c3aed,#9333ea"
                + ");"
                + "color:white;"
                + "text-decoration:none;"
                + "border-radius:9px;"
                + "font-weight:bold;"
                + "transition:.25s;"
                + "}"
        );

        html.append(
                ".botao-biblioteca:hover{"
                + "transform:scale(1.03);"
                + "}"
        );

        // =====================================================
        // BRILHO
        // =====================================================

        html.append(
                ".brilho-card{"
                + "position:absolute;"
                + "width:100px;"
                + "height:100px;"
                + "background:#9333ea;"
                + "filter:blur(70px);"
                + "opacity:.15;"
                + "top:-40px;"
                + "right:-40px;"
                + "pointer-events:none;"
                + "}"
        );

        // =====================================================
        // NENHUM JOGO
        // =====================================================

        html.append(
                ".nenhum-jogo{"
                + "grid-column:1/-1;"
                + "text-align:center;"
                + "padding:50px;"
                + "background:#17101f;"
                + "border:1px solid #38204d;"
                + "border-radius:15px;"
                + "color:#aaa;"
                + "}"
        );

        // =====================================================
        // RESPONSIVO
        // =====================================================

        html.append(
                "@media(max-width:600px){"
                + ".jogos-container{"
                + "margin:20px auto;"
                + "padding:12px;"
                + "}"
                + ".titulo-jogos{"
                + "font-size:30px;"
                + "}"
                + ".catalogo-jogos{"
                + "grid-template-columns:repeat(2,1fr);"
                + "gap:15px;"
                + "}"
                + ".capa-jogo,.sem-capa{"
                + "height:220px;"
                + "}"
                + ".card-jogo{"
                + "padding:9px;"
                + "}"
                + "}"
        );

        html.append("</style>");

        // =====================================================
        // JAVASCRIPT
        // =====================================================

        html.append("<script>");

        html.append(
                "function tentarOutraCapa(img){"
                + "var src=img.getAttribute('src')||'';"
                + "var match=src.match(/steam\\\\/apps\\\\/(\\\\d+)/);"
                + "if(!match){img.style.display='none';return;}"
                + "var id=match[1];"
                + "var tentativas=parseInt(img.getAttribute('data-tentativas')||'0',10);"
                + "var urls=["
                + "'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/'+id+'/library_600x900_2x.jpg',"
                + "'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/'+id+'/library_600x900.jpg',"
                + "'https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/'+id+'/header.jpg',"
                + "'https://cdn.akamai.steamstatic.com/steam/apps/'+id+'/library_600x900_2x.jpg',"
                + "'https://cdn.akamai.steamstatic.com/steam/apps/'+id+'/library_600x900.jpg',"
                + "'https://cdn.akamai.steamstatic.com/steam/apps/'+id+'/header.jpg'"
                + "];"
                + "if(tentativas<urls.length){"
                + "img.setAttribute('data-tentativas',tentativas+1);"
                + "img.src=urls[tentativas];"
                + "}else{"
                + "img.style.display='none';"
                + "}"
                + "}"
        );

        html.append("</script>");

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
                "<a href='index.html'>Início</a>"
        );

        html.append(
                "<a href='jogos'>Jogos</a>"
        );

        html.append(
                "<a href='biblioteca'>Biblioteca</a>"
        );

        html.append(
                "<a href='buscar-usuarios'>"
                + "Buscar usuários"
                + "</a>"
        );

        html.append(
                "<a href='listas'>"
                + "Listas"
                + "</a>"
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
        // CONTEUDO
        // =====================================================

        html.append(
                "<main class='jogos-container'>"
        );

        html.append(
                "<h2 class='titulo-jogos'>"
                + "Explore os Jogos"
                + "</h2>"
        );

        html.append(
                "<p class='subtitulo-jogos'>"
                + "Descubra novos jogos, filtre por gênero "
                + "e escolha seus favoritos."
                + "</p>"
        );

        // =====================================================
        // FILTRO
        // =====================================================

        html.append(
                "<form method='GET' "
                + "action='jogos' "
                + "class='filtro-genero'>"
        );

        html.append(
                "<label for='genero'>"
                + "Filtrar por gênero:"
                + "</label>"
        );

        html.append(
                "<select id='genero' "
                + "name='genero' "
                + "onchange='this.form.submit()'>"
        );

        html.append(
                "<option value=''>"
                + "Todos os gêneros"
                + "</option>"
        );

        String[] generos = {

            "Ação",
            "Aventura",
            "RPG",
            "Terror",
            "Tiro",
            "Estratégia",
            "Corrida",
            "Esporte",
            "Simulação",
            "Plataforma"

        };

        for (String genero : generos) {

            String selecionado =
                    generoFiltro.equalsIgnoreCase(
                            genero
                    )
                    ? " selected"
                    : "";

            html.append(
                    "<option value='"
                    + escapar(genero)
                    + "'"
                    + selecionado
                    + ">"
                    + escapar(genero)
                    + "</option>"
            );
        }

        html.append("</select>");

        if (!generoFiltro.isEmpty()) {

            html.append(
                    "<a href='jogos' "
                    + "style='"
                    + "padding:10px 14px;"
                    + "background:#2d183e;"
                    + "color:#ddd;"
                    + "border:1px solid #4c2670;"
                    + "border-radius:8px;"
                    + "text-decoration:none;"
                    + "'>"
                    + "Limpar"
                    + "</a>"
            );
        }

        html.append("</form>");

        // =====================================================
        // GRID
        // =====================================================

        html.append(
                "<div class='catalogo-jogos'>"
        );

        // =====================================================
        // BANCO
        // =====================================================

        System.out.println(
                "TENTANDO CONECTAR AO SQLITE..."
        );

        try {

            Connection conexao =
                    Conexao.conectar();

            System.out.println(
                    "CONEXAO RETORNOU: "
                    + conexao
            );

            if (conexao == null) {

                System.out.println(
                        "ERRO: CONEXAO COM SQLITE RETORNOU NULL"
                );

                html.append(
                        "<div class='nenhum-jogo'>"
                        + "Erro ao conectar ao banco."
                        + "</div>"
                );

            } else {

                System.out.println(
                        "CONEXAO COM SQLITE REALIZADA!"
                );

                String sql;

                if (!generoFiltro.isEmpty()) {

                    sql =
                            "SELECT "
                            + "id, "
                            + "titulo, "
                            + "genero, "
                            + "plataforma, "
                            + "ano_lancamento, "
                            + "capa "
                            + "FROM jogo "
                            + "WHERE genero LIKE ? "
                            + "ORDER BY titulo";

                } else {

                    sql =
                            "SELECT "
                            + "id, "
                            + "titulo, "
                            + "genero, "
                            + "plataforma, "
                            + "ano_lancamento, "
                            + "capa "
                            + "FROM jogo "
                            + "ORDER BY titulo";
                }

                System.out.println(
                        "SQL DOS JOGOS:"
                );

                System.out.println(
                        sql
                );

                PreparedStatement stmt =
                        conexao.prepareStatement(
                                sql
                        );

                System.out.println(
                        "PREPARED STATEMENT CRIADO!"
                );

                if (!generoFiltro.isEmpty()) {

                    stmt.setString(
                            1,
                            "%"
                            + generoFiltro
                            + "%"
                    );
                }

                System.out.println(
                        "EXECUTANDO SQL..."
                );

                ResultSet resultado =
                        stmt.executeQuery();

                System.out.println(
                        "SQL EXECUTADO COM SUCESSO!"
                );

                int quantidadeJogos = 0;

                while (resultado.next()) {

                    quantidadeJogos++;

                    int id =
                            resultado.getInt("id");

                    String titulo =
                            resultado.getString("titulo");

                    String genero =
                            resultado.getString("genero");

                    String plataforma =
                            resultado.getString("plataforma");

                    String ano =
                            resultado.getString(
                                    "ano_lancamento"
                            );

                    String capa =
                            resultado.getString("capa");

                    // =================================================
                    // FAVORITO
                    // =================================================

                    boolean favorito = false;

                    if (idUsuario != -1) {

                        try {

                            PreparedStatement stmtFavorito =
                                    conexao.prepareStatement(
                                            "SELECT id "
                                            + "FROM favorito "
                                            + "WHERE id_usuario = ? "
                                            + "AND id_jogo = ?"
                                    );

                            stmtFavorito.setInt(
                                    1,
                                    idUsuario
                            );

                            stmtFavorito.setInt(
                                    2,
                                    id
                            );

                            ResultSet rsFavorito =
                                    stmtFavorito.executeQuery();

                            favorito =
                                    rsFavorito.next();

                            rsFavorito.close();
                            stmtFavorito.close();

                        } catch (Exception erroFavorito) {

                            System.out.println(
                                    "ERRO AO VERIFICAR FAVORITO:"
                            );

                            erroFavorito.printStackTrace();

                            favorito = false;
                        }
                    }

                    // =================================================
                    // CARD
                    // =================================================

                    html.append(
                            "<article "
                            + "class='card-jogo'>"
                    );

                    html.append(
                            "<div "
                            + "class='brilho-card'>"
                            + "</div>"
                    );

                    // =================================================
                    // CAPA
                    // =================================================

                    String caminhoCapa =
                            request.getContextPath()
                            + "/capa?id="
                            + id;

                    html.append(
                            "<img "
                            + "class='capa-jogo' "
                            + "src='"
                            + escapar(caminhoCapa)
                            + "' "
                            + "alt='Capa de "
                            + escapar(titulo)
                            + "' "
                            + "onerror=\""
                            + "this.style.display='none';"
                            + "this.nextElementSibling"
                            + ".style.display='flex';"
                            + "\""
                            + ">"
                    );

                    html.append(
                            "<div "
                            + "class='sem-capa' "
                            + "style='display:none;'>"
                            + "<span>"
                            + escapar(titulo)
                            + "</span>"
                            + "</div>"
                    );

                    // =================================================
                    // TITULO
                    // =================================================

                    html.append(
                            "<h3>"
                            + escapar(titulo)
                            + "</h3>"
                    );

                    // =================================================
                    // INFORMACOES
                    // =================================================

                    html.append(
                            "<div class='info-jogo'>"
                    );

                    if (genero != null &&
                            !genero.trim().isEmpty()) {

                        html.append(
                                "<span class='tag-jogo'>"
                                + escapar(genero)
                                + "</span>"
                        );
                    }

                    if (plataforma != null &&
                            !plataforma.trim().isEmpty()) {

                        html.append(
                                "<span class='tag-jogo'>"
                                + escapar(plataforma)
                                + "</span>"
                        );
                    }

                    if (ano != null &&
                            !ano.trim().isEmpty()) {

                        html.append(
                                "<span class='tag-jogo'>"
                                + escapar(ano)
                                + "</span>"
                        );
                    }

                    html.append(
                            "</div>"
                    );

                    // =================================================
                    // FAVORITO
                    // =================================================

                    if (idUsuario != -1) {

                        String classe =
                                favorito
                                ? "botao-favorito ativo"
                                : "botao-favorito";

                        String texto =
                                favorito
                                ? "★ Favorito"
                                : "☆ Favoritar";

                        html.append(
                                "<form "
                                + "method='POST' "
                                + "action='favorito'>"
                        );

                        html.append(
                                "<input "
                                + "type='hidden' "
                                + "name='idJogo' "
                                + "value='"
                                + id
                                + "'>"
                        );

                        html.append(
                                "<button "
                                + "type='submit' "
                                + "class='"
                                + classe
                                + "'>"
                                + texto
                                + "</button>"
                        );

                        html.append(
                                "</form>"
                        );

                    } else {

                        html.append(
                                "<a "
                                + "class='botao-favorito' "
                                + "href='login.html'>"
                                + "☆ Favoritar"
                                + "</a>"
                        );
                    }

                    // =================================================
                    // BIBLIOTECA
                    // =================================================

                    if (idUsuario != -1) {

                        html.append(
                                "<a "
                                + "class='botao-biblioteca' "
                                + "href='adicionar-biblioteca?id="
                                + id
                                + "'>"
                                + "+ Minha biblioteca"
                                + "</a>"
                        );

                    } else {

                        html.append(
                                "<a "
                                + "class='botao-biblioteca' "
                                + "href='login.html'>"
                                + "+ Minha biblioteca"
                                + "</a>"
                        );
                    }

                    html.append(
                            "</article>"
                    );
                }

                System.out.println(
                        "QUANTIDADE DE JOGOS: "
                        + quantidadeJogos
                );

                if (quantidadeJogos == 0) {

                    html.append(
                            "<div "
                            + "class='nenhum-jogo'>"
                            + "Nenhum jogo encontrado."
                            + "</div>"
                    );
                }

                resultado.close();
                stmt.close();
                conexao.close();

                System.out.println(
                        "CONEXAO COM SQLITE FECHADA."
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "ERRO NO JOGOSSERVLET"
            );

            System.out.println(
                    "================================="
            );

            e.printStackTrace();

            html.append(
                    "<div "
                    + "class='nenhum-jogo'>"
                    + "Erro ao carregar os jogos."
                    + "</div>"
            );
        }

        html.append(
                "</div>"
        );

        html.append(
                "</main>"
        );

        html.append(
                "</body>"
        );

        html.append(
                "</html>"
        );

        response.getWriter().println(
                html.toString()
        );

        System.out.println(
                "JOGOS SERVLET FINALIZADO"
        );

        System.out.println(
                "================================="
        );
    }

    // =====================================================
    // PREPARAR CAPA
    // =====================================================

    private String prepararCapa(
            HttpServletRequest request,
            String capa) {

        if (capa == null ||
                capa.trim().isEmpty()) {

            return null;
        }

        String caminho =
                capa.trim();

        // =================================================
        // MARKDOWN
        // =================================================

        if (caminho.startsWith("[")
                &&
                caminho.contains("](")
                &&
                caminho.endsWith(")")) {

            int inicio =
                    caminho.indexOf("](");

            caminho =
                    caminho.substring(
                            inicio + 2,
                            caminho.length() - 1
                    );
        }

        // =================================================
        // STEAM APP ID
        // =================================================

        if (caminho.matches("\\d+")) {

            return
                    "https://shared.cloudflare.steamstatic.com/"
                    + "store_item_assets/steam/apps/"
                    + caminho
                    + "/library_600x900_2x.jpg";
        }

        // =================================================
        // /apps/ID
        // =================================================

        Pattern pattern =
                Pattern.compile(
                        "/apps/(\\d+)"
                );

        Matcher matcher =
                pattern.matcher(
                        caminho
                );

        if (matcher.find()) {

            String appId =
                    matcher.group(1);

            return
                    "https://shared.cloudflare.steamstatic.com/"
                    + "store_item_assets/steam/apps/"
                    + appId
                    + "/library_600x900_2x.jpg";
        }

        // =================================================
        // URL
        // =================================================

        if (caminho.startsWith("http://")
                ||
                caminho.startsWith("https://")) {

            return caminho;
        }

        // =================================================
        // LOCAL
        // =================================================

        while (
                caminho.startsWith("/")
        ) {

            caminho =
                    caminho.substring(1);
        }

        return
                request.getContextPath()
                + "/"
                + caminho;
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
                .replace(
                        "&",
                        "&amp;"
                )
                .replace(
                        "<",
                        "&lt;"
                )
                .replace(
                        ">",
                        "&gt;"
                )
                .replace(
                        "\"",
                        "&quot;"
                )
                .replace(
                        "'",
                        "&#39;"
                );
    }
}