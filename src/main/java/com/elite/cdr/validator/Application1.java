package com.elite.cdr.validator;

import com.beust.jcommander.JCommander;
import com.elite.cdr.validator.utils.Settings;
import org.apache.log4j.Logger;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;
import org.apache.nifi.spark.NiFiDataPacket;
import org.apache.nifi.spark.NiFiReceiver;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;

import static com.elite.cdr.validator.utils.Settings.LOCAL;

public class Application1 {
  private static final Logger LOGGER =
      Logger.getLogger(Application1.class);

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

    Pattern SPACE = Pattern.compile(" ");
    // Build a Site-to-site client config
    /*SiteToSiteClientConfig config = new SiteToSiteClient.Builder()
            .url("https://127.0.0.1:8443/nifi/")
            .portName("Data For Spark")
            .buildConfig();
    LOGGER.info(config.getPortIdentifier());*/
    SparkConf sparkConf = new SparkConf().setAppName("HDFS Streaming example").setMaster("local[*]");
    JavaStreamingContext jsc = new JavaStreamingContext(sparkConf, new Duration(1000L));

    JavaDStream<String> textFile = jsc.textFileStream("hdfs://localhost:9000/user/input/");

    // Print the first ten elements of each RDD generated
    textFile.print();

    // Get the lines that contain the word "ERROR"
    JavaDStream<String> errorLines = textFile.filter(new Function<String, Boolean>() {
      public Boolean call(String line) {
        return line.contains("ERROR");
      }});
    errorLines.print();

   /*JavaPairDStream<String, Integer> pairs = textFile.mapToPair(s -> new Tuple2<>(s, 1));
    JavaPairDStream<String, Integer> wordCounts = pairs.reduceByKey((i1, i2) -> i1 + i2);

    // Print the first ten elements of each RDD generated in this DStream to the console
    wordCounts.print();*/

    // Create a JavaReceiverInputDStream using a NiFi receiver so that we can pull data from
    // specified Port
   /*JavaReceiverInputDStream<NiFiDataPacket> packetStream =
            jsc.receiverStream(new NiFiReceiver(config, StorageLevel.MEMORY_ONLY()));

    // Map the data from NiFi to text, ignoring the attributes
    JavaDStream<String> text = packetStream.map(new Function() {
      @Override
      public Object call(Object o) throws Exception {
        return null;
      }

      public String call(final NiFiDataPacket dataPacket) throws Exception {
        return new String(dataPacket.getContent(), StandardCharsets.UTF_8);
      }
    });*/

    jsc.start();
    jsc.awaitTermination();
    /*
    Dataset<Row> df = spark.read()
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
        Application1.class.getClassLoader().getResource("./");
    try {
      System.setProperty("hadoop.home.dir", resources.toURI().getPath());
    } catch (URISyntaxException e) {
      LOGGER.warn("Unable to setup hadoop.home.dir for winutils, continuing ...");
    }
  }
}
