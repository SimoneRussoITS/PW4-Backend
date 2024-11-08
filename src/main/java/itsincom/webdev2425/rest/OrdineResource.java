package itsincom.webdev2425.rest;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Path("/ordine")
public class OrdineResource {
    private final OrdineRepository ordineRepository;
    private final UtenteRepository utenteRepository;
    private final ProdottoRepository prodottoRepository;
    private final ReactiveMailer reactiveMailer;

    public OrdineResource(OrdineRepository ordineRepository, UtenteRepository utenteRepository, ProdottoRepository prodottoRepository, ReactiveMailer reactiveMailer) {
        this.ordineRepository = ordineRepository;
        this.utenteRepository = utenteRepository;
        this.prodottoRepository = prodottoRepository;
        this.reactiveMailer = reactiveMailer;
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
            Mail mail = Mail.withHtml("simorusso04@gmail.com",
                    "Nuovo ordine (" + ordine.getData() + ")",
                    creaMessaggioHtml(utente.getEmail(), dettaglio, ordine.getData_ritiro())
            );
            Uni<Void> send = reactiveMailer.send(mail);
            send.subscribe().with(
                    success -> System.out.println("Mail inviata"),
                    failure -> System.out.println("Errore nell'invio della mail")
            );
            return Response
                    .status(Response.Status.CREATED)
                    .entity("Ordine aggiunto con successo, è stata inviata una notifica al pasticcere, che confermerà l'ordine il prima possibile")
                    .build();
        }
    }

    @GET
    @Path("/utente")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ordine> getOrdiniUtente(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        if (utente == null || !utente.getRuolo().equals("CLIENTE VERIFICATO")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato").build());
        } else {
            return ordineRepository.getOrdiniUtente(utente.getEmail());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ordine> getOrdini(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        if (utente == null || !utente.getRuolo().equals("ADMIN")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato").build());
        } else {
            return ordineRepository.getOrdini();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateOrdine(@PathParam("id") String id, Ordine ordine, @CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        if (utente == null || !utente.getRuolo().equals("ADMIN")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Accesso negato")
                    .build();
        } else {
            Ordine ordineAggiornato = ordineRepository.update(ordine, id);
            Mail mail = Mail.withText(ordine.getEmail_utente(),
                    "Prenotazione confermata. Il tuo ordine è in preparazione",
                    "La pasticceria ha ricevuto e confermato la tua prenotazione. Il tuo ordine sarà pronto per il ritiro il " + ordine.getData_ritiro() + "."
            );
            Uni<Void> send = reactiveMailer.send(mail);
            send.subscribe().with(
                    success -> System.out.println("Mail inviata"),
                    failure -> System.out.println("Errore nell'invio della mail")
            );
            if (ordineAggiornato == null) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Errore durante l'aggiornamento dell'ordine")
                        .build();
            } else {
                return Response
                        .status(Response.Status.OK)
                        .entity("Ordine aggiornato con successo. E' stata inviata una mail all'utente in cui si conferma la prenotazione e si ricorda la data di ritiro")
                        .build();
            }
        }
    }

    // private methods

    private String creaMessaggioHtml(String emailUtente, List<DettaglioProdotto> dettaglio, Date dataRitiro) {
        LocalDateTime d = LocalDateTime.ofInstant(dataRitiro.toInstant(), ZoneId.systemDefault());
        StringBuilder messaggio = new StringBuilder();
        messaggio.append("<html><body>");
        messaggio.append("<h1>Ordine</h1>");
        messaggio.append("<p>Email utente: ").append(emailUtente).append("</p>");
        messaggio.append("<h2>Dettagli ordine:</h2>");
        messaggio.append("<ul>");
        for (DettaglioProdotto dt : dettaglio) {
            messaggio.append("<li>").append(dt.getNome()).append(" - Quantità: ").append(dt.getQuantita()).append("</li>");
        }
        messaggio.append("</ul>");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        messaggio.append("<p>Data e ora di ritiro: ").append(d.format(formatter)).append("</p>");
        messaggio.append("</body></html>");
        return messaggio.toString();
    }

}
