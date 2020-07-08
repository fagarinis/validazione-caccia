package it.solving.portingcaccia.dto.errors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErroreImportAppostamenti {

    String descrizioneCacciatore;
    String message;

}
