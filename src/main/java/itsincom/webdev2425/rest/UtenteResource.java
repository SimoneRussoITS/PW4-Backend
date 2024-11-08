package itsincom.webdev2425.rest;

import itsincom.webdev2425.persistence.model.Utente;
import itsincom.webdev2425.persistence.repository.UtenteRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/utente")
public class UtenteResource {
    private final UtenteRepository utenteRepository;

    public UtenteResource(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Utente getUtente(@CookieParam("SESSION_COOKIE") String sessionId) {
        // se il cookie non è presente, restituisce una eccezione 401 (Unauthorized) con messaggio "Accesso negato, nessun utente loggato"
        if (sessionId == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato, nessun utente loggato").build());
        } else {
            // se l'utente non è presente nel database, restituisce una eccezione 401 (Unauthorized) con messaggio "Accesso negato, utente non trovato"
            Utente utente = utenteRepository.findById(sessionId);
            if (utente == null) {
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato, utente non trovato").build());
            } else {
                return utente;
            }
        }
    }

}
