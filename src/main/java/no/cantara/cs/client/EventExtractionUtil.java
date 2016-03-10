package no.cantara.cs.client;

import no.cantara.cs.dto.event.EventExtractionConfig;
import no.cantara.cs.dto.event.EventExtractionTag;
import no.cantara.cs.dto.event.ExtractedEventsStore;
import no.cantara.cs.dto.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class EventExtractionUtil {
    private static final Logger log = LoggerFactory.getLogger(EventExtractionUtil.class);

    public static Map<String, List<EventExtractionTag>> groupExtractionConfigsByFile(
            EventExtractionConfig config) {
        Map<String, List<EventExtractionTag>> collect = config.tags.stream()
                .collect(groupingBy(item -> item.filePath));
        log.info(collect.toString());
        return collect;
    }

    public static ExtractedEventsStore mapToExtractedEvents(List<Event> events) {
        ExtractedEventsStore mappedEvents = new ExtractedEventsStore();
        mappedEvents.addEvents(events);
        return mappedEvents;
    }
}
