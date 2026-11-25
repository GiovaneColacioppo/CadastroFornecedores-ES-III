public enum StatusFornecedor {
    ATIVO("Ativo"),
    INATIVO("Inativo");

    private final String descricao;

    StatusFornecedor(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
}