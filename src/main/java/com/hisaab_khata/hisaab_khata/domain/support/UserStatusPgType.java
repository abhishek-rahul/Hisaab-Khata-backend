package com.hisaab_khata.hisaab_khata.domain.support;

import com.hisaab_khata.hisaab_khata.enums.UserStatus;

public class UserStatusPgType extends PostgreSQLEnumType<UserStatus> {

    public UserStatusPgType() {
        super(UserStatus.class, "user_status");
    }
}
