package itsincom.webdev2425.persistence.model;

import org.bson.types.ObjectId;

public class DettaglioProdotto {
    private ObjectId id_prodotto;
    private int quantita;

    public ObjectId getId_prodotto() {
        return id_prodotto;
    }

    public void setId_prodotto(ObjectId id_prodotto) {
        this.id_prodotto = id_prodotto;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }
}
