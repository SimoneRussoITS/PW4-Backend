package itsincom.webdev2425.persistence.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import itsincom.webdev2425.persistence.model.DettaglioProdotto;
import itsincom.webdev2425.persistence.model.Ordine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
@Transactional
public class OrdineRepository implements PanacheMongoRepository<Ordine> {
    public void addOrdine(String email_utente, List<DettaglioProdotto> dettaglio, String data_ritiro) {
        Ordine ordine = Ordine.create(email_utente, dettaglio, data_ritiro);
        persist(ordine);
    }
}
