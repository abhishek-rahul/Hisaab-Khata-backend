package com.hisaab_khata.hisaab_khata;

import com.hisaab_khata.hisaab_khata.mapper.SaleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(RemoveSaleMappersPostProcessor.class)
class HisaabKhataApplicationTests {

	@MockitoBean
	private SaleMapper saleMapper;

	@Test
	void contextLoads() {
	}

}
