package it.solving.portingcaccia.repository;

import it.solving.portingcaccia.model.Cacciatore;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface CacciatoreRepository extends MongoRepository<Cacciatore, String> {
    List<Cacciatore> findByEliminatoIsFalseOrderByCodiceCacciatoreAsc();
}
