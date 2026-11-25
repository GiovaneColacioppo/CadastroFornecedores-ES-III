package strategy;

import dao.FornecedorDAO;
import dominio.EntidadeDominio;
import dominio.Fornecedor;
import java.util.List;

public class ValidarExistencia implements IStrategy {
    @Override
    public String processar(EntidadeDominio entidade) {
        if (!(entidade instanceof Fornecedor)) return null;
        Fornecedor f = (Fornecedor) entidade;
        if (f.getCnpj() == null || f.getCnpj().isBlank()) return null;

        String cnpjLimpo = f.getCnpj().replaceAll("\\D", "");
        Fornecedor filtro = new Fornecedor();
        filtro.setCnpj(cnpjLimpo);

        FornecedorDAO dao = new FornecedorDAO();
        List<EntidadeDominio> encontrados = dao.consultar(filtro);

        for (EntidadeDominio item : encontrados) {
            Fornecedor existente = (Fornecedor) item;
            boolean mesmoCnpj = cnpjLimpo.equals(existente.getCnpj());
            boolean outroRegistro = (f.getId() == null) || !f.getId().equals(existente.getId());
            if (mesmoCnpj && outroRegistro) {
                return "CNPJ já cadastrado para o fornecedor de código " + existente.getCodigo() + ".";
            }
        }
        return null;
    }
}