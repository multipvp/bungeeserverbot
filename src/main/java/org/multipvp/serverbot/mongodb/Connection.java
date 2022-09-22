package org.multipvp.serverbot.mongodb;

import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


//mongodb+srv://lachlankemp:ZjH7&554kW9N@cluster0.2gzwdsk.mongodb.net/?retryWrites=true&w=majority

public class Connection {
    static String uri = "mongodb+srv://lachlankemp:ZjH7&554kW9N@cluster0.2gzwdsk.mongodb.net/?retryWrites=true&w=majority";
    public static MongoClient client() {
        MongoClient mongoClient = MongoClients.create(uri);
        return mongoClient;
    }
    public static MongoCollection userCol() {
        MongoDatabase database = client().getDatabase("users");
        MongoCollection<Document> collection = database.getCollection("users");
        return collection;
    }
}