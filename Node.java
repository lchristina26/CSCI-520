import java.util.*;
import java.lang.Math;

public class Node {
    private int ID;
    private Calendar calendar;
    private Calendar[] otherCals;
    private Log log;
    private int SUCCESS = 0;
    private int FAILURE = -1;
    private int DAY_OVERLAP_FAILURE = -2;
    private int INSERT = 1;
    private int DELETE = 0;
    private int clock = 0;
    private int[][] twoDTT;
    private int numNodes;

    public Node (int nodeID, int numNodes) {
        ID = nodeID;
        calendar = new Calendar();
        log = new Log();
        this.numNodes = numNodes;
        twoDTT = new int[numNodes][numNodes];
        twoDTT[ID-1][ID-1] = clock;
        // init other node's calendars
        otherCals = new Calendar[numNodes];
        for (int i = 0; i < otherCals.length; i++) {
            if (i == ID)
                otherCals[i] = null;
            else
                otherCals[i] = new Calendar();
        }
    }

    public int getID() {
        return ID;
    }
    public int getClock() {
        return clock;
    }
    public Calendar[] getOtherCals() {
        return otherCals;
    }
    public void set2DTT(int[][] table) {
        twoDTT = table;
    }
    public int[][] get2DTT() {
        return twoDTT;
    }
    /*
     * Pass in the time table received and update our 2DTT from this one
     */
    public void updateTT(int[][] inTT, int nodeID) {
        int row = 0;
        int col = 0;
        int maxNum = twoDTT[row][col];

        for (int i = 0; i < numNodes; i++) {
            twoDTT[ID-1][i] = Math.max(twoDTT[ID-1][i], inTT[nodeID-1][i]);
        }
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                twoDTT[i][j] = Math.max(twoDTT[i][j], inTT[i][j]);
            }
        }
    }

    public Calendar getCal() {
        return calendar;
    }

    /*
     * To add an event, first check for conflicts
     * return: SUCCESS for no conflict, FAILURE if conflict detected
     */
    public int addCalEvent(Event e) {
        if (checkForConflict(e) == FAILURE) {
            return FAILURE;
        }
        if (checkForConflict(e) == DAY_OVERLAP_FAILURE)
            return DAY_OVERLAP_FAILURE;
        clock++;
        twoDTT[ID-1][ID-1] = clock;
        calendar.addEvent(e);

        return SUCCESS; // return 0 on success
    }

    public void removeCalEvent(Event e) {
        calendar.removeEvent(e);
    }
    public void removeCalEvent(String name) {
        calendar.removeEvent(name);
    }

    public void updateOthers(int otherID, String eName, String operator) {
        if (operator.equals("insert"))
            otherCals[otherID].addEvent(eName);
        else if (operator.equals("delete"))
            otherCals[otherID].removeEvent(eName);
    }

    public boolean checkOtherAvail(int otherID, double start, double duration) {
        return otherCals[otherID].isAvailable(start, duration);
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
        double end = e.getEnd();
        if (((end - start) <= 0) || (e.getEventLength() > 23.5))
            return DAY_OVERLAP_FAILURE;
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

    public void addEventToLog (Event e) {
        log.updateLog(INSERT, e, ++clock, ID);
    }
    public String toString(Event e, String table) {
        String toSend =  e.getName() + " " + e.getDay() + " " + e.getStart() +
            " " + e.getEnd() + " " + ID + " ";
        for (int i = 0; i < e.getParticipants().length; i++) {
            toSend += (e.getParticipants()[i]+ " ");
        }
        toSend += ("Table " + table);
        return toSend;
    }

    public void resetCalendar() {
        calendar = new Calendar();
    }
    // check if an event already exists in case information is sent 
    // multiple times in the network 
    public boolean containsEvent(Event e) {
        boolean eventExists = false;
        for (Event event : calendar.getEvents()) {
            if (e.getName().equals(event.getName())) {
                eventExists = true;
            }
        }
        return eventExists;
    }

    public Event getEventByName(String eName) {
        Event e = null;
        for (Event event : calendar.getEvents()) {
            if (event.getName().equals(eName)) {
                return event;
            }
        }
        return e;
    }

    public String[] getCalendar() {
        String[] dayStrings = new String[7];
        int dayNum = -1;
        for (int i = 0; i < 7; i++) {
            dayStrings[i] = "";
        }
        for (Event event : calendar.getEvents()) {
            switch(event.getDay()) {
                case "Sunday":
                    dayNum = 0;
                    break;
                case "Monday":
                    dayNum = 1;
                    break;
                case "Tuesday":
                    dayNum = 2;
                    break;
                case "Wednesday":
                    dayNum = 3;
                    break;
                case "Thursday":
                    dayNum = 4;
                    break;
                case "Friday":
                    dayNum = 5;
                    break;
                case "Saturday":
                    dayNum = 6;
                    break;
                default:
                    break;
            }
            if (dayNum >= 0) {
                dayStrings[dayNum] += ("<p class=\"event\">"+event.getName()+
                                "<br>" + event.toTime(event.getStart())+" - "+
                                    event.toTime(event.getEnd()) + "</p>");
            }
        }
        return dayStrings;
    }
}
