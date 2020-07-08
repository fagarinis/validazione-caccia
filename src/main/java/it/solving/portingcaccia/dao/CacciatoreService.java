package it.solving.portingcaccia.dao;

import it.solving.portingcaccia.dto.QueryResultSelezione;
import it.solving.portingcaccia.model.*;
import it.solving.portingcaccia.repository.CacciatoreRepository;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
@Component
@CommonsLog
public class CacciatoreService {

    public static final int CODICE_TOSCANA = 15;
    public static final int CODICE_LAZIO = 6;

    @Autowired
    DataSource db2;

    @Autowired
    CacciatoreRepository cacciatoreRepository;

    static HashMap<Integer, List<String>> regioniProvince = new HashMap<>();
    static HashMap<Integer, String> regioniNomi = new HashMap<>();
    static HashMap<Integer, String> modalitaAbilitazioneDaino = new HashMap();
    static HashMap<Integer, String> modalitaAbilitazioneCapriolo = new HashMap();
    static HashMap<Integer, String> modalitaAbilitazioneCinghiale = new HashMap();
    static HashMap<Integer, String> modalitaAbilitazioneCervo = new HashMap();
    static HashMap<Integer, String> modalitaAbilitazioneMuflone = new HashMap();
    static HashMap<Integer, String> modalitaIscrizione = new HashMap<>();
    static HashMap<Integer, String> specie = new HashMap<>();
    static HashMap<Integer, String> tipologiaAbilitazioneCaccia = new HashMap<>();
    static HashMap<Integer, String> tipologiaTeleprenotazioni = new HashMap<>();
    static HashMap<Integer, String> tipologiaCaccia = new HashMap<>();

    static {
        regioniProvince.put(0, Arrays.asList("CH", "AQ", "PE", "TE"));
        regioniProvince.put(1, Arrays.asList("MT", "PZ"));
        regioniProvince.put(2, Arrays.asList("CZ", "CS", "KR", "RC", "VV"));
        regioniProvince.put(3, Arrays.asList("AV", "BN", "CE", "NA", "SA"));
        regioniProvince.put(4, Arrays.asList("BO", "FE", "FC", "MO", "PR", "PC", "RA", "RE", "RN"));
        regioniProvince.put(5, Arrays.asList("GO", "PN", "TS", "UD"));
        regioniProvince.put(6, Arrays.asList("FR", "LT", "RI", "RM", "VT"));
        regioniProvince.put(7, Arrays.asList("GE", "IM", "SP", "SV"));
        regioniProvince.put(8, Arrays.asList("BG", "BS", "CO", "CR", "LC", "LO", "MN", "MI", "MB", "PV", "SO", "VA"));
        regioniProvince.put(9, Arrays.asList("AN", "AP", "FM", "MC", "PU"));
        regioniProvince.put(10, Arrays.asList("CB", "IS"));
        regioniProvince.put(11, Arrays.asList("AL", "AT", "BI", "CN", "NO", "TO", "VB", "VC"));
        regioniProvince.put(12, Arrays.asList("BA", "BT", "BR", "FG", "LE", "TA"));
        regioniProvince.put(13, Arrays.asList("CA", "CI", "VS", "NU", "OG", "OT", "OR", "SS"));
        regioniProvince.put(14, Arrays.asList("AG", "CL", "CT", "EN", "ME", "PA", "RG", "SR", "TP"));
        regioniProvince.put(15, Arrays.asList("AR", "FI", "GR", "LI", "LU", "MS", "PI", "PT", "PO", "SI"));
        regioniProvince.put(16, Arrays.asList("BZ", "TN"));
        regioniProvince.put(17, Arrays.asList("PG", "TR"));
        regioniProvince.put(18, Arrays.asList("AO"));
        regioniProvince.put(19, Arrays.asList("BL", "PD", "RO", "TV", "VE", "VR", "VI"));

        regioniNomi.put(0, "Abruzzo");
        regioniNomi.put(1, "Basilicata");
        regioniNomi.put(2, "Calabria");
        regioniNomi.put(3, "Campania");
        regioniNomi.put(4, "Emilia-Romagna");
        regioniNomi.put(5, "Friuli-Venezia Giulia");
        regioniNomi.put(6, "Lazio");
        regioniNomi.put(7, "Liguria");
        regioniNomi.put(8, "Lombardia");
        regioniNomi.put(9, "Marche");
        regioniNomi.put(10, "Molise");
        regioniNomi.put(11, "Piemonte");
        regioniNomi.put(12, "Puglia");
        regioniNomi.put(13, "Sardegna");
        regioniNomi.put(14, "Sicilia");
        regioniNomi.put(15, "Toscana");
        regioniNomi.put(16, "Trentino-Alto Adige");
        regioniNomi.put(17, "Umbria");
        regioniNomi.put(18, "Valle d'Aosta");
        regioniNomi.put(19, "Veneto");

        modalitaAbilitazioneCapriolo.put(1, "equipollenza");
        modalitaAbilitazioneCapriolo.put(2, "esame");

        modalitaAbilitazioneDaino.put(1, "equipollenza");
        modalitaAbilitazioneDaino.put(2, "esame");

        modalitaAbilitazioneMuflone.put(1, "equipollenza");
        modalitaAbilitazioneMuflone.put(2, "esame");

        modalitaAbilitazioneCervo.put(1, "equipollenza per ANV");
        modalitaAbilitazioneCervo.put(2, "esame acater");
        modalitaAbilitazioneCervo.put(3, "esame gestione ANV");

        modalitaAbilitazioneCinghiale.put(1, "corso ed esame");
        modalitaAbilitazioneCinghiale.put(2, "equipollenza");
        modalitaAbilitazioneCinghiale.put(3, "esame");

        modalitaIscrizione.put(1, "conduttore cane");
        modalitaIscrizione.put(2, "corso abilitante");
        modalitaIscrizione.put(3, "equipollenza");
        modalitaIscrizione.put(4, "esame di caccia");
        modalitaIscrizione.put(5, "iscritto al 31/12/1995");
        modalitaIscrizione.put(6, "trasferito altra provincia");

        specie.put(1, "capriolo");
        specie.put(2, "cervo");
        specie.put(3, "cinghiale");
        specie.put(4, "daino");
        specie.put(5, "muflone");

        tipologiaAbilitazioneCaccia.put(1, "A");
        tipologiaAbilitazioneCaccia.put(2, "B");
        tipologiaAbilitazioneCaccia.put(3, "C");
        tipologiaAbilitazioneCaccia.put(4, "D");

        tipologiaTeleprenotazioni.put(1, "mobilita");
        tipologiaTeleprenotazioni.put(2, "cinghiale");
        tipologiaTeleprenotazioni.put(3, "stanziale");

        tipologiaCaccia.put(1, "Minuta Selvaggina");
        tipologiaCaccia.put(2, "Colombacci");
        tipologiaCaccia.put(3, "Palmipedi e trampolieri");
        tipologiaCaccia.put(4, "Palmipedi e trampolieri in lago artificiale");
    }

