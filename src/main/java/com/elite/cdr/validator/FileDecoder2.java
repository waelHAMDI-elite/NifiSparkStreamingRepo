package com.elite.cdr.validator;
import com.elite.cdr.validator.Asn1Classes.*;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class FileDecoder2 implements Serializable {

    public List<Cdr> decode(byte[] rddFile)  throws IOException {

        // cdrsConfig contains the config of the cdrs that will be extracted from the file
        HashMap<Integer, String> cdrsConfig = new HashMap<Integer, String>();
        cdrsConfig.put(0, "MO");
        /*cdrsConfig.put(1, "MT");
        cdrsConfig.put(6, "SMSMO");
        cdrsConfig.put(7, "SMSMT");*/

        // fieldsConfig contains the config of the fields that will be extracted from the file
        HashMap<Integer, List<Field>> fieldsConfig = new HashMap<Integer, List<Field>>();
        //fieldsConfig.put(0,new Field("1","imsi","TBCD"));
        fieldsConfig.put(0, Arrays.asList(
                new Field("1", "imsi", "TBCD"),
                new Field("2", "imei", "TBCD"),
                new Field("4", "callingNumber", "ADDRESS"),
                new Field("5", "calledNumber", "ADDRESS"),
                new Field("10/1", true, Arrays.asList("1"), "mscIncoming", "IA5"),
                new Field("11/1", true, Arrays.asList("1"), "mscOutgoing", "IA5"),
                new Field("23", "answerTime", "OCTET")

        ));
        fieldsConfig.put(1, Arrays.asList(
                new Field("1", "imsi", "TBCD"),
                new Field("2", "imei", "TBCD"),
                new Field("4", "callingNumber", "ADDRESS"),
                new Field("5", "calledNumber", "ADDRESS"),
                new Field("7/1", true, Arrays.asList("1"), "mscIncoming", "IA5"),
                new Field("8/1", true, Arrays.asList("1"), "mscOutgoing", "IA5"),
                new Field("20", "answerTime", "OCTET")
        ));
        fieldsConfig.put(6, Arrays.asList(
                new Field("1", "imsi", "TBCD"),
                new Field("2", "imei", "TBCD"),
                new Field("12", "calledNumber", "ADDRESS")

                ));
        fieldsConfig.put(7, Arrays.asList(
                new Field("2", "imsi", "TBCD"),
                new Field("3", "imei", "TBCD"),
                new Field("4", "calledNumber", "ADDRESS"),
                new Field("201", "callingNumber", "ADDRESS")
                ));

        DecodingClass decodingClass = new DecodingClass();
        Asn1Utils asn1Utils = new Asn1Utils();
        // cdrList contains the list of decoded cdrs in the file based on the cdrs config
        List<Cdr> cdrList = new ArrayList<>();

        ASN1InputStream in = new ASN1InputStream(rddFile);
        int c,j=0,compt=0;
        while ((c = in.read()) != -1){
            //j++;
            TagMeta cdrTag = asn1Utils.readTagNumber(in,c);

            LengthMeta lengthCdr = asn1Utils.readLength(in,c,false);
            //System.out.println("cdr "+j+" tag: "+cdrTag+" length: "+lengthCdr);
            if(cdrsConfig.containsKey(cdrTag.tag)){
                StringBuilder tagTree = new StringBuilder();
                j++;
                Cdr cdr = new Cdr(j);
                cdr.setCallType(cdrTag.tag);
                //System.out.println(j + " **** CDR "+cdrTag.tag+" len "+lengthCdr.length+" ****");
                List<Field> fields = fieldsConfig.get(cdrTag.tag);
                //compt=0;
                decodingClass.cdrDecode(in,fields,lengthCdr.length,cdr);

                cdrList.add(cdr);
                //System.out.println(j+" "+cdr.callType+","+cdr.imsi+","+cdr.imei+","+cdr.mscIncoming+","+cdr.mscOutgoing);
            }
            else
                in.skip(lengthCdr.length);
        }
        in.close();
        return cdrList;
    }


}
