package it.solving.portingcaccia.dto;

import lombok.Data;

@Data
public class QueryResultSelezione {

    String numIscrizioneAlboProvenienza;
    String dataIscrizione;
    String sezioneAlboProvinciale;
    String dataAbilitazione;
    Boolean abilitazione;
    String modalitaAbilitazione;
    Double votoEsame;
    String specie;
}
