package org.ngs.payments.repository;

import org.ngs.payments.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {
    Optional<CartEntity> findByUniqueIdAndDeletedFalse(String uniqueId);
}
