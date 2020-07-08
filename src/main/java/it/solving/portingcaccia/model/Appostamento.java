package it.solving.portingcaccia.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Appostamento {

    String codiceCapanno;
    String idWorkflow;

    DatoTipologicaString sedeTerritoriale;

    DatoTipologicaInteger tipologiaCaccia;
    List<AppostamentoComplementare> appostamentiComplementari = new ArrayList<>();
    String foglioCatastale;
    String numeroParticella;
    String coordinataX;
    String coordinataY;
    DatoTipologicaString comuneAppostamento;
    String frazione;
    String localita;
    String nomeProprietario;
    String cognomeProprietario;
    String comuneNascitaProprietario;
    String comuneResidenzaProprietario;
    String frazioneResidenzaProprietario;
    String capResidenzaProprietario;
    LocalDate dataNascitaProprietario;
    String indirizzoResidenzaProprietario;
    String provinciaResidenzaProprietario;
    List<Frequentatore> frequentatori = new ArrayList<>();
    String tipoScelta;
    Boolean ultrasessantenne;

    String numeroProtocollo;

    LocalDate dataProtocollo;
    LocalDate dataRicezione;
    LocalDate dataRinnovo;

    DatoTipologicaInteger stato;

    String codiceProgetto;
    String codiceProcedimento;
    String chkproc;
    List<String> uuid = new ArrayList<>();

    Boolean attivo;
    Boolean fromExport;
    Boolean eliminato;
}
