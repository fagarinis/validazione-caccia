package it.solving.portingcaccia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class CFSEntry {
    String codiceFiscale;
    Integer codiceCacciatore;
    Boolean attributi;
}
