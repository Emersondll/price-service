package com.abinbev.b2b.price.api.config;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@ConditionalOnProperty(prefix = "management.health.customMongoHealth", name = "enabled", matchIfMissing = true)
@Configuration(value = "customMongoHealth")
public class MongoHealthIndicatorConfig extends AbstractHealthIndicator {

	private final MongoTemplate mongoTemplate;

	@Autowired
	public MongoHealthIndicatorConfig(final MongoTemplate mongoTemplate) {

		this.mongoTemplate = mongoTemplate;
	}

	@Override
	protected void doHealthCheck(final Health.Builder builder) throws Exception {

		final Document result = this.mongoTemplate.executeCommand("{ dbStats: 1}");
		builder.up().withDetail("Status OK", result.get("ok").toString());
	}
}
