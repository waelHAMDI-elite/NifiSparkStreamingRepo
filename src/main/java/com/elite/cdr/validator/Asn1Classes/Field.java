package com.elite.cdr.validator.Asn1Classes;

import java.util.List;

public class Field {
    public String tag;
    public boolean constructed;
    public List<String> restOfTag;
    public String name;
    public String binaryType;
    public String applicable;
    public String columnType;

    public Field(String tag, String name, String binaryType) {
        this.tag = tag;
        this.constructed = false;
        this.name = name;
        this.binaryType = binaryType;
    }
    public Field(String tag, boolean constructed, List<String> restOfTag, String name, String binaryType) {
        this.tag = tag;
        this.constructed = constructed;
        this.restOfTag = restOfTag;
        this.name = name;
        this.binaryType = binaryType;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isConstructed() {
        return constructed;
    }

    public void setConstructed(boolean constructed) {
        this.constructed = constructed;
    }

    public List<String> getRestOfTag() {
        return restOfTag;
    }

    public void setRestOfTag(List<String> restOfTag) {
        this.restOfTag = restOfTag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBinaryType() {
        return binaryType;
    }

    public void setBinaryType(String binaryType) {
        this.binaryType = binaryType;
    }

    public String getApplicable() {
        return applicable;
    }

    public void setApplicable(String applicable) {
        this.applicable = applicable;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }
}
