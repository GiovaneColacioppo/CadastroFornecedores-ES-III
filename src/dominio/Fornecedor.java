package dominio;

import java.util.ArrayList;
import java.util.List;

public class Fornecedor extends EntidadeDominio {
    private String codigo;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String email;
    private StatusFornecedor status;
    private Telefone telefone;
    private Endereco endereco;
    private List<HistoricoStatusFornecedor> historicoStatus;
    private List<LogTransacao> logs;
    private String usuarioResponsavel; // usado pela Strategy GerarLog (RNF0012)

    public Fornecedor() {
        this.historicoStatus = new ArrayList<>();
        this.logs = new ArrayList<>();
    }

    public Fornecedor(Long id, String codigo, String razaoSocial, String nomeFantasia,
                      String cnpj, String email, StatusFornecedor status,
                      Telefone telefone, Endereco endereco) {
        super(id);
        this.codigo = codigo;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
        this.email = email;
        this.status = status;
        this.telefone = telefone;
        this.endereco = endereco;
        this.historicoStatus = new ArrayList<>();
        this.logs = new ArrayList<>();
    }

    public boolean estaAtivoParaEstoque() {
        return StatusFornecedor.ATIVO.equals(this.status);
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public StatusFornecedor getStatus() { return status; }
    public void setStatus(StatusFornecedor status) { this.status = status; }
    public Telefone getTelefone() { return telefone; }
    public void setTelefone(Telefone telefone) { this.telefone = telefone; }
    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }
    public List<HistoricoStatusFornecedor> getHistoricoStatus() { return historicoStatus; }
    public void setHistoricoStatus(List<HistoricoStatusFornecedor> historicoStatus) { this.historicoStatus = historicoStatus; }
    public List<LogTransacao> getLogs() { return logs; }
    public void setLogs(List<LogTransacao> logs) { this.logs = logs; }
    public String getUsuarioResponsavel() { return usuarioResponsavel; }
    public void setUsuarioResponsavel(String usuarioResponsavel) { this.usuarioResponsavel = usuarioResponsavel; }
}