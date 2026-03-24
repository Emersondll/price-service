package com.abinbev.b2b.price.api.services.browseprice;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoundValueServiceTest {

	private static final BigDecimal ROUNDED_VALUE = BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP);

	@InjectMocks
	private RoundValueService roundValueService;

	@Test
	void shouldReturnNullWhenValueToBeRoundIsNull() {

		assertThat(roundValueService.execute(null), is(nullValue()));
	}

	@Test
	void shouldReturnRoundedValueWhenValueToBeRoundIsOk() {

		assertThat(roundValueService.execute(BigDecimal.valueOf(99.999)), is(equalTo(ROUNDED_VALUE)));
	}

}
