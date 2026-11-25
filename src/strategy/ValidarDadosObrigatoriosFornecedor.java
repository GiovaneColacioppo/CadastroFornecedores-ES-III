package strategy;

import dominio.EntidadeDominio;
import dominio.Fornecedor;
import java.util.ArrayList;
import java.util.List;

public class ValidarDadosObrigatoriosFornecedor implements IStrategy {
    @Override
    public String processar(EntidadeDominio entidade) {
        if (!(entidade instanceof Fornecedor)) return "Fornecedor inválido.";
        Fornecedor f = (Fornecedor) entidade;
        List<String> erros = new ArrayList<>();

        if (f.getRazaoSocial() == null || f.getRazaoSocial().isBlank()) {
            erros.add("Razão Social é obrigatória");
        } else if (f.getRazaoSocial().trim().length() > 100) {
            erros.add("Razão Social deve ter no máximo 100 caracteres");
        }

        if (f.getNomeFantasia() == null || f.getNomeFantasia().isBlank()) {
            erros.add("Nome Fantasia é obrigatório");
        } else if (f.getNomeFantasia().trim().length() > 60) {
            erros.add("Nome Fantasia deve ter no máximo 60 caracteres");
        }

        String cnpj = f.getCnpj() == null ? "" : f.getCnpj().replaceAll("\\D", "");
        f.setCnpj(cnpj);
        if (cnpj.isEmpty()) {
            erros.add("CNPJ é obrigatório");
        } else if (cnpj.length() != 14) {
            erros.add("CNPJ deve conter 14 dígitos numéricos");
        }

        if (f.getEmail() == null || f.getEmail().isBlank()) {
            erros.add("E-mail é obrigatório");
        } else if (f.getEmail().trim().length() > 100 || !f.getEmail().contains("@")) {
            erros.add("E-mail inválido (máximo 100 caracteres)");
        }

        // Delegação por dependência (metodologia ARQ6)
        if (f.getTelefone() == null) {
            erros.add("Telefone é obrigatório");
        } else {
            String erroTel = new ValidarTelefone().processar(f.getTelefone());
            if (erroTel != null) erros.add(erroTel);
        }

        if (f.getEndereco() == null) {
            erros.add("Endereço é obrigatório");
        } else {
            String erroEnd = new ValidarEndereco().processar(f.getEndereco());
            if (erroEnd != null) erros.add(erroEnd);
        }

        return erros.isEmpty() ? null : String.join("; ", erros);
    }
}