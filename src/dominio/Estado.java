public class Estado extends EntidadeDominio {
    private String nome;
    private String uf;
    private Pais pais;

    public Estado() {}

    public Estado(Long id, String nome, String uf, Pais pais) {
        super(id);
        this.nome = nome;
        this.uf = uf;
        this.pais = pais;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public Pais getPais() { return pais; }
    public void setPais(Pais pais) { this.pais = pais; }
}