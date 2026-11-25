package strategy;

import dominio.EntidadeDominio;
import java.time.LocalDateTime;

public class ComplementarDtCad implements IStrategy {
    @Override
    public String processar(EntidadeDominio entidade) {
        if (entidade != null) {
            entidade.setDtCad(LocalDateTime.now());
        }
        return null;
    }
}