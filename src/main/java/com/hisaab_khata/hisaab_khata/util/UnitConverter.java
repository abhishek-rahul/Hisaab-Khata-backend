package com.hisaab_khata.hisaab_khata.util;


import com.hisaab_khata.hisaab_khata.domain.Product;
import org.springframework.stereotype.Component;

@Component
public class UnitConverter {

    public double toBaseUnit(double qty, String unit, Product product) {

        // If the entered unit equals product.unit → multiply by conversion
        if (unit.equalsIgnoreCase(product.getUnit())) {
            return qty * product.getConversion();
        }

        // If entered unit IS the base unit
        if (unit.equalsIgnoreCase(product.getBaseUnit())) {
            return qty;
        }

        throw new IllegalArgumentException(
                "Invalid unit conversion for product: " + product.getName()
        );
    }
}

