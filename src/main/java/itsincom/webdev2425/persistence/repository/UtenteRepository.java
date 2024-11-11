package itsincom.webdev2425.persistence.repository;

import itsincom.webdev2425.persistence.model.Utente;


import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
@Transactional
public class UtenteRepository implements PanacheRepository<Utente> {
    public Utente findByEmail(String email) {
        Utente utente = find("email", email).firstResult();
        return utente;
    }

    public Utente findByPhone(String telefono) {
        Utente utente = find("telefono", "+39" + telefono).firstResult();
        return utente;
    }

    public Utente findById(String id) {
        Long idLong = Long.parseLong(id);
        Utente utente = findById(idLong);
        return utente;
    }

    public List<Utente> getAllUtenti() {
        return listAll();
    }
}
