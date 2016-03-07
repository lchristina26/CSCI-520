import java.util.*;

public class Calendar {
    ArrayList<Event> events;
    public Calendar() {
        events = new ArrayList<Event>();
    }

    public void addEvent(Event e) {
        events.add(e);
    }

    public void removeEvent(Event e) {
        events.remove(e);
    }

    public ArrayList<Event> getEvents() {
        return events;
    }
}
