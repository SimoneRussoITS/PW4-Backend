package itsincom.webdev2425.persistence.repository;
import itsincom.webdev2425.persistence.model.Utente;


import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UtenteRepository implements PanacheRepository<Utente> {


    public Utente findByEmail(String email) {
        return find("email", email).firstResult();
    }
}
