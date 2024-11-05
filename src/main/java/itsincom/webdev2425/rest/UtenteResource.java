package itsincom.webdev2425.rest;

import itsincom.webdev2425.persistence.repository.UtenteRepository;

public class UtenteResource {
    private final UtenteRepository utenteRepository;

    public UtenteResource(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }
}
