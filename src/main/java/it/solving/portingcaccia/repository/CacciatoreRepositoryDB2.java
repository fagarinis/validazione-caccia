package it.solving.portingcaccia.repository;


import it.solving.portingcaccia.modeldb2.AnagraficaCacciatoreDB2;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CacciatoreRepositoryDB2 extends CrudRepository<AnagraficaCacciatoreDB2, Integer> {

    @Query(value = "SELECT COD_CACCIATORE, COD_FISCALE, COGNOME, NOME FROM SIFV.ANAGRAFICA_CACCIATORE x ORDER BY x.COD_CACCIATORE", nativeQuery = true)
    List<AnagraficaCacciatoreDB2> findAllCacciatoriOrderByCodiceCacciatoreAsc();
}
