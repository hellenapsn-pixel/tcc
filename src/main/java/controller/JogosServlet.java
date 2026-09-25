package controller;

import dao.Conexao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/jogos")
public class JogosServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /*
     * ==========================================================
     * 10 GÊNEROS
     * ==========================================================
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


    /*
     * ==========================================================
     * QUANTIDADE DE JOGOS
     * ==========================================================
     */

    private static final int TOTAL_JOGOS = 500;


    /*
     * ==========================================================
     * INIT
     * ==========================================================
     */

    @Override
    public void init() throws ServletException {

        criarTabela();

        carregarJogos();

    }


    /*
     * ==========================================================
     * CRIAR TABELA
     * ==========================================================
     */

    private void criarTabela() {

        String sql =
            "CREATE TABLE IF NOT EXISTS jogo (" +
            "id INTEGER PRIMARY KEY," +
            "nome TEXT NOT NULL," +
            "genero TEXT NOT NULL," +
            "nota REAL DEFAULT 0," +
            "imagem TEXT" +
            ")";

        try (Connection conexao = Conexao.conectar();
             Statement stmt = conexao.createStatement()) {

            stmt.executeUpdate(sql);

            System.out.println("Tabela jogo criada/verificada.");

        } catch (Exception e) {

            System.out.println("Erro ao criar tabela jogo:");

            e.printStackTrace();
        }
    }


    /*
     * ==========================================================
     * CARREGAR 500 JOGOS DA STEAM
     * ==========================================================
     */

    private void carregarJogos() {

        try {

            Connection conexao = Conexao.conectar();

            if (conexao == null) {
                return;
            }


            /*
             * Se já tiver 500 jogos, não busca novamente.
             */

            String verificar =
                "SELECT COUNT(*) AS total FROM jogo";

            PreparedStatement psVerificar =
                conexao.prepareStatement(verificar);

            ResultSet rs =
                psVerificar.executeQuery();

            int totalBanco = 0;

            if (rs.next()) {
                totalBanco = rs.getInt("total");
            }

            rs.close();
            psVerificar.close();


            if (totalBanco >= TOTAL_JOGOS) {

                System.out.println(
                    "Banco já possui " +
                    totalBanco +
                    " jogos."
                );

                conexao.close();

                return;
            }


            /*
             * Limpa os jogos antigos para montar
             * novamente o catálogo.
             */

            Statement limpar =
                conexao.createStatement();

            limpar.executeUpdate(
                "DELETE FROM jogo"
            );

            limpar.close();


            Set<Integer> idsAdicionados =
                new HashSet<Integer>();


            /*
             * Busca jogos dos 10 gêneros.
             */

            for (String[] genero : GENEROS) {

                String nomeGenero =
                    genero[0];

                String tag =
                    genero[1];


                /*
                 * Até 3 páginas de 50 jogos.
                 */

                for (int pagina = 0; pagina < 3; pagina++) {

                    if (idsAdicionados.size() >= TOTAL_JOGOS) {
                        break;
                    }


                    int inicio =
                        pagina * 50;


                    String url =
                        "https://store.steampowered.com/search/results/"
                        + "?json=1"
                        + "&category1=998"
                        + "&tags=" + tag
                        + "&start=" + inicio
                        + "&count=50"
                        + "&supportedlang=portuguese";


                    System.out.println(
                        "Buscando: " +
                        nomeGenero +
                        " | página " +
                        pagina
                    );


                    String resposta =
                        baixar(url);


                    if (resposta == null ||
                        resposta.trim().isEmpty()) {

                        continue;
                    }


                    adicionarJogosDaSteam(
                        resposta,
                        nomeGenero,
                        conexao,
                        idsAdicionados
                    );
                }
            }


            conexao.close();


            System.out.println(
                "================================="
            );

            System.out.println(
                "JOGOS CARREGADOS: " +
                idsAdicionados.size()
            );

            System.out.println(
                "================================="
            );


        } catch (Exception e) {

            System.out.println(
                "Erro ao carregar jogos da Steam:"
            );

            e.printStackTrace();
        }
    }


    /*
     * ==========================================================
     * BAIXAR JSON DA STEAM
     * ==========================================================
     */

    private String baixar(String endereco) {

        HttpURLConnection conexao = null;

        BufferedReader leitor = null;

        try {

            URL url =
                new URL(endereco);

            conexao =
                (HttpURLConnection) url.openConnection();

            conexao.setRequestMethod("GET");

            conexao.setConnectTimeout(15000);

            conexao.setReadTimeout(15000);

            conexao.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
            );

            conexao.setRequestProperty(
                "Accept",
                "application/json"
            );


            int codigo =
                conexao.getResponseCode();


            if (codigo != 200) {

                System.out.println(
                    "Steam respondeu HTTP " +
                    codigo
                );

                return null;
            }


            leitor =
                new BufferedReader(
                    new InputStreamReader(
                        conexao.getInputStream(),
                        StandardCharsets.UTF_8
                    )
                );


            StringBuilder resposta =
                new StringBuilder();

            String linha;


            while ((linha = leitor.readLine()) != null) {

                resposta.append(linha);
            }


            return resposta.toString();


        } catch (Exception e) {

            System.out.println(
                "Erro ao acessar Steam:"
            );

            e.printStackTrace();

            return null;


        } finally {

            try {

                if (leitor != null) {
                    leitor.close();
                }

            } catch (Exception ignored) {
            }


            if (conexao != null) {
                conexao.disconnect();
            }
        }
    }


    /*
     * ==========================================================
     * LER JOGOS DO JSON
     * ==========================================================
     */

    private void adicionarJogosDaSteam(
            String json,
            String genero,
            Connection conexao,
            Set<Integer> idsAdicionados) {


        /*
         * A Steam retorna:
         *
         * id
         * type
         * name
         *
         * O padrão abaixo pega esses dados.
         */

        Pattern padrao =
            Pattern.compile(
                "\"id\"\\s*:\\s*(\\d+).*?" +
                "\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
                Pattern.DOTALL
            );


        Matcher matcher =
            padrao.matcher(json);


        while (matcher.find()) {


            if (idsAdicionados.size() >= TOTAL_JOGOS) {
                break;
            }


            try {

                int id =
                    Integer.parseInt(
                        matcher.group(1)
                    );


                String nome =
                    matcher.group(2);


                nome =
                    limparTexto(nome);


                if (nome == null ||
                    nome.trim().isEmpty()) {

                    continue;
                }


                /*
                 * Evita duplicados.
                 */

                if (idsAdicionados.contains(id)) {
                    continue;
                }


                /*
                 * Ignora alguns tipos de conteúdo
                 * que não queremos no catálogo.
                 */

                String nomeLower =
                    nome.toLowerCase();


                if (
                    nomeLower.contains("soundtrack") ||
                    nomeLower.contains("ost") ||
                    nomeLower.contains("demo") ||
                    nomeLower.contains("playtest")
                ) {

                    continue;
                }


                /*
                 * URL da capa.
                 *
                 * A Steam utiliza cápsulas de biblioteca
                 * verticais de 600x900.
                 */

                String imagem =
                    "https://cdn.cloudflare.steamstatic.com/steam/apps/"
                    + id
                    + "/library_600x900.jpg";


                /*
                 * Nota inicial.
                 */

                double nota =
                    gerarNota(id);


                inserirJogo(
                    conexao,
                    id,
                    nome,
                    genero,
                    nota,
                    imagem
                );


                idsAdicionados.add(id);


            } catch (Exception e) {

                System.out.println(
                    "Erro ao adicionar jogo:"
                );

                e.printStackTrace();
            }
        }
    }


    /*
     * ==========================================================
     * LIMPAR TEXTO
     * ==========================================================
     */

    private String limparTexto(String texto) {

        if (texto == null) {
            return "";
        }


        return texto
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\/", "/")
            .replace("\\n", " ")
            .replace("\\r", " ")
            .replace("&amp;", "&")
            .trim();
    }


    /*
     * ==========================================================
     * INSERIR JOGO
     * ==========================================================
     */

    private void inserirJogo(
            Connection conexao,
            int id,
            String nome,
            String genero,
            double nota,
            String imagem) {


        String sql =
            "INSERT OR IGNORE INTO jogo " +
            "(id, nome, genero, nota, imagem) " +
            "VALUES (?, ?, ?, ?, ?)";


        try (PreparedStatement ps =
                conexao.prepareStatement(sql)) {


            ps.setInt(1, id);

            ps.setString(2, nome);

            ps.setString(3, genero);

            ps.setDouble(4, nota);

            ps.setString(5, imagem);


            ps.executeUpdate();


        } catch (Exception e) {

            System.out.println(
                "Erro ao inserir jogo: " +
                nome
            );

            e.printStackTrace();
        }
    }


    /*
     * ==========================================================
     * NOTA
     * ==========================================================
     *
     * Apenas uma nota visual inicial.
     *
     * Depois podemos ligar isso às avaliações
     * dos usuários.
     */

    private double gerarNota(int id) {

        int valor =
            Math.abs(id % 11);


        double nota =
            4.0 +
            (valor * 0.1);


        if (nota > 5.0) {
            nota = 5.0;
        }


        return Math.round(
            nota * 10.0
        ) / 10.0;
    }


    /*
     * ==========================================================
     * DO GET
     * ==========================================================
     */

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {


        response.setContentType(
            "text/html;charset=UTF-8"
        );


        /*
         * Busca digitada pelo usuário.
         */

        String busca =
            request.getParameter("busca");


        /*
         * Gênero selecionado.
         */

        String genero =
            request.getParameter("genero");


        List<Jogo> jogos =
            buscarJogos(
                busca,
                genero
            );


        StringBuilder html =
            new StringBuilder();


        html.append(
            "<!DOCTYPE html>"
        );


        html.append(
            "<html lang='pt-BR'>"
        );


        html.append(
            "<head>"
        );


        html.append(
            "<meta charset='UTF-8'>"
        );


        html.append(
            "<meta name='viewport' " +
            "content='width=device-width, initial-scale=1.0'>"
        );


        html.append(
            "<title>Inventory - Jogos</title>"
        );


        /*
         * MESMA FONTE / CSS DAS OUTRAS PÁGINAS
         */

        html.append(
            "<link rel='stylesheet' href='style.css'>"
        );


        /*
         * CSS da página.
         */

        html.append("<style>");

        html.append(

            "html,body{" +
            "margin:0;" +
            "padding:0;" +
            "min-height:100%;" +
            "}" +

            "body{" +
            "font-family:inherit;" +
            "background:#0f0b16;" +
            "color:white;" +
            "}" +

            "header{" +
            "width:100%;" +
            "box-sizing:border-box;" +
            "display:flex;" +
            "align-items:center;" +
            "justify-content:space-between;" +
            "padding:18px 40px;" +
            "background:#0d0914;" +
            "border-bottom:1px solid #30263a;" +
            "}" +

            ".logo-area{" +
            "display:flex;" +
            "align-items:center;" +
            "gap:9px;" +
            "}" +

            ".logo-header{" +
            "width:40px;" +
            "height:40px;" +
            "object-fit:contain;" +
            "}" +

            ".logo-area h1{" +
            "margin:0;" +
            "color:white;" +
            "font-size:30px;" +
            "font-weight:700;" +
            "}" +

            "header nav{" +
            "display:flex;" +
            "align-items:center;" +
            "gap:28px;" +
            "}" +

            "header nav a{" +
            "color:#b9afc5;" +
            "text-decoration:none;" +
            "font-size:14px;" +
            "transition:.2s;" +
            "}" +

            "header nav a:hover{" +
            "color:#c084fc;" +
            "}" +

            "main{" +
            "max-width:1200px;" +
            "margin:auto;" +
            "padding:30px 25px 60px;" +
            "}" +

            ".inicio{" +
            "text-align:center;" +
            "padding:55px 25px;" +
            "margin-bottom:35px;" +
            "background:" +
            "radial-gradient(circle at top," +
            "rgba(124,58,237,.28)," +
            "transparent 65%),#17121f;" +
            "border:1px solid #30263a;" +
            "border-radius:18px;" +
            "box-shadow:0 15px 40px rgba(0,0,0,.3);" +
            "}" +

            ".inicio h2{" +
            "font-size:38px;" +
            "margin-bottom:12px;" +
            "color:white;" +
            "}" +

            ".inicio h2 span{" +
            "color:#a855f7;" +
            "}" +

            ".inicio p{" +
            "font-size:16px;" +
            "max-width:650px;" +
            "margin:auto;" +
            "color:#aaa1b4;" +
            "}" +

            ".busca{" +
            "background:#17131d;" +
            "border:1px solid #30263a;" +
            "border-radius:14px;" +
            "padding:25px;" +
            "margin-bottom:40px;" +
            "}" +

            ".busca h2{" +
            "color:white;" +
            "margin-bottom:15px;" +
            "}" +

            ".busca-form{" +
            "display:flex;" +
            "gap:10px;" +
            "flex-wrap:wrap;" +
            "}" +

            ".busca-form input," +
            ".busca-form select{" +
            "box-sizing:border-box;" +
            "padding:13px 15px;" +
            "background:#0f0b16;" +
            "border:1px solid #7c3aed;" +
            "border-radius:8px;" +
            "color:white;" +
            "outline:none;" +
            "}" +

            ".busca-form input{" +
            "flex:1;" +
            "min-width:220px;" +
            "}" +

            ".busca-form select{" +
            "min-width:180px;" +
            "cursor:pointer;" +
            "}" +

            ".busca-form button{" +
            "padding:12px 25px;" +
            "border:none;" +
            "border-radius:8px;" +
            "background:linear-gradient(135deg,#7c3aed,#a855f7);" +
            "color:white;" +
            "font-weight:bold;" +
            "cursor:pointer;" +
            "transition:.2s;" +
            "}" +

            ".busca-form button:hover{" +
            "transform:translateY(-2px);" +
            "}" +

            ".titulo-catalogo{" +
            "margin-bottom:20px;" +
            "}" +

            ".titulo-catalogo h2{" +
            "margin-bottom:5px;" +
            "color:white;" +
            "}" +

            ".titulo-catalogo p{" +
            "color:#aaa1b4;" +
            "}" +

            ".catalogo-jogos{" +
            "display:grid;" +
            "grid-template-columns:repeat(5,minmax(0,1fr));" +
            "gap:22px;" +
            "}" +

            ".card-jogo{" +
            "background:linear-gradient(145deg,#211a2b,#17131d);" +
            "border:1px solid #30263a;" +
            "border-radius:14px;" +
            "overflow:hidden;" +
            "text-align:center;" +
            "transition:transform .2s,border-color .2s,box-shadow .2s;" +
            "}" +

            ".card-jogo:hover{" +
            "transform:translateY(-6px);" +
            "border-color:rgba(168,85,247,.65);" +
            "box-shadow:0 15px 35px rgba(124,58,237,.18);" +
            "}" +

            ".card-jogo img{" +
            "width:100%;" +
            "height:280px;" +
            "object-fit:cover;" +
            "display:block;" +
            "transition:transform .3s;" +
            "}" +

            ".card-jogo:hover img{" +
            "transform:scale(1.035);" +
            "}" +

            ".card-jogo-conteudo{" +
            "padding:18px;" +
            "}" +

            ".card-jogo h3{" +
            "font-size:17px;" +
            "margin:0 0 8px;" +
            "min-height:40px;" +
            "display:flex;" +
            "align-items:center;" +
            "justify-content:center;" +
            "color:white;" +
            "}" +

            ".genero{" +
            "color:#aaa1b4;" +
            "font-size:13px;" +
            "margin-bottom:8px;" +
            "}" +

            ".nota{" +
            "color:#c084fc;" +
            "font-weight:bold;" +
            "margin-bottom:12px;" +
            "}" +

            ".botao-biblioteca{" +
            "display:block;" +
            "width:100%;" +
            "box-sizing:border-box;" +
            "padding:11px 15px;" +
            "margin-top:12px;" +
            "background:linear-gradient(135deg,#7c3aed,#a855f7);" +
            "color:white;" +
            "text-decoration:none;" +
            "border-radius:8px;" +
            "font-size:13px;" +
            "font-weight:bold;" +
            "transition:.2s;" +
            "}" +

            ".botao-biblioteca:hover{" +
            "transform:translateY(-2px);" +
            "}" +

            ".resultado{" +
            "color:#aaa1b4;" +
            "margin-bottom:20px;" +
            "}" +

            "@media(max-width:1000px){" +
            ".catalogo-jogos{" +
            "grid-template-columns:repeat(4,minmax(0,1fr));" +
            "}" +
            "}" +

            "@media(max-width:800px){" +
            "header{" +
            "flex-direction:column;" +
            "gap:15px;" +
            "}" +
            "header nav{" +
            "flex-wrap:wrap;" +
            "justify-content:center;" +
            "}" +
            ".catalogo-jogos{" +
            "grid-template-columns:repeat(3,minmax(0,1fr));" +
            "}" +
            "}" +

            "@media(max-width:600px){" +
            "main{" +
            "padding:20px 15px 40px;" +
            "}" +
            ".catalogo-jogos{" +
            "grid-template-columns:repeat(2,minmax(0,1fr));" +
            "gap:14px;" +
            "}" +
            ".card-jogo img{" +
            "height:220px;" +
            "}" +
            "}" +

            "@media(max-width:420px){" +
            ".catalogo-jogos{" +
            "grid-template-columns:1fr;" +
            "}" +
            ".card-jogo img{" +
            "height:300px;" +
            "}" +
            "}"
        );

        html.append("</style>");

        html.append("</head>");

        html.append("<body>");


        /*
         * ======================================================
         * HEADER
         * ======================================================
         */

        html.append("<header>");

        html.append(
            "<div class='logo-area'>"
        );

        html.append(
            "<img src='icon.png' " +
            "alt='Logo Inventory' " +
            "class='logo-header'>"
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
            "<a href='buscar-usuarios.html'>Buscar usuários</a>"
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
         * ======================================================
         * MAIN
         * ======================================================
         */

        html.append("<main>");


        html.append(
            "<section class='inicio'>"
        );

        html.append(
            "<h2>Bem-vindo ao " +
            "<span>Inventory</span></h2>"
        );

        html.append(
            "<p>Descubra jogos, avalie suas experiências " +
            "e monte sua biblioteca.</p>"
        );

        html.append(
            "</section>"
        );


        /*
         * ======================================================
         * BUSCA + FILTRO
         * ======================================================
         */

        html.append(
            "<section class='busca'>"
        );

        html.append(
            "<h2>🔎 Buscar jogo</h2>"
        );


        html.append(
            "<form class='busca-form' " +
            "method='GET' action='jogos'>"
        );


        /*
         * CAMPO DE BUSCA
         */

        html.append(
            "<input type='text' " +
            "name='busca' " +
            "placeholder='Digite o nome do jogo...' "
        );


        if (busca != null) {

            html.append(
                "value='" +
                escapeHtml(busca) +
                "' "
            );
        }


        html.append(">");


        /*
         * FILTRO DE GÊNERO
         */

        html.append(
            "<select name='genero'>"
        );


        html.append(
            "<option value=''>Todos os gêneros</option>"
        );


        for (String[] g : GENEROS) {

            html.append(
                "<option value='" +
                escapeHtml(g[0]) +
                "'"
            );


            if (
                genero != null &&
                genero.equals(g[0])
            ) {

                html.append(" selected");
            }


            html.append(
                ">" +
                escapeHtml(g[0]) +
                "</option>"
            );
        }


        html.append("</select>");


        html.append(
            "<button type='submit'>Buscar</button>"
        );


        html.append("</form>");

        html.append("</section>");


        /*
         * ======================================================
         * CATÁLOGO
         * ======================================================
         */

        html.append(
            "<section>"
        );


        html.append(
            "<div class='titulo-catalogo'>"
        );


        html.append(
            "<h2>🎮 Jogos populares</h2>"
        );


        html.append(
            "<p>Confira os jogos disponíveis no Inventory.</p>"
        );


        html.append("</div>");


        html.append(
            "<p class='resultado'>" +
            jogos.size() +
            " jogos encontrados</p>"
        );


        html.append(
            "<div class='catalogo-jogos'>"
        );


        if (jogos.isEmpty()) {

            html.append(
                "<p style='color:#aaa1b4;'>" +
                "Nenhum jogo encontrado.</p>"
            );

        } else {


            for (Jogo jogo : jogos) {

                html.append(
                    "<article class='card-jogo'>"
                );


                /*
                 * CAPA
                 */

                html.append(
                    "<img src='" +
                    escapeHtml(jogo.imagem) +
                    "' " +
                    "alt='" +
                    escapeHtml(jogo.nome) +
                    "' " +

                    "onerror=\"" +
                    "this.onerror=null;" +
                    "this.src='https://cdn.cloudflare.steamstatic.com/steam/apps/" +
                    jogo.id +
                    "/header.jpg';" +
                    "\">"
                );


                html.append(
                    "<div class='card-jogo-conteudo'>"
                );


                html.append(
                    "<h3>" +
                    escapeHtml(jogo.nome) +
                    "</h3>"
                );


                html.append(
                    "<p class='genero'>" +
                    escapeHtml(jogo.genero) +
                    "</p>"
                );


                html.append(
                    "<p class='nota'>⭐ " +
                    String.format(
                        "%.1f",
                        jogo.nota
                    ) +
                    "</p>"
                );


                html.append(
                    "<a class='botao-biblioteca' " +
                    "href='adicionar-biblioteca?id=" +
                    jogo.id +
                    "'>" +
                    "+ Minha biblioteca" +
                    "</a>"
                );


                html.append(
                    "</div>"
                );


                html.append(
                    "</article>"
                );
            }
        }


        html.append(
            "</div>"
        );


        html.append(
            "</section>"
        );


        html.append(
            "</main>"
        );


        /*
         * ======================================================
         * FOOTER
         * ======================================================
         */

        html.append(
            "<footer style='" +
            "text-align:center;" +
            "padding:25px;" +
            "background:#0d0914;" +
            "border-top:1px solid #30263a;" +
            "color:#8c8199;'>" +
            "<p>© 2026 Inventory</p>" +
            "</footer>"
        );


        html.append(
            "</body></html>"
        );


        response.getWriter().write(
            html.toString()
        );
    }


    /*
     * ==========================================================
     * BUSCAR NO SQLITE
     * ==========================================================
     */

    private List<Jogo> buscarJogos(
            String busca,
            String genero) {


        List<Jogo> lista =
            new ArrayList<Jogo>();


        StringBuilder sql =
            new StringBuilder(
                "SELECT id,nome,genero,nota,imagem " +
                "FROM jogo WHERE 1=1"
            );


        List<String> parametros =
            new ArrayList<String>();


        if (
            busca != null &&
            !busca.trim().isEmpty()
        ) {

            sql.append(
                " AND LOWER(nome) LIKE ?"
            );

            parametros.add(
                "%" +
                busca.toLowerCase() +
                "%"
            );
        }


        if (
            genero != null &&
            !genero.trim().isEmpty()
        ) {

            sql.append(
                " AND genero = ?"
            );

            parametros.add(
                genero
            );
        }


        sql.append(
            " ORDER BY nome ASC"
        );


        try (
            Connection conexao =
                Conexao.conectar();

            PreparedStatement ps =
                conexao.prepareStatement(
                    sql.toString()
                )
        ) {


            int posicao = 1;


            for (String parametro : parametros) {

                ps.setString(
                    posicao++,
                    parametro
                );
            }


            ResultSet rs =
                ps.executeQuery();


            while (rs.next()) {

                Jogo jogo =
                    new Jogo();


                jogo.id =
                    rs.getInt("id");


                jogo.nome =
                    rs.getString("nome");


                jogo.genero =
                    rs.getString("genero");


                jogo.nota =
                    rs.getDouble("nota");


                jogo.imagem =
                    rs.getString("imagem");


                lista.add(jogo);
            }


        } catch (Exception e) {

            System.out.println(
                "Erro ao buscar jogos:"
            );

            e.printStackTrace();
        }


        return lista;
    }


    /*
     * ==========================================================
     * ESCAPAR HTML
     * ==========================================================
     */

    private String escapeHtml(String texto) {

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
     * CLASSE JOGO
     * ==========================================================
     */

    private static class Jogo {

        int id;

        String nome;

        String genero;

        double nota;

        String imagem;
    }
}