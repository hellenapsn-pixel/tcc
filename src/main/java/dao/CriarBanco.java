package dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CriarBanco {

    public static void criarTabela() {
        try {
            Connection conexao = Conexao.conectar();

            if (conexao == null) {
                System.out.println("Não foi possível conectar ao banco.");
                return;
            }

            Statement stmt = conexao.createStatement();

            // TABELA USUARIO
            String tabelaUsuario = "CREATE TABLE IF NOT EXISTS usuario ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "nome TEXT NOT NULL,"
                    + "username TEXT,"
                    + "email TEXT NOT NULL UNIQUE,"
                    + "senha TEXT NOT NULL,"
                    + "foto TEXT,"
                    + "bio TEXT,"
                    + "data_nascimento TEXT,"
                    + "pais TEXT,"
                    + "plataforma_favorita TEXT"
                    + ")";
            stmt.execute(tabelaUsuario);

            // VERIFICAR E ADICIONAR USERNAME
            boolean usernameExiste = false;
            ResultSet colunas = stmt.executeQuery("PRAGMA table_info(usuario)");
            while (colunas.next()) {
                if ("username".equalsIgnoreCase(colunas.getString("name"))) {
                    usernameExiste = true;
                    break;
                }
            }
            colunas.close();

            if (!usernameExiste) {
                stmt.execute("ALTER TABLE usuario ADD COLUMN username TEXT");
            }

            stmt.execute("UPDATE usuario SET username = 'usuario' || id WHERE username IS NULL OR username = ''");
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_usuario_username ON usuario(username)");

            // TABELA JOGO
            String tabelaJogo = "CREATE TABLE IF NOT EXISTS jogo ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "titulo TEXT NOT NULL,"
                    + "descricao TEXT,"
                    + "genero TEXT,"
                    + "plataforma TEXT,"
                    + "ano_lancamento INTEGER,"
                    + "capa TEXT"
                    + ")";
            stmt.execute(tabelaJogo);

            // DEMOIS TABELAS
            stmt.execute("CREATE TABLE IF NOT EXISTS seguidor (id INTEGER PRIMARY KEY AUTOINCREMENT, id_seguidor INTEGER NOT NULL, id_seguido INTEGER NOT NULL, data_seguida TEXT DEFAULT CURRENT_TIMESTAMP, UNIQUE(id_seguidor, id_seguido), FOREIGN KEY(id_seguidor) REFERENCES usuario(id), FOREIGN KEY(id_seguido) REFERENCES usuario(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS biblioteca (id INTEGER PRIMARY KEY AUTOINCREMENT, id_usuario INTEGER NOT NULL, id_jogo INTEGER NOT NULL, status TEXT DEFAULT 'quero jogar', data_adicionado TEXT DEFAULT CURRENT_TIMESTAMP, horas_jogadas REAL DEFAULT 0, UNIQUE(id_usuario, id_jogo), FOREIGN KEY(id_usuario) REFERENCES usuario(id), FOREIGN KEY(id_jogo) REFERENCES jogo(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS avaliacao (id INTEGER PRIMARY KEY AUTOINCREMENT, id_usuario INTEGER NOT NULL, id_jogo INTEGER NOT NULL, nota REAL NOT NULL, comentario TEXT, horas_jogadas REAL DEFAULT 0, data_avaliacao TEXT DEFAULT CURRENT_TIMESTAMP, UNIQUE(id_usuario, id_jogo), FOREIGN KEY(id_usuario) REFERENCES usuario(id), FOREIGN KEY(id_jogo) REFERENCES jogo(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS favorito (id INTEGER PRIMARY KEY AUTOINCREMENT, id_usuario INTEGER NOT NULL, id_jogo INTEGER NOT NULL, data_adicionado TEXT DEFAULT CURRENT_TIMESTAMP, UNIQUE(id_usuario, id_jogo), FOREIGN KEY(id_usuario) REFERENCES usuario(id), FOREIGN KEY(id_jogo) REFERENCES jogo(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS lista (id INTEGER PRIMARY KEY AUTOINCREMENT, id_usuario INTEGER NOT NULL, nome TEXT NOT NULL, data_criacao TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(id_usuario) REFERENCES usuario(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS lista_jogo (id INTEGER PRIMARY KEY AUTOINCREMENT, id_lista INTEGER NOT NULL, id_jogo INTEGER NOT NULL, data_adicionado TEXT DEFAULT CURRENT_TIMESTAMP, UNIQUE(id_lista, id_jogo), FOREIGN KEY(id_lista) REFERENCES lista(id), FOREIGN KEY(id_jogo) REFERENCES jogo(id))");

            // POPULAR DADOS
            adicionarJogos(stmt);
            adicionarRoblox(stmt);
            adicionar250Jogos(stmt);

            stmt.close();
            conexao.close();
            System.out.println("Banco do Inventory atualizado com sucesso!");

        } catch (Exception e) {
            System.out.println("ERRO AO ATUALIZAR O BANCO:");
            e.printStackTrace();
        }
    }

    private static void adicionarJogos(Statement stmt) throws Exception {
        String[][] jogos = {
            {"Resident Evil 4", "Terror e ação com Leon S. Kennedy.", "Terror / Ação", "PlayStation / Xbox / PC", "2023", "https://images.igdb.com/igdb/image/upload/t_cover_big/co1r7f.jpg"},
            {"The Last of Us Part I", "Uma jornada em um mundo pós-apocalíptico.", "Ação / Aventura", "PlayStation / PC", "2022", "https://images.igdb.com/igdb/image/upload/t_cover_big/co5s5x.jpg"},
            {"God of War Ragnarök", "Kratos e Atreus enfrentam o destino dos deuses.", "Ação / Aventura", "PlayStation / PC", "2022", "https://images.igdb.com/igdb/image/upload/t_cover_big/co5vmg.jpg"},
            {"Minecraft", "Explore, construa e sobreviva em um mundo de blocos.", "Sandbox", "PC / PlayStation / Xbox / Nintendo", "2011", "https://images.igdb.com/igdb/image/upload/t_cover_big/co49x5.jpg"},
            {"Red Dead Redemption 2", "Uma grande aventura no Velho Oeste.", "Ação / Aventura", "PlayStation / Xbox / PC", "2018", "https://images.igdb.com/igdb/image/upload/t_cover_big/co1q1f.jpg"},
            {"Grand Theft Auto V", "Acompanhe três criminosos em Los Santos.", "Ação / Mundo Aberto", "PlayStation / Xbox / PC", "2013", "https://images.igdb.com/igdb/image/upload/t_cover_big/co2lbd.jpg"},
            {"Silent Hill 2", "Uma jornada assustadora pela cidade de Silent Hill.", "Terror", "PlayStation / Xbox / PC", "2024", "https://images.igdb.com/igdb/image/upload/t_cover_big/co7v9g.jpg"},
            {"Elden Ring", "Explore um enorme mundo de fantasia e desafios.", "RPG / Ação", "PlayStation / Xbox / PC", "2022", "https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.jpg"},
            {"Resident Evil Village", "Ethan Winters enfrenta novos horrores.", "Terror / Ação", "PlayStation / Xbox / PC", "2021", "https://images.igdb.com/igdb/image/upload/t_cover_big/co2l9z.jpg"},
            {"The Witcher 3", "Geralt procura por sua filha adotiva.", "RPG / Aventura", "PlayStation / Xbox / PC / Nintendo", "2015", "https://images.igdb.com/igdb/image/upload/t_cover_big/co1wyy.jpg"},
            {"Cyberpunk 2077", "Explore Night City em um futuro tecnológico.", "RPG / Ação", "PlayStation / Xbox / PC", "2020", "https://images.igdb.com/igdb/image/upload/t_cover_big/co2rzc.jpg"},
            {"Marvel's Spider-Man 2", "Peter Parker e Miles Morales protegem Nova York.", "Ação / Aventura", "PlayStation / PC", "2023", "https://images.igdb.com/igdb/image/upload/t_cover_big/co6v1s.jpg"}
        };

        for (String[] jogo : jogos) {
            inserirJogo(stmt, jogo[0], jogo[1], jogo[2], jogo[3], Integer.parseInt(jogo[4]), jogo[5]);
        }
    }

    private static void adicionarRoblox(Statement stmt) throws Exception {
        inserirJogo(stmt, "Roblox", "Plataforma com milhares de experiências criadas pela comunidade.", "Sandbox / Aventura", "PC / Xbox / Mobile", 2006, "https://images.igdb.com/igdb/image/upload/t_cover_big/co49z9.jpg");
    }

    private static void inserirJogo(Statement stmt, String titulo, String descricao, String genero, String plataforma, int ano, String capa) throws Exception {
        String verificar = "SELECT id FROM jogo WHERE LOWER(titulo) = LOWER('" + titulo.replace("'", "''") + "')";
        ResultSet resultado = stmt.executeQuery(verificar);
        boolean existe = resultado.next();
        resultado.close();

        if (existe) return;

        String sql = "INSERT INTO jogo (titulo, descricao, genero, plataforma, ano_lancamento, capa) VALUES ("
                + "'" + titulo.replace("'", "''") + "',"
                + "'" + descricao.replace("'", "''") + "',"
                + "'" + genero.replace("'", "''") + "',"
                + "'" + plataforma.replace("'", "''") + "',"
                + (ano > 0 ? ano : "NULL") + ","
                + "'" + capa.replace("'", "''") + "'"
                + ")";

        stmt.executeUpdate(sql);
        System.out.println("Jogo adicionado: " + titulo);
    }

    private static void adicionar250Jogos(Statement stmt) throws Exception {
        System.out.println("========================================");
        System.out.println("Buscando 250 novos jogos via SteamSpy...");
        System.out.println("========================================");

        int adicionados = 0;
        int pagina = 0;

        while (adicionados < 250 && pagina < 50) {
            try {
                URL url = new URL("https://steamspy.com/api.php?request=all&page=" + pagina);
                HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("GET");
                
                // USER-AGENT ADICIONADO PARA EVITAR BLOQUEIO DE NUVEM
                conexao.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conexao.setConnectTimeout(15000);
                conexao.setReadTimeout(30000);

                int codigo = conexao.getResponseCode();
                if (codigo != 200) {
                    System.out.println("Erro na API SteamSpy. Código HTTP: " + codigo);
                    conexao.disconnect();
                    break;
                }

                BufferedReader leitor = new BufferedReader(new InputStreamReader(conexao.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder json = new StringBuilder();
                String linha;
                while ((linha = leitor.readLine()) != null) {
                    json.append(linha);
                }
                leitor.close();
                conexao.disconnect();

                Pattern padrao = Pattern.compile("\"appid\"\\s*:\\s*(\\d+).*?\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"", Pattern.DOTALL);
                Matcher matcher = padrao.matcher(json.toString());

                boolean encontrouNaPagina = false;

                while (matcher.find() && adicionados < 250) {
                    encontrouNaPagina = true;
                    int appId;
                    try {
                        appId = Integer.parseInt(matcher.group(1));
                    } catch (Exception erro) {
                        continue;
                    }

                    String titulo = matcher.group(2).replace("\\\"", "\"").replace("\\\\", "\\").trim();
                    if (titulo.isEmpty() || titulo.equalsIgnoreCase("Steam")) continue;

                    // URL DE CAPA CORRIGIDA PARA EXIBIÇÃO EXTERNA (HEADER DA STEAM)
                    String capa = "https://cdn.akamai.steamstatic.com/steam/apps/" + appId + "/header.jpg";

                    String verificar = "SELECT id FROM jogo WHERE LOWER(titulo) = LOWER('" + titulo.replace("'", "''") + "')";
                    ResultSet resultado = stmt.executeQuery(verificar);
                    boolean existe = resultado.next();
                    resultado.close();

                    if (!existe) {
                        String sql = "INSERT INTO jogo (titulo, descricao, genero, plataforma, ano_lancamento, capa) VALUES ("
                                + "'" + titulo.replace("'", "''") + "',"
                                + "'Jogo disponível na Steam.',"
                                + "'Ação',"
                                + "'PC',"
                                + "NULL,"
                                + "'" + capa + "'"
                                + ")";

                        stmt.executeUpdate(sql);
                        adicionados++;
                        System.out.println("Novo jogo " + adicionados + "/250: " + titulo);
                    }
                }

                pagina++;
                if (!encontrouNaPagina) break;

                // Pequeno delay para evitar sobrecarregar a API
                Thread.sleep(500);

            } catch (Exception erroPagina) {
                System.out.println("Erro na página " + pagina + ": " + erroPagina.getMessage());
                pagina++;
            }
        }

        System.out.println("========================================");
        System.out.println("NOVOS JOGOS ADICIONADOS COM SUCESSO: " + adicionados);
        System.out.println("========================================");
    }
}