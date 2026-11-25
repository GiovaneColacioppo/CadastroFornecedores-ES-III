package strategy;

import dao.FornecedorDAO;
import dominio.EntidadeDominio;
import dominio.Fornecedor;
import dominio.StatusFornecedor;
import java.util.List;

public class GerarCodigoFornecedor implements IStrategy {
    @Override
    public String processar(EntidadeDominio entidade) {
        if (!(entidade instanceof Fornecedor)) return null;
        Fornecedor f = (Fornecedor) entidade;

        FornecedorDAO dao = new FornecedorDAO();
        List<EntidadeDominio> todos = dao.consultar(new Fornecedor());

        int maiorNumero = 0;
        for (EntidadeDominio item : todos) {
            String cod = ((Fornecedor) item).getCodigo();
            if (cod != null && cod.matches("F\\d+")) {
                int num = Integer.parseInt(cod.substring(1));
                if (num > maiorNumero) maiorNumero = num;
            }
        }
        f.setCodigo(String.format("F%04d", maiorNumero + 1));
        f.setStatus(StatusFornecedor.ATIVO); // P8.2: novo cadastro sempre inicia ATIVO
        return null;
    }
}