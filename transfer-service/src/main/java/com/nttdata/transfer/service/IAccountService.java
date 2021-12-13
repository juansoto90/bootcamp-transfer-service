package com.nttdata.transfer.service;

import com.nttdata.transfer.model.entity.Account;
import reactor.core.publisher.Mono;

public interface IAccountService {
    public Mono<Account> findByAccountNumber(String accountNumber);
    public Mono<Account> updateAmountAccount(Account account);
}
