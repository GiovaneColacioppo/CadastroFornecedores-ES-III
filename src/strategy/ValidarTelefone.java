package strategy;

import dominio.EntidadeDominio;
import dominio.Telefone;
import java.util.ArrayList;
import java.util.List;

public class ValidarTelefone implements IStrategy {
    @Override
    public String processar(EntidadeDominio entidade) {
        if (!(entidade instanceof Telefone)) return "Telefone inválido.";
        Telefone tel = (Telefone) entidade;
        List<String> erros = new ArrayList<>();

        if (tel.getTipo() == null || tel.getTipo().getId() == null) {
            erros.add("Tipo de Telefone é obrigatório");
        }
        String ddd = tel.getDdd() == null ? "" : tel.getDdd().replaceAll("\\D", "");
        tel.setDdd(ddd);
        if (ddd.isEmpty()) {
            erros.add("DDD é obrigatório");
        } else if (ddd.length() != 2) {
            erros.add("DDD deve conter exatamente 2 dígitos numéricos");
        }

        String numero = tel.getNumero() == null ? "" : tel.getNumero().replaceAll("\\D", "");
        tel.setNumero(numero);
        if (numero.isEmpty()) {
            erros.add("Número do Telefone é obrigatório");
        } else if (numero.length() < 8 || numero.length() > 9) {
            erros.add("Número do Telefone deve conter 8 ou 9 dígitos numéricos");
        }

        return erros.isEmpty() ? null : String.join("; ", erros);
    }
}