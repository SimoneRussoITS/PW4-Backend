package itsincom.webdev2425.persistence.repository;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.auth.impl.hash.SHA512;
import itsincom.webdev2425.persistence.model.Utente;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Transactional
public class AuthRepository implements PanacheRepository<Utente> {
    private final ReactiveMailer reactiveMailer;

    @ConfigProperty(name = "twilio.account.sid")
    String twilioAccountSid;

    @ConfigProperty(name = "twilio.auth.token")
    String twilioAuthToken;

    @ConfigProperty(name = "twilio.phone.number")
    String twilioPhoneNumber;

    public AuthRepository(ReactiveMailer reactiveMailer) {
        this.reactiveMailer = reactiveMailer;
    }

    // register
    public void register(String nome, String cognome, String email, String telefono, String password) {
        telefono = "+39" + telefono;
        // controllo se la mail o il telefono sono già presenti nel db
        if (find("email", email).count() > 0 && !email.isBlank()) {
            throw new WebApplicationException(Response.status(Response.Status.CONFLICT).entity("Email già presente").build());
        }
        if (find("telefono", telefono).count() > 0 && !telefono.isBlank()) {
            throw new WebApplicationException(Response.status(Response.Status.CONFLICT).entity("Telefono già presente").build());
        }
        // l'utente può registrarsi o con la mail o con il telefono o con entrambi
        if (email == null || email.isBlank()) {
            email = "";
        }
        if (telefono.equals("+39")) {
            telefono = "";
        }
        if (email.isBlank() && telefono.isBlank()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Inserire almeno un contatto").build());
        }
        // creo l'utente
        Utente utente = Utente.create(nome, cognome, email, telefono, password);
        persist(utente);

    }

    // login
    public void login(String email, String telefono, String password) {
        SHA512 algorithm = new SHA512();
        String passwordToCheck = algorithm.hash(null, password);
        if (email.isBlank() && telefono.isBlank()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Inserire almeno un contatto").build());
        }

        Utente utente = find("email = ?1 or telefono = ?2", email, telefono).firstResult();
        if (utente == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Utente non trovato").build());
        }
        if (!utente.getPassword().equals(passwordToCheck)) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Password errata").build());
        }
    }

    // invio della mail
    public void inviaMail(String email) {
        // invio della mail
        if (!email.isBlank()) {
            // invio della mail con sendgrid
            Mail mail = Mail.withHtml(email,
                    "Verifica email",
                    "Clicca <a href='http://localhost:3000/VerificaMail?email=" + email + "'>qui</a> per verificare la tua email"
            );
            Uni<Void> send = reactiveMailer.send(mail);
            send.subscribe().with(
                    success -> System.out.println("Mail inviata"),
                    failure -> System.out.println("Errore nell'invio della mail")
            );
        } else {
            throw new RuntimeException("Email non presente");
        }
    }

    // invio del messaggio di verifica tramite telefono
    public void inviaMessaggio(String telefono) {
        // invio del messaggio di verifica tramite twilio
        if (!telefono.isBlank()) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            Verification verification = Verification.creator(
                    "VAe40230670751d93c15ddbe73884cb46b",
                    "+39" + telefono,
                    "sms"
            ).create();
            System.out.println(verification.getStatus());
        } else {
            throw new RuntimeException("Telefono non presente");
        }
    }

    // controllo del codice di verifica
    public Response verificaCodice(String telefono, String codice) {
        Twilio.init(twilioAccountSid, twilioAuthToken);
        VerificationCheck verificationCheck = VerificationCheck.creator("VAe40230670751d93c15ddbe73884cb46b")
                .setTo("+39" + telefono)
                .setCode(codice)
                .create();
        if (verificationCheck.getStatus().equals("approved")) {
            return Response.ok().entity("Codice verificato").build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Codice non verificato").build();
        }
    }


    // aggiornamento del ruolo dell'utente dopo la verifica
    public void aggiornaRuolo(String email) {
        // controllo se l'utente è presente
        Utente utente = find("email", email).firstResult();
        if (utente == null) {
            throw new RuntimeException("Utente non trovato");
        }
        // aggiorno il ruolo dell'utente
        utente.setRuolo("CLIENTE VERIFICATO");
        update("ruolo = ?1 where email = ?2", utente.getRuolo(), email);
    }
}


