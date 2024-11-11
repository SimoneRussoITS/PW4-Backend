package itsincom.webdev2425.persistence.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import itsincom.webdev2425.persistence.model.DettaglioProdotto;
import itsincom.webdev2425.persistence.model.Ordine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    public Ordine addOrdine(String email_utente, List<DettaglioProdotto> dettaglio, LocalDateTime data_ritiro) {
        List<Ordine> ordini = getOrdini();
        if (data_ritiro.getDayOfWeek().getValue() > 5 || data_ritiro.getHour() < 14 || (data_ritiro.getHour() == 18 && data_ritiro.getMinute() > 0) || data_ritiro.getHour() > 18) { // Sabato e domenica chiuso, apertura dalle 14 alle 18
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Non è possibile effettuare ordini nei giorni di sabato e domenica o fuori dall'orario di apertura (14-18)").build());
        } else {
            for (Ordine o : ordini) {
                if (Math.abs(o.getData_ritiro().until(data_ritiro, ChronoUnit.MINUTES)) < 10) { // 10 minuti di attesa tra un ordine e l'altro
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Devi attendere almeno 10 minuti tra un ordine e l'altro").build());
                }
            }
        }
        Ordine ordine = Ordine.create(email_utente, dettaglio, data_ritiro);
        persist(ordine);
        return ordine;
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
