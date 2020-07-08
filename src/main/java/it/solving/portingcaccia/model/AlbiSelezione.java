package it.solving.portingcaccia.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class AlbiSelezione {
    LocalDate dataIscrizione;
    String numIscrizioneAlboProvenienza;
    String numIscrizioneAlboProvenienzaCinghiale;
    Integer numIscrizioneAlboRegionale;
    String sezioneAlboProvinciale;
    List<Specializzazione> specie = new ArrayList<>();
}
