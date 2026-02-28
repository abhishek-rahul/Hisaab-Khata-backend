package com.hisaab_khata.hisaab_khata.domain;


import com.hisaab_khata.hisaab_khata.domain.support.UserRolePgType;
import com.hisaab_khata.hisaab_khata.domain.support.UserStatusPgType;
import com.hisaab_khata.hisaab_khata.enums.UserRole;
import com.hisaab_khata.hisaab_khata.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "mobile"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String mobile;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Type(UserRolePgType.class)
    private UserRole role;

    @Type(UserStatusPgType.class)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Builder.Default
    private Boolean active = true;
}

