package com.javateam.JPAColumnPositioning.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.javateam.JPAColumnPositioning.annotation.ColumnPosition;

import java.time.LocalDateTime;

@Table(name="user_tbl")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) 
    @ColumnPosition(value=1)
    @Column(name = "USER_CD", length = 20)
    private String userCd;

    @ColumnPosition(value=2)
    @Column(name = "USER_NM", length = 30)    
    private String userNm;

    @ColumnPosition(value=3)
    @Column(name = "USER_PS", length = 128)    
    private String userPs;

    @ColumnPosition(value=4)
    @Column(name = "USER_TYPE", length = 15)    
    private String userType;

    @ColumnPosition(value=5)
    @Column(name = "EMAIL", length = 30)    
    private String email;

    @ColumnPosition(value=6)
    @Column(name = "HP_NO", length = 15)    
    private String hpNo;

    @ColumnPosition(value=7)
    @Column(name = "LAST_LOGIN_AT")    
    private LocalDateTime lastLoginAt;

    @ColumnPosition(value=8)
    @Column(name = "PASSWORD_UPDATED_AT")
    private LocalDateTime passwordUpdatedAt;

    @ColumnPosition(value=9)
    @Column(name = "USE_YN", length = 1)
    private String useYn;

    @ColumnPosition(value=10)
    @Column(name = "REMARK", length = 200)    
    private String remark;
}
