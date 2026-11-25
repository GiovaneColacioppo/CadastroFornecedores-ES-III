package strategy;

import dao.LogTransacaoDAO;
import dominio.*;
import java.time.LocalDateTime;

public class GerarLog implements IStrategy {
    private final TipoOperacaoLog operacao;

    public GerarLog(TipoOperacaoLog operacao) {
        this.operacao = operacao;
    }

    @Override
    public String processar(EntidadeDominio entidade) {
        if (!(entidade instanceof Fornecedor)) return null;
        Fornecedor f = (Fornecedor) entidade;

        String usuario = (f.getUsuarioResponsavel() != null && !f.getUsuarioResponsavel().isBlank())
                ? f.getUsuarioResponsavel()
                : "Carlos Mendes / Administrador";

        String dados;
        if (operacao == TipoOperacaoLog.INATIVACAO || operacao == TipoOperacaoLog.ATIVACAO) {
            HistoricoStatusFornecedor h = f.getHistoricoStatus().get(f.getHistoricoStatus().size() - 1);
            Long catId = (operacao == TipoOperacaoLog.INATIVACAO)
                    ? h.getCategoriaInativacao().getId()
                    : h.getCategoriaAtivacao().getId();
            dados = "codigo=" + f.getCodigo()
                    + "; status=" + h.getStatusAplicado().name()
                    + "; categoriaId=" + catId
                    + "; justificativa=" + h.getJustificativa();
        } else {
            dados = "codigo=" + f.getCodigo()
                    + "; razaoSocial=" + f.getRazaoSocial()
                    + "; nomeFantasia=" + f.getNomeFantasia()
                    + "; cnpj=" + f.getCnpj()
                    + "; email=" + f.getEmail()
                    + "; cidade=" + (f.getEndereco() != null ? f.getEndereco().getCidade() : "");
        }

        LocalDateTime dataHora = (f.getDtCad() != null && operacao == TipoOperacaoLog.INCLUSAO)
                ? f.getDtCad()
                : LocalDateTime.now();

        LogTransacao log = new LogTransacao(null, f.getId(), operacao, dataHora, usuario, dados);
        new LogTransacaoDAO().salvar(log);
        f.getLogs().add(log);
        return null;
    }
}