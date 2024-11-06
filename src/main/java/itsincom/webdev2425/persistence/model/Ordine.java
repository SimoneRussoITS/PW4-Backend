package itsincom.webdev2425.persistence.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.util.List;

@MongoEntity(collection = "ordine")
public class Ordine {
    private ObjectId id;
    private List<DettaglioProdotto> dettaglio;
    private String email_utente;
    private String stato;
    private String data;
    private String data_ritiro;

    public static Ordine create(String email_utente, List<DettaglioProdotto> dettaglio, String dataRitiro) {
        return null;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public List<DettaglioProdotto> getDettaglio() {
        return dettaglio;
    }

    public void setDettaglio(List<DettaglioProdotto> dettaglio) {
        this.dettaglio = dettaglio;
    }

    public String getEmail_utente() {
        return email_utente;
    }

    public void setEmail_utente(String email_utente) {
        this.email_utente = email_utente;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData_ritiro() {
        return data_ritiro;
    }

    public void setData_ritiro(String data_ritiro) {
        this.data_ritiro = data_ritiro;
    }
}
