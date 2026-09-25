package controller;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Conexao {

    /*
     * Pasta onde ficarão os dados permanentes
     *
     * No Railway, montamos o Volume em /app/data
     * No Windows local: C:\GameBoxdUploads\data
     */

    private static final String PASTA_DADOS;
    private static final String URL;

    static {
        String sistema = System.getProperty("os.name").toLowerCase();

        if (sistema.contains("win")) {
            PASTA_DADOS = "C:/GameBoxdUploads/data";
        } else {
            PASTA_DADOS = "/app/data";
        }

        File diretorio = new File(PASTA_DADOS);

        if (!diretorio.exists()) {
            diretorio.mkdirs();
        }

        // O JDBC do SQLite exige barras normais "/" mesmo no Windows
        URL = "jdbc:sqlite:" + PASTA_DADOS + "/gameboxd.db";
    }

    /*
     * Conexão com o banco SQLite
     */
    public static Connection conectar() {
        try {
            Class.forName("org.sqlite.JDBC");

            Connection conexao = DriverManager.getConnection(URL);

            System.out.println("=================================");
            System.out.println("CONEXAO COM SQLITE OK!");
            System.out.println("BANCO: " + URL);
            System.out.println("=================================");

            // Garante que as tabelas existam antes de realizar consultas
            criarTabelasSeNaoExistirem(conexao);

            return conexao;

        } catch (Exception e) {
            System.out.println("=================================");
            System.out.println("ERRO AO CONECTAR COM SQLITE:");
            System.out.println("=================================");
            e.printStackTrace();

            return null;
        }
    }

    /*
     * Cria a tabela 'jogo' (e outras se necessário) caso o banco seja novo
     */
    private static void criarTabelasSeNaoExistirem(Connection conexao) {
        String sqlJogo = "CREATE TABLE IF NOT EXISTS jogo (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "titulo TEXT NOT NULL, " +
                         "genero TEXT, " +
                         "plataforma TEXT, " +
                         "ano_lancamento INTEGER, " +
                         "capa TEXT" +
                         ");";

        try (Statement stmt = conexao.createStatement()) {
            stmt.execute(sqlJogo);
        } catch (Exception e) {
            System.out.println("ERRO AO CRIAR TABELAS:");
            e.printStackTrace();
        }
    }

    public static String getPastaDados() {
        return PASTA_DADOS;
    }
}