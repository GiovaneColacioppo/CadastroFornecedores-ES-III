package strategy;

import dominio.Endereco;
import dominio.EntidadeDominio;
import java.util.ArrayList;
import java.util.List;

public class ValidarEndereco implements IStrategy {
    @Override
    public String processar(EntidadeDominio entidade) {
        if (!(entidade instanceof Endereco)) return "Endereço inválido.";
        Endereco end = (Endereco) entidade;
        List<String> erros = new ArrayList<>();

        if (end.getTipoLogradouro() == null || end.getTipoLogradouro().getId() == null) {
            erros.add("Tipo de Logradouro é obrigatório");
        }
        if (end.getLogradouro() == null || end.getLogradouro().isBlank()) {
            erros.add("Logradouro é obrigatório");
        } else if (end.getLogradouro().trim().length() > 100) {
            erros.add("Logradouro deve ter no máximo 100 caracteres");
        }
        if (end.getNumero() == null || end.getNumero().isBlank()) {
            erros.add("Número do endereço é obrigatório");
        } else if (end.getNumero().trim().length() > 10) {
            erros.add("Número do endereço deve ter no máximo 10 caracteres");
        }
        if (end.getComplemento() != null && end.getComplemento().trim().length() > 60) {
            erros.add("Complemento deve ter no máximo 60 caracteres");
        }
        if (end.getBairro() == null || end.getBairro().isBlank()) {
            erros.add("Bairro é obrigatório");
        } else if (end.getBairro().trim().length() > 60) {
            erros.add("Bairro deve ter no máximo 60 caracteres");
        }
        String cep = end.getCep() == null ? "" : end.getCep().replaceAll("\\D", "");
        end.setCep(cep);
        if (cep.isEmpty()) {
            erros.add("CEP é obrigatório");
        } else if (cep.length() != 8) {
            erros.add("CEP deve conter exatamente 8 dígitos numéricos");
        }
        if (end.getCidade() == null || end.getCidade().isBlank()) {
            erros.add("Cidade é obrigatória");
        } else if (end.getCidade().trim().length() > 60) {
            erros.add("Cidade deve ter no máximo 60 caracteres");
        }
        if (end.getEstado() == null || end.getEstado().getId() == null) {
            erros.add("Estado é obrigatório");
        } else if (end.getEstado().getPais() == null || end.getEstado().getPais().getId() == null) {
            erros.add("País é obrigatório");
        }

        return erros.isEmpty() ? null : String.join("; ", erros);
    }
}