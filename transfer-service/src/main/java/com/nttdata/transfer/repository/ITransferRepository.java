package com.nttdata.transfer.repository;

import com.nttdata.transfer.model.entity.Transfer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ITransferRepository extends ReactiveMongoRepository<Transfer, String> {
}
