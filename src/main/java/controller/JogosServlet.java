package controller;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class JogosServlet {

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

            return conexao;

        } catch (Exception e) {
            System.out.println("=================================");
            System.out.println("ERRO AO CONECTAR COM SQLITE:");
            System.out.println("=================================");
            e.printStackTrace();

            return null;
        }
    }

    public static String getPastaDados() {
        return PASTA_DADOS;
    }
}