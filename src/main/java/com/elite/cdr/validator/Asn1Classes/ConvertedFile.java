package com.elite.cdr.validator.Asn1Classes;

import java.util.List;

public class ConvertedFile implements java.io.Serializable{

    private List<Cdr> cdrs;

    public List<Cdr> getCdrs() {
        return cdrs;
    }

    public void setCdrs(List<Cdr> cdrs) {
        this.cdrs = cdrs;
    }
}
