package itsincom.webdev2425.persistence.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "prenotazione")
public class Prenotazione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "id_ordine", length = 50)
    private String idOrdine;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "data_prenotazione")
    private Instant dataPrenotazione;

    public Instant getDataPrenotazione() {
        return dataPrenotazione;
    }

    public void setDataPrenotazione(Instant dataPrenotazione) {
        this.dataPrenotazione = dataPrenotazione;
    }

    public String getIdOrdine() {
        return idOrdine;
    }

    public void setIdOrdine(String idOrdine) {
        this.idOrdine = idOrdine;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
