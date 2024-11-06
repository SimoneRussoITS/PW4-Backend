package itsincom.webdev2425.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import itsincom.webdev2425.persistence.model.Prodotto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
@Transactional
public class ProdottoRepository implements PanacheRepository<Prodotto> {

    public List<Prodotto> getAll(String search) {
        if (search == null) {
            return listAll();
        } else {
            return list("nome like ?1", "%" + search + "%");
        }
    }

    public Prodotto findByName(String nome) {
        Prodotto prodotto = find("nome", nome).firstResult();
        return prodotto;
    }

    public Prodotto getById(String id) {
        Long idLong = Long.parseLong(id);
        return findById(idLong);
    }

    public Prodotto add(Prodotto prodotto) {
        Prodotto newProdotto = Prodotto.create(prodotto.getNome(), prodotto.getDescrizione(), prodotto.getIngredienti(), prodotto.getQuantita(), prodotto.getPrezzo(), prodotto.getFoto());
        persist(newProdotto);
        return newProdotto;
    }

    public Prodotto update(Prodotto prodotto, String id) {
        update("nome = ?1, " +
               "descrizione = ?2, " +
               "ingredienti = ?3, " +
               "quantita = ?4, " +
               "prezzo = ?5, " +
               "foto = ?6 " +
               "where id = ?7",
                prodotto.getNome(),
                prodotto.getDescrizione(),
                prodotto.getIngredienti(),
                prodotto.getQuantita(),
                prodotto.getPrezzo(),
                prodotto.getFoto(),
                id);
        return prodotto;
    }

    public void delete(String id) {
        Long idLong = Long.parseLong(id);
        deleteById(idLong);
    }
}
