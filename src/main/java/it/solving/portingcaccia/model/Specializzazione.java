package it.solving.portingcaccia.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Specializzazione {
    String specie;
    Integer codiceSpecie;

    Boolean abilitazione;
    LocalDate dataAbilitazione;
    Double votoEsame;

    String modalitaAbilitazione;
    Integer codiceModalitaAbilitazione;

    Boolean fromExport;
}
