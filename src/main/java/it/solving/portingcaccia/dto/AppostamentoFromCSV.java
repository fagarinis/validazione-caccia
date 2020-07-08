package it.solving.portingcaccia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppostamentoFromCSV {
    Integer codiceCacciatore;
    String provinciaAppostamento;
    String numeroTabella;
    String tipoAppostamento;
    String nComplementariAutorizzati;
    String numeroFoglioCatastale;
    String numeroParticella;
    String coordinataX;
    String coordinataY;
    String comuneAppostamento;
    String frazioneAppostamento;
    String localitaAppostamento;
    String cognomeProprietario;
    String nomeProprietario;
    String dataNascitaProprietario;
    String comuneNascitaProprietario;
    String indirizzoResidenzaProprietario;
    String comuneResidenzaProprietario;
    String capResidenzaProprietario;
    String provinciaResidenzaProprietario;

}
