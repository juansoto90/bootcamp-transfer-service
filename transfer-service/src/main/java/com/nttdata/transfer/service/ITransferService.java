package com.nttdata.transfer.service;

import com.nttdata.transfer.model.entity.Transfer;
import com.nttdata.transfer.repository.ITransferRepository;
import reactor.core.publisher.Mono;

public interface ITransferService {
    public Mono<Transfer> findById(String id);
    public Mono<Transfer> save(Transfer transfer);
}
