package com.abinbev.b2b.price.api.common;

import java.io.IOException;
import java.time.Instant;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

public class InstantDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<Instant> {

	@Override
	public Instant deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {

		return InstantHelper.convertStringFormattedAsIsoLocalDateToInstant(jsonParser.getValueAsString());
	}

}