package com.azsdk.supportability.eventhubs.config;

import com.azsdk.supportability.eventhubs.EventHubsRecordProcessor;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class EventHubConfig {

    @Value(value = "${azure.eventhub.connectionstring}")
    private String connectionstring;

    @Value(value = "${azure.eventhub.eventhubname}")
    private String eventHubName;

    @Value(value = "${azure.eventhub.consumer.consumergroupname}")
    private String consumerGroupName;

    @Value(value = "${azure.eventhub.consumer.storageconnectionstring}")
    private String storageConnectionString;

    @Value(value = "${azure.eventhub.consumer.storagecontainername}")
    private String storageContainerName;

    @Value(value = "${azure.eventhub.consumer.batchsize:512}")
    private int batchSize;

    @Value("${azure.eventhub.consumer.maxWaitTime:1}")
    private int maxWaitTime;

    @Bean
    public BlobContainerAsyncClient blobContainerAsyncClient() {
        return new BlobContainerClientBuilder()
                .connectionString(storageConnectionString)
                .containerName(storageContainerName)
                .buildAsyncClient();
    }

    @Bean
    public EventProcessorClient eventProcessorClientBuilder(
            @Autowired CheckpointStore checkpointStore,
            @Autowired EventHubsRecordProcessor eventHubRecordProcessor) {

        return new EventProcessorClientBuilder()
                .connectionString(connectionstring, eventHubName)
                .consumerGroup(consumerGroupName)
                .processEventBatch(
                        eventHubRecordProcessor::processContext, batchSize, Duration.ofSeconds(maxWaitTime))
                .processPartitionClose(eventHubRecordProcessor::closeContext)
                .processPartitionInitialization(eventHubRecordProcessor::initContext)
                .processError(eventHubRecordProcessor::errorContext)
                .checkpointStore(checkpointStore)
                .buildEventProcessorClient();
    }

    @Bean
    public CheckpointStore blobCheckpointStore(
            @Autowired BlobContainerAsyncClient blobContainerAsyncClient) {
        return new BlobCheckpointStore(blobContainerAsyncClient);
    }
}
