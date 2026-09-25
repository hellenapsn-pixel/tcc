package controller;

import dao.Conexao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/jogos")
public class JogosServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int TOTAL_JOGOS = 500;

    /*
     * 10 gêneros
     */
    private static final String[][] GENEROS = {

        {"Ação", "19"},
        {"Aventura", "21"},
        {"RPG", "122"},
        {"Indie", "492"},
        {"Estratégia", "9"},
        {"Simulação", "599"},
        {"Esportes", "701"},
        {"Corrida", "699"},
        {"Casual", "597"},
        {"Terror", "1667"}

    };

    @Override
    public void init() throws ServletException {

        System.out.println("=================================");
        System.out.println("INICIANDO JOGOS SERVLET");
        System.out.println("=================================");

        criarTabela();

        carregarJogos();
    }

    /*
     * =========================================================
     * CRIA A TABELA
     * =========================================================
     */
    private void criarTabela() {

        String apagar = "DROP TABLE IF EXISTS jogo";

        String criar =
                "CREATE TABLE jogo (" +
                "id INTEGER PRIMARY KEY, " +
                "nome TEXT NOT NULL, " +
                "genero TEXT NOT NULL, " +
                "nota REAL DEFAULT 0, " +
                "imagem TEXT" +
                ")";

        try (
            Connection conn = Conexao.conectar();
            Statement stmt = conn.createStatement()
        ) {

            stmt.executeUpdate(apagar);

            stmt.executeUpdate(criar);

            System.out.println("=================================");
            System.out.println("TABELA JOGO CRIADA!");
            System.out.println("=================================");

        } catch (Exception e) {

            System.out.println("=================================");
            System.out.println("ERRO AO CRIAR TABELA JOGO:");
            System.out.println("=================================");

            e.printStackTrace();
        }
    }

    /*
     * =========================================================
     * CARREGA OS JOGOS DA STEAM
     * =========================================================
     */
    private void carregarJogos() {

        System.out.println("=================================");
        System.out.println("CARREGANDO JOGOS DA STEAM...");
        System.out.println("=================================");

        Set<Integer> idsAdicionados =
                new HashSet<Integer>();

        int total = 0;

        for (String[] genero : GENEROS) {

            if (total >= TOTAL_JOGOS) {
                break;
            }

            String nomeGenero = genero[0];
            String tag = genero[1];

            System.out.println(
                    "Buscando gênero: " + nomeGenero
            );

            /*
             * 5 páginas de 50 jogos.
             */
            for (int pagina = 0; pagina < 5; pagina++) {

                if (total >= TOTAL_JOGOS) {
                    break;
                }

                int inicio = pagina * 50;

                String resposta =
                        buscarSteam(tag, inicio);

                if (resposta == null ||
                        resposta.isEmpty()) {

                    System.out.println(
                            "Steam não retornou dados para "
                            + nomeGenero
                    );

                    break;
                }

                int adicionados =
                        processarJogos(
                                resposta,
                                nomeGenero,
                                idsAdicionados,
                                TOTAL_JOGOS - total
                        );

                total += adicionados;

                System.out.println(
                        "Total carregado: "
                        + total
                        + "/"
                        + TOTAL_JOGOS
                );

                if (adicionados == 0) {
                    break;
                }
            }
        }

        System.out.println("=================================");
        System.out.println(
                "TOTAL DE JOGOS CARREGADOS: " + total
        );
        System.out.println("=================================");
    }

    /*
     * =========================================================
     * BUSCA NA STEAM
     * =========================================================
     */
    private String buscarSteam(
            String tag,
            int inicio
    ) {

        HttpURLConnection conexao = null;

        try {

            String endereco =
                    "https://store.steampowered.com/search/results/"
                    + "?json=1"
                    + "&category1=998"
                    + "&tags=" + tag
                    + "&start=" + inicio
                    + "&count=50"
                    + "&supportedlang=english";

            System.out.println(
                    "Steam URL: " + endereco
            );

            URL url = new URL(endereco);

            conexao =
                    (HttpURLConnection)
                    url.openConnection();

            conexao.setRequestMethod("GET");

            conexao.setConnectTimeout(15000);

            conexao.setReadTimeout(20000);

            conexao.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0"
            );

            conexao.setRequestProperty(
                    "Accept",
                    "application/json,text/plain,*/*"
            );

            int status =
                    conexao.getResponseCode();

            if (status != 200) {

                System.out.println(
                        "Steam retornou HTTP "
                        + status
                );

                return "";
            }

            BufferedReader leitor =
                    new BufferedReader(
                            new InputStreamReader(
                                    conexao.getInputStream(),
                                    "UTF-8"
                            )
                    );

            StringBuilder resposta =
                    new StringBuilder();

            String linha;

            while (
                    (linha = leitor.readLine())
                    != null
            ) {

                resposta.append(linha);
            }

            leitor.close();

            return resposta.toString();

        } catch (Exception e) {

            System.out.println(
                    "Erro ao consultar Steam:"
            );

            e.printStackTrace();

            return "";

        } finally {

            if (conexao != null) {
                conexao.disconnect();
            }
        }
    }

    /*
     * =========================================================
     * PROCESSA O JSON DA STEAM
     * =========================================================
     */
    private int processarJogos(
            String json,
            String genero,
            Set<Integer> idsAdicionados,
            int limite
    ) {

        int quantidade = 0;

        Pattern padrao = Pattern.compile(
                "\"id\"\\s*:\\s*(\\d+).*?"
                + "\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
                Pattern.DOTALL
        );

        Matcher matcher =
                padrao.matcher(json);

        while (
                matcher.find()
                && quantidade < limite
        ) {

            try {

                int id =
                        Integer.parseInt(
                                matcher.group(1)
                        );

                String nome =
                        matcher.group(2);

                nome =
                        limparTextoJSON(nome);

                String nomeMinusculo =
                        nome.toLowerCase();

                /*
                 * Ignora resultados que não são jogos.
                 */
                if (
                    nomeMinusculo.contains("soundtrack")
                    || nomeMinusculo.contains("ost")
                    || nomeMinusculo.contains("demo")
                    || nomeMinusculo.contains("playtest")
                    || nomeMinusculo.contains("server")
                    || nomeMinusculo.contains("tool")
                ) {

                    continue;
                }

                /*
                 * Evita jogos repetidos.
                 */
                if (idsAdicionados.contains(id)) {
                    continue;
                }

                boolean inserido =
                        inserirJogo(
                                id,
                                nome,
                                genero
                        );

                if (inserido) {

                    idsAdicionados.add(id);

                    quantidade++;
                }

            } catch (Exception e) {

                System.out.println(
                        "Erro processando jogo:"
                );

                e.printStackTrace();
            }
        }

        return quantidade;
    }

    /*
     * =========================================================
     * INSERE JOGO NO SQLITE
     * =========================================================
     */
    private boolean inserirJogo(
            int id,
            String nome,
            String genero
    ) {

        String sql =
                "INSERT INTO jogo "
                + "(id, nome, genero, nota, imagem) "
                + "VALUES (?, ?, ?, ?, ?)";

        String imagem =
                "https://cdn.cloudflare.steamstatic.com/"
                + "steam/apps/"
                + id
                + "/library_600x900.jpg";

        try (
            Connection conn = Conexao.conectar();
            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);

            stmt.setString(2, nome);

            stmt.setString(3, genero);

            stmt.setDouble(4, 0);

            stmt.setString(5, imagem);

            stmt.executeUpdate();

            return true;

        } catch (Exception e) {

            System.out.println(
                    "Erro ao inserir jogo "
                    + id
            );

            return false;
        }
    }

    /*
     * =========================================================
     * DO GET
     * =========================================================
     *
     * CORRIGIDO:
     * agora possui throws IOException
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        String busca =
                request.getParameter("busca");

        String genero =
                request.getParameter("genero");

        if (busca == null) {
            busca = "";
        }

        if (genero == null) {
            genero = "";
        }

        try {

            String html =
                    gerarPagina(
                            busca,
                            genero
                    );

            response.getWriter().write(html);

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().write(
                    "<h1>Erro ao carregar jogos.</h1>"
            );
        }
    }

    /*
     * =========================================================
     * GERA A PÁGINA
     * =========================================================
     */
    private String gerarPagina(
            String busca,
            String genero
    ) {

        StringBuilder html =
                new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='pt-BR'>");

        html.append("<head>");

        html.append("<meta charset='UTF-8'>");

        html.append(
                "<meta name='viewport' "
                + "content='width=device-width, "
                + "initial-scale=1.0'>"
        );

        html.append(
                "<title>Jogos - Inventory</title>"
        );

        /*
         * Usa o CSS das outras páginas.
         */
        html.append(
                "<link rel='stylesheet' "
                + "href='style.css'>"
        );

        html.append("<style>");

        html.append(
                "body{"
                + "margin:0;"
                + "background:#0d0914;"
                + "color:#fff;"
                + "font-family:inherit;"
                + "}"
        );

        html.append(
                ".container{"
                + "width:90%;"
                + "max-width:1055px;"
                + "margin:auto;"
                + "}"
        );

        html.append(
                ".cabecalho{"
                + "height:66px;"
                + "border-bottom:1px solid #30263b;"
                + "display:flex;"
                + "align-items:center;"
                + "justify-content:space-between;"
                + "padding:0 42px;"
                + "box-sizing:border-box;"
                + "}"
        );

        html.append(
                ".logo{"
                + "font-size:28px;"
                + "font-weight:bold;"
                + "color:white;"
                + "text-decoration:none;"
                + "}"
        );

        html.append(
                ".logo span{"
                + "color:#a855f7;"
                + "}"
        );

        html.append(
                ".menu{"
                + "display:flex;"
                + "gap:42px;"
                + "}"
        );

        html.append(
                ".menu a{"
                + "color:#c9c0d4;"
                + "text-decoration:none;"
                + "font-size:14px;"
                + "}"
        );

        html.append(
                ".menu a:hover{"
                + "color:#a855f7;"
                + "}"
        );

        html.append(
                ".boas-vindas{"
                + "margin-top:27px;"
                + "padding:52px 20px;"
                + "text-align:center;"
                + "background:#17121e;"
                + "border:1px solid #30263b;"
                + "border-radius:16px;"
                + "}"
        );

        html.append(
                ".boas-vindas h1{"
                + "margin:0;"
                + "font-size:34px;"
                + "}"
        );

        html.append(
                ".roxo{"
                + "color:#a855f7;"
                + "}"
        );

        html.append(
                ".boas-vindas p{"
                + "color:#aaa0b7;"
                + "margin-top:15px;"
                + "}"
        );

        html.append(
                ".busca-box{"
                + "margin-top:32px;"
                + "padding:28px 24px;"
                + "background:#17121e;"
                + "border:1px solid #30263b;"
                + "border-radius:16px;"
                + "}"
        );

        html.append(
                ".busca-box h2{"
                + "margin-top:0;"
                + "}"
        );

        html.append(
                ".campo{"
                + "width:100%;"
                + "box-sizing:border-box;"
                + "background:#0d0914;"
                + "border:1px solid #9d4edd;"
                + "border-radius:8px;"
                + "padding:12px;"
                + "color:white;"
                + "margin-bottom:10px;"
                + "outline:none;"
                + "}"
        );

        html.append(
                ".botao{"
                + "background:#9d4edd;"
                + "border:0;"
                + "border-radius:8px;"
                + "padding:12px 24px;"
                + "color:white;"
                + "font-weight:bold;"
                + "cursor:pointer;"
                + "}"
        );

        html.append(
                ".botao:hover{"
                + "background:#b56cff;"
                + "}"
        );

        html.append(
                ".titulo-jogos{"
                + "margin-top:40px;"
                + "}"
        );

        html.append(
                ".titulo-jogos p{"
                + "color:#aaa0b7;"
                + "}"
        );

        html.append(
                ".grid{"
                + "display:grid;"
                + "grid-template-columns:"
                + "repeat(5,1fr);"
                + "gap:18px;"
                + "margin-top:25px;"
                + "}"
        );

        html.append(
                ".card{"
                + "background:#17121e;"
                + "border:1px solid #30263b;"
                + "border-radius:12px;"
                + "overflow:hidden;"
                + "transition:.2s;"
                + "}"
        );

        html.append(
                ".card:hover{"
                + "transform:translateY(-4px);"
                + "border-color:#9d4edd;"
                + "}"
        );

        html.append(
                ".capa{"
                + "width:100%;"
                + "height:270px;"
                + "object-fit:cover;"
                + "display:block;"
                + "background:#201827;"
                + "}"
        );

        html.append(
                ".card-info{"
                + "padding:14px;"
                + "}"
        );

        html.append(
                ".card-info h3{"
                + "font-size:16px;"
                + "margin:0 0 8px 0;"
                + "white-space:nowrap;"
                + "overflow:hidden;"
                + "text-overflow:ellipsis;"
                + "}"
        );

        html.append(
                ".genero{"
                + "color:#a855f7;"
                + "font-size:13px;"
                + "}"
        );

        html.append(
                ".biblioteca{"
                + "display:block;"
                + "text-align:center;"
                + "background:#9d4edd;"
                + "color:white;"
                + "text-decoration:none;"
                + "padding:9px;"
                + "border-radius:7px;"
                + "margin-top:12px;"
                + "font-size:13px;"
                + "}"
        );

        html.append(
                ".vazio{"
                + "padding:40px 0;"
                + "color:#aaa0b7;"
                + "}"
        );

        html.append(
                ".rodape{"
                + "margin-top:80px;"
                + "border-top:1px solid #30263b;"
                + "padding:28px;"
                + "text-align:center;"
                + "color:#aaa0b7;"
                + "}"
        );

        html.append(
                "@media(max-width:1000px){"
                + ".grid{"
                + "grid-template-columns:"
                + "repeat(4,1fr);"
                + "}"
                + "}"
        );

        html.append(
                "@media(max-width:800px){"
                + ".grid{"
                + "grid-template-columns:"
                + "repeat(3,1fr);"
                + "}"
                + ".menu{gap:15px;}"
                + "}"
        );

        html.append(
                "@media(max-width:600px){"
                + ".cabecalho{"
                + "padding:0 15px;"
                + "}"
                + ".menu{display:none;}"
                + ".grid{"
                + "grid-template-columns:"
                + "repeat(2,1fr);"
                + "}"
                + ".capa{height:230px;}"
                + "}"
        );

        html.append("</style>");

        html.append("</head>");

        html.append("<body>");

        /*
         * CABEÇALHO
         */
        html.append(
                "<header class='cabecalho'>"
        );

        html.append(
                "<a class='logo' "
                + "href='index.html'>"
                + "👻 Inventory"
                + "</a>"
        );

        html.append("<nav class='menu'>");

        html.append(
                "<a href='index.html'>Início</a>"
        );

        html.append(
                "<a href='buscar-usuarios.html'>"
                + "Buscar usuários"
                + "</a>"
        );

        html.append(
                "<a href='jogos'>Jogos</a>"
        );

        html.append(
                "<a href='perfil'>Meu Perfil</a>"
        );

        html.append(
                "<a href='biblioteca'>Biblioteca</a>"
        );

        html.append(
                "<a href='listas'>Listas</a>"
        );

        html.append(
                "<a href='logout'>Sair</a>"
        );

        html.append("</nav>");

        html.append("</header>");

        html.append(
                "<main class='container'>"
        );

        /*
         * BEM-VINDO
         */
        html.append(
                "<section class='boas-vindas'>"
        );

        html.append(
                "<h1>"
                + "Bem-vindo ao "
                + "<span class='roxo'>Inventory</span>"
                + "</h1>"
        );

        html.append(
                "<p>"
                + "Descubra jogos, avalie suas experiências "
                + "e monte sua biblioteca."
                + "</p>"
        );

        html.append("</section>");

        /*
         * BUSCA
         */
        html.append(
                "<section class='busca-box'>"
        );

        html.append(
                "<h2>🔎 Buscar jogo</h2>"
        );

        html.append(
                "<form method='GET' action='jogos'>"
        );

        html.append(
                "<input "
                + "class='campo' "
                + "type='text' "
                + "name='busca' "
                + "placeholder='Digite o nome do jogo...' "
                + "value='"
                + escaparHTML(busca)
                + "'>"
        );

        /*
         * FILTRO DE GÊNERO
         */
        html.append(
                "<select "
                + "class='campo' "
                + "name='genero'>"
        );

        html.append(
                "<option value=''>"
                + "Todos os gêneros"
                + "</option>"
        );

        for (String[] g : GENEROS) {

            String selecionado =
                    g[0].equals(genero)
                    ? " selected"
                    : "";

            html.append(
                    "<option value='"
                    + escaparHTML(g[0])
                    + "'"
                    + selecionado
                    + ">"
                    + escaparHTML(g[0])
                    + "</option>"
            );
        }

        html.append("</select>");

        html.append(
                "<button "
                + "class='botao' "
                + "type='submit'>"
                + "Buscar"
                + "</button>"
        );

        html.append("</form>");

        html.append("</section>");

        /*
         * JOGOS
         */
        html.append(
                "<section class='titulo-jogos'>"
        );

        html.append(
                "<h2>🎮 Jogos populares</h2>"
        );

        html.append(
                "<p>"
                + "Confira os jogos disponíveis no Inventory."
                + "</p>"
        );

        /*
         * CONSULTA
         */
        String sql;

        boolean temBusca =
                !busca.trim().isEmpty();

        boolean temGenero =
                !genero.trim().isEmpty();

        if (temBusca && temGenero) {

            sql =
                    "SELECT id,nome,genero,nota,imagem "
                    + "FROM jogo "
                    + "WHERE LOWER(nome) LIKE ? "
                    + "AND genero = ? "
                    + "ORDER BY nome "
                    + "LIMIT 500";

        } else if (temBusca) {

            sql =
                    "SELECT id,nome,genero,nota,imagem "
                    + "FROM jogo "
                    + "WHERE LOWER(nome) LIKE ? "
                    + "ORDER BY nome "
                    + "LIMIT 500";

        } else if (temGenero) {

            sql =
                    "SELECT id,nome,genero,nota,imagem "
                    + "FROM jogo "
                    + "WHERE genero = ? "
                    + "ORDER BY nome "
                    + "LIMIT 500";

        } else {

            sql =
                    "SELECT id,nome,genero,nota,imagem "
                    + "FROM jogo "
                    + "ORDER BY nome "
                    + "LIMIT 500";
        }

        int quantidade = 0;

        try (
            Connection conn = Conexao.conectar();
            PreparedStatement stmt =
                    conn.prepareStatement(sql)
        ) {

            if (temBusca && temGenero) {

                stmt.setString(
                        1,
                        "%"
                        + busca.toLowerCase()
                        + "%"
                );

                stmt.setString(
                        2,
                        genero
                );

            } else if (temBusca) {

                stmt.setString(
                        1,
                        "%"
                        + busca.toLowerCase()
                        + "%"
                );

            } else if (temGenero) {

                stmt.setString(
                        1,
                        genero
                );
            }

            ResultSet rs =
                    stmt.executeQuery();

            /*
             * GRID
             */
            html.append(
                    "<div class='grid'>"
            );

            while (rs.next()) {

                quantidade++;

                int id =
                        rs.getInt("id");

                String nome =
                        rs.getString("nome");

                String gen =
                        rs.getString("genero");

                double nota =
                        rs.getDouble("nota");

                String imagem =
                        rs.getString("imagem");

                if (
                    imagem == null ||
                    imagem.trim().isEmpty()
                ) {

                    imagem =
                            "https://cdn.cloudflare.steamstatic.com/"
                            + "steam/apps/"
                            + id
                            + "/library_600x900.jpg";
                }

                html.append(
                        "<article class='card'>"
                );

                /*
                 * CAPA STEAM
                 */
                html.append(
                        "<img "
                        + "class='capa' "
                        + "src='"
                        + escaparHTML(imagem)
                        + "' "
                        + "alt='"
                        + escaparHTML(nome)
                        + "' "
                        + "loading='lazy' "
                        + "onerror=\"this.onerror=null;"
                        + "this.src='"
                        + "https://cdn.cloudflare.steamstatic.com/"
                        + "steam/apps/"
                        + id
                        + "/header.jpg';\">"
                );

                html.append(
                        "<div class='card-info'>"
                );

                html.append(
                        "<h3 title='"
                        + escaparHTML(nome)
                        + "'>"
                        + escaparHTML(nome)
                        + "</h3>"
                );

                html.append(
                        "<div class='genero'>"
                        + escaparHTML(gen)
                        + "</div>"
                );

                if (nota > 0) {

                    html.append(
                            "<div>"
                            + "⭐ "
                            + nota
                            + "</div>"
                    );
                }

                html.append(
                        "<a "
                        + "class='biblioteca' "
                        + "href='adicionar-biblioteca?id="
                        + id
                        + "'>"
                        + "+ Minha biblioteca"
                        + "</a>"
                );

                html.append("</div>");

                html.append("</article>");
            }

            rs.close();

            html.append("</div>");

        } catch (Exception e) {

            System.out.println(
                    "Erro ao buscar jogos:"
            );

            e.printStackTrace();

            html.append(
                    "<div class='vazio'>"
                    + "Erro ao carregar os jogos."
                    + "</div>"
            );
        }

        /*
         * CONTADOR
         */
        html.append(
                "<p>"
                + quantidade
                + " jogos encontrados"
                + "</p>"
        );

        /*
         * NENHUM JOGO
         */
        if (quantidade == 0) {

            html.append(
                    "<div class='vazio'>"
                    + "Nenhum jogo encontrado."
                    + "</div>"
            );
        }

        html.append("</section>");

        html.append("</main>");

        /*
         * RODAPÉ
         */
        html.append(
                "<footer class='rodape'>"
                + "© 2026 Inventory"
                + "</footer>"
        );

        html.append("</body>");

        html.append("</html>");

        return html.toString();
    }

    /*
     * =========================================================
     * LIMPA JSON
     * =========================================================
     */
    private String limparTextoJSON(
            String texto
    ) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\/", "/")
                .replace("\\n", " ")
                .replace("\\r", " ")
                .replace("\\t", " ")
                .trim();
    }

    /*
     * =========================================================
     * ESCAPA HTML
     * =========================================================
     */
    private String escaparHTML(
            String texto
    ) {

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