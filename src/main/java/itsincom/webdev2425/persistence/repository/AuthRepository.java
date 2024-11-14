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

    public AuthRepository(ReactiveMailer reactiveMailer) {
        this.reactiveMailer = reactiveMailer;
    }

    public void register(String nome, String cognome, String email, String telefono, String password) {
        // controllo se la mail o il telefono sono già presenti nel db
        if (find("email", email).count() > 0 && !email.isBlank()) {
            throw new WebApplicationException(Response.status(Response.Status.CONFLICT).entity("Email già presente").build());
        }
        if (find("telefono", telefono).count() > 0 && !telefono.isBlank()) {
            throw new WebApplicationException(Response.status(Response.Status.CONFLICT).entity("Telefono già presente").build());
        }

        // creo l'utente e lo salvo nel db
        Utente utente = Utente.create(nome, cognome, email, telefono, password);
        persist(utente);

    }

    public void login(String email, String telefono, String password) {
        // cripto la password inserita nel form con SHA512
        SHA512 algorithm = new SHA512();
        String passwordToCheck = algorithm.hash(null, password);

        // controllo se l'utente è presente nel db e se la password è corretta
        Utente utente = find("email = ?1 or telefono = ?2", email, telefono).firstResult();
        if (utente == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Utente non trovato").build());
        }
        if (!utente.getPassword().equals(passwordToCheck)) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Password errata").build());
        }
    }

    public void inviaMail(String email) {
        // invio della mail con quarkus mailer
        Mail mail = Mail.withHtml(email,
                "Pasticceria C'est la Vie - Verifica email",
                HtmlConfermaEmail(email)
        );
        Uni<Void> send = reactiveMailer.send(mail);

        // controllo se la mail è stata inviata con successo
        send.subscribe().with(
                success -> System.out.println("Mail inviata"),
                failure -> System.out.println("Errore nell'invio della mail")
        );
    }

    public void inviaMessaggio(String telefono) {
        // invio del messaggio con twilio
        Twilio.init(twilioAccountSid, twilioAuthToken);
        Verification verification = Verification.creator(
                "VAe40230670751d93c15ddbe73884cb46b",
                "+39" + telefono,
                "sms"
        ).create();
        System.out.println(verification.getStatus());
    }

    public Response verificaCodice(String telefono, String codice) {
        // controllo del codice di verifica tramite twilio
        Twilio.init(twilioAccountSid, twilioAuthToken);
        VerificationCheck verificationCheck = VerificationCheck.creator("VAe40230670751d93c15ddbe73884cb46b")
                .setTo("+39" + telefono)
                .setCode(codice)
                .create();
        // controllo se il codice è stato verificato
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

    // private methods
    private String HtmlConfermaEmail(String email) {
        StringBuilder messaggio = new StringBuilder();
        messaggio.append("<html><body>");
        messaggio.append("<h1>Conferma email</h1>");
        // http://localhost:3000/VerificaMail?email=" + email
        messaggio.append("<p>Clicca <a href='http://localhost:3000/VerificaMail?email=").append(email).append("'>qui</a> per confermare la tua email</p>");
        messaggio.append("</body></html>");
        return messaggio.toString();
    }
}


