package it.solving.portingcaccia.model;


import lombok.Data;

import java.time.LocalDate;

@Data
public class AlbiCinghiale {
    String numIscrizioneAlboProvenienza;
    Integer numIscrizioneAlboRegionale;
    LocalDate dataIscrizione;
    String sezioneAlboProvinciale;

    String modalitaIscrizione;
    Integer codiceModalitaIscrizione;

    Boolean fromExport;
}
