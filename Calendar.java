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

    // add event by name of event
    public void addEvent(String name) {
        for (Event event : events) {
            if ((event.getName()).equals(name)) {
                events.add(event);
                break;
            }
        }
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public boolean isAvailable(double start, double duration) {
        double count = 0.0;
        for (Event event : events) {
            double st = event.getStart();
            count = 0.0;
            if (st == start) {
                return false;
            } else {
                while(count < duration) {
                    if (st == (start + count))
                        return false;
                    count+=0.5;
                }
            }
        }
        return true;
    }
}
