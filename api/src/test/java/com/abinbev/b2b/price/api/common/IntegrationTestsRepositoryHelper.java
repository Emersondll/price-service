package com.abinbev.b2b.price.api.common;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class IntegrationTestsRepositoryHelper {

	/**
	 * Find data into MongoDB.
	 *
	 * @param mongoOperations The mongoOperations object injected by test context.
	 * @param filters         The filters of searching. Each element into the map represents a condition where the data represented by map key mut be equal to map value.
	 * @param collectionName  The collection name.
	 * @return The result list.
	 */
	public static List<Map<Object, Object>> findIntoMongo(final MongoOperations mongoOperations, final Map<String, String> filters,
			final String collectionName) {

		Criteria criteria = new Criteria();
		for (final Map.Entry<String, String> entry : filters.entrySet()) {
			criteria = criteria.and(entry.getKey()).is(entry.getValue());
		}
		final List<? extends Map> result = mongoOperations.find(new Query(criteria), Map.class, collectionName);
		return (List<Map<Object, Object>>) result;
	}

	public static void insertIntoMongo(final MongoOperations mongoOperations, final List<? extends Map<?, ?>> mongoData, final String collectionName) {

		mongoData.forEach(data -> mongoOperations.insert(data, collectionName));
	}
}
