package com.app.siy.model;

import java.io.Serializable;

/**
 * Created by Manish-Pc on 12/12/2017.
 */

public class PaymentHistoryData implements Serializable {

    private String imageUrl;
    private String receiverName;
    private String receiverContact;
    private String receivingDate;
    private String receivingAmount;




    public PaymentHistoryData(){}

    public PaymentHistoryData(String receiverName, String receiverContact, String receivingDate, String receivingAmount){
        this.receiverName = receiverName;
        this.receiverContact = receiverContact;
        this.receivingDate = receivingDate;
        this.receivingAmount = receivingAmount;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverContact() {
        return receiverContact;
    }

    public void setReceiverContact(String receiverContact) {
        this.receiverContact = receiverContact;
    }

    public String getReceivingDate() {
        return receivingDate;
    }

    public void setReceivingDate(String receivingDate) {
        this.receivingDate = receivingDate;
    }

    public String getReceivingAmount() {
        return receivingAmount;
    }

    public void setReceivingAmount(String receivingAmount) {
        this.receivingAmount = receivingAmount;
    }
}
