package se.sdssf;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadFromCsvFile {
    private static final Logger LOGGER = Logger.getLogger( ReadFromCsvFile.class.getName() );

    private static ReadFromCsvFile ourInstance = new ReadFromCsvFile();

    public static ReadFromCsvFile getInstance() {
        return ourInstance;
    }

    private ReadFromCsvFile() {
    }

    public    List<FormData> read(String file) {

        List<FormData> fds =  new ArrayList<FormData>();


        try (Reader reader = Files.newBufferedReader(Paths.get(file))) {
            ((BufferedReader) reader).readLine();


            try (CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {



                for (CSVRecord csvRecord : csvParser) {

                    final FormData fd = new FormData();

                    fd.setErcID( Long.parseLong(csvRecord.get(0)));
                    // Accessing Values by Column Index
                    String name = csvRecord.get(1);
                    String[] split = name.split(" ");

                    try {
                        fd.setEfternamn(split[split.length-1]);
                    } catch (Exception e) {
                    }
                    try {
                        String tilltalsnan = "";
                        for (int i = 0; i < split.length-1 ; i++) {
                            tilltalsnan += split[i] + " ";
                        }
                        fd.setTilltalsnamn(tilltalsnan);
                    } catch (Exception e) {
                    }


                    fd.setFodelsetid( csvRecord.get(2));
                    fd.setNationalitet(csvRecord.get(3));
                    fd.setLand(csvRecord.get(3));
                    String adress = csvRecord.get(4);
                    final String[] adresslines = adress.split("\n");

                    fd.setAdressfonster1("Polisen Arlanda");
                    try {
                        fd.setAdress( adresslines[0]);
                    } catch (Exception e) {
                    }
                    try {
                        fd.setPostnummer(adresslines[2]);
                    } catch (Exception e) {
                    }
                    try {
                        fd.setOrt( adresslines[1]);
                    } catch (Exception e) {
                    }


                    fd.setEpost(csvRecord.get(5));

                    fd.setTelefon(csvRecord.get(6));

                    fd.setVapentyp1(csvRecord.get(7));

                    fd.setFabrikat1(csvRecord.get(8));

                    fd.setModell1( csvRecord.get(9));

                    fd.setTillverkningsnummer1( csvRecord.get(10));

                    fd.setKaliber1(csvRecord.get(11));

                    fd.setAmmunition1(csvRecord.get(12));


                    fd.setVapentyp2( csvRecord.get(13)) ;

                    fd.setFabrikat2( csvRecord.get(14)) ;

                    fd.setModell2(csvRecord.get(15));

                    fd.setTillverkningsnummer2(csvRecord.get(16));


                    fd.setKaliber2(csvRecord.get(17));

                    fd.setAmmunition2( csvRecord.get(18));


                    fd.setEfternamnVard("Svenska Dynamiska");
                    fd.setTilltalsnamnVard( "Sportskytteförbundet");
                    fd.setPersonnrVard("842001-1713");
                    fd.setTelefonVard("0707638280");
                    fd.setAdressVard( "");
                    fd.setLandVard("Sverige");
                    fd.setNationalitetVard("Svensk");
                    fd.setEpostVard("ORDFORANDE@SDSSF.SE");
                    fd.setAdressVard( "Syrenvägen 7");
                    fd.setOrtVard( "Väckelsång");
                    fd.setPostnummerVard("362 51");
                    fd.setDatumIn("2018-JUL-20");
                    fd.setDatumUt("2018-AUG-19");

                    fd.urlp = csvRecord.get(19);
                    fd.urlf = csvRecord.get(20);
                    fd.urlo = csvRecord.get(21);

                    fd.urlPOA = csvRecord.get(22);

                    fds.add(fd);


                    // ERCID,Name,"Date of birth",Nationality,Address,Email,Phone,Type,Manufacturer,
                    // Model,"Serial number",Caliber,"Ammunition quantity","Type 2","Manufacturer 2",
                    // "Model 2","Serial number 2","Caliber 2","Ammunition quantity 2","Copy of Passport or photo ID",
                    // "Copy of firearms license (EU firearm passport for      EU countries)","Other supporting documents",
                    // "Processing fee (police fee)",     "Total Amount","Comment or Message",Date,"Date GMT",ID


                    LOGGER.log( Level.FINE, "returning list ", fds.toString());

                }

            }

        } catch (IOException e) {
            LOGGER.log( Level.SEVERE, "Exception {1} ", e.toString());
        }

        return fds;
    }
}
