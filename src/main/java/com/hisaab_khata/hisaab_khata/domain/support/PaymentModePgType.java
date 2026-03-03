package com.hisaab_khata.hisaab_khata.domain.support;

import com.hisaab_khata.hisaab_khata.enums.PaymentMode;

public class PaymentModePgType extends PostgreSQLEnumType<PaymentMode> {

    public PaymentModePgType() {
        super(PaymentMode.class, "payment_mode");
    }
}
