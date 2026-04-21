package helpers;

import java.util.List;
import java.util.Map;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class IntegrationTestsRepositoryHelper {

	private static final String DATABASE = "Prices";

	public static void insertIntoMongo(final String collectionName, final List<? extends Map<?, ?>> mongoData) {

		final String uri = "mongodb://localhost";
		final MongoClient mongoClient = MongoClients.create(uri);
		final MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE);
		final MongoCollection<Map> mongoCollection = mongoDatabase.getCollection(collectionName, Map.class);

		mongoCollection.insertMany(mongoData);
		mongoClient.close();
	}

}
