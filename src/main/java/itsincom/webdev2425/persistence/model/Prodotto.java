package itsincom.webdev2425.persistence.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "prodotto")
public class Prodotto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "ingredienti")
    private String ingredienti;

    @Column(name = "quantita", nullable = false)
    private Integer quantita;

    @Column(name = "prezzo", nullable = false, precision = 10, scale = 2)
    private BigDecimal prezzo;

    @Column(name = "Foto", nullable = false)
    private String foto;

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    public Integer getQuantita() {
        return quantita;
    }

    public void setQuantita(Integer quantita) {
        this.quantita = quantita;
    }

    public String getIngredienti() {
        return ingredienti;
    }

    public void setIngredienti(String ingredienti) {
        this.ingredienti = ingredienti;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
