import java.util.*;

public class Node {
    private int ID;
    private Calendar calendar;
    private int SUCCESS = 0;
    private int FAILURE = -1;

    public Node (int nodeID) {
        ID = nodeID;
        calendar = new Calendar();
    }

    public int getID() {
        return ID;
    }
    
   /*
    * To add an event, first check for conflicts
    * return: SUCCESS for no conflict, FAILURE if conflict detected
    */
    public int addCalEvent(Event e) {
        if (checkForConflict(e) == FAILURE) {
            return FAILURE;
        }

        calendar.addEvent(e);

        return SUCCESS; // return 0 on success
    }

    public void removeCalEvent(Event e) {
        calendar.removeEvent(e);
    }

    /* Compare start time of event with that time in the calendar and return
     * success if that start time is empty up to the event to add duration
     */
    public int checkForConflict(Event e) {
        double length = e.getEventLength();
        ArrayList<Event> events = calendar.getEvents();
        int numEvents = events.size();
        boolean conflict = false;
        double start = e.getStart();

        // sort through all events to check for conflict
        for (int i = 0; i < numEvents; i++) {
            // if the start time of e is already taken return a failure
            if (events.get(i).getStart() == start) {
                return FAILURE;
            } else {
                // if an empty slot is found, check if there is enough time
                // to add event, if not return a failure
                for (double j = start; j < length; j+=0.5) {
                    if (j == events.get(i).getStart()) {
                        return FAILURE;
                    }
                }
            }
        }

        return SUCCESS; // no conflicts were found!
    }

}
