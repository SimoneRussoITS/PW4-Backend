package itsincom.webdev2425.persistence.repository;

import com.twilio.rest.verify.v2.service.Verification;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import itsincom.webdev2425.persistence.model.Utente;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
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
    @Transactional
    public void register(String nome, String cognome, String email, String telefono, String password) {
        // controllo se la mail o il telefono sono già presenti nel db
        if (find("email", email).count() > 0) {
            throw new RuntimeException("Email già presente");
        }
        if (find("telefono", telefono).count() > 0) {
            throw new RuntimeException("Telefono già presente");
        }
        // l'utente può registrarsi o con la mail o con il telefono o con entrambi
        if (email == null || email.isBlank()) {
            email = "";
        }
        if (telefono == null || telefono.isBlank()) {
            telefono = "";
        }
        if (email.isBlank() && telefono.isBlank()) {
            throw new RuntimeException("Inserire almeno un contatto");
        }
        // creo l'utente
        Utente utente = Utente.create(nome, cognome, email, telefono, password);
        persist(utente);

    }

    // login
    @Transactional
    public Utente login(String email, String telefono, String password) {
        // controllo se l'utente è presente a seconda se ha inserito la mail o il telefono o entrambi
        Utente utente = find("email = ?1 or telefono = ?2", email, telefono).firstResult();
        if (utente == null) {
            throw new RuntimeException("Utente non trovato");
        }
        if (!utente.getPassword().equals(String.valueOf(password.hashCode()))) {
            throw new RuntimeException("Password errata");
        }
        // se l'utente è presente e la password è corretta ritorno l'utente
        return utente;
    }

    // verifica della mail o del telefono tramite invio di una mail o di un sms
    @Transactional
    public void verifica(String email, String telefono) {
        // invio della mail o dell'sms
        if (!email.isBlank()) {
            // invio della mail con sendgrid
            Mail mail = Mail.withHtml(email,
                    "Verifica email",
                    "Clicca <a href='http://localhost:8080/auth/verifica/" + email + "'>qui</a> per verificare la tua email"
            );
            Uni<Void> send = reactiveMailer.send(mail);
            send.subscribe().with(
                    success -> System.out.println("Mail inviata"),
                    failure -> System.out.println("Errore nell'invio della mail")
            );
        }
//        if (!telefono.isBlank()) {
//            // invio del sms con twilio
//            Twilio.init(twilioAccountSid, twilioAuthToken);
//            Verification verification = Verification.creator(
//                            "VAe40230670751d93c15ddbe73884cb46b",
//                            telefono,
//                            "sms")
//                    .create();
//            verification.getSid();
//        }
    }

    // aggiornamento del ruolo dell'utente dopo la verifica
    @Transactional
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
