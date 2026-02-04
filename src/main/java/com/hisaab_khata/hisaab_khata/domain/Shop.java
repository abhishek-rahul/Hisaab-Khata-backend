package com.hisaab_khata.hisaab_khata.domain;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shop")
public class Shop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shopName;

    private String ownerName;

    @Column(nullable = false, unique = true)
    private String phone;

    //private String passwordHash;


    private String address;

    private String gstNumber;  // optional


}

