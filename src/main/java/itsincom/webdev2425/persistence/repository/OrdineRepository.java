package itsincom.webdev2425.persistence.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import itsincom.webdev2425.persistence.model.DettaglioProdotto;
import itsincom.webdev2425.persistence.model.Ordine;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

@ApplicationScoped
public class OrdineRepository implements PanacheMongoRepository<Ordine> {
    public List<Ordine> getOrdini() {
        return listAll();
    }

    public Ordine getById(String id) {
        ObjectId objectId = new ObjectId(id);
        return findById(objectId);
    }

    public void addOrdine(String email_utente, List<DettaglioProdotto> dettaglio, Date data_ritiro) {
        List<Ordine> ordini = getOrdini();
        for (Ordine o : ordini) {
            long diff = data_ritiro.getTime() - o.getData_ritiro().getTime();
            if (diff < 600000) {
                throw new RuntimeException("Devono passare almeno 10 minuti tra un ordine e l'altro");
            }
        }
        Ordine ordine = Ordine.create(email_utente, dettaglio, data_ritiro);
        persist(ordine);
    }

    public List<Ordine> getOrdiniUtente(String email_utente) {
        return list("email_utente", email_utente);
    }

    public Ordine update(Ordine ordine, String id) {
        Ordine ordineDaAggiornare = getById(id);
        ordineDaAggiornare.setStato(ordine.getStato());
        update(ordineDaAggiornare);
        return ordineDaAggiornare;
    }
}
