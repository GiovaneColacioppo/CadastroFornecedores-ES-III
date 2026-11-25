public class Endereco extends EntidadeDominio {
    private TipoLogradouro tipoLogradouro;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cep;
    private String cidade;
    private Estado estado;

    public Endereco() {}

    public Endereco(Long id, TipoLogradouro tipoLogradouro, String logradouro, String numero,
                    String complemento, String bairro, String cep, String cidade, Estado estado) {
        super(id);
        this.tipoLogradouro = tipoLogradouro;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cep = cep;
        this.cidade = cidade;
        this.estado = estado;
    }

    public TipoLogradouro getTipoLogradouro() { return tipoLogradouro; }
    public void setTipoLogradouro(TipoLogradouro tipoLogradouro) { this.tipoLogradouro = tipoLogradouro; }
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
}