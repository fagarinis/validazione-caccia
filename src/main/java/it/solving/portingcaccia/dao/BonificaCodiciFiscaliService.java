package it.solving.portingcaccia.dao;

import it.solving.portingcaccia.dto.CFSEntry;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@CommonsLog
public class BonificaCodiciFiscaliService {

    //    @Qualifier("db2Datasource")
    @Autowired
    DataSource db2;

    String dropAccettati = "DROP TABLE IF EXISTS SIFV.PORTING_CC_ACCETTATI";
    String dropDubbiAttributi = "DROP TABLE IF EXISTS SIFV.PORTING_CC_DUBBI_ATTRIBUTI";
    String dropDubbiNoAttributi = "DROP TABLE IF EXISTS SIFV.PORTING_CC_DUBBI_NO_ATTRIBUTI";
    String dropScartati = "DROP TABLE IF EXISTS SIFV.PORTING_CC_SCARTATI";

    String createAccettati = "CREATE TABLE SIFV.PORTING_CC_ACCETTATI (CODICE_CACCIATORE INTEGER)";
    String createDubbiAttributi = "CREATE TABLE SIFV.PORTING_CC_DUBBI_ATTRIBUTI (CODICE_CACCIATORE INTEGER)";
    String createDubbiNoAttributi = "CREATE TABLE SIFV.PORTING_CC_DUBBI_NO_ATTRIBUTI (CODICE_CACCIATORE INTEGER)";
    String createScartati = "CREATE TABLE SIFV.PORTING_CC_SCARTATI (CODICE_CACCIATORE INTEGER)";

    String insertIntoAccettati = "INSERT INTO SIFV.PORTING_CC_ACCETTATI VALUES(?)";
    String insertIntoDubbiAttributi = "INSERT INTO SIFV.PORTING_CC_DUBBI_ATTRIBUTI VALUES(?)";
    String insertIntoDubbiNoAttributi = "INSERT INTO SIFV.PORTING_CC_DUBBI_NO_ATTRIBUTI VALUES(?)";
    String insertIntoScartati = "INSERT INTO SIFV.PORTING_CC_SCARTATI VALUES(?)";

