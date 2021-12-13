package com.nttdata.transfer.model.dto;

import lombok.Data;

@Data
public class TransferDto {
    private String originAccountNumber;
    private String destinationAccountNumber;
    private String transferType;
    private double amount;
}
