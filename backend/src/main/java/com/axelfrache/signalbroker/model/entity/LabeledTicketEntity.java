package com.axelfrache.signalbroker.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "labeled_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LabeledTicketEntity {

    @Id
    @Column(name = "\"ticketId\"", nullable = false)
    private String ticketId;

    @Column(name = "\"schemaVersion\"", nullable = false)
    private Long schemaVersion;

    @Column(name = "subject")
    private String subject;

    @Column(name = "contact")
    private String contact;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Column(name = "\"labeledAt\"")
    private Double labeledAt;

    @Column(name = "\"ticketType\"")
    private String ticketType;

    @Column(name = "\"receivedAt\"")
    private Double receivedAt;

    @Column(name = "body")
    private String body;

    @Column(name = "category")
    private String category;

    @Column(name = "priority")
    private String priority;

    @Column(name = "\"commonId\"")
    private Long commonId;

}
