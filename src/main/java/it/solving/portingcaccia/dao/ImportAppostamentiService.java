package it.solving.portingcaccia.dao;

import it.solving.portingcaccia.dto.AppostamentoFromCSV;
import it.solving.portingcaccia.dto.errors.ErroreImportAppostamenti;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@CommonsLog
public class ImportAppostamentiService {

    @Autowired
    DataSource db2;

    @Value("${appostamenti.logging.file}")
    String importAppostamentiLog;

    @Value("${appostamenti.prefixFile}")
    String appostamentiLocationFiles;


    public void startImportAppostamenti() {
        try (Connection connection = db2.getConnection();
             PreparedStatement dropTableStatement = connection.prepareStatement("DROP TABLE IF EXISTS SIFV.PORTING_APPOSTAMENTI");
             PreparedStatement createTable = connection.prepareStatement("CREATE TABLE SIFV.PORTING_APPOSTAMENTI (COD_CACCIATORE INTEGER,PROVINCIA_APPOSTAMENTO VARCHAR(255),NUMERO_TABELLA VARCHAR(255),TIPO_APPOSTAMENTO VARCHAR(255),N_COMPLEMENTARI_AUTORIZZATI VARCHAR(255),NUMERO_FOGLIO_CATASTALE VARCHAR(255),NUMERO_PARTICELLA VARCHAR(255),COORDINATA_X VARCHAR(255),COORDINATA_Y VARCHAR(255),COMUNE_APPOSTAMENTO VARCHAR(255),FRAZIONE_APPOSTAMENTO VARCHAR(255),LOCALITA_APPOSTAMENTO VARCHAR(255),COGNOME_PROPRIETARIO VARCHAR(255),NOME_PROPRIETARIO VARCHAR(255),DATA_NASCITA_PROPRIETARIO VARCHAR(255),COMUNE_NASCITA_PROPRIETARIO VARCHAR(255),INDIRIZZO_RESIDENZA_PROPRIETARIO VARCHAR(255),COMUNE_RESIDENZA_PROPRIETARIO VARCHAR(255),CAP_RESIDENZA_PROPRIETARIO VARCHAR(255),PROVINCIA_RESIDENZA_PROPRIETARIO VARCHAR(255))");
             PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO SIFV.PORTING_APPOSTAMENTI (COD_CACCIATORE, PROVINCIA_APPOSTAMENTO, NUMERO_TABELLA, TIPO_APPOSTAMENTO, N_COMPLEMENTARI_AUTORIZZATI, NUMERO_FOGLIO_CATASTALE, NUMERO_PARTICELLA, COORDINATA_X, COORDINATA_Y, COMUNE_APPOSTAMENTO, FRAZIONE_APPOSTAMENTO, LOCALITA_APPOSTAMENTO, COGNOME_PROPRIETARIO, NOME_PROPRIETARIO, DATA_NASCITA_PROPRIETARIO,COMUNE_NASCITA_PROPRIETARIO, INDIRIZZO_RESIDENZA_PROPRIETARIO, COMUNE_RESIDENZA_PROPRIETARIO, CAP_RESIDENZA_PROPRIETARIO, PROVINCIA_RESIDENZA_PROPRIETARIO) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
             PreparedStatement findCacciatoreByCodCacc = connection.prepareStatement("SELECT * FROM SIFV.ANAGRAFICA_CACCIATORE WHERE COD_CACCIATORE = ?")
        ) {

            dropTableStatement.execute();
            createTable.execute();

            List<AppostamentoFromCSV> appostamentiFromCSVS = new ArrayList<>();

            AtomicReference<String> actualSubject = new AtomicReference<>();
            List<ErroreImportAppostamenti> erroriImportAppostamentiCodiceCacciatore = new ArrayList<>();
            List<ErroreImportAppostamenti> erroriImportAppostamentiNonPresenti = new ArrayList<>();
            List<ErroreImportAppostamenti> erroriImportAppostamentiProcedurali = new ArrayList<>();

            AtomicInteger elaborati = new AtomicInteger();
            AtomicInteger inseriti = new AtomicInteger();

            try {

                CSVParser parse = CSVFormat.DEFAULT
                        .withHeader(
                                "CODICE CACCIATORE",                //0
                                "COGNOME",                          //1
                                "NOME",                             //2
                                "LUOGO DI NASCITA",                 //3
                                "DATA DI NASCITA",                  //4
                                "VIA/PIAZZA",                       //5
                                "FRAZIONE",                         //6
                                "COMUNE",                           //7
                                "CAP",                              //8
                                "PROVINCIA",                        //9
                                "RECAPITO TELEFONICO",              //10
                                "E-MAIL",                           //11
                                "PEC",                              //12
                                "CODICE FISCALE",                   //13
                                "OPZIONE CACCIA ",                  //14
                                "PROVINCIA APP",                        //15
                                "N° TABELLA",                       //16
                                "TIPO APPOSTAMENTO",                //17
                                "N° COMPLEMENTARI AUTORIZZATI",     //18
                                "N° FOGLIO CATASTALE",              //19
                                "N° PARTICELLA",                    //20
                                "X (EST)",                          //21
                                "Y (NORD)",                         //22
                                "COMUNE APP",                           //23
                                "FRAZIONE APP",                         //24
                                "LOCALITA’ APP",                        //25
                                "COGNOME PROP",                          //26
                                "NOME PROP",                             //27
                                "COMUNE DI NASCITA PROP",                //28
                                "DATA DI NASCITA PROP",                  //29
                                "VIA/PIAZZA PROP",                       //30
                                "COMUNE PROP",                           //31
                                "CAP PROP",                              //32
                                "PROVINCIA PROP")                        //33
                        .parse(Files.newBufferedReader(Paths.get(appostamentiLocationFiles, "appostamenti_TOT.csv"), Charset.forName("Windows-1252")));

                List<CSVRecord> allRecords = parse.getRecords();
                //Escludo l'header
                List<CSVRecord> records = allRecords.subList(1,allRecords.size());


                appostamentiFromCSVS.addAll(records.stream().map(record -> {
                    elaborati.getAndIncrement();
                    try {

                        String provincia = record.get(15);
                        actualSubject.set(record.get(0) + " " + record.get(1) + " " + record.get(2) + " " + record.get(13) + " " + provincia);
                        String numeroTabella = record.get(16);

                        try {
                            //se non riesce a parsare vado avanti e prendo "numeroTabella" per buono
                            int progressivoTabella = Integer.parseInt(numeroTabella);
                            numeroTabella = provincia + progressivoTabella;
                        } catch (NumberFormatException e) {
                            //ignored
                        }


                        int codiceCacciatoreInt = Integer.parseInt(record.get(0));

                        //Controllo che il codiceCacciatore sia presente
                        findCacciatoreByCodCacc.clearParameters();
                        findCacciatoreByCodCacc.setInt(1, codiceCacciatoreInt);

                        ResultSet resultSet = findCacciatoreByCodCacc.executeQuery();
                        if (!resultSet.next()) {
                            erroriImportAppostamentiNonPresenti.add(new ErroreImportAppostamenti(actualSubject.get(), "Cacciatore non presente in SIFV"));
                            log.error(String.format("Cacciatore non presente in SIFV: %s", actualSubject.get()));
                        }
                        /////

                        return AppostamentoFromCSV
                                .builder()
                                .codiceCacciatore(codiceCacciatoreInt)
                                .provinciaAppostamento(provincia)
                                .numeroTabella(numeroTabella)
                                .tipoAppostamento(record.get(17))
                                .nComplementariAutorizzati(record.get(18))
                                .numeroFoglioCatastale(record.get(19))
                                .numeroParticella(record.get(20))
                                .coordinataX(record.get(21))
                                .coordinataY(record.get(22))
                                .comuneAppostamento(record.get(23))
                                .frazioneAppostamento(record.get(24))
                                .localitaAppostamento(record.get(25))
                                .cognomeProprietario(record.get(26))
                                .nomeProprietario(record.get(27))
                                .comuneNascitaProprietario(record.get(28))
                                .dataNascitaProprietario(record.get(29))
                                .indirizzoResidenzaProprietario(record.get(30))
                                .comuneResidenzaProprietario(record.get(31))
                                .capResidenzaProprietario(record.get(32))
                                .provinciaResidenzaProprietario(record.get(33))
                                .build();
                    } catch (NumberFormatException e) {
                        erroriImportAppostamentiCodiceCacciatore.add(new ErroreImportAppostamenti(actualSubject.get(), "Codice cacciatore non numerico"));
                        log.error(String.format("Codice cacciatore non numerico: %s", actualSubject.get()));
                    } catch (Exception e) {
                        erroriImportAppostamentiProcedurali.add(new ErroreImportAppostamenti(actualSubject.get(), "Errore procedurale " + e.getMessage()));
                        log.error("Errore procedurale", e);
                    } finally {
                        actualSubject.set(null);
                    }

                    return null;
                }).collect(Collectors.toList()));
            } catch (Exception e) {
                erroriImportAppostamentiProcedurali.add(new ErroreImportAppostamenti(actualSubject.get(), "Errore procedurale " + e.getMessage()));
                log.error("Errore procedurale", e);
            }


            appostamentiFromCSVS.stream().filter(Objects::nonNull).forEach(appostamentoFromCSVS -> {
                inseriti.getAndIncrement();
                try {
                    insertStatement.clearParameters();

                    int i = 1;
                    insertStatement.setInt(i++, appostamentoFromCSVS.getCodiceCacciatore());
                    insertStatement.setString(i++, appostamentoFromCSVS.getProvinciaAppostamento());
                    insertStatement.setString(i++, appostamentoFromCSVS.getNumeroTabella());
                    insertStatement.setString(i++, appostamentoFromCSVS.getTipoAppostamento());
                    insertStatement.setString(i++, appostamentoFromCSVS.getNComplementariAutorizzati());
                    insertStatement.setString(i++, appostamentoFromCSVS.getNumeroFoglioCatastale());
                    insertStatement.setString(i++, appostamentoFromCSVS.getNumeroParticella());
                    insertStatement.setString(i++, appostamentoFromCSVS.getCoordinataX());
                    insertStatement.setString(i++, appostamentoFromCSVS.getCoordinataY());
                    insertStatement.setString(i++, appostamentoFromCSVS.getComuneAppostamento());
                    insertStatement.setString(i++, appostamentoFromCSVS.getFrazioneAppostamento());
                    insertStatement.setString(i++, appostamentoFromCSVS.getLocalitaAppostamento());
                    insertStatement.setString(i++, appostamentoFromCSVS.getCognomeProprietario());
                    insertStatement.setString(i++, appostamentoFromCSVS.getNomeProprietario());
                    insertStatement.setString(i++, appostamentoFromCSVS.getDataNascitaProprietario());
                    insertStatement.setString(i++, appostamentoFromCSVS.getComuneNascitaProprietario());
                    insertStatement.setString(i++, appostamentoFromCSVS.getIndirizzoResidenzaProprietario());
                    insertStatement.setString(i++, appostamentoFromCSVS.getComuneResidenzaProprietario());
                    insertStatement.setString(i++, appostamentoFromCSVS.getCapResidenzaProprietario());
                    insertStatement.setString(i, appostamentoFromCSVS.getProvinciaResidenzaProprietario());

                    insertStatement.executeUpdate();
                } catch (Exception e) {
                    log.error("Errore in fase di inserting", e);
                }
            });

            try (FileWriter fileWriter = new FileWriter(importAppostamentiLog, false)) {


                fileWriter.append("REPORT IMPORT APPOSTAMENTI").append("\n");
                fileWriter.append("################################################################").append("\n");
                fileWriter.append(String.format("Appostamenti elaborati da CSV: %d", elaborati.get())).append("\n");
                fileWriter.append(String.format("Appostamenti inseriti nel database: %d", inseriti.get())).append("\n");
                fileWriter.append("################################################################").append("\n");
                fileWriter.append("ERRORI CC NON NUMERICO").append("\n");

                for (int i = 0, erroriImportAppostamentiCodiceCacciatoreSize = erroriImportAppostamentiCodiceCacciatore.size(); i < erroriImportAppostamentiCodiceCacciatoreSize; i++) {
                    ErroreImportAppostamenti erroreImportAppostamenti = erroriImportAppostamentiCodiceCacciatore.get(i);
                    fileWriter.append(String.valueOf(i + 1)).append(". ").append(erroreImportAppostamenti.toString()).append("\n");
                }

                fileWriter.append("ERRORI CACCIATORE NON PRESENTE IN SIFV").append("\n");

                for (int i = 0, erroriImportAppostamentiNonPresentiSize = erroriImportAppostamentiNonPresenti.size(); i < erroriImportAppostamentiNonPresentiSize; i++) {
                    ErroreImportAppostamenti erroreImportAppostamenti = erroriImportAppostamentiNonPresenti.get(i);
                    fileWriter.append(String.valueOf(i + 1)).append(". ").append(erroreImportAppostamenti.toString()).append("\n");
                }

                fileWriter.append("ERRORI PROCEDURALI").append("\n");

                for (int i = 0, erroriImportAppostamentiProceduraliSize = erroriImportAppostamentiProcedurali.size(); i < erroriImportAppostamentiProceduraliSize; i++) {
                    ErroreImportAppostamenti erroreImportAppostamenti = erroriImportAppostamentiProcedurali.get(i);
                    fileWriter.append(String.valueOf(i + 1)).append(". ").append(erroreImportAppostamenti.toString()).append("\n");
                }
            } catch (Exception e) {
                log.error(e);
            }
        } catch (Exception e) {
            log.error(e);
        }


    }


}
