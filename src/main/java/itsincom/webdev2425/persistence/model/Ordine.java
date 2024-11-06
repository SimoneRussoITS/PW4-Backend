package itsincom.webdev2425.persistence.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

@MongoEntity(collection = "ordine")
public class Ordine {
    private ObjectId id;
    private List<DettaglioProdotto> dettaglio;
    private String email_utente;
    private String stato;
    private Date data;
    private Date data_ritiro;
    private double prezzoTotale;

    public static Ordine create(String email_utente, List<DettaglioProdotto> dettaglio, Date dataRitiro) {
        Ordine ordine = new Ordine();
        ordine.setEmail_utente(email_utente);
        ordine.setDettaglio(dettaglio);
        ordine.setData(new Date());
        ordine.setData_ritiro(dataRitiro);
        ordine.setPrezzoTotale(calculatePrezzoTotale(dettaglio));
        ordine.setStato("IN ATTESA DI CONFERMA");
        return ordine;
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

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Date getData_ritiro() {
        return data_ritiro;
    }

    public void setData_ritiro(Date data_ritiro) {
        this.data_ritiro = data_ritiro;
    }

    public double getPrezzoTotale() {
        return prezzoTotale;
    }

    public void setPrezzoTotale(double prezzoTotale) {
        this.prezzoTotale = prezzoTotale;
    }

    private static double calculatePrezzoTotale(List<DettaglioProdotto> dettaglio) {
        double prezzo = 0;
        for (DettaglioProdotto dettaglioProdotto : dettaglio) {
            prezzo += dettaglioProdotto.getPrezzo_unitario() * dettaglioProdotto.getQuantita();
        }
        return prezzo;
    }
}
