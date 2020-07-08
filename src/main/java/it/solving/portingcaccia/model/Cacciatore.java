package it.solving.portingcaccia.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "CACCIATORI")
public class Cacciatore {

    @Id
    String id;

    Boolean fromExport;
    Boolean attivo;
    Boolean eliminato;
    Integer codiceCacciatore;
    String codiceFiscale;
    String nome;
    String cognome;
    LocalDate dataNascita;
    String indirizzoResidenza;
    String comuneResidenza;
    String capResidenza;
    String provinciaResidenza;
    String regioneResidenza;
    String telefono;
    Character sesso;
    String indirizzoDomicilio;
    String comuneDomicilio;
    String capDomicilio;
    String provinciaDomicilio;
    String regioneDomicilio;
    String pec;
    String mail;
    Boolean registratoApaci;
    LocalDate dataInserimento;
    LocalDate dataModifica;
    String utente;
    String codiceCircoscrizione;
    String comuneNascita;
    Integer codiceRegioneResidenza;
    Integer codiceRegioneDomicilio;
    String codiceComuneResidenza;
    String codiceComuneDomicilio;

    String richiestaCacciaCinghiale;
    String cittadinanza;
    Boolean isComuneEsterno;

    AlbiCinghiale albiCinghiale;
    AlbiSelezione albiSelezione;
    List<Venatoria> venatorie = new ArrayList<>();
    List<Appostamento> appostamenti = new ArrayList<>();
    String atcCreazione;
    String comuneCreazione;

    Integer validita;

    LocalDate dataPrimaIscrizioneATC;
    LocalDate dataUltimaCeATC;
}
