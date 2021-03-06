package no.cantara.cs.dto.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExtractedEventsStore {
    private final Map<String, EventGroup> eventGroups;

    public ExtractedEventsStore() {
        eventGroups = new HashMap<>();
    }

    public Map<String, EventGroup> getEventGroups() {
        return eventGroups;
    }

    public void addEvents(List<Event> eventsToAdd) {
        Map<String, List<Event>> eventsGroupedByGroup = eventsToAdd.stream()
                .collect(Collectors.groupingBy(
                        Event::getGroupName
                ));

        eventsGroupedByGroup.keySet().stream()
                .forEach(groupName -> {
                    List<Event> eventFilesToAdd = eventsGroupedByGroup.get(groupName);
                    eventGroups.putIfAbsent(groupName, new EventGroup());
                    EventGroup eventGroup = eventGroups.get(groupName);
                    eventGroup.addEvents(eventFilesToAdd);
                });
    }

    public EventGroup getEventGroup(String groupName) {
        return eventGroups.get(groupName);
    }

}
