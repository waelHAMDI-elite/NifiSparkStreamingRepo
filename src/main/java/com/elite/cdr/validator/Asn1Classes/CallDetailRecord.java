package com.elite.cdr.validator.Asn1Classes;

import com.elite.cdr.validator.Asn1Types.ADDRESS;
import org.bouncycastle.asn1.*;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;


public class CallDetailRecord extends ASN1Object implements Serializable {

    ASN1Sequence callDetailRecord;

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public String toString() {
        return "CallDetailRecord{" +
                "callDetailRecord=" + callDetailRecord +
                ", cdr=" + cdr +
                ", recordNumber=" + recordNumber +
                ", callingNumber='" + callingNumber + '\'' +
                ", calledNumber='" + calledNumber + '\'' +
                ", startDate='" + startDate + '\'' +
                ", startTime='" + startTime + '\'' +
                ", duration=" + duration +
                '}';
    }

    ASN1Sequence cdr;

    int recordNumber;      //APPLICATION  2
    String callingNumber;  //APPLICATION  8
    String calledNumber;   //APPLICATION  9
    String startDate;      //APPLICATION 16
    String startTime;      //APPLICATION 18
    int duration;          //APPLICATION 19

    public CallDetailRecord(int recordNumber, String callingNumber, String calledNumber, String startDate, String startTime, int duration) {
        this.recordNumber = recordNumber;
        this.callingNumber = callingNumber;
        this.calledNumber = calledNumber;
        this.startDate = startDate;
        this.startTime = startTime;
        this.duration = duration;
    }

    public CallDetailRecord(ASN1Sequence inSeq) throws UnsupportedEncodingException {
        cdr = inSeq;
        ADDRESS address = new ADDRESS();
        for (Enumeration<ASN1Encodable> en = cdr.getObjects(); en.hasMoreElements(); ) {
            ASN1Encodable em = en.nextElement();
            ASN1Primitive emp = em.toASN1Primitive();
            DERApplicationSpecific emt = (DERApplicationSpecific) emp;

            //System.out.println("emt.getApplicationTag(): "+emt.getApplicationTag());

            switch (emt.getApplicationTag()) {
                case 2:
                    recordNumber = emt.getContents()[0];
                    break;
                case 8:
                    //callingNumber = new String(emt.getContents(), "UTF-8");
                    /***********/
                        callingNumber = address.decode(emt.getContents());
                    /***********/
                    break;
                case 9:
                    //calledNumber = new String(emt.getContents(), "UTF-8");
                    /***********/
                    calledNumber = address.decode(emt.getContents());
                    /***********/
                    break;
                case 16:
                    startDate = new String(emt.getContents(), "UTF-8");
                    break;
                case 18:
                    startTime = new String(emt.getContents(), "UTF-8");
                    break;
                case 19:
                    duration = emt.getContents()[0];
                    break;
                default:
                    //Unknown application number. In production would either log or error.
                    break;
            }
        }

    }

    @Override
    public ASN1Primitive toASN1Primitive() {
        return callDetailRecord;
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public String getCallingNumber() {
        return callingNumber;
    }

    public String getCalledNumber() {
        return calledNumber;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }


}
