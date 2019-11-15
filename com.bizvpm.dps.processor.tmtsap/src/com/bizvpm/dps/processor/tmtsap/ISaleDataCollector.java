package com.bizvpm.dps.processor.tmtsap;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

public interface ISaleDataCollector {

	 void runGetData(MongoCollection<Document> saleDataCol, int year, int month) throws Exception;

}