    private String CONDIZIONE_AFTER_ERROR = "WHERE CODICE_CACCIATORE >= 63";
    private String SQL_CACCIATORI = "SELECT * FROM SIFV.CACCIATORI_DA_IMPORTARE " + CONDIZIONE_AFTER_ERROR + " ORDER BY CODICE_CACCIATORE";
    private String SQL_ALBI_CINGHIALE = "SELECT\n" +
            "SIFV.ANAGRAFICA_CACCIATORE.COD_CACCIATORE AS CODICE_CACCIATORE,\n" +
            "SIFV.ALBI.NUM_ISCRIZIONE AS NUMERO_ISCRIZIONE_ALBO_PROVENIENZA,\n" +
            "SIFV.ALBI.DATA_ISCRIZIONE AS DATA_ISCRIZIONE,\n" +
            "SIFV.ALBI.SEZ_ALBO_PROV AS SEZIONE_ALBO_PROVINCIALE,\n" +
            "SIFV.VOCABOLARI.VOCE AS MODALITA_ISCRIZIONE\n" +
            "FROM\n" +
            "SIFV.ANAGRAFICA_CACCIATORE\n" +
            "INNER JOIN SIFV.ALBI ON SIFV.ANAGRAFICA_CACCIATORE.COD_CACCIATORE = SIFV.ALBI.COD_CACCIATORE\n" +
            "INNER JOIN SIFV.ALBI_CINGHIALE_BATTUTA ON SIFV.ALBI.PK_ALBO = SIFV.ALBI_CINGHIALE_BATTUTA.PK_ALBO  \n" +
            "INNER JOIN SIFV.VOCABOLARI ON SIFV.ALBI_CINGHIALE_BATTUTA.FK_MODALITA_ISCR = SIFV.VOCABOLARI.PK_VOCABOLARIO  \n" +
            "WHERE SIFV.ANAGRAFICA_CACCIATORE.COD_CACCIATORE = ?";

    private String SQL_ALBI_SELEZIONE = "SELECT\n" +
            "\tSIFV.ANAGRAFICA_CACCIATORE.COD_CACCIATORE AS CODICE_CACCIATORE,\n" +
            "\tSIFV.ALBI.NUM_ISCRIZIONE AS NUMERO_ISCRIZIONE_ALBO_PROVENIENZA,\n" +
            "\tSIFV.ALBI.DATA_ISCRIZIONE AS DATA_ISCRIZIONE,\n" +
            "\tSIFV.ALBI.SEZ_ALBO_PROV AS SEZIONE_ALBO_PROVINCIALE,\n" +
            "\tSIFV.ALBI_SELEZIONE_SPECIE.DATA_ABILITAZIONE AS DATA_ABILITAZIONE,\n" +
            "\tSIFV.ALBI_SELEZIONE_SPECIE.ABILITAZIONE AS ABILITAZIONE,\n" +
            "\tSIFV.ALBI_SELEZIONE_SPECIE.VOTO_ESAME AS VOTO_ESAME,\n" +
            "\tMODALITA_ABILITAZIONE.VOCE AS MODALITA_ABILITAZIONE,\n" +
            "\tSPECIE.VOCE AS SPECIE\n" +
            "FROM\n" +
            "\tSIFV.ANAGRAFICA_CACCIATORE\n" +
            "INNER JOIN SIFV.ALBI ON\n" +
            "\tSIFV.ANAGRAFICA_CACCIATORE.COD_CACCIATORE = SIFV.ALBI.COD_CACCIATORE\n" +
            "LEFT JOIN SIFV.ALBI_SELEZIONE_SPECIE ON\n" +
            "\tSIFV.ALBI.PK_ALBO = SIFV.ALBI_SELEZIONE_SPECIE.FK_ALBO\n" +
            "LEFT JOIN SIFV.VOCABOLARI MODALITA_ABILITAZIONE ON\n" +
            "\tSIFV.ALBI_SELEZIONE_SPECIE.FK_MOD_ABILITAZIONE = MODALITA_ABILITAZIONE.PK_VOCABOLARIO\n" +
            "LEFT JOIN SIFV.VOCABOLARI SPECIE ON\n" +
            "\tSIFV.ALBI_SELEZIONE_SPECIE.FK_SPECIE = SPECIE.PK_VOCABOLARIO\n" +
            "WHERE\n" +
            "\tSIFV.ALBI.TIPO_ALBO = 2\n" +
            "\tAND SIFV.ANAGRAFICA_CACCIATORE.COD_CACCIATORE = ?";

    private String SQL_VENATORIE = "SELECT\n" +
            "\tVC.COD_CACCIATORE AS CODICE_CACCIATORE,\n" +
            "\tVC.DATA_I_ISCR AS DATA_ISCRIZIONE,\n" +
            "\tVC.DATA_ULTIMA_CE,\n" +
            "\tVC.ATC_R AS ATC_RESIDENZA_CODICE,\n" +
            "\tVC.ATC_I AS PRIMO_ATC_CODICE,\n" +
            "\tVC.ATC_I_DATA AS PRIMO_ATC_DATA,\n" +
            "\tVC.ATC_I_PRE AS PRIMO_ATC_PRE_CODICE,\n" +
            "\tVC.ATC_I_PRE_DATA AS PRIMO_ATC_PRE_DATA,\n" +
            "\tVC.ATC_ULTERIORE AS ULTERIORE_ATC_CODICE,\n" +
            "\tVC.ATC_ULTERIORE_PRE AS ULTERIORE_ATC_PRE_CODICE,\n" +
            "\tVC.ATC_ULTERIORE_UNGU AS ULTERIORE_ATC_UNGULATI_CODICE,\n" +
            "\tVC.ATC_ULTERIORE_PRE_UNGU AS ULTERIORE_ATC_UNGULATI_PRE_CODICE,\n" +
            "\tCONT_REG9 AS GIORNATE_TOSCANA,\n" +
            "\tCONT_REG10 AS GIORNATE_UMBRIA,\n" +
            "\tCONT_REG12 AS GIORNATE_LAZIO,\n" +
            "\tOPZ_ART_28 AS TIPOLOGIA_CACCIA\n" +
            "FROM\n" +
            "\tSIFV.VENATORIA_CACCIATORE VC\n" +
            "WHERE\n" +
            "\tCOD_CACCIATORE = ?";

