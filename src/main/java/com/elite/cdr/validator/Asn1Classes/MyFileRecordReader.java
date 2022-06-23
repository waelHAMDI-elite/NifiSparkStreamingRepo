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
import org.apache.hadoop.util.LineReader;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;

import java.io.IOException;
import java.io.InputStream;

public class MyFileRecordReader extends RecordReader<LongWritable, Text> {
    private Path path;
    private InputStream is;
    private FSDataInputStream fsin;
    private ASN1InputStream asnin;
    private ASN1Primitive obj;

    private long start, end, position = 0;
    private LineReader in;

    private LongWritable currentKey = new LongWritable();
    private Text currentValue = new Text();
    private boolean isProcessed = false;

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {

        currentKey.set(position + 1);
        currentValue.clear();
        int newSize = 0;
        long length = Integer.MAX_VALUE;

        while (position < end) {
            //System.out.println("while");
            //fsin.seek(postition);
            int cmp = 0;
            int i;

            fsin.seek(position);
            //System.out.println("fsin read :"+(fsin.read()!=50 )+"//// cmp + pos :"+ (cmp+postition>end-1));
            while ((i = fsin.readByte()) != -1 && fsin.getPos() < end) {
                cmp++;
                if (cmp == 2) {
                    length = i + fsin.getPos();
                }
                if (fsin.getPos() <= length) {

                    //System.out.println("byte: " + i+ "----------- pos  :"+fsin.getPos()+"----------length   :"+length);
                    byte[] b = {(byte) i};
                    currentValue.append(b, 0, 1);
                } else {
                    //System.out.println(i);
                    position = length;
                    return true;
                }
            }
            byte[] b = {(byte) i};
            currentValue.append(b, 0, 1);
            newSize = (int) length;
            if (newSize == 0) {
                break;
            }
            position = length;
            // System.out.println("new pos: "+postition+"+++++++++++ length : "+length);
        }
        //System.out.println("final positin ----- "+postition);
        if (position == end) ; //System.out.println("there is no spillage");
        else {
            int i;
            while ((i = fsin.readByte()) != -1 && fsin.getPos() == length) {
                // System.out.println("byte: " + i+ "----------- pos  :"+fsin.getPos()+"----------length   :"+length);
                byte[] b = {(byte) i};
                currentValue.append(b, 0, 1);
            }
        }
        if (newSize == 0) {
            currentKey = null;
            currentValue = null;
            return false;

        } else {
            return true;
        }
    }

    @Override
    public LongWritable getCurrentKey() throws IOException, InterruptedException {
        return currentKey;
    }

    @Override
    public Text getCurrentValue() throws IOException, InterruptedException {
        return currentValue;
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
        if (start == end) {
            return 0.0f;
        } else {
            return Math.min(1.0f, (position - start) / (float) (end - start));
        }
    }

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();
        FileSplit isplit = (FileSplit) split;

        start = isplit.getStart();
        end = start + isplit.getLength();

        //System.out.println("start:"+start+"************ end: "+end);
        path = ((FileSplit) split).getPath();
        FileSystem fs = path.getFileSystem(conf);
        fsin = fs.open(path);
        //System.out.println("pos :     ---------"+fsin.readByte());

        //boolean skipFirstRecord = false;
        if (start != 0) {
            //System.out.println("Skip first recored");

            int firstByte = fsin.readByte();
            int tempSize = fsin.readByte();

            while (isRecordStart(48) == false) System.out.println("not record start");
            start = fsin.getPos();

        }
        //System.out.println("Dont Skip first recored");
        position = start;
    }

    @Override
    public void close() throws IOException {
        // asnin.close();
        //is.close();
        if (fsin != null) fsin.close();
    }


    public boolean isRecordStart(int stratingByte) throws IOException {

        int firstByte = fsin.readByte();
        int tempSize = fsin.readByte();
        long position = fsin.getPos();


        if (firstByte == stratingByte) {
            fsin.seek(tempSize + fsin.getPos());
            if (fsin.readByte() != stratingByte) {
                fsin.seek(fsin.getPos() - 1);
                start = fsin.getPos();
                return true;
            }

        } else {
            fsin.seek(position + 1);
            int i;
            while ((i = fsin.readByte()) != -1) {
                if (i == stratingByte) {
                    fsin.seek(fsin.getPos() - 1);
                    break;
                }
            }
            return false;

        }
        return false;
    }
}
