package itsincom.webdev2425.rest;

import itsincom.webdev2425.persistence.model.DettaglioProdotto;
import itsincom.webdev2425.persistence.model.Ordine;
import itsincom.webdev2425.persistence.repository.OrdineRepository;
import itsincom.webdev2425.persistence.repository.UtenteRepository;
import jakarta.ws.rs.POST;

import java.util.List;

public class OrdineResource {
    private final OrdineRepository ordineRepository;
    private final UtenteRepository utenteRepository;

    public OrdineResource(OrdineRepository ordineRepository, UtenteRepository utenteRepository) {
        this.ordineRepository = ordineRepository;
        this.utenteRepository = utenteRepository;
    }

    @POST
    public void addOrdine(Ordine ordine) {
        utenteRepository.findByEmail(ordine.getEmail_utente());
        ordineRepository.addOrdine(ordine.getEmail_utente(), ordine.getDettaglio(), ordine.getData_ritiro());
    }
}
