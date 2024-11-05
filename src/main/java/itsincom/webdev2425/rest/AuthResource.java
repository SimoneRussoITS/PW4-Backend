package itsincom.webdev2425.rest;

import itsincom.webdev2425.persistence.model.Utente;
import itsincom.webdev2425.persistence.repository.AuthRepository;
import itsincom.webdev2425.persistence.repository.UtenteRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
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
        authRepository.register(utente.getNome(), utente.getCognome(), utente.getEmail(), utente.getTelefono(), utente.getPassword());
        authRepository.inviaNotifica(utente.getEmail(), utente.getTelefono());
        return Response.status(Response.Status.CREATED).entity("Utente registrato, controlla la tua casella di posta per verificare l'account e continuare con la login.").build();

    }

    // login
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(Utente utente) {
        Utente u = utenteRepository.findByEmail(utente.getEmail());
        if (u.getRuolo().equals("CLIENTE NON VERIFICATO")) {
            authRepository.inviaNotifica(u.getEmail(), u.getTelefono());
            return Response.status(Response.Status.UNAUTHORIZED).entity("Accesso negato, verifica la tua email prima di accedere. Ti abbiamo inviato una mail con il link di conferma.").build();
        } else {
            authRepository.login(utente.getEmail(), utente.getTelefono(), utente.getPassword());
            return Response.status(Response.Status.OK).entity("Accesso effettuato").build();
        }

    }

    // verifica utente
    @POST
    @Path("/verifica/{email}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void verifica(@PathParam("email") String email) {
        authRepository.aggiornaRuolo(email);
    }

}
