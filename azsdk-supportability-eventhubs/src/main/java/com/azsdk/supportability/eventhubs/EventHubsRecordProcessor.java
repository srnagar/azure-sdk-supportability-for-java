package com.azsdk.supportability.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.CloseReason;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventHubsRecordProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsRecordProcessor.class);
    private final Map<String, Pair<Long, Long>> checkpointMap = new ConcurrentHashMap<>();
    private final Map<String, Long> checkpointTimeMap = new ConcurrentHashMap<>();

    @Value(value = "${azure.eventhub.consumer.checkpointinterval:15000}")
    private long checkpointIntervalMillis;

    @Autowired
    private CheckpointStore checkpointStore;

    public EventHubsRecordProcessor() {
        // comment for sonar
    }

    public void errorContext(ErrorContext errorContext) {
        LOGGER.error(
                "[EVENT-HUB-ERROR] ErrorNotificationHandler error occurred on partition Id {}",
                errorContext.getPartitionContext().getPartitionId(),
                errorContext.getThrowable());
    }

    public void closeContext(CloseContext closeContext) {

        PartitionContext partitionContext = closeContext.getPartitionContext();
        String partitionId = partitionContext.getPartitionId();
        Pair<Long, Long> pair = checkpointMap.get(partitionId);

        if (pair != null && CloseReason.EVENT_PROCESSOR_SHUTDOWN == closeContext.getCloseReason()) {
            Checkpoint checkpoint = new Checkpoint();
            checkpoint.setConsumerGroup(partitionContext.getConsumerGroup());
            checkpoint.setEventHubName(partitionContext.getEventHubName());
            checkpoint.setFullyQualifiedNamespace(partitionContext.getFullyQualifiedNamespace());
            checkpoint.setPartitionId(partitionId);
            checkpoint.setSequenceNumber(pair.getFirst());
            checkpoint.setOffset(pair.getSecond());
            checkpointStore.updateCheckpoint(checkpoint);
            LOGGER.info("Partition closed checkpoint: {}", partitionId);
        }

        LOGGER.info(
                "Partition context closed {} reason {}", partitionId, closeContext.getCloseReason());
    }

    public void initContext(InitializationContext initContext) {
        LOGGER.info("Partition context init {}", initContext.getPartitionContext().getPartitionId());
    }

    public void processContext(EventBatchContext eventContext) {

        List<EventData> eventDataList = eventContext.getEvents();
        int msgListSize = eventDataList.size();

        if (msgListSize == 0) {
            return;
        }

        String partitionId = eventContext.getPartitionContext().getPartitionId();
        LOGGER.info("Batch received of size: {}", msgListSize);

        EventData lastEvent = eventDataList.get(msgListSize - 1);
        long sequenceNo = lastEvent.getSequenceNumber();
        // set sequence and offset of azure event hub
        checkpointMap.put(partitionId, new Pair<>(sequenceNo, lastEvent.getOffset()));

        // Logs request
        LOGGER.trace(
                "EventHubRecordProcessor msg-list-size, {}, partition-id, {}",
                msgListSize,
                partitionId);

        try {
            // process messages
            // .....
            // checkpoint after successful processing
            doCheckpointAtInterval(eventContext, partitionId, sequenceNo);
        } catch (Exception e) {
            LOGGER.info("Error while processing messages ", e);
        }
    }

    /**
     * @param eventContext
     * @param partitionId
     * @param sequenceNo
     */
    public void doCheckpointing(EventBatchContext eventContext, String partitionId, long sequenceNo) {

        LOGGER.info("EventHubRecordProcessor checkpointing pid {} seq {}", partitionId, sequenceNo);

        try {
            eventContext.updateCheckpoint();
        } catch (Exception e) {
            LOGGER.error(
                    "Error occurred while Event hub checkpointing for partition [{}], error: ",
                    partitionId,
                    e);
        }
    }

    private void doCheckpointAtInterval(
            EventBatchContext eventContext, String partitionId, long sequenceNo) {
        // Checkpoint after checkpointIntervalMillis
        long now = System.currentTimeMillis();
        if (now > checkpointTimeMap.getOrDefault(partitionId, 0L)) {
            doCheckpointing(eventContext, partitionId, sequenceNo);
            checkpointTimeMap.put(partitionId, now + checkpointIntervalMillis);
        }
    }
}
