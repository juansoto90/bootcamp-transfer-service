package com.nttdata.transfer.service.impl;

import com.nttdata.transfer.model.entity.Transfer;
import com.nttdata.transfer.repository.ITransferRepository;
import com.nttdata.transfer.service.ITransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements ITransferService {

    private final ITransferRepository repository;

    @Override
    public Mono<Transfer> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Mono<Transfer> save(Transfer transfer) {
        return repository.save(transfer);
    }
}
