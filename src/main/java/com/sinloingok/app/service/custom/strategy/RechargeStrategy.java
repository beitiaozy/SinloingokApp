package com.sinloingok.app.service.custom.strategy;

import java.math.BigDecimal;

public interface RechargeStrategy {
    
    boolean support(String type);

    boolean shouldSkip(ConsumptionContext context);

    BigDecimal[] calculate(ConsumptionContext context);
}
