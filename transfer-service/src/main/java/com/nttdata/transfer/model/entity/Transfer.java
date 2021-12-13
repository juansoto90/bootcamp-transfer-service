package com.nttdata.transfer.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("transfer")
public class Transfer {
    @Id
    private String id;
    private String transferType;
    private boolean input;
    private String originAccountNumber;
    private String destinationAccountNumber;
    private String operationNumber;
    private double amount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acquisitionDate = LocalDateTime.now();
    private String status;

    private Account originAccount;
    private Account destinationAccount;
}
