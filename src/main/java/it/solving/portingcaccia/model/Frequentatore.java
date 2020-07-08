package it.solving.portingcaccia.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Frequentatore {

    String codiceFiscale;
    String nominativo;
    LocalDate dataNascita;
    String comuneNascita;
    String codiceCacciatore;
    DatoTipologicaInteger tipologiaCaccia;

}
