package itsincom.webdev2425.rest;

import itsincom.webdev2425.persistence.model.Utente;
import itsincom.webdev2425.persistence.repository.AuthRepository;
import itsincom.webdev2425.persistence.repository.UtenteRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/auth")
public class AuthResource {
    private final AuthRepository authRepository;
    private final UtenteRepository utenteRepository;

    public AuthResource(AuthRepository authRepository, UtenteRepository utenteRepository) {
        this.authRepository = authRepository;
        this.utenteRepository = utenteRepository;
    }

    // register
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(Utente utente) {
        String mail = utente.getEmail();
        String telefono = utente.getTelefono();
        if (!mail.isBlank() && !telefono.isBlank()) {
            authRepository.inviaMail(utente.getEmail());
            authRepository.register(utente.getNome(), utente.getCognome(), utente.getEmail(), utente.getTelefono(), utente.getPassword());
            return Response.status(Response.Status.CREATED).entity("Utente registrato con successo. Verifica la tua email prima di accedere. Ti abbiamo inviato una mail con il link di conferma.").build();
        } else {
            if (!mail.isBlank()) {
                authRepository.inviaMail(utente.getEmail());
                authRepository.register(utente.getNome(), utente.getCognome(), utente.getEmail(), utente.getTelefono(), utente.getPassword());
                return Response.status(Response.Status.CREATED).entity("Utente registrato con successo. Verifica la tua email prima di accedere. Ti abbiamo inviato una mail con il link di conferma.").build();
            } else {
                authRepository.inviaMessaggio(utente.getTelefono());
                authRepository.register(utente.getNome(), utente.getCognome(), utente.getEmail(), utente.getTelefono(), utente.getPassword());
                return Response.status(Response.Status.CREATED).entity("Utente registrato con successo. Verifica il tuo telefono prima di accedere. Ti abbiamo inviato un messaggio con il codice di conferma.").build();
            }
        }
    }

    // login
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(Utente utente) {
        if (utente.getEmail() == null && utente.getTelefono() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Inserire almeno un contatto.").build();
        } else {
            if (utente.getPassword().isBlank()) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Inserire la password.").build();
            }
            Utente u;
            if (utente.getEmail() != null) {
                u = utenteRepository.findByEmail(utente.getEmail());
            } else {
                u = utenteRepository.findByPhone(utente.getTelefono());
            }
            if (u == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Utente non trovato.").build();
            }
            if (u.getRuolo().equals("CLIENTE NON VERIFICATO")) {
                if (!u.getEmail().isBlank() && !u.getTelefono().isBlank()) {
                    authRepository.inviaMail(utente.getEmail());
                } else {
                    if (!u.getEmail().isBlank()) {
                        authRepository.inviaMail(utente.getEmail());

                    } else {
                        authRepository.inviaMessaggio(utente.getTelefono());
                    }
                }
                return Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato, verifica la tua email prima di accedere. Ti abbiamo inviato una mail con il link di conferma.").build();
            } else {
                authRepository.login(u.getEmail(), u.getTelefono(), utente.getPassword());
                NewCookie sessionCookie = new NewCookie.Builder("SESSION_COOKIE")
                        .path("/")
                        .value(String.valueOf(u.getId()))
                        .build();
                return Response.ok()
                        .cookie(sessionCookie)
                        .entity("Accesso effettuato con successo.")
                        .build();
            }
        }
    }

    // verifica utente tramite mail
    @POST
    @Path("/verifica/{email}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void verificaByEmail(@PathParam("email") String email) {
        authRepository.aggiornaRuolo(email);
    }

    // verifica utente tramite telefono
    @POST
    @Path("/verifica/{telefono}/{codice}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verificaByPhone(@PathParam("codice") String codice, @PathParam("telefono") String telefono) {
        Response response = authRepository.verificaCodice(telefono, codice);
        if (response.getStatus() == 200) {
            Utente u = utenteRepository.findByPhone(telefono);
            authRepository.aggiornaRuolo(u.getEmail());
            return Response.ok().entity("Utente verificato con successo.").build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Codice non verificato.").build();
        }

    }

    // logout
    @DELETE
    @Path("/logout")
    public Response logout(@CookieParam("SESSION_COOKIE") int sessionId) {
        // cancella il cookie se esiste
        if (sessionId > 0) {
            NewCookie sessionCookie = new NewCookie.Builder("SESSION_COOKIE")
                    .path("/")
                    .maxAge(0)
                    .build();
            return Response.ok()
                    .cookie(sessionCookie)
                    .entity("Logout effettuato con successo.")
                    .build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato, effettua il login per poter effettuare il logout.").build();
        }
    }

}
