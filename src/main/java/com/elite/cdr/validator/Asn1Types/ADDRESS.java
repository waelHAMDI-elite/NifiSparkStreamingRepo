package com.elite.cdr.validator.Asn1Types;

public class ADDRESS {

    public String decode(byte[] fieldValue) {
        StringBuilder builder = new StringBuilder();
        String value="";
        for (byte b: fieldValue) {
            String str = Integer.toHexString(b%16);
            str.concat(Integer.toHexString(b/16));
            builder.append(str);
        }
        return builder.toString();
    }
}
