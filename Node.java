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
    private int DUPLICATE = 2;
    private int BAD_CONNECTION = -3;
    private int INSERT = 1;
    private int DELETE = 0;
    private int clock = 0;
    private int[][] twoDTT;
    private int numNodes;
    private Run client;

    public Node (int nodeID, int numNodes) {
        ID = nodeID;
        calendar = new Calendar();
        log = new Log();
        this.numNodes = numNodes;
        twoDTT = new int[numNodes][numNodes];
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                twoDTT[i][j] = 0;
            }
        }
        twoDTT[ID-1][ID-1] = clock;
        // init other node's calendars
        otherCals = new Calendar[numNodes];
        for (int i = 0; i < otherCals.length; i++) {
            if (i == ID)
                otherCals[i] = null;
            else
                otherCals[i] = new Calendar();
        }
        client = new Run();
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
        int ret = checkForConflict(e);
        if (ret == FAILURE || ret == DAY_OVERLAP_FAILURE) 
            return ret;
        clock++;
        twoDTT[ID-1][ID-1] = clock;
        calendar.addEvent(e);
        log.updateLog("INSERT", e, clock, ID); 
        String tableStr = convertTo1D(twoDTT);
        ret = sendPartialLog(tableStr);
        return ret; // return 0 on success
    }

    public int addCalEvent(Event e, String recv) {
        int ret = checkForConflict(e);
        if (ret == FAILURE || ret == DAY_OVERLAP_FAILURE) 
            return ret;
        clock++;
        twoDTT[ID-1][ID-1] = clock;
        calendar.addEvent(e);
        log.updateLog("INSERT", e, clock, ID); 
        return ret; // return 0 on success
    }

    public int sendPartialLog(String tableStr) {
        for (int k = 1; k < numNodes+1; k++) {
            if (k != ID) {
                String NP = getPartialLog(k);//ERROR here when send 
                byte [] byteStr = (NP + " TABLE: " + tableStr + " ID " + ID).
                    getBytes();
                try {
                    client.sendPacket(k, byteStr);
                } catch (Exception x) {
                    return BAD_CONNECTION;
                }
            }
        }
        return SUCCESS;
    }
    public int removeCalEvent(Event e) {
        calendar.removeEvent(e);
        clock++;
        twoDTT[ID-1][ID-1] = clock;
        log.updateLog("DELETE", e, clock, ID);
        String tableStr = convertTo1D(twoDTT);
        int ret = sendPartialLog(tableStr);
        return ret;
    }
    public int removeCalEvent(Event e, String recv) {
        calendar.removeEvent(e);
        clock++;
        twoDTT[ID-1][ID-1] = clock;
        log.updateLog("DELETE", e, clock, ID);
        return 0;
    }
    public void removeCalEvent(String name) {
        Event e = calendar.getEventByName(name);
        calendar.removeEvent(name);
        clock++;
        twoDTT[ID-1][ID-1] = clock;
        log.updateLog("DELETE", e, clock, ID);
    }

    /* Compare start time of event with that time in the calendar and return
     * success if that start time is empty up to the event to add duration
     */
    public int checkForConflict(Event e) {
        ArrayList<Event> events = calendar.getEvents();
        int numEvents = events.size();
        double start = e.getStart();
        double end = e.getEnd();
       // for (Event ev : events) {
       //     if (ev.getName().equals(e.getName())) {
       //         return SUCCESS;
       //     }
       // }
        // sort through all events to check for conflict
        for (int i = 0; i < numEvents; i++) {
            Event ev = events.get(i);
            // if the start time of e is already taken return a failure
            if (ev.getDay().contains(e.getDay())) {
                if (!checkTime(start, end, ev, e))
                    return FAILURE;
            }
        }

        return SUCCESS; // no conflicts were found!
    }

    public boolean checkTime(double start, double end, Event ev, Event e) {
        double length = e.getEventLength();
        for (double m = start; m < length; m+=0.5) {
            if (m == ev.getStart()) {
                for (int j = 0; j < ev.getParticipants().length; j++) {
                    for (int k = 0; k < e.getParticipants().length; k++) {
                        if (e.getParticipants()[k] == ev.getParticipants()[j]){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public String getPartialLog(int id) {
        String pl = "";
        String partStr = "";
        if (twoDTT[id-1][ID-1] < twoDTT[ID-1][ID-1]) {
            for (LogEvent eR : log.getLog()) {
                if (eR.clock <= twoDTT[ID-1][ID-1]) {
                    for(int i = 0; i < eR.event.getParticipants().length; i++){
                        partStr += (" " + eR.event.getParticipants()[i]);
                    }
                    pl += (" EVENT " + eR.operator + " " + 
                            eR.event.getName() + " " + 
                            eR.event.getDay() + " " +
                            eR.event.getStart() + " " + 
                            eR.event.getEnd() + " Participants: " + 
                            partStr + " NodeID: " +
                            eR.nodeID + " Clock: " + eR.clock);
                    partStr = "";
                }
            }
        }
        return pl;
    }

    public void resetCalendar() {
        calendar = new Calendar();
    }

    public void resetTable() {
        clock = 0;
        twoDTT = new int[numNodes][numNodes];
    }

    public void resetLog() {
        log = new Log();
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
                if (!dayStrings[dayNum].contains(event.getName())) {
                    dayStrings[dayNum] += ("<p class=\"event\">"+
                            event.getName()+
                            "<br>" + event.toTime(event.getStart())+" - "+
                            event.toTime(event.getEnd()) + "<br>" + 
                            "Participants: ");
                    for (int i = 0; i < event.getParticipants().length; i++) {
                        if (event.getParticipants()[i] != 0) {
                            dayStrings[dayNum] += (event.getParticipants()[i]
                                    +" "); 
                        }
                    }
                    dayStrings[dayNum] += "</p><hr>";
                }
            }
        }
        return dayStrings;
    }
    public String convertTo1D(int[][] arr) {
        int count = 0;
        String str = "";
        for (int i = 0; i < 4; i++) {
            while (count < 4) {
                str+= (arr[i][count] + " ");
                count++;
            }
            count = 0;
        }
        return str;
    }

    public ArrayList<Event> readLog(String[] str) {
        ArrayList<Event> eventList = new ArrayList<Event>();
        ArrayList<Integer> partic = new ArrayList<Integer>();
        int[] intInvitees;
        for (int i = 0; i < str.length; i++) {
            if (str[i].contains("Participants")) {
                int count = i+1;
                while (!str[count].contains("NodeID")) {
                    int guest = Integer.parseInt(str[count].trim());
                    partic.add(guest);
                    count++;
                }
                if (partic.size() > 0) {
                    intInvitees = new int[partic.size()];
                    for (int j = 0; j < partic.size(); j++)
                        intInvitees[j] = partic.get(j);
                } else {
                    intInvitees = new int[1];
                    intInvitees[0] = ID;
                }
                if (!str[i-5].contains("DELETE")) {
                    Event newE = new Event(str[i-4], str[i-3],
                            Double.parseDouble(str[i-2]),
                            Double.parseDouble(str[i-1]), intInvitees);
                    if (!containsEvent(newE))
                        eventList.add(newE);
                    for (int j = 0; j < str.length; j++) {
                        Event remE = getEventByName(str[i-4].trim());
                        if (str[j].trim().equals(str[i-4].trim()) && 
                                str[j-1].trim().contains("DELETE")) {
                            if (eventList.contains(remE)) {
                                eventList.remove(remE);
                            }
                            removeCalEvent(remE, "r");
                        }
                    }   
                } else {
                    for (int j = 0; j < str.length; j++) {
                        Event remE = getEventByName(str[i-4].trim());
                        if (str[i-4].trim().equals(str[j].trim())) {
                            if (eventList.contains(remE)) {
                                eventList.remove(remE);
                            }
                            removeCalEvent(remE, "r");
                        }   
                    }
                }
                partic = new ArrayList<Integer>();
            }
        }
        return eventList;
    }
}
