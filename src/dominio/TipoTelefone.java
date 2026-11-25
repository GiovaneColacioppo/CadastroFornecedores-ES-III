package dominio;

public class TipoTelefone extends EntidadeDominio {
    private String descricao;

    public TipoTelefone() {}

    public TipoTelefone(Long id, String descricao) {
        super(id);
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}