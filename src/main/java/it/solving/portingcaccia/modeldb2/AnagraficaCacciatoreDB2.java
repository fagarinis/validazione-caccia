package it.solving.portingcaccia.modeldb2;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class AnagraficaCacciatoreDB2 {

    @Id
    Integer COD_CACCIATORE;
    String COD_FISCALE;
    String COGNOME;
    String NOME;

}

