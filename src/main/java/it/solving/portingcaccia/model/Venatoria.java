package it.solving.portingcaccia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Venatoria {

    String stagioneVenatoria;
    Boolean rinunciaATC;
    Character areaContigua;

    DatoTipologicaInteger residenzaATC;

    DatoTipologicaInteger primoATC;
    DatoTipologicaInteger primoATCPreiscrizione;

    List<DatoTipologicaInteger> ulterioriATC = new ArrayList<>();
    List<DatoTipologicaInteger> ulterioriATCPreiscrizione = new ArrayList<>();

    Integer giornateStanziale;
    Integer giornateMobilita;

    Boolean abilitazionePacchettoCinqueGiornate;
    String codiceStanziale;

    Integer numeroTesserinoVenatorio;
    LocalDate dataRilascioTesserinoVenatorio;
    LocalDate dataRiconsegnaTesserinoVenatorio;
    Boolean tesserinoCompilatoManualmente;

    Integer numeroTesserinoVenatorioDuplicato;
    LocalDate dataRilascioTesserinoVenatorioDuplicato;
    LocalDate dataRiconsegnaTesserinoVenatorioDuplicato;
    Boolean tesserinoCompilatoManualmenteDuplicato;

    Boolean toscaccia;

    DatoTipologicaInteger tipologiaAbilitazioneCaccia;

    List<Teleprenotazione> teleprenotazioni = new ArrayList<>();

    LocalDate dataPrimoATC;

    public Venatoria(String stagioneVenatoria) {
        this.stagioneVenatoria = stagioneVenatoria;
    }
}