    private String SQL_ATC_ANA = "SELECT\n" +
            "\tNOME\n" +
            "FROM\n" +
            "\tSIFV.ANAGRAFICA_ATC\n" +
            "WHERE\n" +
            "\tCOD_ATC = ?";

    private String SQL_UPDATE_PRESCRIPT_VOCABOLARI = "UPDATE SIFV.VOCABOLARI SET VOCE = 'equipollenza per ANV' WHERE PK_VOCABOLARIO = 21";
    private String SQL_UPDATE_PRESCRIPT_DATA_NASCITA = "UPDATE SIFV.ANAGRAFICA_CACCIATORE SET DATA_NASCITA = '12111958' WHERE COD_CACCIATORE = 9059959";
    private String SQL_APPPOSTAMENTI_BY_COD_CAC = "SELECT * FROM SIFV.PORTING_APPOSTAMENTI WHERE COD_CACCIATORE = ?";
    private String SQL_TELEPRENOTAZIONI_ANA = "SELECT * FROM SIFV.PRENOTA WHERE COD_CACCIATORE = ?";
    private String SQL_CONTATORI = "SELECT * FROM SIFV.CONTATORI_CACCIATORE WHERE COD_CACCIATORE = ?";
    private String SQL_ATC_R = "SELECT COD_ATC FROM SIFV.ATC_COMUNE WHERE COD_ISTAT_COMUNE = ?";
    private String cfRegex = "^[A-Za-z]{6}\\d{2}[A-Za-z]\\d{2}[A-Za-z]\\d{3}[A-Za-z]$";

    private String SQL_PROVINCIA_BY_CODICE = "SELECT * FROM SIFV.ANAGRAFICA_PROVINCIA WHERE SIGLA_PROVINCIA = ?";

    private String SQL_GET_INFO_CACCIATORI_RIPESCAGGIO =
            "SELECT " +
                    "* " +
                    "FROM " +
                    "( " +
                    "SELECT " +
                    "AC.COD_CACCIATORE AS CODICE_CACCIATORE, " +
                    "AC.COGNOME, " +
                    "AC.NOME, " +
                    "AC.COD_FISCALE AS CODICE_FISCALE, " +
                    "AC.COD_ISTAT_COMUNE_NAS AS CODICE_ISTAT_COMUNE_NASCITA, " +
                    "CM3.DESC_COMUNE AS COMUNE_NASCITA, " +
                    "AC.DATA_NASCITA, " +
                    "AC.SESSO, " +
                    "AC.COD_CIRC_RES AS CODICE_CIRCOSCRIZIONE, " +
                    "AC.NUM_TELEF AS NUMERO_TELEFONO, " +
                    "AC.NUM_FISSO AS NUMERO_FISSO, " +
                    "AC.NUM_MOBILE AS NUMERO_MOBILE, " +
                    "AC.INDIRIZ_RES AS INDIRIZZO_RESIDENZA, " +
                    "AC.CAP_COMUNE_RES AS CAP_COMUNE_RESIDENZA, " +
                    "AC.COD_ISTAT_COMUNE_RES AS CODICE_ISTAT_COMUNE_RESIDENZA, " +
                    "AC.EMAIL, " +
                    "AC.PEC, " +
                    "CM1.DESC_COMUNE AS COMUNE_RESIDENZA, " +
                    "PR1.SIGLA_PROVINCIA AS PROVINCIA_RESIDENZA, " +
                    "AC.INDIRIZZO_DOM AS INDIRIZZO_DOMICILIO, " +
                    "AC.CAP_DOM AS CAP_DOMICILIO, " +
                    "AC.ISTAT_COMUNE_DOM AS CODICE_ISTAT_COMUNE_DOMICILIO, " +
                    "CM2.DESC_COMUNE AS COMUNE_DOMICILIO, " +
                    "PR2.SIGLA_PROVINCIA AS PROVINCIA_DOMICILIO " +
                    "FROM " +
                    "SIFV.ANAGRAFICA_CACCIATORE AC " +
                    "LEFT JOIN SIFV.ANAGRAFICA_COMUNE CM1 ON " +
                    "(CM1.COD_ISTAT_COMUNE = AC.COD_ISTAT_COMUNE_RES ) " +
                    "LEFT JOIN SIFV.ANAGRAFICA_PROVINCIA PR1 ON " +
                    "(CM1.COD_ISTAT_PROVINCIA = PR1.COD_ISTAT_PROVINCIA ) " +
                    "LEFT JOIN SIFV.ANAGRAFICA_COMUNE CM2 ON " +
                    "(CM2.COD_ISTAT_COMUNE = AC.ISTAT_COMUNE_DOM ) " +
                    "LEFT JOIN SIFV.ANAGRAFICA_PROVINCIA PR2 ON " +
                    "(CM2.COD_ISTAT_PROVINCIA = PR2.COD_ISTAT_PROVINCIA ) " +
                    "LEFT JOIN SIFV.ANAGRAFICA_COMUNE CM3 ON " +
                    "(CM3.COD_ISTAT_COMUNE = AC.COD_ISTAT_COMUNE_NAS )) " +
                    "WHERE ";

