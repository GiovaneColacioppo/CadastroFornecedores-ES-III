package controle;

import dominio.*;
import java.util.*;

public class CtrlFornecedor {
    private final Fachada fachada;

    public CtrlFornecedor() {
        this.fachada = new Fachada();
    }

    private Long parseLong(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Long.parseLong(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Constrói o grafo de domínio de baixo para cima (metodologia ARQ6)
    public Fornecedor definirFornecedor(Map<String, String> dados) {
        Pais pais = new Pais(parseLong(dados.get("paisId")), null, null);
        Estado estado = new Estado(parseLong(dados.get("estadoId")), null, dados.get("estadoUf"), pais);
        TipoLogradouro tipoLog = new TipoLogradouro(parseLong(dados.get("tipoLogradouroId")), null);
        Endereco endereco = new Endereco(
            null, tipoLog, dados.get("logradouro"), dados.get("numero"),
            dados.get("complemento"), dados.get("bairro"), dados.get("cep"),
            dados.get("cidade"), estado
        );

        TipoTelefone tipoTel = new TipoTelefone(parseLong(dados.get("tipoTelefoneId")), null);
        Telefone telefone = new Telefone(null, tipoTel, dados.get("ddd"), dados.get("numeroTelefone"));

        StatusFornecedor status = null;
        String statusStr = dados.get("status");
        if (statusStr != null && !statusStr.isBlank()) {
            status = StatusFornecedor.valueOf(statusStr.trim().toUpperCase());
        }

        Fornecedor fornecedor = new Fornecedor(
            parseLong(dados.get("id")),
            dados.get("codigo"),
            dados.get("razaoSocial"),
            dados.get("nomeFantasia"),
            dados.get("cnpj"),
            dados.get("email"),
            status,
            telefone,
            endereco
        );
        fornecedor.setUsuarioResponsavel(dados.getOrDefault("usuario", "Carlos Mendes / Administrador"));
        return fornecedor;
    }

    public String salvar(Map<String, String> dados, Fornecedor[] outFornecedor) {
        Fornecedor f = definirFornecedor(dados);
        String erro = fachada.salvar(f);
        if (outFornecedor != null && outFornecedor.length > 0) outFornecedor[0] = f;
        return erro;
    }

    public String alterar(Long id, Map<String, String> dados) {
        dados.put("id", String.valueOf(id));
        Fornecedor f = definirFornecedor(dados);
        return fachada.alterar(f);
    }

    public String inativar(Long id, Map<String, String> dados) {
        Fornecedor f = new Fornecedor();
        f.setId(id);
        f.setUsuarioResponsavel(dados.getOrDefault("usuario", "Carlos Mendes / Administrador"));

        CategoriaInativacao cat = new CategoriaInativacao(parseLong(dados.get("categoriaId")), null);
        HistoricoStatusFornecedor hist = new HistoricoStatusFornecedor(
            null, StatusFornecedor.INATIVO, dados.get("justificativa"), null, cat, null
        );
        f.getHistoricoStatus().add(hist);
        return fachada.inativar(f);
    }

    public String ativar(Long id, Map<String, String> dados) {
        Fornecedor f = new Fornecedor();
        f.setId(id);
        f.setUsuarioResponsavel(dados.getOrDefault("usuario", "Carlos Mendes / Administrador"));

        CategoriaAtivacao cat = new CategoriaAtivacao(parseLong(dados.get("categoriaId")), null);
        HistoricoStatusFornecedor hist = new HistoricoStatusFornecedor(
            null, StatusFornecedor.ATIVO, dados.get("justificativa"), null, null, cat
        );
        f.getHistoricoStatus().add(hist);
        return fachada.ativar(f);
    }

    public List<EntidadeDominio> consultar(Map<String, String> filtros) {
        Fornecedor f = definirFornecedor(filtros);
        return fachada.consultar(f);
    }

    public Fornecedor consultarPorId(Long id) {
        Fornecedor f = new Fornecedor();
        f.setId(id);
        List<EntidadeDominio> res = fachada.consultar(f);
        return res.isEmpty() ? null : (Fornecedor) res.get(0);
    }

    public Map<String, List<EntidadeDominio>> consultarDominios() {
        Map<String, List<EntidadeDominio>> dom = new LinkedHashMap<>();
        dom.put("paises", fachada.consultar(new Pais()));
        dom.put("estados", fachada.consultar(new Estado()));
        dom.put("tiposLogradouro", fachada.consultar(new TipoLogradouro()));
        dom.put("tiposTelefone", fachada.consultar(new TipoTelefone()));
        dom.put("categoriasInativacao", fachada.consultar(new CategoriaInativacao()));
        dom.put("categoriasAtivacao", fachada.consultar(new CategoriaAtivacao()));
        return dom;
    }
}