    public void startBonificaCodiciFiscali() {


        //CONTROLLO CHE IL CF SIA VUOTO PERCHE' ALTRIMENTI NON HO PARAMETRI DI CONFRONTO
        String selectAll = "SELECT * FROM SIFV.CACCIATORI_TOTALI_INFO_WITH_ATTRIBUTE_FLAG WHERE CODICE_FISCALE != '' ORDER BY CODICE_CACCIATORE";
        try (Connection connection = db2.getConnection();
             PreparedStatement preparedStatementAll = connection.prepareStatement(selectAll);
             PreparedStatement preparedStatementAccettatiDrop = connection.prepareStatement(dropAccettati);
             PreparedStatement preparedStatementDubbiAttributiDrop = connection.prepareStatement(dropDubbiAttributi);
             PreparedStatement preparedStatementDubbiNoAttributiDrop = connection.prepareStatement(dropDubbiNoAttributi);
             PreparedStatement preparedStatementScartatiDrop = connection.prepareStatement(dropScartati);

             PreparedStatement preparedStatementAccettatiCreate = connection.prepareStatement(createAccettati);
             PreparedStatement preparedStatementDubbiAttributiCreate = connection.prepareStatement(createDubbiAttributi);
             PreparedStatement preparedStatementDubbiNoAttributiCreate = connection.prepareStatement(createDubbiNoAttributi);
             PreparedStatement preparedStatementScartatiCreate = connection.prepareStatement(createScartati);

             PreparedStatement preparedStatementAccettatiInsert = connection.prepareStatement(insertIntoAccettati);
             PreparedStatement preparedStatementDubbiAttributiInsert = connection.prepareStatement(insertIntoDubbiAttributi);
             PreparedStatement preparedStatementDubbiNoAttributiInsert = connection.prepareStatement(insertIntoDubbiNoAttributi);
             PreparedStatement preparedStatementScartatiInsert = connection.prepareStatement(insertIntoScartati);
        ) {
            List<Integer> accettati = new ArrayList<>();
            List<Integer> scartati = new ArrayList<>();
            List<Integer> dubbiConAttributi = new ArrayList<>();
            List<Integer> dubbiSenzaAttributi = new ArrayList<>();

            ResultSet resultSet = preparedStatementAll.executeQuery();
            List<CFSEntry> entries = new ArrayList<>();
            int contatore = 0;

            log.info("Costruisco la lista...");

            while (resultSet.next()) {
                log.info("Cacciatore " + contatore++);
                entries.add(
                        CFSEntry.builder()
                                .attributi(resultSet.getBoolean("HAVE_ATTRIBUTES"))
                                .codiceCacciatore(resultSet.getInt("CODICE_CACCIATORE"))
                                .codiceFiscale(resultSet.getString("CODICE_FISCALE"))
                                .build()
                );
            }

            log.info("Costruisco la mappa...");

            Map<String, List<CFSEntry>> cfsMap = entries.stream().collect(Collectors.groupingBy(CFSEntry::getCodiceFiscale));
            cfsMap.forEach((cf, duplicati) -> {
                boolean tuttiSenzaAttributi = duplicati.stream().noneMatch(CFSEntry::getAttributi);

                if (duplicati.size() == 1) {
                    accettati.addAll(duplicati.stream().map(CFSEntry::getCodiceCacciatore).collect(Collectors.toList()));
                } else if (tuttiSenzaAttributi) {
                    dubbiSenzaAttributi.addAll(duplicati.stream().map(CFSEntry::getCodiceCacciatore).collect(Collectors.toList()));
                } else {
                    List<Integer> duplicatiConAttributi = new ArrayList<>();

                    duplicati.forEach(duplicato -> {
                        //se il duplicato ha attibuto lo piazzo nei duplicati con attributi
                        if (duplicato.getAttributi()) {
                            duplicatiConAttributi.add(duplicato.getCodiceCacciatore());
                            //altrimenti negli scartati
                        } else {
                            scartati.add(duplicato.getCodiceCacciatore());
                        }
                    });

                    //se i duplicati con attributi sono uno solo allora lo accetto
                    if (duplicatiConAttributi.size() == 1) {
                        accettati.addAll(duplicatiConAttributi);
                        //altrimenti lo metto tra in dubbio
                    } else {
                        dubbiConAttributi.addAll(duplicatiConAttributi);
                    }
                }

            });

            log.info("Elimino le tabelle precedenti");

            preparedStatementAccettatiDrop.execute();
            preparedStatementDubbiAttributiDrop.execute();
            preparedStatementDubbiNoAttributiDrop.execute();
            preparedStatementScartatiDrop.execute();


            log.info("Ricreo le tabelle precedenti");

            preparedStatementAccettatiCreate.execute();
            preparedStatementDubbiAttributiCreate.execute();
            preparedStatementDubbiNoAttributiCreate.execute();
            preparedStatementScartatiCreate.execute();

            log.info("Inserisco i nuovi valori...");


            accettati.forEach(accettato -> {
                try {
                    preparedStatementAccettatiInsert.setInt(1, accettato);
                    preparedStatementAccettatiInsert.executeUpdate();
                    preparedStatementAccettatiInsert.clearParameters();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            scartati.forEach(scartato -> {
                try {
                    preparedStatementScartatiInsert.setInt(1, scartato);
                    preparedStatementScartatiInsert.executeUpdate();
                    preparedStatementScartatiInsert.clearParameters();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            dubbiConAttributi.forEach(dubbioConAttributi -> {
                try {
                    preparedStatementDubbiAttributiInsert.setInt(1, dubbioConAttributi);
                    preparedStatementDubbiAttributiInsert.executeUpdate();
                    preparedStatementDubbiAttributiInsert.clearParameters();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            dubbiSenzaAttributi.forEach(dubbioSenzaAttributi -> {
                try {
                    preparedStatementDubbiNoAttributiInsert.setInt(1, dubbioSenzaAttributi);
                    preparedStatementDubbiNoAttributiInsert.executeUpdate();
                    preparedStatementDubbiNoAttributiInsert.clearParameters();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            log.info("Fatto");
        } catch (SQLException e) {
            log.error(e);
        }
    }

}
