package com.elite.cdr.validator;
import com.elite.cdr.validator.Asn1Classes.Cdr;
import com.elite.cdr.validator.Asn1Classes.DecodingClass;
import com.elite.cdr.validator.Asn1Classes.Field;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class FileDecoder implements Serializable {

    public List<Cdr> decode(byte[] rddFile)  throws IOException {

        // cdrsConfig contains the config of the cdrs that will be extracted from the file
        HashMap<Integer, String> cdrsConfig = new HashMap<Integer, String>();
        cdrsConfig.put(0, "MO");
        cdrsConfig.put(1, "MT");
        cdrsConfig.put(6, "SMSMO");
        cdrsConfig.put(7, "SMSMT");

        // fieldsConfig contains the config of the fields that will be extracted from the file
        HashMap<Integer, List<Field>> fieldsConfig = new HashMap<Integer, List<Field>>();
        //fieldsConfig.put(0,new Field("1","imsi","TBCD"));
        fieldsConfig.put(0, Arrays.asList(
                new Field("1", "imsi", "TBCD"),
                new Field("2", "imei", "TBCD"),
                new Field("4", "callingNumber", "ADDRESS"),
                new Field("5", "calledNumber", "ADDRESS"),
                new Field("10", true, Arrays.asList("1"), "mscIncoming", "IA5"),
                new Field("11", true, Arrays.asList("1"), "mscOutgoing", "IA5"),
                new Field("23", "answerTime", "ANSWERTIME")

        ));
        fieldsConfig.put(1, Arrays.asList(
                new Field("1", "imsi", "TBCD"),
                new Field("2", "imei", "TBCD"),
                new Field("4", "callingNumber", "ADDRESS"),
                new Field("5", "calledNumber", "ADDRESS"),
                new Field("7", true, Arrays.asList("1"), "mscIncoming", "IA5"),
                new Field("8", true, Arrays.asList("1"), "mscOutgoing", "IA5"),
                new Field("20", "answerTime", "ANSWERTIME")
        ));
        fieldsConfig.put(6, Arrays.asList(
                new Field("1", "imsi", "TBCD"),
                new Field("2", "imei", "TBCD"),
                new Field("12", "calledNumber", "ADDRESS")

        ));
        fieldsConfig.put(7, Arrays.asList(
                new Field("2", "imsi", "TBCD"),
                new Field("3", "imei", "TBCD"),
                new Field("4", "calledNumber", "ADDRESS")
                //new Field("201", "callingNumber", "ADDRESS")
        ));

        DecodingClass DecodingClass = new DecodingClass();

        // cdrList contains the list of decoded cdrs in the file based on the cdrs config
        List<Cdr> cdrList = new ArrayList<>();

        ASN1InputStream in = new ASN1InputStream(rddFile);

        while (in.available() > 0) {
            //ASN1Primitive primitive = in.readObject();
            DLTaggedObject dlobj = (DLTaggedObject) in.readObject();
            //if(dlobj.getTagNo()==0){
            int cdrTag = dlobj.getTagNo();

            // Test tag of current cdr with our cdrs config
            if (cdrsConfig.containsKey(cdrTag)) {
                // Take the list of fields of this cdr
                List<Field> fields = fieldsConfig.get(cdrTag);

                ASN1Primitive primitive = dlobj.getObject();
                DLSequence sequence = (DLSequence) primitive;
                Cdr cdr = new Cdr();
                //System.out.println(cdrTag);
                cdr.setCallType(cdrTag);

                // Looping through the cdr
                for (Enumeration<ASN1Encodable> en = sequence.getObjects(); en.hasMoreElements(); ) {
                    ASN1Encodable em = en.nextElement();
                    DLTaggedObject dl = (DLTaggedObject) em;
                    String fieldTag = String.valueOf(dl.getTagNo());

                    // looking for the current field in our list of fields
                    Field field = fields.stream().filter(f -> fieldTag.equals(f.getTag())).findFirst().orElse(null);

                    if(field != null){
                        if (field.constructed) {
                            //findOriginField(dl,fieldConfig.restOfTag);
                            for (int i = 0; i < field.restOfTag.size(); i++) {
                                ASN1Primitive prmtv = dl.getObject();
                                DLTaggedObject dl0 = (DLTaggedObject) prmtv;
                                int rtag = Integer.parseInt(field.restOfTag.get(i));
                                //System.out.println("sub "+rtag+" tag f " +dl0.getTagNo());
                                if (dl0.getTagNo() == rtag) {
                                    //System.out.println("sub "+rtag+" tag f " +dl0.getTagNo());
                                    String originField = dl0.getBaseObject().toString();
                                    String decoded = DecodingClass.decode(originField, field);
                                    cdr.put(decoded, field);
                                }
                            }
                        } else {
                            String originField = dl.getBaseObject().toString();
                            String decoded = DecodingClass.decode(originField, field);
                            cdr.put(decoded, field);
                        }
                    }
                }
                cdrList.add(cdr);
                // j++;
                // System.out.println(cdr.callType+","+cdr.imsi+","+cdr.imei+","+cdr.mscIncoming+","+cdr.mscOutgoing);
            }
        }
        in.close();
        return cdrList;
    }
}
