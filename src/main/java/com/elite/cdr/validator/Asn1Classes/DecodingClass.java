package com.elite.cdr.validator.Asn1Classes;

import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class DecodingClass {
    public DecodingClass() {
    }

    public String TBCDString(String field){
        StringBuilder sb = new StringBuilder();
        for(int i=1;i<field.length()-1;i=i+2) {
            sb.append(field.charAt(i + 1));
            if(field.charAt(i) != 'f')
                sb.append(field.charAt(i));
        }
        return sb.toString();
    }

    public String IA5String(String field){
        StringBuilder sb0 = new StringBuilder();
        for(int i=1;i<field.length()-1;i+=2) {
            StringBuilder sb1 = new StringBuilder();
            sb1.append(Character.toString(field.charAt(i)));
            sb1.append(Character.toString(field.charAt(i+1)));
            int s = Integer.parseInt(sb1.toString(),16);
            char ch = (char)s;
            sb0.append(ch);
        }
        return sb0.toString();
    }

    public String AddressString(String field){
        StringBuilder sb = new StringBuilder();
        int d,e;
        for(int i=1;i<field.length()-1;i+=2) {
            StringBuilder sb1 = new StringBuilder();
            String str1 = Character.toString(field.charAt(i + 1));
            //sb1.append(Character.toString(field.charAt(i + 1)));
            d = Integer.parseInt(str1,16);
            sb.append(d);
            if(field.charAt(i) != 'f') {
                StringBuilder sb2 = new StringBuilder();
                str1 = Character.toString(field.charAt(i));
                e = Integer.parseInt(str1,16);
                sb.append(e);
            }

        }
        return sb.toString();
    }

    private String OctetString(String field) {
       /* StringBuilder sb0 = new StringBuilder();
        for(int i=1; i<field.length()-1; i++) {
            StringBuilder sb1 = new StringBuilder();

            sb0.append(field.charAt(i));
        }*/
        return field;
    }

    private String AnswertimeString(String field) {
       /* StringBuilder sb0 = new StringBuilder();
        for(int i=1; i<field.length()-1; i++) {
            StringBuilder sb1 = new StringBuilder();

            sb0.append(field.charAt(i));
        }*/
        return field.substring(1,field.length()-6);
    }

    public String decode(String originField,Field fieldConfig) {
        String decoded=null;
        switch (fieldConfig.binaryType){
            case "TBCD":
                decoded = TBCDString(originField);
                break;
            case "IA5":
                decoded = IA5String(originField);
                break;
            case "OCTET":
                decoded = originField; //OctetString(originField);
                break;
            case "ADDRESS":
                decoded = AddressString(originField);
                break;
            case "ANSWERTIME":
                    decoded = AnswertimeString(originField);
                break;


        }
        //System.out.println("decoded "+decoded);
        return decoded;
    }

    public void constructedFieldDecoding(int i, Cdr cdr, Field field, DLTaggedObject dl) {
        i++;
        ASN1Primitive prmtv = dl.getObject();
        DLSequence sequence = (DLSequence) prmtv;
        for (Enumeration<ASN1Encodable> en = sequence.getObjects(); en.hasMoreElements(); ) {
            ASN1Encodable em = en.nextElement();
            if(em instanceof DLSequence)
                constructedFieldDecoding(i,cdr,field,(DLTaggedObject)em);
            else {
                DLTaggedObject dl1 = (DLTaggedObject) em;
                String fieldTag = String.valueOf(dl.getTagNo());
            }
        }
          /*  List<Integer> restOfTag = field.getRestOfTag();
            int rtag = restOfTag.get(i);
            if (dl0.getTagNo() == rtag ) {
                i++;
                if(i==restOfTag.size()){
                    String originField = dl0.getBaseObject().toString();
                    String decoded = decode(originField, field);
                    cdr.put(decoded, field);
                }else {
                    constructedFieldDecoding(i,cdr,field,dl);
                }
            }*/
    }

    public void cdrDecode(ASN1InputStream in, List<Field> fields, int lengthCdr, Cdr cdr) throws IOException {
        int compt=0;
        while(compt<lengthCdr) {
            StringBuilder tagTree = new StringBuilder();
            List<Integer> tags = new ArrayList<>();
            int level=0;
            Previous previous = new Previous();
            previous.setPrevious(false);
            int k = fieldDecode(in,tags,fields,cdr,previous, level);
            compt = compt + k;
            //System.out.println("compt after field "+ k);
        }
    }

    private int fieldDecode(ASN1InputStream in, List<Integer> tags, List<Field> fields, Cdr cdr, Previous previous,int level) throws IOException {
        Asn1Utils asn1Utils = new Asn1Utils();
        int compt=0;
        level++;
        //System.out.println("level : "+level);
        int c = in.read();
        int d = c;
        compt++;
        boolean constructed = d % 64 > 32 ? true : false;
        //System.out.println("constructed : "+constructed);

        TagMeta tagMeta = asn1Utils.readTagNumber(in, c);
        compt = compt + tagMeta.bytesOfTag;

        LengthMeta lengthMeta = asn1Utils.readLength(in, 0, false);
        compt = compt + lengthMeta.bytesOfLength;
        //System.out.println("len "+ lengthMeta.length);

        int fieldTag = tagMeta.tag;
        int fieldLength = lengthMeta.length;

        //System.out.println("previous "+ previous);
        tags.add(fieldTag);
       /* tagTree.append(fieldTag);
        tagTree.append("/");*/


        if(constructed){
            //in.skip(fieldLength);compt = compt + fieldLength;
            previous.setPrevious(false);
            int j=0;
            while(j<fieldLength) {
                int k = fieldDecode(in, tags, fields, cdr, previous,level);
                //System.out.println("compt after decode : "+k);
                j=j+k;
                compt = compt + k;
            }
        }else {  // primitive

            if(previous.isPrevious()) { // previous field : primitive
                int len = tags.size();
                tags.set(len-2,tags.get(len-1));
                tags.remove(len-1);
            }
            //System.out.println("previous : "+previous.isPrevious());
            //System.out.println("tagTree : "+tags.toString());
            previous.setPrevious(true);
            compt = compt + fieldLength;
            StringBuilder etiq = new StringBuilder();
            for (int i = 0; i < tags.size(); i++) {
                if(i>0)
                    etiq.append("/");
                etiq.append(tags.get(i));
            }
            //String finalEtiq = etiq;
            //System.out.println("etiq "+ etiq);
            Field field = fields.stream().filter(f -> etiq.toString().equals(f.getTag())).findFirst().orElse(null);
            if (field != null) {
                //System.out.println("tag "+ field.tag+"name "+field.name);
               /* if (field.constructed) {
                    in.skip(fieldLength);
                } else {*/
                StringBuilder sb = new StringBuilder();
                int k = 0;
                while (k < fieldLength) {
                    // to read bytes from the binary file as hexadecimal values in 2 digits example '61','00','FF',...
                    sb.append(Integer.toHexString((in.read() & 0xff)+256).substring(1));
                    k++;
                }
                String decoded = decode(sb.toString(), field);
                cdr.put(decoded, field);
                //}
            } else {
                in.skip(fieldLength);
            }

        }
        return compt;

    }

}
