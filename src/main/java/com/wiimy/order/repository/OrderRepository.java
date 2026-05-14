package com.wiimy.order.repository;

import com.wiimy.order.entity.Order;
import com.wiimy.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findBySessionIdOrderByCreatedAtDesc(String sessionId);

    List<Order> findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByOrderNumber(String orderNumber);
}
