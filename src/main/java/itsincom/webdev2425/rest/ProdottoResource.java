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
    public List<Prodotto> getProdotti(@QueryParam("search") String search) {
        return prodottoRepository.getAll(search);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addProdotto(Prodotto prodotto, @CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) {
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        if (utente == null || !utente.getRuolo().equals("ADMIN")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Accesso negato")
                    .build();
        } else {
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
        Utente utente = utenteRepository.findById(String.valueOf(sessionId));
        if (utente == null || !utente.getRuolo().equals("ADMIN")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Accesso negato")
                    .build();
        } else {
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
}