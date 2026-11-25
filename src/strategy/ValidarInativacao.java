package strategy;

import dao.FornecedorDAO;
import dominio.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ValidarInativacao implements IStrategy {
    @Override
    public String processar(EntidadeDominio entidade) {
        if (!(entidade instanceof Fornecedor)) return "Fornecedor inválido.";
        Fornecedor f = (Fornecedor) entidade;
        List<String> erros = new ArrayList<>();

        if (f.getId() == null) {
            return "Fornecedor não identificado para inativação.";
        }

        Fornecedor filtro = new Fornecedor();
        filtro.setId(f.getId());
        List<EntidadeDominio> encontrados = new FornecedorDAO().consultar(filtro);
        if (encontrados.isEmpty()) {
            return "Fornecedor não encontrado.";
        }
        Fornecedor atual = (Fornecedor) encontrados.get(0);
        if (atual.getStatus() != StatusFornecedor.ATIVO) {
            return "Somente fornecedores com status ATIVO podem ser inativados.";
        }
        f.setCodigo(atual.getCodigo());
        f.setRazaoSocial(atual.getRazaoSocial());

        if (f.getHistoricoStatus() == null || f.getHistoricoStatus().isEmpty()) {
            return "Categoria de inativação e justificativa são obrigatórias.";
        }

        HistoricoStatusFornecedor hist = f.getHistoricoStatus().get(f.getHistoricoStatus().size() - 1);
        if (hist.getCategoriaInativacao() == null || hist.getCategoriaInativacao().getId() == null) {
            erros.add("Categoria de inativação é obrigatória");
        }
        if (hist.getJustificativa() == null || hist.getJustificativa().isBlank()) {
            erros.add("Justificativa é obrigatória");
        } else if (hist.getJustificativa().trim().length() > 255) {
            erros.add("Justificativa deve ter no máximo 255 caracteres");
        }

        if (erros.isEmpty()) {
            hist.setStatusAplicado(StatusFornecedor.INATIVO);
            hist.setDataHora(LocalDateTime.now());
        }
        return erros.isEmpty() ? null : String.join("; ", erros);
    }
}