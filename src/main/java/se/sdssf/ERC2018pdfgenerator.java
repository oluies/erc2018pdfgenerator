package se.sdssf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by oluies on
 */
public class ERC2018pdfgenerator {

    private static final Logger LOGGER = Logger.getLogger( ERC2018pdfgenerator.class.getName() );


    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("must supply parameters: <pdffile> <csvfile>");
        } else {
            String InpdfFileName = args[0];
            String IncsvFileName = args[1];




            try {



                ReadFromCsvFile instance = ReadFromCsvFile.getInstance();
                final List<FormData> fds = instance.read(IncsvFileName);


                for(FormData formdata : fds) {

                    InputStream is = new FileInputStream(InpdfFileName);
                    PDDocument doc = PDDocument.load(is);

                    PDDocumentCatalog catalog = doc.getDocumentCatalog();
                    PDAcroForm form = catalog.getAcroForm();

                    List<PDField> fields = form.getFields();

                    Class  aClass = FormData.class;



                    for (PDField pdField : fields) {
                        String pdFieldName =  pdField.getFullyQualifiedName();

                        if("Jakt/tavling".compareToIgnoreCase(pdFieldName) == 0 ) continue;

                        Field field = aClass.getField(pdFieldName);

                        try {
                            String value = (String) field.get(formdata);

                            String fieldInfo = String.format("name    : \"%s\"   \"%s\"\n", pdFieldName, value);
                            LOGGER.log( Level.FINE, "returning list ", fieldInfo);


                            try {

                                if (form != null)
                                {
                                    System.out.println(pdFieldName.toString());
                                    System.out.println(value.toString());
                                    form.getField(pdFieldName.toString()).setValue(value.toString());

                                }
                            }  catch (Exception e) {
                                System.out.println(e.getLocalizedMessage());
                            }

                        } catch (Exception e) {
                            System.out.println(e.getLocalizedMessage());

                        }

                    }

                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
                    LocalDateTime now = LocalDateTime.now();
                    System.out.println(dtf.format(now));
                    form.getField("DatumHuvud").setValue(dtf.format(now) );
                    form.getField("PlatsJakt").setValue("Villingsberg" );

                    PDCheckBox jaktMal = (PDCheckBox)  form.getField("Jakt/tavling");
                    jaktMal.check();


                    PDButton ih =  (PDButton) form.getField("JaktMal");
                    //ih.setValue("Mal");

                    ih.setValue("2");
                    //System.out.println( "TYPE JAKTMÅL ***** "+  ih.getOnValues() + " " +   ih.getValueAsString() + " " + form.getField("JaktMal").getFieldType());

                    PDCheckBox KopiaTillstand = (PDCheckBox)  form.getField("KopiaTillstand");
                    KopiaTillstand.check();

                    PDCheckBox KopiaEu = (PDCheckBox)  form.getField("KopiaEu");
                    KopiaEu.check();

                    //Retrieving the page
                    PDPage page = doc.getPage(0);

                    // Create a new trust manager that trust all certificates
                    TrustManager[] trustAllCerts = new TrustManager[]{
                            new X509TrustManager() {
                                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                    return null;
                                }
                                public void checkClientTrusted(
                                        java.security.cert.X509Certificate[] certs, String authType) {
                                }
                                public void checkServerTrusted(
                                        java.security.cert.X509Certificate[] certs, String authType) {
                                }
                            }
                    };


                    // Activate the new trust manager
                    try {
                        SSLContext sc = SSLContext.getInstance("SSL");
                        sc.init(null, trustAllCerts, new java.security.SecureRandom());
                        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    } catch (Exception e) {
                    }


                    PDDocument invitation = PDDocument.load( new FileInputStream("inbjudan.pdf"));


                    PDDocumentCatalog invitationcatalog = invitation.getDocumentCatalog();
                    PDAcroForm finvitationorm = invitationcatalog.getAcroForm();


                    finvitationorm.getField("Födelsetid").setValue(formdata.getFodelsetid());
                    finvitationorm.getField("Efternamn_2").setValue(formdata.getEfternamn());
                    finvitationorm.getField("Tilltalsnamn_2").setValue(formdata.getTilltalsnamn());
                    finvitationorm.getField("Adress_2").setValue(formdata.getAdress());
                    finvitationorm.getField("Postnummer_2").setValue(formdata.getPostnummer());
                    finvitationorm.getField("Ort_2").setValue(formdata.getOrt());
                    finvitationorm.getField("Land_2").setValue(formdata.getLand());
                    finvitationorm.getField("Nationalitet_2").setValue(formdata.getNationalitet());
                    finvitationorm.getField("Telefon_2").setValue(formdata.getTelefon());
                    finvitationorm.getField("Epostadress_2").setValue(formdata.getEpost());



                    /*INFO: Födelsetid
                    May 26, 2018 1:51:32 PM se.sdssf.ERC2018pdfgenerator main
                    INFO: Efternamn_2
                    May 26, 2018 1:51:32 PM se.sdssf.ERC2018pdfgenerator main
                    INFO: Tilltalsnamn_2
                    May 26, 2018 1:51:32 PM se.sdssf.ERC2018pdfgenerator main
                    INFO: Adress_2
                    May 26, 2018 1:51:32 PM se.sdssf.ERC2018pdfgenerator main
                    INFO: Postnummer_2
                    May 26, 2018 1:51:32 PM se.sdssf.ERC2018pdfgenerator main
                    INFO: Ort_2
                    May 26, 2018 1:51:32 PM se.sdssf.ERC2018pdfgenerator main
                    INFO: Land_2
                    May 26, 2018 1:51:32 PM se.sdssf.ERC2018pdfgenerator main
                    INFO: Nationalitet_2
                    May 26, 2018 1:51:32 PM se.sdssf.ERC2018pdfgenerator main
                    INFO: Telefon_2
                    May 26, 2018 1:51:32 PM se.sdssf.ERC2018pdfgenerator main
                    INFO: Epostadress_2*/


                    PDPage invitationpage1 = invitation.getPage(0);
                    doc.addPage(invitationpage1);


                    addImageFromURL(doc,formdata.getUrlf());
                    addImageFromURL(doc,formdata.getUrlo());
                    addImageFromURL(doc,formdata.getUrlp());
                    addImageFromURL(doc,formdata.getUrlPOA());


                    String fileName = "ERC_" + formdata.getErcID() + "_" + formdata.getEfternamn() + "_" + formdata.getTilltalsnamn() + "_" +  formdata.getLand()+".pdf";
                    String fixed = fileName.replaceAll("[^\\x00-\\x7F]", "").replace("\n", "").replace("'$'\\r''","").replace(" ","_");

                    doc.save(fixed);

                    doc.close();
                    doc = null;



                    // OK PDFBOX HAS SOME MEMMORY ISSUES ARGHH
                    System.gc();
                    System.gc();


                }



            } catch (FileNotFoundException e) {
                System.out.println(String.format("\"%s\": file non trovato.", InpdfFileName));
                System.out.println(String.format(e.getMessage()));
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println(String.format("\"%s\": file non è un PDF valido.", InpdfFileName));
                System.out.println(String.format(e.getMessage()));
                e.printStackTrace();


            }  catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }



    }

    private static void addImageFromURL(final PDDocument doc,final String urlstring ) throws IOException {

        if(urlstring != null && !urlstring.isEmpty()) {
            LOGGER.log(Level.INFO,"URL " + urlstring);

            if (urlstring.endsWith(".pdf")) {

                InputStream input = new URL(urlstring).openStream();
                final PDDocument urldoc = PDDocument.load( input);

                for (PDPage pdPage : urldoc.getPages()) {
                    doc.addPage(pdPage);

                }
                //urldoc.close();
                //input.close();

            } else {
                BufferedImage image = null;
                try {
                    URL url = new URL(urlstring);
                    System.out.println(url);

                    image = ImageIO.read(url);
                } catch (IOException e) {
                    System.out.println(e.toString());
                }


                System.out.println(image.toString());

                // createFromFile is the easiest way with an image file
                // if you already have the image in a BufferedImage,
                // call LosslessFactory.createFromImage() instead
                final PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);
                //PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, doc);

                final PDPage imagepage = new PDPage();

                final PDPageContentStream contentStream = new PDPageContentStream(doc, imagepage, PDPageContentStream.AppendMode.APPEND, true, true);


                // contentStream.drawImage(ximage, 20, 20 );
                // better method inspired by http://stackoverflow.com/a/22318681/535646
                // reduce this value if the image is too large
                float scale = 0.4f;
                contentStream.drawImage(pdImage, 20, 20, 500, 500);

                doc.addPage(imagepage);

                contentStream.close();


                System.out.println("Image inserted");
            }
        }

    }


}
