package com.elite.cdr.validator.Asn1Classes;

import org.bouncycastle.asn1.ASN1InputStream;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class Asn1Utils {

    public TagMeta readTagNumber(InputStream var0, int var1) throws IOException {
        int compt=0;
        int var2 = var1 & 31;
        if (var2 == 31) {
            var2 = 0;
            int var3 = var0.read();
            compt++;
            if (var3 < 31) {
                if (var3 < 0) {
                    throw new EOFException("EOF found inside tag value.");
                }

                throw new IOException("corrupted stream - high tag number < 31 found");
            }

            if ((var3 & 127) == 0) {
                throw new IOException("corrupted stream - invalid high tag number found");
            }

            while((var3 & 128) != 0) {
                if (var2 >>> 24 != 0) {
                    throw new IOException("Tag number more than 31 bits");
                }

                var2 |= var3 & 127;
                var2 <<= 7;
                var3 = var0.read();
                compt++;
                if (var3 < 0) {
                    throw new EOFException("EOF found inside tag value.");
                }
            }

            var2 |= var3 & 127;
        }
        TagMeta meta = new TagMeta();
        meta.setTag(var2);
        meta.setBytesOfTag(compt);
        return meta;
        //return var2;
    }
    // var1 = this.limit (ASN1InputStream) var2=false
    public LengthMeta readLength(InputStream var0, int var1, boolean var2) throws IOException {
        int compt=0;
        LengthMeta meta = new LengthMeta();
        int var3 = var0.read();
        compt++;
        if (0 == var3 >>> 7) {
            meta.setLength(var3);
            meta.setBytesOfLength(compt);
            return meta;
        } else if (128 == var3) {
            meta.setLength(-1);
            meta.setBytesOfLength(compt);
            return meta;
        } else if (var3 < 0) {
            throw new EOFException("EOF found when length expected");
        } else if (255 == var3) {
            throw new IOException("invalid long form definite-length 0xFF");
        } else {
            int var4 = var3 & 127;
            int var5 = 0;
            var3 = 0;

            do {
                int var6 = var0.read();
                compt++;
                if (var6 < 0) {
                    throw new EOFException("EOF found reading length");
                }

                if (var3 >>> 23 != 0) {
                    throw new IOException("long form definite-length more than 31 bits");
                }

                var3 = (var3 << 8) + var6;
                ++var5;
            } while(var5 < var4);

           /* if (var3 >= var1 && !var2) {
                throw new IOException("corrupted stream - out of bounds length found: " + var3 + " >= " + var1);
            } else {
                return var3;
            }*/
            meta.setLength(var3);
            meta.setBytesOfLength(compt);
            return meta;
            //return var3;
        }
    }

    public void skipIndefiniteLength(ASN1InputStream in) throws IOException {
        int c,d;
        while(true) {
            c = in.read();
            d = in.read();
            if (c == 0 && d == 0) {
                break;
            }
        }
    }
}
