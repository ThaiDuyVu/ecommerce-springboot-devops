package com.project.ecommerce.Request;

import lombok.Data;

@Data
public class VNPayCallbackRequest {

    private String vnpAmount;

    private String vnpBankCode;

    private String vnpBankTranNo;

    private String vnpCardType;

    private String vnpOrderInfo;

    private String vnpPayDate;

    private String vnpResponseCode;

    private String vnpTransactionNo;

    private String vnpTransactionStatus;

    private String vnpTxnRef;

    private String vnpSecureHash;

}