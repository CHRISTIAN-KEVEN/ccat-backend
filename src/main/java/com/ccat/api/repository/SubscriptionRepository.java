package com.ccat.api.repository;

import com.ccat.api.model.entity.Subscription;
import com.ccat.api.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserLgIdOrderByDtCreatedDesc(Long userId);
    Optional<Subscription> findByStrStripeSubscriptionId(String stripeSubscriptionId);
    Optional<Subscription> findTopByUserLgIdAndEmStatusOrderByDtStartDesc(Long userId, SubscriptionStatus status);
    boolean existsByUserLgIdAndEmStatus(Long userId, SubscriptionStatus status);
}
