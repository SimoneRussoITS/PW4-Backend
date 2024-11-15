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
import java.time.format.DateTimeFormatter;
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
        // controllo che l'utente sia loggato e sia un cliente verificato
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        if (utente == null || !utente.getRuolo().equals("CLIENTE VERIFICATO")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Accesso negato")
                    .build();
        } else {
            // controllo che l'ordine contenga almeno un prodotto
            if (ordine.getDettaglio().isEmpty()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("L'ordine deve contenere almeno un prodotto")
                        .build();
            }
            // controllo che i prodotti siano disponibili
            List<DettaglioProdotto> dettaglio = ordine.getDettaglio();
            List<Prodotto> prodottiDaAggiornare = prodottoRepository.listAll();
            for (DettaglioProdotto d : dettaglio) {
                Prodotto p = prodottoRepository.findByName(d.getNome());
                if (p.getQuantita() <= 0) {
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("Il prodotto " + d.getNome() + " non è disponibile.")
                            .build();
                } else {
                    p.setQuantita(p.getQuantita() - d.getQuantita());
                    if (p.getQuantita() < 0) {
                        return Response
                                .status(Response.Status.BAD_REQUEST)
                                .entity("Non ci sono abbastanza prodotti " + d.getNome() + " disponibili.")
                                .build();
                    }
                    prodottiDaAggiornare.add(p);
                }
            }
            // controllo che il commento non superi i 255 caratteri e lo setto a "" se è null
            if (ordine.getCommento() == null) {
                ordine.setCommento("");
            } else if (ordine.getCommento().length() > 255) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Il commento non può superare i 255 caratteri")
                        .build();
            }
            // aggiungo l'ordine
            Ordine o = ordineRepository.addOrdine(utente.getEmail(), ordine.getDettaglio(), ordine.getData_ritiro(), ordine.getCommento());
            // aggiorno la quantità dei prodotti
            for (Prodotto p : prodottiDaAggiornare) {
                prodottoRepository.update(p, String.valueOf(p.getId()));
            }
            // invio una mail al pasticcere per notificare il nuovo ordine
            String dataFormattata = o.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Mail mail = Mail.withHtml("simorusso04@gmail.com",
                    "Pasticceria C'est la Vie - Nuovo ordine (" + dataFormattata + ")",
                    HtmlNuovoOrdine(utente.getEmail(), dettaglio, ordine.getData_ritiro(), o.getId().toString(), ordine.getCommento())
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
    // ?storico=true per ottenere la lista degli ordini ritirati (stato = "RITIRATO")
    // ?correnti=true per ottenere la lista degli ordini in attesa di conferma (stato = "IN ATTESA") e in preparazione (stato = "IN PREPARAZIONE")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ordine> getOrdiniUtente(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @QueryParam("storico") @DefaultValue("false") boolean storico, @QueryParam("correnti") @DefaultValue("false") boolean correnti) {
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        if (utente == null || !utente.getRuolo().equals("CLIENTE VERIFICATO")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato").build());
        } else {
            if (storico) {
                return ordineRepository.getStoricoOrdiniUtente(utente.getEmail());
            } else if (correnti) {
                return ordineRepository.getOrdiniCorrentiUtente(utente.getEmail());
            } else {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Devi specificare almeno un parametro tra storico e correnti").build());
            }
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ordine> getOrdini(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        Utente utente = utenteRepository.findById(String.valueOf(sessionId)); // controllo che l'utente sia loggato e sia un admin o un cliente verificato
        if (utente == null || !utente.getRuolo().equals("ADMIN") && !utente.getRuolo().equals("CLIENTE VERIFICATO")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato").build());
        } else {
            return ordineRepository.getOrdini();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateOrdine(@PathParam("id") String id, Ordine ordine, @CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        Utente utente = utenteRepository.findById(String.valueOf(sessionId)); // controllo che l'utente sia loggato e sia un admin
        if (utente == null || !utente.getRuolo().equals("ADMIN")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Accesso negato")
                    .build();
        } else {
            // aggiorno l'ordine
            Ordine ordineAggiornato = ordineRepository.update(ordine, id);
            // invio una mail all'utente per confermare la prenotazione e ricordare la data di ritiro
            Mail mail = Mail.withHtml(ordineAggiornato.getEmail_utente(),
                    "Pasticceria C'est la Vie - Prenotazione confermata",
                    HtmlOrdineConfermato(ordineAggiornato.getData_ritiro())
            );
            Uni<Void> send = reactiveMailer.send(mail);
            send.subscribe().with(
                    success -> System.out.println("Mail inviata"),
                    failure -> System.out.println("Errore nell'invio della mail" + failure)
            );
            return Response
                    .status(Response.Status.OK)
                    .entity("Ordine aggiornato con successo. E' stata inviata una mail all'utente in cui si conferma la prenotazione e si ricorda la data di ritiro")
                    .build();
        }
    }

    @GET
    @Path("/excel/{data}")
    public Response getExcelOrdini(@PathParam("data") String data, @CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        Utente admin = utenteRepository.findById(String.valueOf(sessionId)); // controllo che l'utente sia loggato e sia un admin
        if (admin == null || !admin.getRuolo().equals("ADMIN")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Accesso negato")
                    .build();
        } else {
            ordineRepository.getExcelOrdini(data); // genero il file excel
            return Response
                    .status(Response.Status.OK)
                    .entity("File excel generato con successo")
                    .build();
        }
    }

    // private methods

    private String HtmlNuovoOrdine(String emailUtente, List<DettaglioProdotto> dettaglio, LocalDateTime dataRitiro, String idOrdine, String commento) {
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
        messaggio.append("<p>Data ritiro: ").append(dataRitiro.format(formatter)).append("</p>");
        if (commento != null && !commento.isEmpty()) {
            messaggio.append("<p>Commento dal cliente: ").append(commento).append("</p>");
        }
        messaggio.append("<p>Clicca <a href='http://localhost:3000/ConfermaPrenotazione?id=").append(idOrdine).append("'>qui</a> per confermare l'ordine</p>");
        messaggio.append("</body></html>");
        return messaggio.toString();
    }

    private String HtmlOrdineConfermato(LocalDateTime dataRitiro) {
        // mostrare solo il fatto che l'ordine è stato confermato dalla pasticceria e la data di ritiro
        StringBuilder messaggio = new StringBuilder();
        messaggio.append("<html><body>");
        messaggio.append("<h1>Ordine confermato</h1>");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        messaggio.append("<p>Il tuo ordine è stato confermato dalla pasticceria. Data di ritiro: ").append(dataRitiro.format(formatter)).append("</p>");
        messaggio.append("</body></html>");
        return messaggio.toString();
    }
}
