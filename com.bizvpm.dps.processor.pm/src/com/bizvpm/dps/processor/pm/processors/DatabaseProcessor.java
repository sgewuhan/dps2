package com.bizvpm.dps.processor.pm.processors;

import com.bizvpm.dps.processor.pm.Activator;
import com.bizvpm.dps.runtime.IProcessorRunable;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public abstract class DatabaseProcessor implements IProcessorRunable {

	public DatabaseProcessor() {
	}

	protected MongoCollection<BasicDBObject> getCollection(String name) {
		return getDB().getCollection(name, BasicDBObject.class);
	}

	protected MongoDatabase getDB() {
		return Activator.db();
	}
}
