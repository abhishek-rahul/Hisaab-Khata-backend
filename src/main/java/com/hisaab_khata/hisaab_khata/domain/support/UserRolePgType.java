package com.hisaab_khata.hisaab_khata.domain.support;

import com.hisaab_khata.hisaab_khata.enums.UserRole;

public class UserRolePgType extends PostgreSQLEnumType<UserRole> {

    public UserRolePgType() {
        super(UserRole.class, "user_role");
    }
}
