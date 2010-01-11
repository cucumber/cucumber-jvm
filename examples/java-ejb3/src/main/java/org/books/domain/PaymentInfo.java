package org.books.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class PaymentInfo {
    @Id @GeneratedValue
    private long id;
}
