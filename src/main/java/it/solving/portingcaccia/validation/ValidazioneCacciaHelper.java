package it.solving.portingcaccia.validation;

import it.solving.portingcaccia.model.Cacciatore;
import it.solving.portingcaccia.modeldb2.AnagraficaCacciatoreDB2;
import it.solving.portingcaccia.repository.CacciatoreRepository;
import it.solving.portingcaccia.repository.CacciatoreRepositoryDB2;
import javafx.collections.transformation.SortedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class ValidazioneCacciaHelper {

    @Autowired
    CacciatoreRepository cacciatoreRepository;

    @Autowired
    CacciatoreRepositoryDB2 cacciatoreRepositoryDB2;


    public List<Integer> findAllCodiciCacciatoriNonPresentiSuMongo() {
        List<Integer> codiciCacciatoreMongo = findAllCodiciCacciatoreMongo();
        List<Integer> codiciCacciatoreDb2 = findAllCodiciCacciatoreDB2();

        List<Integer> codiciNonPresenti = codiciCacciatoreDb2.stream().filter(codice -> !codiciCacciatoreMongo.contains(codice)).collect(Collectors.toList());

        return codiciNonPresenti;

    }

    public List<Integer> findAllCodiciCacciatoreMongo() {
        return findAllCacciatoriMongo().stream().map(cacciatore -> cacciatore.getCodiceCacciatore()).collect(Collectors.toList());
    }

    public List<Cacciatore> findAllCacciatoriMongo() {
        System.out.println("-----Fetching cacciatori from MongoDB... ");
        List<Cacciatore> listaCacciatoriMongoDB = cacciatoreRepository.findAll().stream()
                .filter(cacciatore -> cacciatore.getEliminato() == false)
                .collect(Collectors.toList());
        System.out.println(listaCacciatoriMongoDB.size() + " found -------");
        return listaCacciatoriMongoDB;
    }

    public List<Integer> findAllCodiciCacciatoreDB2() {
        return findAllCacciatoriDB2().stream().map(cacciatore -> cacciatore.getCOD_CACCIATORE()).collect(Collectors.toList());
    }

    public List<AnagraficaCacciatoreDB2> findAllCacciatoriDB2() {
        System.out.print("-----Fetching cacciatori from DB2... ");
        List<AnagraficaCacciatoreDB2> listaCacciatoriDB2 = cacciatoreRepositoryDB2.findAllCacciatoriOrderByCodiceCacciatoreAsc();
        System.out.println(listaCacciatoriDB2.size() + " found -------");
        return listaCacciatoriDB2;
    }

    public void writeToTxt(List<Integer> listaDaStampare, String fileName) throws IOException {
        File file = new File(fileName+".txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        for (int i = 0; i < listaDaStampare.size(); i++) {
            bw.write(listaDaStampare.get(i).toString()+"\n");
        }
        bw.flush();
        bw.close();
    }
}
