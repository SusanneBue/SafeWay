package com.bue.susanne.safeway;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abaumgra on 29/01/2017.
 */

public class EventListPOJO {

    private List<EventPOJO> events;

    public EventListPOJO(){
        this.events = new ArrayList<EventPOJO>();
    }

    public List<EventPOJO> getEvents() {
        return events;
    }

    public void setEvents(List<EventPOJO> events) {
        this.events = events;
    }


    public void addEvent(EventPOJO event) {
        this.events.add(event);
    }



}
