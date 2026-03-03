package com.hisaab_khata.hisaab_khata;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Test-only config: removes MapStruct-generated Sale mapper bean definitions
 * before any post-processor (e.g. Mockito) introspects them, avoiding
 * ClassNotFoundException for SaleItemMapper.
 */
@Configuration
public class RemoveSaleMappersPostProcessor {

    private static final String SALE_MAPPER_IMPL = "saleMapperImpl";
    private static final String SALE_ITEM_MAPPER_IMPL = "saleItemMapperImpl";

    @Bean
    public static BeanDefinitionRegistryPostProcessor removeSaleMappersRegistryPostProcessor() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
                for (String name : new String[]{SALE_MAPPER_IMPL, SALE_ITEM_MAPPER_IMPL}) {
                    if (registry.containsBeanDefinition(name)) {
                        registry.removeBeanDefinition(name);
                    }
                }
            }
        };
    }
}
