package com.awesometech.dps.processor.irobot.service;

import java.util.List;

import org.bson.Document;
import org.glassfish.jersey.client.ClientConfig;

import com.awesometech.service.pdm.EngnisQuotationService;
import com.bizvisionsoft.service.provider.BsonProvider;
import com.eclipsesource.jaxrs.consumer.ConsumerFactory;

public class PdmClient {

	// QED处理完成后，将QED返回到PDM
	public static void pushToPDM(String host,List<Document> jobList) {
		try {
			ClientConfig config = new ClientConfig().register(new BsonProvider<Object>());
			EngnisQuotationService service = ConsumerFactory.createConsumer(host, config,
					EngnisQuotationService.class);
			service.updateJobData(jobList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
