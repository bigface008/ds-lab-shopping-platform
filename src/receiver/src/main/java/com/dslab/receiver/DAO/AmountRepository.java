package com.dslab.receiver.DAO;

import com.dslab.receiver.model.AmountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AmountRepository extends JpaRepository<AmountEntity, String> {
    Optional<AmountEntity> findAmountByCurrency(String currency);
}
