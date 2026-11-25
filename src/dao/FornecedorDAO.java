package dao;

import dominio.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FornecedorDAO implements IDAO {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Connection conectar() throws SQLException {
        return ConexaoSQLite.conectar();
    }

    private Long obterUltimoId(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            return rs.next() ? rs.getLong(1) : null;
        }
    }

    @Override
    public void salvar(EntidadeDominio entidade) {
        Fornecedor f = (Fornecedor) entidade;
        try (Connection conn = conectar()) {
            conn.setAutoCommit(false);
            try {
                // 1. Inserir Telefone
                String sqlTel = "INSERT INTO telefones (tel_tpt_id, tel_ddd, tel_numero) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlTel)) {
                    ps.setLong(1, f.getTelefone().getTipo().getId());
                    ps.setString(2, f.getTelefone().getDdd());
                    ps.setString(3, f.getTelefone().getNumero());
                    ps.executeUpdate();
                }
                Long telId = obterUltimoId(conn);
                f.getTelefone().setId(telId);

                // 2. Inserir Endereco
                Endereco end = f.getEndereco();
                String sqlEnd = "INSERT INTO enderecos (end_tpl_id, end_logradouro, end_numero, end_complemento, "
                        + "end_bairro, end_cep, end_cidade, end_est_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlEnd)) {
                    ps.setLong(1, end.getTipoLogradouro().getId());
                    ps.setString(2, end.getLogradouro());
                    ps.setString(3, end.getNumero());
                    if (end.getComplemento() == null || end.getComplemento().isBlank()) {
                        ps.setNull(4, Types.VARCHAR);
                    } else {
                        ps.setString(4, end.getComplemento());
                    }
                    ps.setString(5, end.getBairro());
                    ps.setString(6, end.getCep());
                    ps.setString(7, end.getCidade());
                    ps.setLong(8, end.getEstado().getId());
                    ps.executeUpdate();
                }
                Long endId = obterUltimoId(conn);
                end.setId(endId);

                // 3. Inserir Fornecedor
                String sqlFor = "INSERT INTO fornecedores (for_codigo, for_razao_social, for_nome_fantasia, "
                        + "for_cnpj, for_email, for_status, for_tel_id, for_end_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlFor)) {
                    ps.setString(1, f.getCodigo());
                    ps.setString(2, f.getRazaoSocial());
                    ps.setString(3, f.getNomeFantasia());
                    ps.setString(4, f.getCnpj());
                    ps.setString(5, f.getEmail());
                    ps.setString(6, f.getStatus().name());
                    ps.setLong(7, telId);
                    ps.setLong(8, endId);
                    ps.executeUpdate();
                }
                f.setId(obterUltimoId(conn));
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar fornecedor: " + e.getMessage(), e);
        }
    }

    @Override
    public void alterar(EntidadeDominio entidade) {
        Fornecedor f = (Fornecedor) entidade;
        try (Connection conn = conectar()) {
            conn.setAutoCommit(false);
            try {
                boolean alteracaoDeStatus = f.getHistoricoStatus() != null && !f.getHistoricoStatus().isEmpty();

                if (alteracaoDeStatus) {
                    // Fluxo A4 / A5: altera status e grava historico_status_fornecedor
                    HistoricoStatusFornecedor hist = f.getHistoricoStatus().get(f.getHistoricoStatus().size() - 1);
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE fornecedores SET for_status = ? WHERE for_id = ?")) {
                        ps.setString(1, hist.getStatusAplicado().name());
                        ps.setLong(2, f.getId());
                        ps.executeUpdate();
                    }

                    String sqlHist = "INSERT INTO historico_status_fornecedor "
                            + "(hsf_for_id, hsf_status_aplicado, hsf_justificativa, hsf_cin_id, hsf_cat_id, hsf_data_hora) "
                            + "VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlHist)) {
                        ps.setLong(1, f.getId());
                        ps.setString(2, hist.getStatusAplicado().name());
                        ps.setString(3, hist.getJustificativa());
                        if (hist.getCategoriaInativacao() != null && hist.getCategoriaInativacao().getId() != null) {
                            ps.setLong(4, hist.getCategoriaInativacao().getId());
                            ps.setNull(5, Types.INTEGER);
                        } else {
                            ps.setNull(4, Types.INTEGER);
                            ps.setLong(5, hist.getCategoriaAtivacao().getId());
                        }
                        LocalDateTime dh = hist.getDataHora() != null ? hist.getDataHora() : LocalDateTime.now();
                        ps.setString(6, dh.format(FMT));
                        ps.executeUpdate();
                    }
                    f.setStatus(hist.getStatusAplicado());
                } else {
                    // Fluxo A3: altera dados cadastrais mantendo o status atual (P8.2)
                    Long telId = null;
                    Long endId = null;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT for_tel_id, for_end_id FROM fornecedores WHERE for_id = ?")) {
                        ps.setLong(1, f.getId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                telId = rs.getLong("for_tel_id");
                                endId = rs.getLong("for_end_id");
                            }
                        }
                    }

                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE telefones SET tel_tpt_id = ?, tel_ddd = ?, tel_numero = ? WHERE tel_id = ?")) {
                        ps.setLong(1, f.getTelefone().getTipo().getId());
                        ps.setString(2, f.getTelefone().getDdd());
                        ps.setString(3, f.getTelefone().getNumero());
                        ps.setLong(4, telId);
                        ps.executeUpdate();
                    }

                    Endereco end = f.getEndereco();
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE enderecos SET end_tpl_id = ?, end_logradouro = ?, end_numero = ?, "
                            + "end_complemento = ?, end_bairro = ?, end_cep = ?, end_cidade = ?, end_est_id = ? "
                            + "WHERE end_id = ?")) {
                        ps.setLong(1, end.getTipoLogradouro().getId());
                        ps.setString(2, end.getLogradouro());
                        ps.setString(3, end.getNumero());
                        if (end.getComplemento() == null || end.getComplemento().isBlank()) {
                            ps.setNull(4, Types.VARCHAR);
                        } else {
                            ps.setString(4, end.getComplemento());
                        }
                        ps.setString(5, end.getBairro());
                        ps.setString(6, end.getCep());
                        ps.setString(7, end.getCidade());
                        ps.setLong(8, end.getEstado().getId());
                        ps.setLong(9, endId);
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE fornecedores SET for_razao_social = ?, for_nome_fantasia = ?, "
                            + "for_cnpj = ?, for_email = ? WHERE for_id = ?")) {
                        ps.setString(1, f.getRazaoSocial());
                        ps.setString(2, f.getNomeFantasia());
                        ps.setString(3, f.getCnpj());
                        ps.setString(4, f.getEmail());
                        ps.setLong(5, f.getId());
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar fornecedor: " + e.getMessage(), e);
        }
    }

    @Override
    public void excluir(EntidadeDominio entidade) {
        throw new UnsupportedOperationException("Fornecedor não é excluído fisicamente (use inativação - RN0084).");
    }

    @Override
    public List<EntidadeDominio> consultar(EntidadeDominio entidade) {
        Fornecedor filtro = (entidade instanceof Fornecedor) ? (Fornecedor) entidade : new Fornecedor();
        List<EntidadeDominio> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT f.for_id, f.for_codigo, f.for_razao_social, f.for_nome_fantasia, f.for_cnpj, f.for_email, f.for_status, "
          + "t.tel_id, t.tel_ddd, t.tel_numero, tt.tpt_id, tt.tpt_descricao, "
          + "e.end_id, e.end_logradouro, e.end_numero, e.end_complemento, e.end_bairro, e.end_cep, e.end_cidade, "
          + "tl.tpl_id, tl.tpl_descricao, "
          + "est.est_id, est.est_nome, est.est_uf, "
          + "p.pai_id, p.pai_nome, p.pai_sigla "
          + "FROM fornecedores f "
          + "INNER JOIN telefones t ON f.for_tel_id = t.tel_id "
          + "INNER JOIN tipos_telefone tt ON t.tel_tpt_id = tt.tpt_id "
          + "INNER JOIN enderecos e ON f.for_end_id = e.end_id "
          + "INNER JOIN tipos_logradouro tl ON e.end_tpl_id = tl.tpl_id "
          + "INNER JOIN estados est ON e.end_est_id = est.est_id "
          + "INNER JOIN paises p ON est.est_pai_id = p.pai_id "
          + "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (filtro.getId() != null) {
            sql.append("AND f.for_id = ? ");
            params.add(filtro.getId());
        }
        if (filtro.getCodigo() != null && !filtro.getCodigo().isBlank()) {
            sql.append("AND UPPER(f.for_codigo) LIKE UPPER(?) ");
            params.add("%" + filtro.getCodigo().trim() + "%");
        }
        if (filtro.getRazaoSocial() != null && !filtro.getRazaoSocial().isBlank()) {
            sql.append("AND UPPER(f.for_razao_social) LIKE UPPER(?) ");
            params.add("%" + filtro.getRazaoSocial().trim() + "%");
        }
        if (filtro.getNomeFantasia() != null && !filtro.getNomeFantasia().isBlank()) {
            sql.append("AND UPPER(f.for_nome_fantasia) LIKE UPPER(?) ");
            params.add("%" + filtro.getNomeFantasia().trim() + "%");
        }
        if (filtro.getCnpj() != null && !filtro.getCnpj().isBlank()) {
            String cnpjLimpo = filtro.getCnpj().replaceAll("\\D", "");
            if (!cnpjLimpo.isEmpty()) {
                sql.append("AND f.for_cnpj LIKE ? ");
                params.add("%" + cnpjLimpo + "%");
            }
        }
        if (filtro.getStatus() != null) {
            sql.append("AND f.for_status = ? ");
            params.add(filtro.getStatus().name());
        }
        if (filtro.getEndereco() != null) {
            if (filtro.getEndereco().getCidade() != null && !filtro.getEndereco().getCidade().isBlank()) {
                sql.append("AND UPPER(e.end_cidade) LIKE UPPER(?) ");
                params.add("%" + filtro.getEndereco().getCidade().trim() + "%");
            }
            if (filtro.getEndereco().getEstado() != null) {
                Estado estFiltro = filtro.getEndereco().getEstado();
                if (estFiltro.getId() != null) {
                    sql.append("AND est.est_id = ? ");
                    params.add(estFiltro.getId());
                } else if (estFiltro.getUf() != null && !estFiltro.getUf().isBlank()) {
                    sql.append("AND UPPER(est.est_uf) = UPPER(?) ");
                    params.add(estFiltro.getUf().trim());
                }
            }
        }

        sql.append("ORDER BY f.for_id ASC");

        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pais pais = new Pais(rs.getLong("pai_id"), rs.getString("pai_nome"), rs.getString("pai_sigla"));
                    Estado estado = new Estado(rs.getLong("est_id"), rs.getString("est_nome"), rs.getString("est_uf"), pais);
                    TipoLogradouro tipoLog = new TipoLogradouro(rs.getLong("tpl_id"), rs.getString("tpl_descricao"));
                    Endereco endereco = new Endereco(
                        rs.getLong("end_id"), tipoLog, rs.getString("end_logradouro"),
                        rs.getString("end_numero"), rs.getString("end_complemento"),
                        rs.getString("end_bairro"), rs.getString("end_cep"),
                        rs.getString("end_cidade"), estado
                    );
                    TipoTelefone tipoTel = new TipoTelefone(rs.getLong("tpt_id"), rs.getString("tpt_descricao"));
                    Telefone telefone = new Telefone(
                        rs.getLong("tel_id"), tipoTel, rs.getString("tel_ddd"), rs.getString("tel_numero")
                    );
                    Fornecedor f = new Fornecedor(
                        rs.getLong("for_id"), rs.getString("for_codigo"),
                        rs.getString("for_razao_social"), rs.getString("for_nome_fantasia"),
                        rs.getString("for_cnpj"), rs.getString("for_email"),
                        StatusFornecedor.valueOf(rs.getString("for_status")),
                        telefone, endereco
                    );
                    lista.add(f);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar fornecedores: " + e.getMessage(), e);
        }
        return lista;
    }
}