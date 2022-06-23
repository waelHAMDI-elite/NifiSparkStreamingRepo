package com.elite.cdr.validator.Asn1Classes;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;

public class MyMapFunction implements MapFunction<Row, String> {
    @Override
    public String call(Row row) throws Exception {
        //System.out.println();
        return row.mkString();
    }
}
