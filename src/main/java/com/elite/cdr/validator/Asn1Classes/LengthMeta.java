package com.elite.cdr.validator.Asn1Classes;

public class LengthMeta {
    public int length;
    public int bytesOfLength;

    public LengthMeta() {}

    public LengthMeta(int length, int bytesOfLength) {
        this.length = length;
        this.bytesOfLength = bytesOfLength;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getBytesOfLength() {
        return bytesOfLength;
    }

    public void setBytesOfLength(int bytesOfLength) {
        this.bytesOfLength = bytesOfLength;
    }
}
