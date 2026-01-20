package com.wif3006.banking_system.model;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id", unique = true)
    private DepositAccount account;

    @Column(nullable = false)
    private String cardNumber;

    @Column(nullable = false)
    private int transactionLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(nullable = false)
    private String pinNumber;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    public enum CardStatus {
        ACTIVE,
        INACTIVE,
        FROZEN
    }
}
