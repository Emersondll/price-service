package com.abinbev.b2b.price.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.abinbev.b2b.commons.platformId.core.PlatformIdEncoderDecoder;

@Configuration
public class CodecConfig {

	@Bean
	public PlatformIdEncoderDecoder platformIdEncoderDecoder() {

		return new PlatformIdEncoderDecoder();
	}
}
