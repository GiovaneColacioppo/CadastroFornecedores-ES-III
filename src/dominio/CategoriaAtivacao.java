package dominio;

public class CategoriaAtivacao extends EntidadeDominio {
    private String descricao;

    public CategoriaAtivacao() {}

    public CategoriaAtivacao(Long id, String descricao) {
        super(id);
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}