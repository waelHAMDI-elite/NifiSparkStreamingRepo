package com.elite.cdr.validator.Asn1Classes;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RawFileRecordReader extends RecordReader<LongWritable ,Text >  {
    private Path path;
    private InputStream is;
    private FSDataInputStream fsin;
    private ASN1InputStream asnin;
    private ASN1Primitive obj;

    private  LongWritable currentKey;
    private Text currentValue;
    private boolean isProcessed = false;


    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {

        if (isProcessed) return false;



        int recordCounter = 0;
        while ((obj = asnin.readObject()) != null) {

            CallDetailRecord thisCdr = new CallDetailRecord((ASN1Sequence) obj);
            recordCounter++;

            System.out.println("CallDetailRecord "+thisCdr.getRecordNumber()+" Calling "+thisCdr.getCallingNumber()
                    +" Called "+thisCdr.getCalledNumber()+ " Start Date-Time "+thisCdr.getStartDate()+"-"
                    +thisCdr.getStartTime()+" duration "+thisCdr.getDuration()
            );

        }
        isProcessed = true;
        currentKey = new LongWritable( recordCounter);
        //Return number of records
        currentValue = new Text(String.valueOf(recordCounter));

        return true;
    }

    @Override
    public LongWritable getCurrentKey() throws IOException, InterruptedException {
        return currentKey;
    }

    @Override
    public  Text getCurrentValue() throws IOException, InterruptedException {
        return currentValue;
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
        return isProcessed ? 1 : 0;
    }

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();
        path = ((FileSplit) split).getPath();
        FileSystem fs = path.getFileSystem(conf);
        FSDataInputStream fsin = fs.open(path);
        //tessssssssssssssst
        int i;
        while((i=fsin.read())!=-1)System.out.println(i +"---- pos :"+fsin.getPos());
        is=decompressStream(fsin);

        asnin = new ASN1InputStream(is);
    }

    @Override
    public void close() throws IOException {
        asnin.close();
        is.close();
        if (fsin!=null) fsin.close();
    }

    public static InputStream decompressStream(InputStream input) {
        InputStream returnStream=null;
        org.apache.commons.compress.compressors.CompressorInputStream cis = null;
        BufferedInputStream bis=null;
        try {
            bis = new BufferedInputStream(input);
            bis.mark(1024);   //Mark stream to reset if uncompressed data
            cis = new org.apache.commons.compress.compressors.CompressorStreamFactory().createCompressorInputStream(bis);
            returnStream = cis;
        } catch (org.apache.commons.compress.compressors.CompressorException ce) { //CompressorStreamFactory throws CompressorException for uncompressed files
            try {
                bis.reset();
            } catch (IOException ioe) {
                String errmessageIOE="IO Exception ( "+ioe.getClass().getName()+" ) : "+ioe.getMessage();
                System.out.println(errmessageIOE);
            }
            returnStream = bis;
        } catch (Exception e) {
            String errmessage="Exception ( "+e.getClass().getName()+" ) : "+e.getMessage();
            System.out.println(errmessage);
        }
        return returnStream;
    }
}
