package controller;

import dao.Conexao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/jogos")
public class JogosServlet extends HttpServlet {

    private static final int LIMITE_JOGOS = 500;

    /*
     * URL da API pública do SteamSpy.
     *
     * Ela retorna vários jogos de uma vez.
     */
    private static final String STEAMSPY_URL =
            "https://steamspy.com/api.php?request=all&page=1";

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String busca = request.getParameter("busca");

        try (Connection conn = Conexao.conectar()) {

            if (conn == null) {
                mostrarErro(response,
                        "Não foi possível conectar ao banco de dados.");
                return;
            }

            criarTabelaSeNaoExistir(conn);

            /*
             * Se ainda não houver 500 jogos,
             * carrega automaticamente.
             */
            int quantidade = contarJogos(conn);

            if (quantidade < LIMITE_JOGOS) {

                System.out.println("=================================");
                System.out.println("CATÁLOGO DE JOGOS");
                System.out.println("Jogos encontrados no banco: "
                        + quantidade);
                System.out.println("Carregando catálogo...");
                System.out.println("=================================");

                carregarJogos(conn);
            }

            mostrarPagina(conn, request, response, busca);

        } catch (Exception e) {

            e.printStackTrace();

            mostrarErro(response,
                    "Erro ao carregar os jogos: " + e.getMessage());
        }
    }

    /*
     * ==========================================================
     * CRIA TABELA
     * ==========================================================
     */

    private void criarTabelaSeNaoExistir(Connection conn)
            throws Exception {

        String sql =
                "CREATE TABLE IF NOT EXISTS jogo (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "steam_id INTEGER UNIQUE," +
                "titulo TEXT NOT NULL," +
                "genero TEXT," +
                "plataforma TEXT," +
                "ano_lancamento INTEGER," +
                "nota REAL," +
                "capa TEXT" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }

        /*
         * Caso sua tabela já existisse com algumas colunas antigas,
         * tentamos adicionar as colunas que faltarem.
         */

        adicionarColunaSeNaoExistir(
                conn,
                "steam_id",
                "INTEGER UNIQUE"
        );

        adicionarColunaSeNaoExistir(
                conn,
                "nota",
                "REAL"
        );

        adicionarColunaSeNaoExistir(
                conn,
                "capa",
                "TEXT"
        );
    }

    private void adicionarColunaSeNaoExistir(
            Connection conn,
            String coluna,
            String tipo) {

        try {

            String sql =
                    "ALTER TABLE jogo ADD COLUMN "
                    + coluna + " " + tipo;

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            }

        } catch (Exception e) {

            /*
             * Se a coluna já existir, não fazemos nada.
             */
        }
    }

    /*
     * ==========================================================
     * CONTAR JOGOS
     * ==========================================================
     */

    private int contarJogos(Connection conn)
            throws Exception {

        String sql = "SELECT COUNT(*) FROM jogo";

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    /*
     * ==========================================================
     * CARREGAR 500 JOGOS
     * ==========================================================
     */

    private void carregarJogos(Connection conn) {

        try {

            String json = baixarDados(STEAMSPY_URL);

            if (json == null || json.isEmpty()) {

                System.out.println(
                        "Não foi possível obter dados do SteamSpy."
                );

                return;
            }

            List<JogoDados> jogos =
                    interpretarJogos(json);

            System.out.println(
                    "Jogos recebidos da API: "
                    + jogos.size()
            );

            int adicionados = 0;

            String sql =
                    "INSERT OR IGNORE INTO jogo " +
                    "(steam_id, titulo, genero, plataforma, " +
                    "ano_lancamento, nota, capa) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt =
                         conn.prepareStatement(sql)) {

                for (JogoDados jogo : jogos) {

                    if (adicionados >= LIMITE_JOGOS) {
                        break;
                    }

                    if (jogo.id <= 0 ||
                        jogo.nome == null ||
                        jogo.nome.trim().isEmpty()) {

                        continue;
                    }

                    stmt.setInt(1, jogo.id);

                    stmt.setString(
                            2,
                            limparTexto(jogo.nome)
                    );

                    stmt.setString(
                            3,
                            jogo.genero
                    );

                    stmt.setString(
                            4,
                            "PC"
                    );

                    stmt.setInt(
                            5,
                            jogo.ano
                    );

                    stmt.setDouble(
                            6,
                            jogo.nota
                    );

                    /*
                     * Capa vertical da Steam.
                     */
                    String capa =
                            "https://shared.fastly.steamstatic.com/"
                            + "store_item_assets/steam/apps/"
                            + jogo.id
                            + "/library_600x900_2x.jpg";

                    stmt.setString(7, capa);

                    stmt.executeUpdate();

                    adicionados++;
                }
            }

            System.out.println(
                    "Jogos adicionados: "
                    + adicionados
            );

        } catch (Exception e) {

            System.out.println(
                    "ERRO AO CARREGAR JOGOS"
            );

            e.printStackTrace();
        }
    }

    /*
     * ==========================================================
     * INTERPRETAR JSON DO STEAMSPY
     * ==========================================================
     */

    private List<JogoDados> interpretarJogos(
            String json) {

        List<JogoDados> lista =
                new ArrayList<JogoDados>();

        /*
         * O SteamSpy retorna um objeto em que as chaves
         * são os AppIDs.
         *
         * Procuramos:
         *
         * "123456": {
         *     "appid": 123456,
         *     "name": "Nome do jogo"
         * }
         */

        Pattern bloco =
                Pattern.compile(
                        "\"(\\d+)\"\\s*:\\s*\\{(.*?)\\}",
                        Pattern.DOTALL
                );

        Matcher matcher =
                bloco.matcher(json);

        while (matcher.find()) {

            try {

                int id =
                        Integer.parseInt(
                                matcher.group(1)
                        );

                String dados =
                        matcher.group(2);

                /*
                 * Nome
                 */
                String nome =
                        pegarTexto(
                                dados,
                                "\"name\"\\s*:\\s*\"(.*?)\""
                        );

                if (nome == null ||
                    nome.trim().isEmpty()) {

                    continue;
                }

                /*
                 * Gênero
                 */
                String genero =
                        pegarTexto(
                                dados,
                                "\"genre\"\\s*:\\s*\"(.*?)\""
                        );

                if (genero == null ||
                    genero.trim().isEmpty()) {

                    genero = "Aventura";
                }

                /*
                 * Nota aproximada baseada no userscore.
                 */
                double nota = 4.0;

                String userscore =
                        pegarTexto(
                                dados,
                                "\"userscore\"\\s*:\\s*(\\d+)"
                        );

                if (userscore != null) {

                    try {

                        double score =
                                Double.parseDouble(
                                        userscore
                                );

                        if (score > 0) {

                            nota =
                                    1.0
                                    + (score / 100.0) * 4.0;

                        }
                    } catch (Exception e) {
                        nota = 4.0;
                    }
                }

                /*
                 * Ano.
                 */
                int ano = 0;

                String data =
                        pegarTexto(
                                dados,
                                "\"release_date\"\\s*:\\s*\"(.*?)\""
                        );

                if (data != null) {

                    Matcher anoMatcher =
                            Pattern.compile(
                                    "(\\d{4})"
                            ).matcher(data);

                    if (anoMatcher.find()) {

                        ano =
                                Integer.parseInt(
                                        anoMatcher.group(1)
                                );
                    }
                }

                /*
                 * Se não encontrou ano,
                 * coloca 0.
                 */
                if (ano < 1900) {
                    ano = 0;
                }

                JogoDados jogo =
                        new JogoDados();

                jogo.id = id;
                jogo.nome = nome;
                jogo.genero = genero;
                jogo.ano = ano;
                jogo.nota = Math.round(
                        nota * 10.0
                ) / 10.0;

                lista.add(jogo);

            } catch (Exception e) {

                /*
                 * Se um jogo estiver com dados inválidos,
                 * simplesmente pula para o próximo.
                 */
            }
        }

        return lista;
    }

    /*
     * ==========================================================
     * PEGAR TEXTO DO JSON
     * ==========================================================
     */

    private String pegarTexto(
            String texto,
            String regex) {

        Pattern pattern =
                Pattern.compile(
                        regex,
                        Pattern.DOTALL
                );

        Matcher matcher =
                pattern.matcher(texto);

        if (matcher.find()) {

            return matcher.group(1);
        }

        return null;
    }

    /*
     * ==========================================================
     * BAIXAR API
     * ==========================================================
     */

    private String baixarDados(
            String endereco) {

        HttpURLConnection conexao = null;

        try {

            URL url =
                    new URL(endereco);

            conexao =
                    (HttpURLConnection)
                    url.openConnection();

            conexao.setRequestMethod("GET");

            conexao.setConnectTimeout(15000);

            conexao.setReadTimeout(30000);

            conexao.setRequestProperty(
                    "User-Agent",
                    "Inventory-GameBoxd/1.0"
            );

            int codigo =
                    conexao.getResponseCode();

            if (codigo != 200) {

                System.out.println(
                        "API retornou HTTP "
                        + codigo
                );

                return null;
            }

            InputStream input =
                    conexao.getInputStream();

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    input,
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder resultado =
                    new StringBuilder();

            String linha;

            while ((linha = reader.readLine())
                    != null) {

                resultado.append(linha);
            }

            reader.close();

            return resultado.toString();

        } catch (Exception e) {

            System.out.println(
                    "Erro ao acessar API:"
            );

            e.printStackTrace();

            return null;

        } finally {

            if (conexao != null) {
                conexao.disconnect();
            }
        }
    }

    /*
     * ==========================================================
     * MOSTRAR PÁGINA
     * ==========================================================
     */

    private void mostrarPagina(
            Connection conn,
            HttpServletRequest request,
            HttpServletResponse response,
            String busca)
            throws Exception {

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

        /*
         * CSS.
         */

        html.append("<style>");

        html.append(
                "html,body{"
                + "margin:0;"
                + "padding:0;"
                + "min-height:100%;"
                + "font-family:Arial,sans-serif;"
                + "background:#0d0914;"
                + "color:white;"
                + "}"
        );

        html.append(
                "header{"
                + "width:100%;"
                + "box-sizing:border-box;"
                + "display:flex;"
                + "align-items:center;"
                + "justify-content:space-between;"
                + "padding:18px 40px;"
                + "background:#0d0914;"
                + "border-bottom:1px solid #30263a;"
                + "}"
        );

        html.append(
                ".logo-area{"
                + "display:flex;"
                + "align-items:center;"
                + "gap:9px;"
                + "}"
        );

        html.append(
                ".logo-area h1{"
                + "margin:0;"
                + "font-size:30px;"
                + "color:white;"
                + "}"
        );

        html.append(
                "nav{"
                + "display:flex;"
                + "gap:28px;"
                + "}"
        );

        html.append(
                "nav a{"
                + "color:#b9afc5;"
                + "text-decoration:none;"
                + "font-size:14px;"
                + "}"
        );

        html.append(
                "nav a:hover{"
                + "color:#c084fc;"
                + "}"
        );

        html.append(
                "main{"
                + "max-width:1200px;"
                + "margin:auto;"
                + "padding:30px 25px 60px;"
                + "}"
        );

        html.append(
                ".inicio{"
                + "text-align:center;"
                + "padding:45px 25px;"
                + "margin-bottom:35px;"
                + "background:"
                + "radial-gradient("
                + "circle at top,"
                + "rgba(124,58,237,.28),"
                + "transparent 65%),"
                + "#17121f;"
                + "border:1px solid #30263a;"
                + "border-radius:18px;"
                + "}"
        );

        html.append(
                ".inicio h2{"
                + "font-size:38px;"
                + "margin:0 0 12px;"
                + "}"
        );

        html.append(
                ".inicio span{"
                + "color:#a855f7;"
                + "}"
        );

        html.append(
                ".inicio p{"
                + "color:#aaa1b4;"
                + "}"
        );

        html.append(
                ".busca{"
                + "background:#17131d;"
                + "border:1px solid #30263a;"
                + "border-radius:14px;"
                + "padding:25px;"
                + "margin-bottom:40px;"
                + "}"
        );

        html.append(
                ".busca-form{"
                + "display:flex;"
                + "gap:10px;"
                + "}"
        );

        html.append(
                ".busca input{"
                + "flex:1;"
                + "padding:13px 15px;"
                + "background:#0f0b16;"
                + "border:1px solid #7c3aed;"
                + "border-radius:8px;"
                + "color:white;"
                + "outline:none;"
                + "}"
        );

        html.append(
                ".busca button{"
                + "padding:12px 25px;"
                + "border:none;"
                + "border-radius:8px;"
                + "background:linear-gradient("
                + "135deg,#7c3aed,#a855f7);"
                + "color:white;"
                + "font-weight:bold;"
                + "cursor:pointer;"
                + "}"
        );

        html.append(
                ".catalogo-jogos{"
                + "display:grid;"
                + "grid-template-columns:"
                + "repeat(5,minmax(0,1fr));"
                + "gap:22px;"
                + "}"
        );

        html.append(
                ".card-jogo{"
                + "background:linear-gradient("
                + "145deg,#211a2b,#17131d);"
                + "border:1px solid #30263a;"
                + "border-radius:14px;"
                + "overflow:hidden;"
                + "text-align:center;"
                + "transition:.2s;"
                + "}"
        );

        html.append(
                ".card-jogo:hover{"
                + "transform:translateY(-6px);"
                + "border-color:rgba(168,85,247,.65);"
                + "box-shadow:"
                + "0 15px 35px "
                + "rgba(124,58,237,.18);"
                + "}"
        );

        html.append(
                ".card-jogo img{"
                + "width:100%;"
                + "height:280px;"
                + "object-fit:cover;"
                + "display:block;"
                + "}"
        );

        html.append(
                ".card-conteudo{"
                + "padding:18px;"
                + "}"
        );

        html.append(
                ".card-jogo h3{"
                + "font-size:17px;"
                + "margin:0 0 8px;"
                + "min-height:40px;"
                + "display:flex;"
                + "align-items:center;"
                + "justify-content:center;"
                + "}"
        );

        html.append(
                ".genero{"
                + "color:#aaa1b4;"
                + "font-size:13px;"
                + "min-height:32px;"
                + "}"
        );

        html.append(
                ".nota{"
                + "color:#c084fc;"
                + "font-weight:bold;"
                + "}"
        );

        html.append(
                ".botao{"
                + "display:block;"
                + "padding:11px;"
                + "margin-top:12px;"
                + "background:linear-gradient("
                + "135deg,#7c3aed,#a855f7);"
                + "color:white;"
                + "text-decoration:none;"
                + "border-radius:8px;"
                + "font-size:13px;"
                + "font-weight:bold;"
                + "}"
        );

        html.append(
                "footer{"
                + "text-align:center;"
                + "padding:25px;"
                + "background:#0d0914;"
                + "border-top:1px solid #30263a;"
                + "color:#8c8199;"
                + "}"
        );

        html.append(
                "@media(max-width:1000px){"
                + ".catalogo-jogos{"
                + "grid-template-columns:"
                + "repeat(4,1fr);"
                + "}"
                + "}"
        );

        html.append(
                "@media(max-width:800px){"
                + "header{"
                + "flex-direction:column;"
                + "gap:15px;"
                + "}"
                + ".catalogo-jogos{"
                + "grid-template-columns:"
                + "repeat(3,1fr);"
                + "}"
                + "}"
        );

        html.append(
                "@media(max-width:600px){"
                + ".catalogo-jogos{"
                + "grid-template-columns:"
                + "repeat(2,1fr);"
                + "gap:14px;"
                + "}"
                + ".busca-form{"
                + "flex-direction:column;"
                + "}"
                + ".card-jogo img{"
                + "height:220px;"
                + "}"
                + "}"
        );

        html.append(
                "@media(max-width:420px){"
                + ".catalogo-jogos{"
                + "grid-template-columns:1fr;"
                + "}"
                + "}"
        );

        html.append("</style>");

        html.append("</head>");

        html.append("<body>");

        /*
         * HEADER
         */

        html.append("<header>");

        html.append(
                "<div class='logo-area'>"
        );

        html.append(
                "<h1>Inventory</h1>"
        );

        html.append("</div>");

        html.append("<nav>");

        html.append(
                "<a href='index.html'>Início</a>"
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

        /*
         * MAIN
         */

        html.append("<main>");

        html.append(
                "<section class='inicio'>"
        );

        html.append(
                "<h2>Catálogo do "
                + "<span>Inventory</span></h2>"
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
                "<section class='busca'>"
        );

        html.append(
                "<h2>🔎 Buscar jogo</h2>"
        );

        html.append(
                "<form class='busca-form' "
                + "method='GET' action='jogos'>"
        );

        html.append(
                "<input type='text' "
                + "name='busca' "
                + "placeholder='Digite o nome do jogo...'"
        );

        html.append(
                "<button type='submit'>Buscar</button>"
        );

        html.append("</form>");

        html.append("</section>");

        /*
         * TÍTULO
         */

        html.append(
                "<h2>"
                + (busca == null || busca.trim().isEmpty()
                    ? "🎮 Jogos populares"
                    : "🔎 Resultados para: "
                      + escaparHTML(busca))
                + "</h2>"
        );

        html.append(
                "<p style='color:#aaa1b4;'>"
                + "Jogos disponíveis no Inventory"
                + "</p>"
        );

        /*
         * CATÁLOGO
         */

        html.append(
                "<section class='catalogo-jogos'>"
        );

        String sql;

        if (busca != null &&
            !busca.trim().isEmpty()) {

            sql =
                    "SELECT id,titulo,genero,"
                    + "plataforma,ano_lancamento,"
                    + "nota,capa "
                    + "FROM jogo "
                    + "WHERE titulo LIKE ? "
                    + "ORDER BY titulo";

        } else {

            sql =
                    "SELECT id,titulo,genero,"
                    + "plataforma,ano_lancamento,"
                    + "nota,capa "
                    + "FROM jogo "
                    + "ORDER BY titulo";
        }

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            if (busca != null &&
                !busca.trim().isEmpty()) {

                stmt.setString(
                        1,
                        "%" + busca.trim() + "%"
                );
            }

            try (ResultSet rs =
                         stmt.executeQuery()) {

                int quantidade = 0;

                while (rs.next()) {

                    quantidade++;

                    int id =
                            rs.getInt("id");

                    String titulo =
                            rs.getString("titulo");

                    String genero =
                            rs.getString("genero");

                    double nota =
                            rs.getDouble("nota");

                    String capa =
                            rs.getString("capa");

                    if (capa == null ||
                        capa.trim().isEmpty()) {

                        capa =
                                "https://shared.fastly."
                                + "steamstatic.com/"
                                + "store_item_assets/"
                                + "steam/apps/"
                                + rs.getInt("steam_id")
                                + "/library_600x900.jpg";
                    }

                    html.append(
                            "<article class='card-jogo'>"
                    );

                    html.append(
                            "<img src='"
                            + escaparHTML(capa)
                            + "' alt='Capa de "
                            + escaparHTML(titulo)
                            + "' "
                            + "loading='lazy'>"
                    );

                    html.append(
                            "<div class='card-conteudo'>"
                    );

                    html.append(
                            "<h3>"
                            + escaparHTML(titulo)
                            + "</h3>"
                    );

                    html.append(
                            "<p class='genero'>"
                            + escaparHTML(
                                    genero == null
                                    ? "Jogo"
                                    : genero
                              )
                            + "</p>"
                    );

                    html.append(
                            "<p class='nota'>"
                            + "⭐ "
                            + String.format(
                                    "%.1f",
                                    nota
                              )
                            + "</p>"
                    );

                    html.append(
                            "<a class='botao' "
                            + "href='adicionar-biblioteca?id="
                            + id
                            + "'>"
                            + "+ Minha biblioteca"
                            + "</a>"
                    );

                    html.append(
                            "</div>"
                    );

                    html.append(
                            "</article>"
                    );
                }

                if (quantidade == 0) {

                    html.append(
                            "<div style='"
                            + "grid-column:1/-1;"
                            + "text-align:center;"
                            + "padding:50px;"
                            + "'>"
                    );

                    html.append(
                            "<h2>Nenhum jogo encontrado.</h2>"
                    );

                    html.append(
                            "</div>"
                    );
                }
            }
        }

        html.append("</section>");

        html.append("</main>");

        html.append(
                "<footer>"
                + "<p>© 2026 Inventory</p>"
                + "</footer>"
        );

        html.append("</body>");

        html.append("</html>");

        response.getWriter().write(
                html.toString()
        );
    }

    /*
     * ==========================================================
     * LIMPAR TEXTO
     * ==========================================================
     */

    private String limparTexto(
            String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("\\\"", "\"")
                .replace("\\/", "/")
                .replace("\\\\", "\\")
                .trim();
    }

    /*
     * ==========================================================
     * SEGURANÇA HTML
     * ==========================================================
     */

    private String escaparHTML(
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

    /*
     * ==========================================================
     * ERRO
     * ==========================================================
     */

    private void mostrarErro(
            HttpServletResponse response,
            String mensagem)
            throws IOException {

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        response.getWriter().println(
                "<!DOCTYPE html>"
                + "<html lang='pt-BR'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<title>Erro - Inventory</title>"
                + "</head>"
                + "<body style='"
                + "background:#0d0914;"
                + "color:white;"
                + "font-family:Arial;"
                + "text-align:center;"
                + "padding:80px;"
                + "'>"
                + "<h1>Erro no Inventory</h1>"
                + "<p>"
                + escaparHTML(mensagem)
                + "</p>"
                + "</body>"
                + "</html>"
        );
    }

    /*
     * ==========================================================
     * CLASSE DOS DADOS
     * ==========================================================
     */

    private static class JogoDados {

        int id;

        String nome;

        String genero;

        int ano;

        double nota;
    }
}