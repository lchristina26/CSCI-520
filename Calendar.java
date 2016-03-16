import java.util.*;

public class Calendar {
    ArrayList<Event> events;
    public Calendar() {
        events = new ArrayList<Event>();
    }

    public void addEvent(Event e) {
        events.add(e);
    }
    // remove event 
    public void removeEvent(Event e) {
        events.remove(e);
    }
    // remove event by name of event
    public void removeEvent(String name) {
        for (Event event : events) {
            if ((event.getName()).equals(name)) {
                events.remove(event);
                break;
            }
        }
    }

    public ArrayList<Event> getEvents() {
        return events;
    }
}
