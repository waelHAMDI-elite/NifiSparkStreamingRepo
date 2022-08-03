package com.elite.cdr.validator.functionClasses;

import com.elite.cdr.validator.Asn1Classes.Cdr;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class FunctionsClass {
   // public void createTable
    public void addrNorm() throws SQLException, ClassNotFoundException {
        // Register JDBC driver.
        Class.forName("org.apache.ignite.IgniteJdbcThinDriver");

        // Open the JDBC connection.
        Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1");

        conn.createStatement().executeUpdate("update Cdr set callingnumber=concat('223',substr(callingnumber,4)) where callingnumber like '110%'");
        conn.createStatement().executeUpdate("update Cdr set callingnumber=substr(callingnumber,3) where callingnumber like '19%'");
        conn.createStatement().executeUpdate("update Cdr set callingnumber=concat('223',substr(callingnumber,3)) where callingnumber like '18%'");
        conn.createStatement().executeUpdate("update Cdr set callingnumber=concat('223',substr(callingnumber,3)) where callingnumber like '11%'");
        conn.createStatement().executeUpdate("update Cdr set callingnumber=substr(callingnumber,4) where callingnumber like '223%' and length(callingnumber)>11");
        conn.createStatement().executeUpdate("update Cdr set callingnumber=substr(callingnumber,3) where callingnumber like '00%'");

        conn.createStatement().executeUpdate("update Cdr set callednumber=concat('223',substr(callednumber,4)) where callednumber like '110%'");
        conn.createStatement().executeUpdate("update Cdr set callednumber=substr(callednumber,3) where callednumber like '19%'");
        conn.createStatement().executeUpdate("update Cdr set callednumber=concat('223',substr(callednumber,3)) where callednumber like '18%'");
        conn.createStatement().executeUpdate("update Cdr set callednumber=concat('223',substr(callednumber,3)) where callednumber like '11%'");
        conn.createStatement().executeUpdate("update Cdr set callednumber=substr(callednumber,4) where callednumber like '223%' and length(callednumber)>11");
        conn.createStatement().executeUpdate("update Cdr set callednumber=substr(callednumber,3) where callednumber like '00%'");

    }

    public void loadToHdfs(SparkSession spark) {

        Dataset<Row> jdbcDF = spark.read()
                .format("jdbc")
                .option("url", "jdbc:ignite:thin://127.0.0.1")
                .option("dbtable", "Cdr")
                .option("user", "ignite")
                .option("password", "ignite")
                .option("fetchsize","100")
                .load();
        System.out.println("*********************** "+jdbcDF.count()+" *****************************");

        jdbcDF.show();


    }


}
