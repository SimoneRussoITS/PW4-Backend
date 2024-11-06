package itsincom.webdev2425.persistence.model;

import org.bson.types.Decimal128;

public class DettaglioProdotto {
    private String nome;
    private int quantita;
    private double prezzo_unitario;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    public double getPrezzo_unitario() {
        return prezzo_unitario;
    }

    public void setPrezzo_unitario(double prezzo_unitario) {
        this.prezzo_unitario = prezzo_unitario;
    }
}
