package dao;

import dominio.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DominioDAO implements IDAO {

    private Connection conectar() throws SQLException {
        return ConexaoSQLite.conectar();
    }

    @Override
    public void salvar(EntidadeDominio entidade) {
        throw new UnsupportedOperationException("Domínios são carregados via script de implantação (RNF0013).");
    }

    @Override
    public void alterar(EntidadeDominio entidade) {
        throw new UnsupportedOperationException("Domínios são somente leitura neste caso de uso.");
    }

    @Override
    public void excluir(EntidadeDominio entidade) {
        throw new UnsupportedOperationException("Domínios são somente leitura neste caso de uso.");
    }

    @Override
    public List<EntidadeDominio> consultar(EntidadeDominio entidade) {
        List<EntidadeDominio> resultado = new ArrayList<>();
        try (Connection conn = conectar(); Statement st = conn.createStatement()) {
            if (entidade instanceof Pais) {
                try (ResultSet rs = st.executeQuery("SELECT pai_id, pai_nome, pai_sigla FROM paises ORDER BY pai_nome")) {
                    while (rs.next()) {
                        resultado.add(new Pais(rs.getLong("pai_id"), rs.getString("pai_nome"), rs.getString("pai_sigla")));
                    }
                }
            } else if (entidade instanceof Estado) {
                String sql = "SELECT e.est_id, e.est_nome, e.est_uf, p.pai_id, p.pai_nome, p.pai_sigla "
                        + "FROM estados e INNER JOIN paises p ON e.est_pai_id = p.pai_id ORDER BY e.est_uf";
                try (ResultSet rs = st.executeQuery(sql)) {
                    while (rs.next()) {
                        Pais p = new Pais(rs.getLong("pai_id"), rs.getString("pai_nome"), rs.getString("pai_sigla"));
                        resultado.add(new Estado(rs.getLong("est_id"), rs.getString("est_nome"), rs.getString("est_uf"), p));
                    }
                }
            } else if (entidade instanceof TipoLogradouro) {
                try (ResultSet rs = st.executeQuery("SELECT tpl_id, tpl_descricao FROM tipos_logradouro ORDER BY tpl_descricao")) {
                    while (rs.next()) {
                        resultado.add(new TipoLogradouro(rs.getLong("tpl_id"), rs.getString("tpl_descricao")));
                    }
                }
            } else if (entidade instanceof TipoTelefone) {
                try (ResultSet rs = st.executeQuery("SELECT tpt_id, tpt_descricao FROM tipos_telefone ORDER BY tpt_descricao")) {
                    while (rs.next()) {
                        resultado.add(new TipoTelefone(rs.getLong("tpt_id"), rs.getString("tpt_descricao")));
                    }
                }
            } else if (entidade instanceof CategoriaInativacao) {
                try (ResultSet rs = st.executeQuery("SELECT cin_id, cin_descricao FROM categorias_inativacao ORDER BY cin_descricao")) {
                    while (rs.next()) {
                        resultado.add(new CategoriaInativacao(rs.getLong("cin_id"), rs.getString("cin_descricao")));
                    }
                }
            } else if (entidade instanceof CategoriaAtivacao) {
                try (ResultSet rs = st.executeQuery("SELECT cat_id, cat_descricao FROM categorias_ativacao ORDER BY cat_descricao")) {
                    while (rs.next()) {
                        resultado.add(new CategoriaAtivacao(rs.getLong("cat_id"), rs.getString("cat_descricao")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar domínio: " + e.getMessage(), e);
        }
        return resultado;
    }
}