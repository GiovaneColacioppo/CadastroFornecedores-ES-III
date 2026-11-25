package dominio;

import java.time.LocalDateTime;

public class HistoricoStatusFornecedor extends EntidadeDominio {
    private StatusFornecedor statusAplicado;
    private String justificativa;
    private LocalDateTime dataHora;
    private CategoriaInativacao categoriaInativacao;
    private CategoriaAtivacao categoriaAtivacao;

    public HistoricoStatusFornecedor() {}

    public HistoricoStatusFornecedor(Long id, StatusFornecedor statusAplicado, String justificativa,
                                     LocalDateTime dataHora, CategoriaInativacao categoriaInativacao,
                                     CategoriaAtivacao categoriaAtivacao) {
        super(id);
        this.statusAplicado = statusAplicado;
        this.justificativa = justificativa;
        this.dataHora = dataHora;
        this.categoriaInativacao = categoriaInativacao;
        this.categoriaAtivacao = categoriaAtivacao;
    }

    public StatusFornecedor getStatusAplicado() { return statusAplicado; }
    public void setStatusAplicado(StatusFornecedor statusAplicado) { this.statusAplicado = statusAplicado; }
    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public CategoriaInativacao getCategoriaInativacao() { return categoriaInativacao; }
    public void setCategoriaInativacao(CategoriaInativacao categoriaInativacao) { this.categoriaInativacao = categoriaInativacao; }
    public CategoriaAtivacao getCategoriaAtivacao() { return categoriaAtivacao; }
    public void setCategoriaAtivacao(CategoriaAtivacao categoriaAtivacao) { this.categoriaAtivacao = categoriaAtivacao; }
}