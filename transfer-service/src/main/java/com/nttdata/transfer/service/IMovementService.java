package com.nttdata.transfer.service;

import com.nttdata.transfer.model.entity.Movement;
import reactor.core.publisher.Mono;

public interface IMovementService {
    public Mono<Movement> save(Movement movement);
}
