package com.azsdk.supportability.eventhubs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import com.azure.messaging.eventhubs.EventProcessorClient;

@SpringBootApplication
public class EventHubsApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventHubsApplication.class, args);
	}

    @Autowired
    private EventProcessorClient eventProcessorClient;

    @PostConstruct
    public void init(){
        eventProcessorClient.start();
    }

    @PreDestroy
    public void destroy(){
        eventProcessorClient.stop();
    }

}
