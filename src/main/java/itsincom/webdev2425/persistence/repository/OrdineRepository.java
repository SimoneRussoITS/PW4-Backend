package itsincom.webdev2425.persistence.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import itsincom.webdev2425.persistence.model.DettaglioProdotto;
import itsincom.webdev2425.persistence.model.Ordine;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Date;
import java.util.List;

@ApplicationScoped
public class OrdineRepository implements PanacheMongoRepository<Ordine> {
    public List<Ordine> getOrdini() {
        return listAll();
    }

    public void addOrdine(String email_utente, List<DettaglioProdotto> dettaglio, Date data_ritiro) {
        List<Ordine> ordini = getOrdini();
        for (Ordine o : ordini) {
            if (o.getData_ritiro().getTime() - data_ritiro.getTime() < 600000) {
                throw new RuntimeException("Devono passare almeno 10 minuti tra un ordine e l'altro");
            }
        }
        Ordine ordine = Ordine.create(email_utente, dettaglio, data_ritiro);
        persist(ordine);
    }
}
