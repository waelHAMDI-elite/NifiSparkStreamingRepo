package com.elite.cdr.validator.Asn1Classes;

public class TagMeta {
    public int tag;
    public int bytesOfTag;

    public TagMeta() {}

    public TagMeta(int tag, int bytesOfTag) {
        this.tag = tag;
        this.bytesOfTag = bytesOfTag;
    }

    public int getTag() {
        return tag;
    }

    public int getBytesOfTag() {
        return bytesOfTag;
    }



    public void setTag(int tag) {
        this.tag = tag;
    }

    public void setBytesOfTag(int bytesOfTag) {
        this.bytesOfTag = bytesOfTag;
    }
}
