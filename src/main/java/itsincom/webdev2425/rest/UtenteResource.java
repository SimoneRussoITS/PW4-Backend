package itsincom.webdev2425.rest;

import itsincom.webdev2425.persistence.model.Utente;
import itsincom.webdev2425.persistence.repository.UtenteRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/utente")
public class UtenteResource {
    private final UtenteRepository utenteRepository;

    public UtenteResource(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Utente> getUtenti() {
        return utenteRepository.getAllUtenti();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Utente getUtenteById(@PathParam("id") String id, @CookieParam("SESSION_COOKIE") String sessionId) {
        if (sessionId == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato, nessun utente loggato").build());
        } else {
            // controllo che l'utente loggato sia un admin
            Utente admin = utenteRepository.findById(sessionId);
            if (admin == null || !admin.getRuolo().equals("ADMIN")) {
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato").build());
            } else {
                // controllo che l'utente da cercare esista
                Utente u = utenteRepository.findById(id);
                if (u == null) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Utente non trovato").build());
                } else {
                    return u;
                }
            }
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Utente getUtente(@CookieParam("SESSION_COOKIE") String sessionId) {
        if (sessionId == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato, nessun utente loggato").build());
        } else {
            // controllo che l'utente loggato esista
            Utente utente = utenteRepository.findById(sessionId);
            if (utente == null) {
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato, utente non trovato").build());
            } else {
                return utente;
            }
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUtente(Utente utente, @CookieParam("SESSION_COOKIE") String sessionId, @PathParam("id") String id) {
        if (sessionId == null) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Accesso negato, nessun utente loggato")
                    .build();
        } else {
            Utente admin = utenteRepository.findById(sessionId); // controllo che l'utente loggato sia un admin
            if (admin == null || !admin.getRuolo().equals("ADMIN")) {
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity("Accesso negato")
                        .build();
            } else {
                // aggiorno l'utente
                Utente u = utenteRepository.updateUtente(utente, id);
                if (u == null) {
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("Errore durante l'aggiornamento dell'utente")
                            .build();
                } else {
                    return Response
                            .status(Response.Status.OK)
                            .entity("Utente aggiornato con successo")
                            .build();
                }
            }
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUtente(@PathParam("id") String id, @CookieParam("SESSION_COOKIE") String sessionId) {
        if (sessionId == null) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Accesso negato, nessun utente loggato")
                    .build();
        } else {
            Utente admin = utenteRepository.findById(sessionId); // controllo che l'utente loggato sia un admin
            if (admin == null || !admin.getRuolo().equals("ADMIN")) {
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity("Accesso negato")
                        .build();
            } else {
                // controllo che l'utente da eliminare esista
                Utente daEliminare = utenteRepository.findById(id);
                if (daEliminare == null) {
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity("Utente non trovato")
                            .build();
                } else {
                    // elimino l'utente
                    utenteRepository.delete(id);
                    return Response
                            .status(Response.Status.OK)
                            .entity("Utente eliminato con successo")
                            .build();
                }
            }
        }
    }
}
