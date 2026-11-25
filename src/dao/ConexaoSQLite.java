package dao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class ConexaoSQLite {
    private static final String DB_FILE = "fornecedores.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;

    public static Connection conectar() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void inicializarBancoSeNecessario(String caminhoSchemaSql) {
        Path dbPath = Path.of(DB_FILE);
        boolean bancoNovo = !Files.exists(dbPath) || dbPath.toFile().length() == 0;
        if (!bancoNovo) {
            return;
        }
        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            String sql = Files.readString(Path.of(caminhoSchemaSql), StandardCharsets.UTF_8);
            StringBuilder comando = new StringBuilder();
            for (String linha : sql.split("\\R")) {
                String limpa = linha.trim();
                if (limpa.isEmpty() || limpa.startsWith("--")) continue;
                comando.append(limpa).append(" ");
                if (limpa.endsWith(";")) {
                    stmt.execute(comando.toString());
                    comando.setLength(0);
                }
            }
            System.out.println("Banco SQLite criado e domínios carregados com sucesso.");
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Erro ao inicializar banco SQLite: " + e.getMessage(), e);
        }
    }
}