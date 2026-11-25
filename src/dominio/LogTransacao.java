package dominio;

import java.time.LocalDateTime;

public class LogTransacao extends EntidadeDominio {
    private Long fornecedorId;
    private TipoOperacaoLog operacao;
    private LocalDateTime dataHora;
    private String usuarioResponsavel;
    private String dadosAlterados;

    public LogTransacao() {}

    public LogTransacao(Long id, Long fornecedorId, TipoOperacaoLog operacao, LocalDateTime dataHora,
                        String usuarioResponsavel, String dadosAlterados) {
        super(id);
        this.fornecedorId = fornecedorId;
        this.operacao = operacao;
        this.dataHora = dataHora;
        this.usuarioResponsavel = usuarioResponsavel;
        this.dadosAlterados = dadosAlterados;
    }

    public Long getFornecedorId() { return fornecedorId; }
    public void setFornecedorId(Long fornecedorId) { this.fornecedorId = fornecedorId; }
    public TipoOperacaoLog getOperacao() { return operacao; }
    public void setOperacao(TipoOperacaoLog operacao) { this.operacao = operacao; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getUsuarioResponsavel() { return usuarioResponsavel; }
    public void setUsuarioResponsavel(String usuarioResponsavel) { this.usuarioResponsavel = usuarioResponsavel; }
    public String getDadosAlterados() { return dadosAlterados; }
    public void setDadosAlterados(String dadosAlterados) { this.dadosAlterados = dadosAlterados; }
}