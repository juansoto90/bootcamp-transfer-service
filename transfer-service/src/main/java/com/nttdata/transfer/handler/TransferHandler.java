package com.nttdata.transfer.handler;

import com.nttdata.transfer.exception.TransferException;
import com.nttdata.transfer.exception.messageException;
import com.nttdata.transfer.model.dto.TransferDto;
import com.nttdata.transfer.model.entity.Account;
import com.nttdata.transfer.model.entity.Movement;
import com.nttdata.transfer.model.entity.Transfer;
import com.nttdata.transfer.service.IAccountService;
import com.nttdata.transfer.service.IMovementService;
import com.nttdata.transfer.service.ITransferService;
import com.nttdata.transfer.util.Generator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TransferHandler {

    private final ITransferService service;
    private final IAccountService iAccountService;
    private final IMovementService iMovementService;

    public Mono<ServerResponse> findById(ServerRequest request){
        String id = request.pathVariable("id");
        return service.findById(id)
                .flatMap(t -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(t)
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> accountsBetweenTransfer(ServerRequest request){
        Mono<TransferDto> transferDtoMono = request.bodyToMono(TransferDto.class);
        Transfer transfer = new Transfer();
        return transferDtoMono
                .flatMap(dto -> {
                    if (!(dto.getTransferType().equals("OWN_ACCOUNTS") || dto.getTransferType().equals("THIRD_PARTY_ACCOUNTS"))){
                        return Mono.error(
                                new WebClientResponseException(404,
                                        messageException.incorrectTransferType(),
                                        null,null,null)
                        );
                    }
                    return Mono.just(dto);
                })
                .flatMap(dto -> iAccountService.findByAccountNumber(dto.getOriginAccountNumber())
                                                .map(a -> {
                                                    transfer.setOriginAccount(a);
                                                    transfer.setOriginAccountNumber(dto.getOriginAccountNumber());
                                                    return dto;
                                                })
                                                .onErrorResume(error -> {
                                                    WebClientResponseException errorResponse = (WebClientResponseException) error;
                                                    if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                                                        return Mono.error(
                                                                new WebClientResponseException(404,
                                                                        messageException.originAccountNotFound(),
                                                                        null,null,null)
                                                        );
                                                    }
                                                    return Mono.error(errorResponse);
                                                })
                )
                .flatMap(dto -> {
                    if (dto.getAmount() > transfer.getOriginAccount().getBalance()){
                        return Mono.error(
                                new WebClientResponseException(404,
                                        messageException.insufficientBalance(),
                                        null,null,null)
                        );
                    }
                    return Mono.just(dto);
                })
                .flatMap(dto -> iAccountService.findByAccountNumber(dto.getDestinationAccountNumber())
                        .map(a -> {
                            transfer.setDestinationAccount(a);
                            transfer.setDestinationAccountNumber(dto.getDestinationAccountNumber());
                            return dto;
                        })
                        .onErrorResume(error -> {
                            WebClientResponseException errorResponse = (WebClientResponseException) error;
                            if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                                return Mono.error(
                                        new WebClientResponseException(404,
                                                messageException.destinationAccountNotFound(),
                                                null,null,null)
                                );
                            }
                            return Mono.error(errorResponse);
                        })
                )
                .flatMap(dto -> {
                    if (dto.getTransferType().equals("OWN_ACCOUNTS")){
                        if (!transfer.getOriginAccount().getCustomer().getDocumentNumber().equals(transfer.getDestinationAccount().getCustomer().getDocumentNumber())){
                            return Mono.error(
                                    new WebClientResponseException(404,
                                            messageException.ownAccountError(),
                                            null,null,null)
                            );
                        }
                    }
                    else //THIRD_PARTY_ACCOUNTS
                        if (transfer.getOriginAccount().getCustomer().getDocumentNumber().equals(transfer.getDestinationAccount().getCustomer().getDocumentNumber())){
                            return Mono.error(
                                    new WebClientResponseException(404,
                                            messageException.thirdPartyAccountError(),
                                            null,null,null)
                            );
                        }
                    transfer.setTransferType(dto.getTransferType());
                    transfer.setAmount(dto.getAmount());
                    transfer.setStatus("PROCESSED");
                    return Mono.just(transfer);
                })
                .flatMap(t -> {
                    double balanceI = t.getAmount() + t.getDestinationAccount().getBalance();
                    Account accInput = new Account();
                    accInput.setAccountNumber(t.getDestinationAccountNumber());
                    accInput.setBalance(balanceI);
                    return iAccountService.updateAmountAccount(accInput)
                            .flatMap(ai -> {
                                double balanceO = t.getOriginAccount().getBalance() - t.getAmount();
                                Account accOutput = new Account();
                                accOutput.setAccountNumber(t.getOriginAccountNumber());
                                accOutput.setBalance(balanceO);
                                return iAccountService.updateAmountAccount(accOutput)
                                        .flatMap(ao -> {
                                            Transfer transferInput = new Transfer();
                                            transferInput.setTransferType(t.getTransferType());
                                            transferInput.setInput(true);
                                            transferInput.setOriginAccountNumber(t.getOriginAccountNumber());
                                            transferInput.setDestinationAccountNumber(t.getDestinationAccountNumber());
                                            transferInput.setOperationNumber(Generator.generateOperationNumber());
                                            transferInput.setAmount(t.getAmount());
                                            transferInput.setStatus(t.getStatus());
                                            transferInput.setOriginAccount(t.getOriginAccount());
                                            transferInput.setDestinationAccount(t.getDestinationAccount());
                                            return service.save(transferInput)
                                                    .flatMap(ti -> {
                                                        Transfer transferOutput = new Transfer();
                                                        transferOutput.setTransferType(t.getTransferType());
                                                        transferOutput.setInput(false);
                                                        transferOutput.setOriginAccountNumber(t.getOriginAccountNumber());
                                                        transferOutput.setDestinationAccountNumber(t.getDestinationAccountNumber());
                                                        transferOutput.setOperationNumber(Generator.generateOperationNumber());
                                                        transferOutput.setAmount(t.getAmount()*-1);
                                                        transferOutput.setStatus(t.getStatus());
                                                        transferOutput.setOriginAccount(t.getOriginAccount());
                                                        transferOutput.setDestinationAccount(t.getDestinationAccount());
                                                        return service.save(transferOutput)
                                                                .flatMap(to -> {
                                                                    Movement movementInput = new Movement();
                                                                    movementInput.setOperationNumber(ti.getOperationNumber());
                                                                    movementInput.setAccountNumber(t.getDestinationAccountNumber());
                                                                    movementInput.setCardNumber("");
                                                                    movementInput.setMovementType("TRANSFER");
                                                                    movementInput.setAccountType(t.getDestinationAccount().getAccountType());
                                                                    movementInput.setCardType("");
                                                                    movementInput.setDocumentNumber(t.getDestinationAccount().getCustomer().getDocumentNumber());
                                                                    movementInput.setAmount(t.getAmount());
                                                                    movementInput.setConcept(t.getTransferType().equals("OWN_ACCOUNTS") ? "TRANSFER OWN ACCOUNTS" : "TRANSFER THIRD PARTY ACCOUNT");
                                                                    movementInput.setStatus("PROCESSED");
                                                                    return iMovementService.save(movementInput)
                                                                            .flatMap(mi -> {
                                                                                Movement movementOuput = new Movement();
                                                                                movementOuput.setOperationNumber(to.getOperationNumber());
                                                                                movementOuput.setAccountNumber(t.getOriginAccountNumber());
                                                                                movementOuput.setCardNumber("");
                                                                                movementOuput.setMovementType("TRANSFER");
                                                                                movementOuput.setAccountType(t.getOriginAccount().getAccountType());
                                                                                movementOuput.setCardType("");
                                                                                movementOuput.setDocumentNumber(t.getOriginAccount().getCustomer().getDocumentNumber());
                                                                                movementOuput.setAmount(t.getAmount()*-1);
                                                                                movementOuput.setConcept(t.getTransferType().equals("OWN_ACCOUNTS") ? "TRANSFER OWN ACCOUNTS" : "TRANSFER THIRD PARTY ACCOUNT");
                                                                                movementOuput.setStatus("PROCESSED");
                                                                                return iMovementService.save(movementOuput)
                                                                                        .flatMap(mo ->  Flux.just(ai, ao, ti, to, mi, mo).collectList());
                                                                            });
                                                                });
                                                    });
                                        });
                            });
                })
                .flatMap(t -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(t)
                )
                .onErrorResume(TransferException::errorHandler);
    }

}
