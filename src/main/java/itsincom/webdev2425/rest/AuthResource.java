package itsincom.webdev2425.rest;

import itsincom.webdev2425.persistence.model.Utente;
import itsincom.webdev2425.persistence.repository.AuthRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/auth")
public class AuthResource {
    private final AuthRepository authRepository;

    public AuthResource(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    // register
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public void register(Utente utente) {
        authRepository.register(utente.getNome(), utente.getCognome(), utente.getEmail(), utente.getTelefono(), utente.getPassword());
        authRepository.verifica(utente.getEmail(), utente.getTelefono());
    }

    // login
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public void login(Utente utente) {
        Utente utenteLoggato = authRepository.login(utente.getEmail(), utente.getTelefono(), utente.getPassword());
        if (utenteLoggato.getRuolo().equals("CLIENTE NON VERIFICATO")) {
            authRepository.verifica(utenteLoggato.getEmail(), utenteLoggato.getTelefono());
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
