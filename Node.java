import java.util.*;
import java.lang.Math;

public class Node {
    private int ID;
    private Calendar calendar;
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
        twoDTT[ID][ID] = clock;
    }

    public int getID() {
        return ID;
    }
    public int getClock() {
        return clock;
    }
    public void set2DTT(int[][] table) {
        twoDTT = table;
    }
    /*
     * Pass in the time table received and update our 2DTT from this one
     */
    public int[][] updateTT(int[][] inTT, int nodeID) {
        int row = 0;
        int col = 0;
        int maxNum = twoDTT[row][col];

        for (int i = 0; i < numNodes; i++) {
            twoDTT[ID][i] = Math.max(twoDTT[ID][i], inTT[nodeID][i]);
        }
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                twoDTT[i][j] = Math.max(twoDTT[i][j], inTT[i][j]);
            }
        }
        return twoDTT;
    }
    
   /*
    * To add an event, first check for conflicts
    * return: SUCCESS for no conflict, FAILURE if conflict detected
    */
    public int addCalEvent(Event e) {
        if (checkForConflict(e) == FAILURE) {
            return FAILURE;
        }
        clock++;
        twoDTT[ID][ID] = clock;
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
}
