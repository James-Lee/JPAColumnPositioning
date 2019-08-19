package com.javateam.JPAColumnPositioning.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.javateam.JPAColumnPositioning.annotation.ColumnPosition;

@Table(name="store_table")
@Entity
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ColumnPosition(1)
    @Column(name="store_code")
    private String storeCode;

    @ColumnPosition(2)
    @Column(name="store_name")
    private String storeName;

    @ColumnPosition(3)
    @Column(name="address")
    private String address;

    @ColumnPosition(4)
    @Column(name="zip_code")
    private String zipCode;

    @ColumnPosition(5)
    @Column(name="ceo_name")
    private String ceoName;

}
