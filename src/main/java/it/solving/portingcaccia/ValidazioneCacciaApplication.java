package it.solving.portingcaccia;

import it.solving.portingcaccia.dao.BonificaCodiciFiscaliService;
import it.solving.portingcaccia.dao.CacciatoreService;
import it.solving.portingcaccia.dao.ImportAppostamentiService;
import it.solving.portingcaccia.model.Cacciatore;
import it.solving.portingcaccia.modeldb2.AnagraficaCacciatoreDB2;
import it.solving.portingcaccia.repository.CacciatoreRepository;
import it.solving.portingcaccia.repository.CacciatoreRepositoryDB2;
import it.solving.portingcaccia.validation.ValidazioneCacciaHelper;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@CommonsLog
public class ValidazioneCacciaApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ValidazioneCacciaApplication.class, args);
    }


    @Autowired
    ValidazioneCacciaHelper validazioneCacciaHelper;


    @Override
    public void run(String... args) throws IOException {
        System.out.println("-----START-------");


//        Map<Integer,String> result = validazioneCacciaHelper.findAllCodiciCacciatoreCodiceFiscaleMongoDB();
//        List<Cacciatore> result = validazioneCacciaHelper.findAllCacciatoriMongo();
        List<Integer> codiciNonPresentiSuMongo = validazioneCacciaHelper.findAllCodiciCacciatoriNonPresentiSuMongo();

        validazioneCacciaHelper.writeToTxt(codiciNonPresentiSuMongo, "codiciNonPresentiSuMongo");

//        System.out.println(codiciNonPresentiSuMongo);
//        System.out.println(codiciNonPresentiSuMongo.size());

        System.out.println("------END--------");
    }

}
