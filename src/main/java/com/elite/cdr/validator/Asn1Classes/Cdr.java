package com.elite.cdr.validator.Asn1Classes;

import java.io.Serializable;

public class Cdr implements Serializable {
    public String callType;
    public String imsi;
    public String imei;
    public String msisdn;
    public String callingNumber;
    public String calledNumber;
    public String mscIncoming;
    public String mscOutgoing;
    public String location;
    public String answerTime;

    public int recordNumber;

    public Cdr() {
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(int tag) {
        switch (tag) {
            case 0 :
                this.callType = "MO";
                break;
            case 1 :
                this.callType = "MT";
                break;
            case 6 :
                this.callType = "SMSMO";
                break;
            case 7 :
                this.callType = "SMSMT";
                break;
        }
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(String callingNumber) {
        this.callingNumber = callingNumber;
    }

    public String getCalledNumber() {
        return calledNumber;
    }

    public void setCalledNumber(String calledNumber) {
        this.calledNumber = calledNumber;
    }

    public String getMscIncoming() {
        return mscIncoming;
    }

    public void setMscIncoming(String mscIncoming) {
        this.mscIncoming = mscIncoming;
    }

    public String getMscOutgoing() {
        return mscOutgoing;
    }

    public void setMscOutgoing(String mscOutgoing) {
        this.mscOutgoing = mscOutgoing;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(String answerTime) {
        this.answerTime = answerTime;
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(int recordNumber) {
        this.recordNumber = recordNumber;
    }

    public void put(String decoded, Field fieldConfig) {
        switch (fieldConfig.name){
            case "imsi":
                setImsi(decoded);
                break;
            case "imei":
                setImei(decoded);
                break;
            case "answerTime":
                setAnswerTime(decoded);
                break;
            case "callingNumber":
                setCallingNumber(decoded);
                break;
            case "calledNumber":
                setCalledNumber(decoded);
                break;
            case "mscIncoming":
                setMscIncoming(decoded);
                break;
            case "mscOutgoing":
                setMscOutgoing(decoded);
                break;
            case "recordNumber":
                setRecordNumber(Integer.parseInt(decoded));
                break;
        }
    }
}
