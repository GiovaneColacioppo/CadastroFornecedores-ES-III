package dominio;

public class TipoLogradouro extends EntidadeDominio {
    private String descricao;

    public TipoLogradouro() {}

    public TipoLogradouro(Long id, String descricao) {
        super(id);
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}