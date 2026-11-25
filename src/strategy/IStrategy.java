package strategy;

import dominio.EntidadeDominio;

public interface IStrategy {
    String processar(EntidadeDominio entidade);
}