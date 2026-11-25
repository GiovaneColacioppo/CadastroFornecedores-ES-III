package dominio;

import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class EntidadeDominio implements Serializable {
    private Long id;
    private LocalDateTime dtCad;

    public EntidadeDominio() {}

    public EntidadeDominio(Long id) {
        this.id = id;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDtCad() { return dtCad; }
    public void setDtCad(LocalDateTime dtCad) { this.dtCad = dtCad; }
}