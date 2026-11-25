package controle;

import dao.*;
import dominio.*;
import strategy.*;
import java.util.*;

public class Fachada {
    private final Map<String, Map<String, List<IStrategy>>> rns;
    private final Map<String, IDAO> daos;

    public Fachada() {
        daos = new HashMap<>();
        rns = new HashMap<>();

        // Mapa de DAOs
        FornecedorDAO fornecedorDAO = new FornecedorDAO();
        DominioDAO dominioDAO = new DominioDAO();
        daos.put(Fornecedor.class.getName(), fornecedorDAO);
        daos.put(Pais.class.getName(), dominioDAO);
        daos.put(Estado.class.getName(), dominioDAO);
        daos.put(TipoLogradouro.class.getName(), dominioDAO);
        daos.put(TipoTelefone.class.getName(), dominioDAO);
        daos.put(CategoriaInativacao.class.getName(), dominioDAO);
        daos.put(CategoriaAtivacao.class.getName(), dominioDAO);

        // Strategies por operação de Fornecedor (ARQ6)
        Map<String, List<IStrategy>> rnsFornecedor = new HashMap<>();

        rnsFornecedor.put("SALVAR", Arrays.asList(
            new ValidarDadosObrigatoriosFornecedor(),
            new ValidarExistencia(),
            new GerarCodigoFornecedor(),
            new ComplementarDtCad()
        ));

        rnsFornecedor.put("ALTERAR", Arrays.asList(
            new ValidarDadosObrigatoriosFornecedor(),
            new ValidarExistencia()
        ));

        rnsFornecedor.put("INATIVAR", Collections.singletonList(
            new ValidarInativacao()
        ));

        rnsFornecedor.put("ATIVAR", Collections.singletonList(
            new ValidarAtivacao()
        ));

        rns.put(Fornecedor.class.getName(), rnsFornecedor);
    }

    private String executarRegras(EntidadeDominio entidade, String operacao) {
        StringBuilder sb = new StringBuilder();
        Map<String, List<IStrategy>> rnsEntidade = rns.get(entidade.getClass().getName());
        if (rnsEntidade != null) {
            List<IStrategy> regras = rnsEntidade.get(operacao);
            if (regras != null) {
                for (IStrategy s : regras) {
                    String msg = s.processar(entidade);
                    if (msg != null && !msg.isBlank()) {
                        if (sb.length() > 0) sb.append("; ");
                        sb.append(msg);
                    }
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    public String salvar(EntidadeDominio entidade) {
        String erro = executarRegras(entidade, "SALVAR");
        if (erro != null) return erro;

        IDAO dao = daos.get(entidade.getClass().getName());
        dao.salvar(entidade);
        new GerarLog(TipoOperacaoLog.INCLUSAO).processar(entidade);
        return null;
    }

    public String alterar(EntidadeDominio entidade) {
        String erro = executarRegras(entidade, "ALTERAR");
        if (erro != null) return erro;

        IDAO dao = daos.get(entidade.getClass().getName());
        dao.alterar(entidade);
        new GerarLog(TipoOperacaoLog.ALTERACAO).processar(entidade);
        return null;
    }

    public String inativar(EntidadeDominio entidade) {
        String erro = executarRegras(entidade, "INATIVAR");
        if (erro != null) return erro;

        IDAO dao = daos.get(entidade.getClass().getName());
        dao.alterar(entidade);
        new GerarLog(TipoOperacaoLog.INATIVACAO).processar(entidade);
        return null;
    }

    public String ativar(EntidadeDominio entidade) {
        String erro = executarRegras(entidade, "ATIVAR");
        if (erro != null) return erro;

        IDAO dao = daos.get(entidade.getClass().getName());
        dao.alterar(entidade);
        new GerarLog(TipoOperacaoLog.ATIVACAO).processar(entidade);
        return null;
    }

    public List<EntidadeDominio> consultar(EntidadeDominio entidade) {
        IDAO dao = daos.get(entidade.getClass().getName());
        return dao.consultar(entidade);
    }
}