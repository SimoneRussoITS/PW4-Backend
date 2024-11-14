package itsincom.webdev2425.rest;

import itsincom.webdev2425.persistence.model.Prodotto;
import itsincom.webdev2425.persistence.model.Utente;
import itsincom.webdev2425.persistence.repository.ProdottoRepository;
import itsincom.webdev2425.persistence.repository.UtenteRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/prodotto")
public class ProdottoResource {
    private final ProdottoRepository prodottoRepository;
    private final UtenteRepository utenteRepository;

    public ProdottoResource(ProdottoRepository prodottoRepository, UtenteRepository utenteRepository) {
        this.prodottoRepository = prodottoRepository;
        this.utenteRepository = utenteRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Prodotto> getProdotti(@QueryParam("search") String search, @CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        if (utente == null || !utente.getRuolo().equals("ADMIN") && !utente.getRuolo().equals("CLIENTE VERIFICATO")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato").build());
        }
        return prodottoRepository.getAll(search);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Prodotto getProdotto(@PathParam("id") String id, @CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        if (utente == null || !utente.getRuolo().equals("ADMIN") && !utente.getRuolo().equals("CLIENTE VERIFICATO")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato").build());
        }
        return prodottoRepository.getById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addProdotto(Prodotto prodotto, @CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        // controllo se l'utente è un admin
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        if (utente == null || !utente.getRuolo().equals("ADMIN")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Accesso negato")
                    .build();
        } else {
            // controllo se i campi obbligatori sono stati inseriti
            checkFields(prodotto);
            // aggiungo il prodotto
            Prodotto p = prodottoRepository.add(prodotto);
            if (p == null) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Errore durante l'aggiunta del prodotto")
                        .build();
            } else {
                return Response
                        .status(Response.Status.CREATED)
                        .entity("Prodotto aggiunto con successo")
                        .build();
            }
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProdotto(Prodotto prodotto, @PathParam("id") String id, @CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        // controllo se l'utente è un admin
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        if (utente == null || !utente.getRuolo().equals("ADMIN")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Accesso negato")
                    .build();
        } else {
            // controllo se i campi obbligatori sono stati inseriti
            checkFields(prodotto);
            // aggiorno il prodotto
            Prodotto p = prodottoRepository.update(prodotto, id);
            if (p == null) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Errore durante l'aggiornamento del prodotto")
                        .build();
            } else {
                return Response
                        .status(Response.Status.OK)
                        .entity("Prodotto aggiornato con successo")
                        .build();
            }
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProdotto(@PathParam("id") String id, @CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        // controllo se l'utente è un admin
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        if (utente == null || !utente.getRuolo().equals("ADMIN")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Accesso negato")
                    .build();
        } else {
            Prodotto p = prodottoRepository.getById(id);
            if (p == null) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Il prodotto specificato non esiste")
                        .build();
            } else {
                prodottoRepository.delete(id);
                return Response
                        .status(Response.Status.OK)
                        .entity("Prodotto eliminato con successo")
                        .build();
            }
        }
    }

    @GET
    @Path("/excel")
    public Response getExcelInventario(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        if (utente == null || !utente.getRuolo().equals("ADMIN")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Accesso negato")
                    .build();
        } else {
            prodottoRepository.getExcelInventario();
            return Response
                    .status(Response.Status.OK)
                    .entity("File excel generato con successo")
                    .build();
        }
    }

    // private methods

    private void checkFields(Prodotto prodotto) {
        // imposta un messaggio di errore diverso per ogni campo vuoto
        if (prodotto.getNome() == null || prodotto.getNome().isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Nome non inserito").build());
        }
        if (prodotto.getDescrizione() == null || prodotto.getDescrizione().isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Descrizione non inserita").build());
        }
        if (prodotto.getIngredienti() == null || prodotto.getIngredienti().isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Ingredienti non inseriti").build());
        }
        if (prodotto.getQuantita() == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Quantità non inserita").build());
        }
        if (prodotto.getPrezzo() == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Prezzo non inserito").build());
        }
        if (prodotto.getFoto() == null || prodotto.getFoto().isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Foto non inserita").build());
        }
    }
}
