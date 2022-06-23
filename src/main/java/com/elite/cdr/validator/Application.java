package com.elite.cdr.validator;

import com.beust.jcommander.JCommander;
import com.elite.cdr.validator.Asn1Classes.Cdr;
import com.elite.cdr.validator.utils.Settings;
import org.apache.log4j.Logger;
import org.apache.nifi.remote.client.KeystoreType;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;
import org.apache.nifi.remote.protocol.SiteToSiteTransportProtocol;
import org.apache.nifi.spark.NiFiDataPacket;
import org.apache.nifi.spark.NiFiReceiver;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static com.elite.cdr.validator.utils.Settings.LOCAL;

public class Application {
  private static final Logger LOGGER =
      Logger.getLogger(Application.class);

  private static final String LOGGER_SEPARATOR =
      "****************************************************";

  public static void main(String[] args) throws InterruptedException {

    LOGGER.info("Starting ASN1 Reader ");

    LOGGER.info("############### Run with the args " + Arrays.toString(args));

    Settings settings = new Settings();

    try {
      JCommander.newBuilder().addObject(settings).build().parse(args);
      LOGGER.info("############### Run in " + settings.getRunningEnv() + " mode");

    } catch (Exception e) {
      LOGGER.error(e);
      System.exit(1);
    }

    if (LOCAL.equals(settings.getRunningEnv())) {
      setHadoopHome();
    }

    //SparkConf conf = new SparkConf().setAppName("Feedback Analyzer").setMaster("local[*]");
    //SparkSession spark = SparkSession.builder().config(conf).getOrCreate();
    String url=null,portName=null,keystoreFilename = null,keystorePass=null,truststoreFilename=null,truststorePass=null;
    try (InputStream input = new FileInputStream("C:\\IntelliJProjects\\NifiSparkStreaming\\src\\main\\resources\\myapp.properties")) {

      Properties prop = new Properties();
      // load a properties file
      prop.load(input);

      //retrieve properties
      url = prop.getProperty("spark.NifiSparkStreaming.url");
      //System.out.println(url);
      portName = prop.getProperty("spark.NifiSparkStreaming.portName");
      //System.out.println(portName);
      keystoreFilename = prop.getProperty("spark.NifiSparkStreaming.keystoreFilename");
      //System.out.println(keystoreFilename);
      keystorePass = prop.getProperty("spark.NifiSparkStreaming.keystorePass");
      //System.out.println(keystorePass);
      truststoreFilename = prop.getProperty("spark.NifiSparkStreaming.truststoreFilename");
      //System.out.println(truststoreFilename);
      truststorePass = prop.getProperty("spark.NifiSparkStreaming.truststorePass");
      //System.out.println(truststorePass);

    } catch (IOException ex) {
      ex.printStackTrace();
    }

    Pattern SPACE = Pattern.compile(" ");
    // Build a Site-to-site client config
    SiteToSiteClientConfig config = new SiteToSiteClient.Builder()
            .url(url)
            .portName(portName)
            .keystoreFilename(keystoreFilename)
            .keystorePass(keystorePass)
            .keystoreType(KeystoreType.PKCS12)
            .truststoreFilename(truststoreFilename)
            .truststorePass(truststorePass)
            .truststoreType(KeystoreType.PKCS12)
            .buildConfig();

    /*
        By doing that the NiFiReceiver will create an SSLContext when it builds the SiteToSiteClient
        from the SiteToSiteClientConfig, which is after serialization. See the 'Site to Site properties' in the
        "nifi.properties" file
     */

    SparkConf sparkConf = new SparkConf().setAppName("NiFi-Spark Streaming example").setMaster("local[*]");
    JavaStreamingContext jsc = new JavaStreamingContext(sparkConf, new Duration(1000L));


    // Create a JavaReceiverInputDStream using a NiFi receiver so that we can pull data from specified Port
   JavaReceiverInputDStream<NiFiDataPacket> packetStream =
            jsc.receiverStream(new NiFiReceiver(config, StorageLevel.MEMORY_ONLY()));


    // Map the data(files) from NiFi to converted files
    JavaDStream<Object> files = packetStream.map(packet -> new FileDecoder(packet.getContent()));

    /*
         ICI je veux que la liste ce cdrs stockés dans 'files' se transforme en dataset<Row>
     */
      
    files.print();

    jsc.start();
    jsc.awaitTermination();

    //Dataset<Row> df = jsccreateDataFrame(files,List.class);
    /*    Dataset<Row> df = spark.read()
            .format("csv").option("header","true").option("delimiter", ";")
            .load(settings.getFilePath());

    df.show();
     */

    final long begin = System.currentTimeMillis();
    logSuccessMessage(begin);
  }

  private static void logSuccessMessage(long begin) {
    final long endTime = System.currentTimeMillis();
    final long elapsedTime = (endTime - begin);

    LOGGER.info(LOGGER_SEPARATOR);
    LOGGER.info("Duration: " + elapsedTime / 1000d + " seconds");
    LOGGER.info("Batch executed with success");
    LOGGER.info(LOGGER_SEPARATOR);
  }

  private static void logErrorMessage(long begin, Exception exception) {
    final long end = System.currentTimeMillis();
    final long elapsedTime = (end - begin);

    LOGGER.error(LOGGER_SEPARATOR);
    LOGGER.error("Duration: " + elapsedTime / 1000d + " seconds");
    LOGGER.error("Error occurred while executing treatment");
    LOGGER.error("Check stacktrace for more information", exception);
    LOGGER.error(LOGGER_SEPARATOR);
  }

  public static void setHadoopHome() {
    final URL resources =
        Application.class.getClassLoader().getResource("./");
    try {
      System.setProperty("hadoop.home.dir", resources.toURI().getPath());
    } catch (URISyntaxException e) {
      LOGGER.warn("Unable to setup hadoop.home.dir for winutils, continuing ...");
    }
  }
}
