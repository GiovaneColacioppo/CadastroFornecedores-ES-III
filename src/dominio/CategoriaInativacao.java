public class CategoriaInativacao extends EntidadeDominio {
    private String descricao;

    public CategoriaInativacao() {}

    public CategoriaInativacao(Long id, String descricao) {
        super(id);
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}