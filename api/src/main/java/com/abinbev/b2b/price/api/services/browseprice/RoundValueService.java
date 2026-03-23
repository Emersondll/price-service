package com.abinbev.b2b.price.api.services.browseprice;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.newrelic.api.agent.Trace;

@Service
public class RoundValueService {

	@Trace(dispatcher = true)
	public BigDecimal execute(final BigDecimal value) {

		return value != null ? value.setScale(2, RoundingMode.HALF_UP) : null;
	}

}
