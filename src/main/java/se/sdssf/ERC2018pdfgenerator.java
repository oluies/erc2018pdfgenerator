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
        InputStream is = null;
        PDDocument doc = null;
        if (args.length == 0) {
            System.out.println("must supply parameters: <pdffile> <csvfile>");
        } else {
            String InpdfFileName = args[0];
            String IncsvFileName = args[1];



            try {
                is = new FileInputStream(InpdfFileName);
                System.out.println("isvalid" + ((FileInputStream) is).getFD().valid());

                ReadFromCsvFile instance = ReadFromCsvFile.getInstance();
                final List<FormData> fds = instance.read(IncsvFileName);
                doc = PDDocument.load(is);
                PDDocumentCatalog catalog = doc.getDocumentCatalog();
                PDAcroForm form = catalog.getAcroForm();

                List<PDField> fields = form.getFields();

                Class  aClass = FormData.class;

                for(FormData formdata : fds) {

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


                    addImageFromURL(doc,formdata.urlf);
                    addImageFromURL(doc,formdata.urlo);
                    addImageFromURL(doc,formdata.urlp);



                    doc.setAllSecurityToBeRemoved(true);

                    doc.save(formdata.ercID + "_" + formdata.Efternamn + "_" + formdata.Nationalitet+".pdf");
                    doc.close();

                }

            } catch (FileNotFoundException e) {
                System.out.println(String.format("\"%s\": file non trovato.", InpdfFileName));
            } catch (IOException e) {
                System.out.println(String.format("\"%s\": file non è un PDF valido.", InpdfFileName));
            }  catch (NoSuchFieldException e) {
                e.printStackTrace();
            } finally {
                if (is != null){
                    try {
                        is.close();
                    } catch (IOException e) {
                        System.out.println(String.format("\"%s\": impossibile chiudere lo stream.", InpdfFileName));
                    }
                }
                if (doc != null) {
                    try {
                        doc.close();
                    } catch (IOException e) {
                        System.out.println(String.format("\"%s\": impossibile chiudere il documento PDF.", InpdfFileName));
                    }
                }
            }
        }



    }

    private static void addImageFromURL(final PDDocument doc,final String urlstring ) throws IOException {

        if(urlstring != null && !urlstring.isEmpty()){
        BufferedImage image  = null;
        try {
            URL url = new URL(urlstring);
            System.out.println(url);

            image = ImageIO.read(url);
        } catch (IOException e) {
            System.out.println(e.toString());
        }


        System.out.println( image.toString());

        // createFromFile is the easiest way with an image file
        // if you already have the image in a BufferedImage,
        // call LosslessFactory.createFromImage() instead
        PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);
        //PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, doc);

        PDPage imagepage = new PDPage();
        doc.addPage(imagepage);

        PDPageContentStream contentStream = new PDPageContentStream(doc, imagepage, PDPageContentStream.AppendMode.APPEND, true, true);


        // contentStream.drawImage(ximage, 20, 20 );
        // better method inspired by http://stackoverflow.com/a/22318681/535646
        // reduce this value if the image is too large
        float scale = 1f;
        contentStream.drawImage(pdImage, 20, 20, pdImage.getWidth()*scale, pdImage.getHeight()*scale);

        contentStream.close();


        System.out.println("Image inserted");
        }

    }


}
