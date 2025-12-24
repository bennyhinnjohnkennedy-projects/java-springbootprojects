package it.sky.keplero.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
public class TransactionalService {

    @Transactional
    public <T> T executeTransaction(Supplier<T> action) {
        return action.get();
    }

}
