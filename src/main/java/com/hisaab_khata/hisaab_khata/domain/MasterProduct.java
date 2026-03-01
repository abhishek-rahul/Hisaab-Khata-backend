package com.hisaab_khata.hisaab_khata.domain;

import com.hisaab_khata.hisaab_khata.enums.BaseUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "master_product")
public class MasterProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "canonical_name", nullable = false)
    private String canonicalName;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "base_unit", nullable = false)
    private BaseUnit baseUnit;
}
