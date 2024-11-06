package itsincom.webdev2425.rest;

import itsincom.webdev2425.persistence.model.DettaglioProdotto;
import itsincom.webdev2425.persistence.model.Ordine;
import itsincom.webdev2425.persistence.model.Prodotto;
import itsincom.webdev2425.persistence.model.Utente;
import itsincom.webdev2425.persistence.repository.OrdineRepository;
import itsincom.webdev2425.persistence.repository.ProdottoRepository;
import itsincom.webdev2425.persistence.repository.UtenteRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/ordine")
public class OrdineResource {
    private final OrdineRepository ordineRepository;
    private final UtenteRepository utenteRepository;
    private final ProdottoRepository prodottoRepository;

    public OrdineResource(OrdineRepository ordineRepository, UtenteRepository utenteRepository, ProdottoRepository prodottoRepository) {
        this.ordineRepository = ordineRepository;
        this.utenteRepository = utenteRepository;
        this.prodottoRepository = prodottoRepository;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addOrdine(Ordine ordine, @CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        // l'utente deve avere il ruolo CLIENTE VERIFICATO per poter effettuare un ordine
        if (utente == null || !utente.getRuolo().equals("CLIENTE VERIFICATO")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Accesso negato")
                    .build();
        } else {
            // controllo che i prodotti siano disponibili
            List<DettaglioProdotto> dettaglio = ordine.getDettaglio();
            List<Prodotto> prodottiDaAggiornare = prodottoRepository.listAll();
            for (DettaglioProdotto d : dettaglio) {
                if (d.getQuantita() < 1) {
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("Il prodotto " + d.getNome() + " non è disponibile.")
                            .build();
                } else {
                    Prodotto p = prodottoRepository.findByName(d.getNome());
                    p.setQuantita(p.getQuantita() - d.getQuantita());
                    prodottiDaAggiornare.add(p);
                }
            }
            ordineRepository.addOrdine(utente.getEmail(), ordine.getDettaglio(), ordine.getData_ritiro());
            for (Prodotto p : prodottiDaAggiornare) {
                prodottoRepository.update(p, String.valueOf(p.getId()));
            }
            // TODO: invio una notifica al pasticcere
            return Response
                    .status(Response.Status.CREATED)
                    .entity("Ordine aggiunto con successo, è stata inviata una notifica al pasticcere, che confermerà l'ordine il prima possibile")
                    .build();
        }
    }
}
