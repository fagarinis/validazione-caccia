package it.solving.portingcaccia.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Teleprenotazione {
    Integer codiceTelefonata;

    LocalDate dataPrenotazione;
    String dataNascitaCacciatore;

    DatoTipologicaInteger atcPrenotazione;
    Boolean isMobile;
    DatoTipologicaInteger tipoPrenotazione;

    LocalDate prenotatoIl;

    String modalitaPrenotazione;

    Boolean eliminato;
    Boolean fromExport;
    Boolean confermata;
}
