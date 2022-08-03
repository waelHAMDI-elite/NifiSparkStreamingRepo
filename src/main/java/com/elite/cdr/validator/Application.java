package com.elite.cdr.validator;

import com.beust.jcommander.JCommander;
import com.elite.cdr.validator.Asn1Classes.Cdr;
import com.elite.cdr.validator.Asn1Classes.ConvertedFile;
import com.elite.cdr.validator.functionClasses.FunctionsClass;
import com.elite.cdr.validator.utils.Settings;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
//import org.apache.ignite.spark.IgniteDataFrameSettings;
import org.apache.log4j.Logger;
import org.apache.nifi.remote.client.KeystoreType;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;
import org.apache.nifi.spark.NiFiDataPacket;
import org.apache.nifi.spark.NiFiReceiver;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.ArrayType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

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

  private static final String CONFIG = "C:\\Apache Ignite\\apache-ignite-2.13.0-bin\\examples\\config\\example-ignite.xml";

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
    //SparkSession spark = SparkSession.builder().configAsn1(conf).getOrCreate();
    String url=null,portName=null,keystoreFilename = null,keystorePass=null,truststoreFilename=null,truststorePass=null;
    String propPath = settings.getPropretiesPath();
    try (InputStream input = new FileInputStream(propPath)) {

      Properties prop = new Properties();

      // load a properties file
      prop.load(input);

      //retrieve properties
      url = prop.getProperty("spark.NifiSparkStreaming.url");
      portName = prop.getProperty("spark.NifiSparkStreaming.portName");
      keystoreFilename = prop.getProperty("spark.NifiSparkStreaming.keystoreFilename");
      keystorePass = prop.getProperty("spark.NifiSparkStreaming.keystorePass");
      truststoreFilename = prop.getProperty("spark.NifiSparkStreaming.truststoreFilename");
      truststorePass = prop.getProperty("spark.NifiSparkStreaming.truststorePass");

    } catch (IOException ex) {
      ex.printStackTrace();
    }

    // Build a Site-to-site client configAsn1
    /*SiteToSiteClientConfig configAsn1 = new SiteToSiteClient.Builder()
            .url(url)
            .portName(portName)
            .keystoreFilename(keystoreFilename)
            .keystorePass(keystorePass)
            .keystoreType(KeystoreType.PKCS12)
            .truststoreFilename(truststoreFilename)
            .truststorePass(truststorePass)
            .truststoreType(KeystoreType.PKCS12)
            .buildConfig();*/
      SiteToSiteClientConfig configAsn1 = new SiteToSiteClient.Builder()
              .url("http://127.0.0.1:8090/nifi/")
              .portName("Asn1 For Spark")
              .buildConfig();

      /*SiteToSiteClientConfig configAscii = new SiteToSiteClient.Builder()
              .url("http://127.0.0.1:8090/nifi/")
              .portName("Ascii For Spark")
              .buildConfig();*/

    /*
        By doing that the NiFiReceiver will create an SSLContext when it builds the SiteToSiteClient
        from the SiteToSiteClientConfig, which is after serialization. See the 'Site to Site properties' in the
        "nifi.properties" file
     */

    SparkConf sparkConf = new SparkConf().setAppName("NiFi Spark Streaming example").setMaster("local[*]");
    JavaStreamingContext jsc = new JavaStreamingContext(sparkConf, Durations.seconds(1));
    
    // Create a JavaReceiverInputDStream using a NiFi receiver so that we can pull data from specified Port
    JavaReceiverInputDStream<NiFiDataPacket> packetStream =
            jsc.receiverStream(new NiFiReceiver(configAsn1, StorageLevel.MEMORY_ONLY()));

      // Create a JavaReceiverInputDStream using a NiFi receiver so that we can pull data from specified Port
      /*JavaReceiverInputDStream<NiFiDataPacket> lines =
              jsc.receiverStream(new NiFiReceiver(configAscii, StorageLevel.MEMORY_ONLY()));

    // Map the data from NiFi to text, ignoring the attributes
    JavaDStream<String> text = lines.map(dataPacket -> new String(dataPacket.getContent(), StandardCharsets.UTF_8));
    //JavaDStream words = lines.map(x -> Arrays.asList(x.spl).iterator());
    text.print();*/

    JavaDStream<List<Cdr>> files = packetStream.map(
            new Function<NiFiDataPacket, List<Cdr>>() {
              @Override
              public List<Cdr> call(NiFiDataPacket niFiDataPacket) throws Exception {
                FileDecoder fileDecoder = new FileDecoder();
                return fileDecoder.decode(niFiDataPacket.getContent());
              }
            });

    files.foreachRDD(rdd -> {
      JavaRDD<Cdr> rowRDD = rdd.flatMap(List::iterator);
      SparkSession spark = SparkSession.builder().config(rdd.context().getConf()).getOrCreate();


      Dataset<Row> cdrsDF = spark.createDataFrame(rowRDD,Cdr.class).select("id","CALLTYPE","IMSI","IMEI","MSISDN","CALLINGNUMBER","CALLEDNUMBER","MSCINCOMING","MSCOUTGOING","LOCATION","ANSWERTIME");
      cdrsDF.show();

      if (cdrsDF.count() > 0){
        //cdrsDF.write().mode(SaveMode.Overwrite).csv("C:\\IntelliJOutput\\StreamingOut");

        cdrsDF.write()
                //.format(IgniteDataFrameSettings.FORMAT_IGNITE())
                //.option(IgniteDataFrameSettings.OPTION_CONFIG_FILE(), CONFIG)
                //.option(IgniteDataFrameSettings.OPTION_TABLE(), "Cdr")
                .format("jdbc")
                .option("url","jdbc:ignite:thin://127.0.0.1")
                .option("dbtable","Cdr")
                .option("user","ignite")
                .option("password","ignite")
                .mode(SaveMode.Append)
                .save();

        FunctionsClass functions = new FunctionsClass();
        functions.addrNorm();
        functions.loadToHdfs(spark);

      }

        //cdrsDF.write().mode(SaveMode.Overwrite).csv("hdfs://localhost:9000/user/dataFromSpark/file1");
        //cdrsDF.write().csv("hdfs://localhost:9000/user/dataFromSpark/file1");
        //cdrsDF.write().csv("C:\\IntelliJOutput\\output");
        //System.out.println("count(*) = "+cdrsDF.count());
    });

    //files.print();

    jsc.start();
    jsc.awaitTermination();

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

