package dao;

import dominio.EntidadeDominio;
import dominio.LogTransacao;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogTransacaoDAO implements IDAO {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Connection conectar() throws SQLException {
        return ConexaoSQLite.conectar();
    }

    @Override
    public void salvar(EntidadeDominio entidade) {
        LogTransacao log = (LogTransacao) entidade;
        String sql = "INSERT INTO logs_transacao (log_for_id, log_operacao, log_data_hora, "
                + "log_usuario_responsavel, log_dados_alterados) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, log.getFornecedorId());
            ps.setString(2, log.getOperacao().name());
            LocalDateTime dh = log.getDataHora() != null ? log.getDataHora() : LocalDateTime.now();
            ps.setString(3, dh.format(FMT));
            ps.setString(4, log.getUsuarioResponsavel());
            ps.setString(5, log.getDadosAlterados());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao gravar log de transação: " + e.getMessage(), e);
        }
    }

    @Override
    public void alterar(EntidadeDominio entidade) {
        throw new UnsupportedOperationException("Logs de transação são imutáveis.");
    }

    @Override
    public void excluir(EntidadeDominio entidade) {
        throw new UnsupportedOperationException("Logs de transação não podem ser excluídos.");
    }

    @Override
    public List<EntidadeDominio> consultar(EntidadeDominio entidade) {
        return new ArrayList<>();
    }
}