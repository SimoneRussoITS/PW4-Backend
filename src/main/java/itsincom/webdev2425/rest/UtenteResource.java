package itsincom.webdev2425.rest;

import itsincom.webdev2425.persistence.model.Utente;
import itsincom.webdev2425.persistence.repository.UtenteRepository;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/utente")
public class UtenteResource {
    private final UtenteRepository utenteRepository;

    public UtenteResource(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Utente getUtente(@CookieParam("SESSION_COOKIE") String sessionId) {
        return utenteRepository.findById(sessionId);
    }

}