    private DatoTipologicaString getProvinciaBySigla(String sigla, Connection connection) {
        DatoTipologicaString datoTipologicaString = new DatoTipologicaString();
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(SQL_PROVINCIA_BY_CODICE)
        ) {
            preparedStatement.setString(1, sigla);
            ResultSet resultSet = preparedStatement.executeQuery();


            if (resultSet.next()) {
                datoTipologicaString.setCodice(resultSet.getString("SIGLA_PROVINCIA"));
                datoTipologicaString.setDescrizione(resultSet.getString("DESC_PROVINCIA"));
            }

            return datoTipologicaString;
        } catch (SQLException e) {
            log.error("Errore", e);
        }
        return datoTipologicaString;
    }


    public List<Cacciatore> startImportCacciatori(boolean ripescaggio, Integer[] ccToRipescare) {
        List<Cacciatore> cacciatores = new ArrayList<>();
        try (Connection connection = db2.getConnection();
             PreparedStatement prescript1 = connection.prepareStatement(SQL_UPDATE_PRESCRIPT_VOCABOLARI);
             PreparedStatement prescript2 = connection.prepareStatement(SQL_UPDATE_PRESCRIPT_DATA_NASCITA);
             Statement statement1 = connection.createStatement();
        ) {
            prescript1.execute();
            prescript2.execute();
            ResultSet resultSet1 = null;
            if (!ripescaggio) {
                resultSet1 = statement1.executeQuery(SQL_CACCIATORI);
            } else {
                String completeQuery = SQL_GET_INFO_CACCIATORI_RIPESCAGGIO + String.join(" or ", Arrays.stream(ccToRipescare).map(cc -> " CODICE_CACCIATORE = " + cc + " ").collect(Collectors.toList()));
                resultSet1 = statement1.executeQuery(completeQuery);
            }
            int contatoreCacciatore = 0;
            int contatoreAlboRegionale = 0;
            List<String> errori = new ArrayList<>();
            while (resultSet1.next()) {
                try {
                    Cacciatore cacciatore = new Cacciatore();
                    log.info("cacciatore " + resultSet1.getInt("CODICE_CACCIATORE") + " " + contatoreCacciatore);

                    cacciatore.setCodiceCacciatore(resultSet1.getInt("CODICE_CACCIATORE"));
                    cacciatore.setCognome(resultSet1.getString("COGNOME"));
                    cacciatore.setNome(resultSet1.getString("NOME"));
                    cacciatore.setCodiceFiscale(resultSet1.getString("CODICE_FISCALE").toUpperCase());
                    cacciatore.setComuneNascita(resultSet1.getString("COMUNE_NASCITA"));
                    Optional.ofNullable(resultSet1.getString("SESSO")).ifPresent(sesso -> cacciatore.setSesso(sesso.charAt(0)));
                    Optional.ofNullable(resultSet1.getString("DATA_NASCITA")).ifPresent(data -> cacciatore.setDataNascita(LocalDate.parse(data, DateTimeFormatter.ofPattern("ddMMyyyy"))));
                    cacciatore.setCodiceCircoscrizione(resultSet1.getString("CODICE_CIRCOSCRIZIONE"));

                    String numeroTelefono = clean(resultSet1.getString("NUMERO_TELEFONO"));
                    String numeroFisso = clean(resultSet1.getString("NUMERO_FISSO"));
                    String numeroMobile = clean(resultSet1.getString("NUMERO_MOBILE"));


                    cacciatore.setTelefono(clean(StringUtils.join(Stream.of(numeroTelefono, numeroFisso, numeroMobile).filter(s -> s != null).collect(Collectors.toList()), " - ")));

                    cacciatore.setIndirizzoResidenza(resultSet1.getString("INDIRIZZO_RESIDENZA"));
                    cacciatore.setCapResidenza(resultSet1.getString("CAP_COMUNE_RESIDENZA"));

                    cacciatore.setCodiceComuneResidenza(resultSet1.getString("CODICE_ISTAT_COMUNE_RESIDENZA"));
                    cacciatore.setComuneCreazione(resultSet1.getString("CODICE_ISTAT_COMUNE_RESIDENZA"));

                    cacciatore.setComuneResidenza(resultSet1.getString("COMUNE_RESIDENZA"));
                    cacciatore.setProvinciaResidenza(resultSet1.getString("PROVINCIA_RESIDENZA"));

                    cacciatore.setIndirizzoDomicilio(clean(resultSet1.getString("INDIRIZZO_DOMICILIO")));
                    cacciatore.setCapDomicilio(clean(resultSet1.getString("CAP_DOMICILIO")));
                    cacciatore.setCodiceComuneDomicilio(clean(resultSet1.getString("CODICE_ISTAT_COMUNE_DOMICILIO")));
                    cacciatore.setComuneDomicilio(clean(resultSet1.getString("COMUNE_DOMICILIO")));
                    cacciatore.setProvinciaDomicilio(clean(resultSet1.getString("PROVINCIA_DOMICILIO")));
                    cacciatore.setMail(resultSet1.getString("EMAIL"));
                    cacciatore.setPec(clean(resultSet1.getString("PEC")));
                    cacciatore.setFromExport(true);
                    cacciatore.setAttivo(true);
                    cacciatore.setEliminato(false);
                    cacciatore.setDataInserimento(LocalDate.now());
                    cacciatore.setRegistratoApaci(false);

                    String provinciaDomicilioTemp = cacciatore.getProvinciaDomicilio();
                    String provinciaResidenzaTemp = cacciatore.getProvinciaResidenza();

                    regioniProvince.forEach((integer, strings) -> {
                        if (strings.contains(provinciaDomicilioTemp)) {
                            cacciatore.setCodiceRegioneDomicilio(integer);
                            cacciatore.setRegioneDomicilio(regioniNomi.get(integer));
                        }

                        if (strings.contains(provinciaResidenzaTemp)) {
                            cacciatore.setCodiceRegioneResidenza(integer);
                            cacciatore.setRegioneResidenza(regioniNomi.get(integer));
                        }
                    });

                    Pattern cfPattern = Pattern.compile(this.cfRegex);
                    Matcher matcher = cfPattern.matcher(cacciatore.getCodiceFiscale());

                    if (StringUtils.isBlank(cacciatore.getCodiceFiscale())) {
                        cacciatore.setValidita(1);
                    } else if (!matcher.find()) {
                        cacciatore.setValidita(2);
                    } else {
                        cacciatore.setValidita(0);
                    }


                    try (PreparedStatement statement2 = connection.prepareStatement(SQL_ALBI_CINGHIALE)) {

                        statement2.setInt(1, cacciatore.getCodiceCacciatore());

                        ResultSet resultSet2 = statement2.executeQuery();

                        while (resultSet2.next()) {
                            AlbiCinghiale albiCinghiale = new AlbiCinghiale();
                            albiCinghiale.setNumIscrizioneAlboRegionale(contatoreAlboRegionale++);
                            albiCinghiale.setNumIscrizioneAlboProvenienza(resultSet2.getString("NUMERO_ISCRIZIONE_ALBO_PROVENIENZA"));
                            Optional.ofNullable(resultSet2.getString("DATA_ISCRIZIONE")).ifPresent(dataIscrizione -> albiCinghiale.setDataIscrizione(LocalDate.parse(dataIscrizione, DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                            albiCinghiale.setSezioneAlboProvinciale(resultSet2.getString("SEZIONE_ALBO_PROVINCIALE"));
                            albiCinghiale.setModalitaIscrizione(resultSet2.getString("MODALITA_ISCRIZIONE").toUpperCase());
                            albiCinghiale.setFromExport(true);

                            modalitaIscrizione.forEach((integer, s) -> {
                                if (StringUtils.equalsIgnoreCase(albiCinghiale.getModalitaIscrizione(), s)) {
                                    albiCinghiale.setCodiceModalitaIscrizione(integer);
                                }
                            });

                            cacciatore.setAlbiCinghiale(albiCinghiale);
                        }
                    }

                    ////////////////////////////////////////////////////////////////////////////////SELEZIONE - SVITATI IL CERVELLO E VAI IN FERIE!

                    final String CINGHIALE = "cinghiale";

                    try (PreparedStatement statement3 = connection.prepareStatement(SQL_ALBI_SELEZIONE)) {

                        statement3.setInt(1, cacciatore.getCodiceCacciatore());

                        ResultSet resultSet3 = statement3.executeQuery();
                        List<QueryResultSelezione> queryResultSelezioneList = new ArrayList<>();

                        while (resultSet3.next()) {
                            QueryResultSelezione queryResultSelezione = new QueryResultSelezione();
                            queryResultSelezione.setNumIscrizioneAlboProvenienza(resultSet3.getString("NUMERO_ISCRIZIONE_ALBO_PROVENIENZA"));
                            queryResultSelezione.setDataIscrizione(resultSet3.getString("DATA_ISCRIZIONE"));
                            queryResultSelezione.setSezioneAlboProvinciale(resultSet3.getString("SEZIONE_ALBO_PROVINCIALE"));
                            queryResultSelezione.setDataAbilitazione(resultSet3.getString("DATA_ABILITAZIONE"));
                            queryResultSelezione.setAbilitazione(resultSet3.getBoolean("ABILITAZIONE"));
                            queryResultSelezione.setVotoEsame(resultSet3.getDouble("VOTO_ESAME"));
                            queryResultSelezione.setModalitaAbilitazione(resultSet3.getString("MODALITA_ABILITAZIONE"));
                            queryResultSelezione.setSpecie(resultSet3.getString("SPECIE"));
                            queryResultSelezioneList.add(queryResultSelezione);
                        }


                        queryResultSelezioneList = queryResultSelezioneList.stream().map(queryResultSelezione -> {
                            if (queryResultSelezione.getNumIscrizioneAlboProvenienza() == null) {
                                queryResultSelezione.setNumIscrizioneAlboProvenienza("");
                            }

                            return queryResultSelezione;
                        }).collect(Collectors.toList());

                        Map<String, List<QueryResultSelezione>> albiSelezioneMap = queryResultSelezioneList.stream().collect(Collectors.groupingBy(QueryResultSelezione::getNumIscrizioneAlboProvenienza));

                        AtomicReference<List<QueryResultSelezione>> selezionePrincipale = new AtomicReference<>(null);
                        AtomicReference<List<QueryResultSelezione>> selezioneCinghiale = new AtomicReference<>(null);

                        albiSelezioneMap.forEach((numeroIscrizione, queryResultSeleziones) -> {
                            if (queryResultSeleziones.size() > 1 && selezionePrincipale.get() == null) {
                                selezionePrincipale.set(queryResultSeleziones);
                            } else if (queryResultSeleziones.size() > 1 && selezionePrincipale.get() != null) {
                                selezioneCinghiale.set(selezionePrincipale.get());
                                selezionePrincipale.set(queryResultSeleziones);
                            } else if (queryResultSeleziones.size() == 1 && selezionePrincipale.get() != null) {
                                if (queryResultSeleziones.get(0).getSpecie() == null || queryResultSeleziones.get(0).getSpecie().equalsIgnoreCase(CINGHIALE) || queryResultSeleziones.get(0).getSpecie().isEmpty()) {
                                    selezioneCinghiale.set(queryResultSeleziones);
                                } else {
                                    selezioneCinghiale.set(selezionePrincipale.get());
                                    selezionePrincipale.set(queryResultSeleziones);
                                }
                            } else if (queryResultSeleziones.size() == 1 && selezionePrincipale.get() == null) {
                                selezionePrincipale.set(queryResultSeleziones);
                            }
                        });


                        int contatoreAlbiSelezione = 0;
                        AlbiSelezione albiSelezione = new AlbiSelezione();

                        if (selezionePrincipale.get() != null)
                            for (QueryResultSelezione querySelezione : selezionePrincipale.get()) {
                                Specializzazione specializzazione = new Specializzazione();
                                if (contatoreAlbiSelezione == 0) {
                                    albiSelezione.setNumIscrizioneAlboRegionale(contatoreAlboRegionale++);
                                    albiSelezione.setNumIscrizioneAlboProvenienza(querySelezione.getNumIscrizioneAlboProvenienza());
                                    Optional.ofNullable(querySelezione.getDataIscrizione()).ifPresent(dataIscrizione -> albiSelezione.setDataIscrizione(LocalDate.parse(dataIscrizione, DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                                    albiSelezione.setSezioneAlboProvinciale(querySelezione.getSezioneAlboProvinciale());
                                }


                                Optional.ofNullable(querySelezione.getDataAbilitazione()).ifPresent(dataAbilitazione -> specializzazione.setDataAbilitazione(LocalDate.parse(dataAbilitazione, DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                                Optional.ofNullable(querySelezione.getAbilitazione()).ifPresent(abilitazione -> specializzazione.setAbilitazione(abilitazione));
                                Optional.ofNullable(querySelezione.getVotoEsame()).ifPresent(voto -> specializzazione.setVotoEsame(voto));
                                Optional.ofNullable(querySelezione.getSpecie()).ifPresent(specie -> specializzazione.setSpecie(specie.toUpperCase()));
                                Optional.ofNullable(querySelezione.getModalitaAbilitazione()).ifPresent(modalitaAbilitazione -> specializzazione.setModalitaAbilitazione(modalitaAbilitazione.toUpperCase()));


                                specializzazione.setFromExport(true);

                                if (specializzazione.getSpecie() != null) {
                                    specie.forEach((integer, s) -> {
                                        if (StringUtils.equalsIgnoreCase(specializzazione.getSpecie(), s)) {
                                            specializzazione.setCodiceSpecie(integer);
                                        }
                                    });

                                    setModalitaAbilitazione(specializzazione);
                                }

                                albiSelezione.getSpecie().add(specializzazione);


                                contatoreAlbiSelezione++;
                            }

                        if (selezioneCinghiale.get() != null && !selezioneCinghiale.get().isEmpty()) {
                            QueryResultSelezione querySelezioneCinghiale = selezioneCinghiale.get().get(0);
                            Specializzazione specializzazione = new Specializzazione();

                            albiSelezione.setNumIscrizioneAlboProvenienzaCinghiale(querySelezioneCinghiale.getNumIscrizioneAlboProvenienza());


                            //////////////
                            if (albiSelezione.getSpecie().stream().filter(specializzazione1 -> {
                                if (specializzazione1.getSpecie() != null) {
                                    return specializzazione1.getSpecie().equalsIgnoreCase(CINGHIALE);
                                } else {
                                    return false;
                                }
                            }).count() == 0) {
                                Optional.ofNullable(querySelezioneCinghiale.getDataAbilitazione()).ifPresent(dataAbilitazione -> specializzazione.setDataAbilitazione(LocalDate.parse(dataAbilitazione, DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                                Optional.ofNullable(querySelezioneCinghiale.getAbilitazione()).ifPresent(abilitazione -> specializzazione.setAbilitazione(abilitazione));
                                Optional.ofNullable(querySelezioneCinghiale.getVotoEsame()).ifPresent(voto -> specializzazione.setVotoEsame(voto));
                                specializzazione.setSpecie(CINGHIALE.toUpperCase());
                                Optional.ofNullable(querySelezioneCinghiale.getModalitaAbilitazione()).ifPresent(modalitaAbilitazione -> specializzazione.setModalitaAbilitazione(modalitaAbilitazione.toUpperCase()));

                                specializzazione.setFromExport(true);

                                if (specializzazione.getSpecie() != null) {
                                    specie.forEach((integer, s) -> {
                                        if (StringUtils.equalsIgnoreCase(specializzazione.getSpecie(), s)) {
                                            specializzazione.setCodiceSpecie(integer);
                                        }
                                    });

                                    setModalitaAbilitazione(specializzazione);
                                }
                                albiSelezione.getSpecie().add(specializzazione);
                            }
                            /////////////


                        }


                        if (!albiSelezione.getSpecie().isEmpty())
                            cacciatore.setAlbiSelezione(albiSelezione);
                    }

                    // Venatorie ##############################################

                    try (PreparedStatement statement4 = connection.prepareStatement(SQL_VENATORIE)) {


                        statement4.setInt(1, cacciatore.getCodiceCacciatore());

                        ResultSet resultSet4 = statement4.executeQuery();

                        while (resultSet4.next()) {
                            //todo l'anno dipende da quando si fa l'import
                            Venatoria venatoriaAttuale = new Venatoria(String.valueOf(getAnnoAttualeStagioneVenatoria()));
                            Venatoria venatoriaPrec = new Venatoria(String.valueOf(getAnnoAttualeStagioneVenatoria() - 1));


                            try (PreparedStatement preparedStatement5 = connection.prepareStatement(SQL_ATC_ANA);
                                 PreparedStatement preparedStatementATCR = connection.prepareStatement(SQL_ATC_R)) {


                                AtomicReference<ResultSet> resultSet5 = new AtomicReference<>();

                                Optional.ofNullable(resultSet4.getString("DATA_ISCRIZIONE")).ifPresent(data -> cacciatore.setDataPrimaIscrizioneATC(LocalDate.parse(data, DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

                                Optional.ofNullable(resultSet4.getString("DATA_ULTIMA_CE")).ifPresent(data -> cacciatore.setDataUltimaCeATC(LocalDate.parse(data, DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

                                //todo sto qui
                                String codiceComuneResidenza = cacciatore.getCodiceComuneResidenza();
                                preparedStatementATCR.setString(1, codiceComuneResidenza);
                                ResultSet resultSetATCR = preparedStatementATCR.executeQuery();

                                if (resultSetATCR.next()) {
                                    int codiceResidenzaATC = resultSetATCR.getInt("COD_ATC");

                                    if (cacciatore.getCodiceCacciatore() > 9000000)
                                        venatoriaAttuale.setResidenzaATC(new DatoTipologicaInteger(0, "Residenza esterna"));
                                    else if (codiceResidenzaATC != 0) {
                                        preparedStatement5.clearParameters();
                                        preparedStatement5.setInt(1, codiceResidenzaATC);
                                        resultSet5.set(preparedStatement5.executeQuery());
                                        String nome = "";

                                        while (resultSet5.get().next()) {
                                            nome = resultSet5.get().getString("NOME");
                                        }


                                        venatoriaAttuale.setResidenzaATC(new DatoTipologicaInteger(codiceResidenzaATC, nome));
                                    }
                                }


                                int codicePrimoATC = resultSet4.getInt("PRIMO_ATC_CODICE");

                                if (codicePrimoATC != 0) {
                                    preparedStatement5.clearParameters();
                                    preparedStatement5.setInt(1, codicePrimoATC);
                                    resultSet5.set(preparedStatement5.executeQuery());
                                    String nome = "";
                                    while (resultSet5.get().next()) {
                                        nome = resultSet5.get().getString("NOME");
                                    }
                                    venatoriaAttuale.setPrimoATC(new DatoTipologicaInteger(codicePrimoATC, nome));
                                    Optional.ofNullable(resultSet4.getString("PRIMO_ATC_DATA")).ifPresent(data -> venatoriaAttuale.setDataPrimoATC(LocalDate.parse(data, DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                                }

                                String codiciUlterioreATC = resultSet4.getString("ULTERIORE_ATC_CODICE");

                                if (StringUtils.isNotBlank(codiciUlterioreATC)) {
                                    List<DatoTipologicaInteger> ulterioriATC = new ArrayList<>();

                                    Arrays.asList(codiciUlterioreATC.split(" ")).forEach(string -> {
                                        int codiceUlterioreAtc = Integer.parseInt(string);
                                        try {
                                            preparedStatement5.clearParameters();
                                            preparedStatement5.setInt(1, codiceUlterioreAtc);
                                            resultSet5.set(preparedStatement5.executeQuery());
                                            String nome = "";

                                            while (resultSet5.get().next()) {
                                                nome = resultSet5.get().getString("NOME");
                                            }

                                            ulterioriATC.add(new DatoTipologicaInteger(codiceUlterioreAtc, nome));
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    });


                                    venatoriaAttuale.setUlterioriATC(ulterioriATC);
                                }


                                venatoriaAttuale.setAbilitazionePacchettoCinqueGiornate(false);
                                venatoriaAttuale.setGiornateStanziale(0);
                                venatoriaAttuale.setGiornateMobilita(0);

                                if (cacciatore.getCodiceRegioneResidenza() != null) {
                                    if (cacciatore.getCodiceRegioneResidenza().equals(CODICE_TOSCANA)) {
                                        try (PreparedStatement preparedStatement6 = connection.prepareStatement(SQL_CONTATORI)) {
                                            preparedStatement6.setInt(1, cacciatore.getCodiceCacciatore());
                                            ResultSet resultSet6 = preparedStatement6.executeQuery();

                                            if (resultSet6.next()) {
                                                String codiceTessera = resultSet6.getString("CODICE_TESSERA");
                                                Integer giornate = resultSet6.getInt("CONT_STANZIALE");

                                                if (StringUtils.isNotBlank(codiceTessera)) {
                                                    venatoriaAttuale.setAbilitazionePacchettoCinqueGiornate(true);
                                                    venatoriaAttuale.setCodiceStanziale(codiceTessera);
                                                    venatoriaAttuale.setGiornateStanziale(5 - giornate);
                                                }
                                            }
                                        }

                                    } else if (cacciatore.getCodiceRegioneResidenza().equals(CODICE_LAZIO)) {
                                        venatoriaAttuale.setGiornateMobilita(18);
                                    } else {
                                        venatoriaAttuale.setGiornateMobilita(20);
                                    }
                                }

                                String tipologiaCaccia = resultSet4.getString("TIPOLOGIA_CACCIA");
                                tipologiaAbilitazioneCaccia.keySet().stream().filter(key -> tipologiaAbilitazioneCaccia.get(key).equals(tipologiaCaccia)).findFirst().ifPresent(key -> {
                                    DatoTipologicaInteger tipologiaAbilitazioneCaccia = new DatoTipologicaInteger(key, CacciatoreService.tipologiaAbilitazioneCaccia.get(key));
                                    venatoriaAttuale.setTipologiaAbilitazioneCaccia(tipologiaAbilitazioneCaccia);
                                });


                                //----------------------------------------------------------------------------------------------------------


                                int codicePrimoATCPre = resultSet4.getInt("PRIMO_ATC_PRE_CODICE");

                                if (codicePrimoATCPre != 0) {
                                    preparedStatement5.clearParameters();
                                    preparedStatement5.setInt(1, codicePrimoATCPre);
                                    resultSet5.set(preparedStatement5.executeQuery());

                                    String nome = "";

                                    while (resultSet5.get().next()) {
                                        nome = resultSet5.get().getString("NOME");
                                    }

                                    venatoriaPrec.setPrimoATC(new DatoTipologicaInteger(codicePrimoATCPre, nome));
                                    Optional.ofNullable(resultSet4.getString("PRIMO_ATC_PRE_DATA")).ifPresent(data -> venatoriaPrec.setDataPrimoATC(LocalDate.parse(data, DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
                                }

                                String codiciUlterioreATCPre = resultSet4.getString("ULTERIORE_ATC_PRE_CODICE");

                                if (StringUtils.isNotBlank(codiciUlterioreATCPre)) {
                                    List<DatoTipologicaInteger> ulterioriATC = new ArrayList<>();

                                    Arrays.asList(codiciUlterioreATCPre.split(" ")).forEach(string -> {
                                        int codiceUlterioreAtc = Integer.parseInt(string);
                                        try {
                                            preparedStatement5.clearParameters();
                                            preparedStatement5.setInt(1, codiceUlterioreAtc);
                                            resultSet5.set(preparedStatement5.executeQuery());
                                            String nome = "";

                                            while (resultSet5.get().next()) {
                                                nome = resultSet5.get().getString("NOME");
                                            }

                                            ulterioriATC.add(new DatoTipologicaInteger(codiceUlterioreAtc, nome));
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    });


                                    venatoriaPrec.setUlterioriATC(ulterioriATC);
                                }
                            }


                            if (venatoriaPrec.getPrimoATC() != null) {
                                venatoriaAttuale.setPrimoATCPreiscrizione(venatoriaPrec.getPrimoATC());
                            }

                            if (!venatoriaPrec.getUlterioriATC().isEmpty()) {
                                venatoriaAttuale.setUlterioriATCPreiscrizione(venatoriaPrec.getUlterioriATC());
                            }


                            cacciatore.setVenatorie(Arrays.asList(venatoriaPrec, venatoriaAttuale));
                        }
                    }
                    //######################################################################## teleprenotazioni

                    try (PreparedStatement statementTeleprenotazioni = connection.prepareStatement(SQL_TELEPRENOTAZIONI_ANA)) {

                        statementTeleprenotazioni.setInt(1, cacciatore.getCodiceCacciatore());

                        ResultSet resultSetTeleprenotazioni = statementTeleprenotazioni.executeQuery();

                        while (resultSetTeleprenotazioni.next()) {
                            Teleprenotazione teleprenotazione = new Teleprenotazione();

                            teleprenotazione.setCodiceTelefonata(resultSetTeleprenotazioni.getInt("COD_TELEFONATA"));

                            teleprenotazione.setFromExport(true);
                            teleprenotazione.setEliminato(false);
                            teleprenotazione.setConfermata(true);

                            teleprenotazione.setDataPrenotazione(resultSetTeleprenotazioni.getDate("DATA_PRE").toLocalDate());
                            teleprenotazione.setIsMobile(resultSetTeleprenotazioni.getBoolean("IS_MOBILITA"));
                            teleprenotazione.setPrenotatoIl(resultSetTeleprenotazioni.getDate("PRENOTATO_IL").toLocalDate());
                            teleprenotazione.setModalitaPrenotazione(resultSetTeleprenotazioni.getString("MODALITA_PREN").trim());

                            tipologiaTeleprenotazioni.forEach((key, value) -> {
                                try {
                                    if (value.equals(resultSetTeleprenotazioni.getString("TIPO_PRE").trim())) {
                                        DatoTipologicaInteger datoTipologicaInteger = new DatoTipologicaInteger(key, value);

                                        teleprenotazione.setTipoPrenotazione(datoTipologicaInteger);
                                    }
                                } catch (SQLException e) {
                                    log.error(e);
                                }
                            });

                            Integer codiceATC = resultSetTeleprenotazioni.getInt("ATC_PRE");


                            try (PreparedStatement statement6 = connection.prepareStatement(SQL_ATC_ANA)) {
                                statement6.setInt(1, codiceATC);
                                ResultSet resultSet6 = statement6.executeQuery();

                                if (resultSet6.next()) {
                                    String nomeATC = resultSet6.getString("NOME");

                                    DatoTipologicaInteger datoTipologicaInteger = new DatoTipologicaInteger(codiceATC, nomeATC);
                                    teleprenotazione.setAtcPrenotazione(datoTipologicaInteger);
                                }


                                int annoTeleprenotazione = teleprenotazione.getDataPrenotazione().getYear();

                                cacciatore.getVenatorie().stream().filter(venatoria -> venatoria.getStagioneVenatoria().equals(String.valueOf(annoTeleprenotazione))).findFirst().ifPresent(venatoria -> {
                                    venatoria.getTeleprenotazioni().add(teleprenotazione);
                                });
                            }
                        }
                    }

                    try (PreparedStatement statementAppostamenti = connection.prepareStatement(SQL_APPPOSTAMENTI_BY_COD_CAC)) {
                        statementAppostamenti.setInt(1, cacciatore.getCodiceCacciatore());
                        ResultSet resultSetAppostamenti = statementAppostamenti.executeQuery();
                        while (resultSetAppostamenti.next()) {
                            Appostamento appostamento = new Appostamento();

                            String tabellaRT = resultSetAppostamenti.getString("NUMERO_TABELLA");
                            String tipologia = resultSetAppostamenti.getString("TIPO_APPOSTAMENTO");

                            DatoTipologicaString provinciaAppostamento = this.getProvinciaBySigla(resultSetAppostamenti.getString("PROVINCIA_APPOSTAMENTO"), connection);

                            appostamento.setSedeTerritoriale(provinciaAppostamento);
                            appostamento.setEliminato(false);
                            appostamento.setAttivo(true);
                            appostamento.setFromExport(true);

                            if (tipologia.equalsIgnoreCase("COLOMBACCI") || tipologia.equalsIgnoreCase("COLOMBACCIO")) {
                                appostamento.setCodiceCapanno(tabellaRT + "C");
                                appostamento.setTipologiaCaccia(new DatoTipologicaInteger(2, tipologiaCaccia.get(2)));
                            } else if (tipologia.equalsIgnoreCase("MINUTA SELVAGGINA") || tipologia.equalsIgnoreCase("SELVAGGINA MINUTA")) {
                                appostamento.setCodiceCapanno(tabellaRT + "MS");
                                appostamento.setTipologiaCaccia(new DatoTipologicaInteger(1, tipologiaCaccia.get(1)));
                            } else {
                                appostamento.setCodiceCapanno(tabellaRT + "PT");
                                appostamento.setTipologiaCaccia(new DatoTipologicaInteger(3, tipologiaCaccia.get(3)));
                            }
                            appostamento.setFoglioCatastale(resultSetAppostamenti.getString("NUMERO_FOGLIO_CATASTALE"));
                            appostamento.setNumeroParticella(resultSetAppostamenti.getString("NUMERO_PARTICELLA"));
                            appostamento.setCoordinataX(resultSetAppostamenti.getString("COORDINATA_X"));
                            appostamento.setCoordinataY(resultSetAppostamenti.getString("COORDINATA_Y"));
                            appostamento.setComuneAppostamento(DatoTipologicaString.builder().descrizione(resultSetAppostamenti.getString("COMUNE_APPOSTAMENTO")).build());
                            appostamento.setFrazione(resultSetAppostamenti.getString("FRAZIONE_APPOSTAMENTO"));
                            appostamento.setLocalita(resultSetAppostamenti.getString("LOCALITA_APPOSTAMENTO"));
                            appostamento.setCognomeProprietario(resultSetAppostamenti.getString("COGNOME_PROPRIETARIO"));
                            appostamento.setNomeProprietario(resultSetAppostamenti.getString("NOME_PROPRIETARIO"));
                            try {
                                appostamento.setDataNascitaProprietario(LocalDate.parse(resultSetAppostamenti.getString("DATA_NASCITA_PROPRIETARIO"), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            } catch (Exception e) {/*ignored*/}
                            appostamento.setIndirizzoResidenzaProprietario(resultSetAppostamenti.getString("INDIRIZZO_RESIDENZA_PROPRIETARIO"));
                            appostamento.setComuneResidenzaProprietario(resultSetAppostamenti.getString("COMUNE_RESIDENZA_PROPRIETARIO"));
                            appostamento.setComuneNascitaProprietario(resultSetAppostamenti.getString("COMUNE_NASCITA_PROPRIETARIO"));
                            appostamento.setCapResidenzaProprietario(resultSetAppostamenti.getString("CAP_RESIDENZA_PROPRIETARIO"));
                            appostamento.setProvinciaResidenzaProprietario(resultSetAppostamenti.getString("PROVINCIA_RESIDENZA_PROPRIETARIO"));
//                            appostamento.setCodiceProgetto("RTAFC0004");

                            cacciatore.getAppostamenti().add(appostamento);
                        }
                    }

                    cacciatoreRepository.save(cacciatore);
                } catch (Exception e) {
                    log.error("Errore codice cacciatore: " + resultSet1.getInt("CODICE_CACCIATORE"), e);
                    errori.add("ERRORE CODICE CACCIATORE " + resultSet1.getInt("CODICE_CACCIATORE") + " " + e.getMessage());
                    throw new RuntimeException("ERRORE");
                } finally {
                    contatoreCacciatore++;
                }
            }

            log.info("Scrivo gli errori su file");

            try (FileWriter fileWriter = new FileWriter("C:\\Users\\cladl\\Desktop\\report.log")) {
                errori.forEach(errore -> {
                    try {
                        fileWriter.append(errore).append("\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (Exception e) {
            log.error(e);
        }

        return cacciatores;
    }

    private void setModalitaAbilitazione(Specializzazione specializzazione) {
        switch (specializzazione.getCodiceSpecie()) {
            case 1:
                modalitaAbilitazioneCapriolo.forEach((integer, s) -> {
                    if (StringUtils.equalsIgnoreCase(specializzazione.getModalitaAbilitazione(), s)) {
                        specializzazione.setCodiceModalitaAbilitazione(integer);
                    }
                });
                break;
            case 2:
                modalitaAbilitazioneCervo.forEach((integer, s) -> {
                    if (StringUtils.equalsIgnoreCase(specializzazione.getModalitaAbilitazione(), s)) {
                        specializzazione.setCodiceModalitaAbilitazione(integer);
                    }
                });
                break;
            case 3:
                modalitaAbilitazioneCinghiale.forEach((integer, s) -> {
                    if (StringUtils.equalsIgnoreCase(specializzazione.getModalitaAbilitazione(), s)) {
                        specializzazione.setCodiceModalitaAbilitazione(integer);
                    }
                });
                break;
            case 4:
                modalitaAbilitazioneDaino.forEach((integer, s) -> {
                    if (StringUtils.equalsIgnoreCase(specializzazione.getModalitaAbilitazione(), s)) {
                        specializzazione.setCodiceModalitaAbilitazione(integer);
                    }
                });
                break;
            case 5:
                modalitaAbilitazioneMuflone.forEach((integer, s) -> {
                    if (StringUtils.equalsIgnoreCase(specializzazione.getModalitaAbilitazione(), s)) {
                        specializzazione.setCodiceModalitaAbilitazione(integer);
                    }
                });
                break;
        }
    }

    public static Integer getAnnoAttualeStagioneVenatoria() {
        LocalDate now = LocalDate.now();
        if (now.getMonthValue() == 1) {
            return now.getYear() - 1;
        } else {
            return now.getYear();
        }
    }

    private String clean(String input) {
        if (StringUtils.isBlank(input) || StringUtils.equalsIgnoreCase(input, "null") || StringUtils.equalsIgnoreCase(input, "0")) {
            return null;
        }

        return input;
    }

}

