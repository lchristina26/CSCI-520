import java.util.*;

public class Log {
    ArrayList<LogEvent> logEvents;

    public Log() {
        logEvents = new ArrayList<LogEvent>();
    }

    public void updateLog(String operator, Event e, int clock, int nodeID) {
        LogEvent newEvent = new LogEvent();
        newEvent.operator = operator;
        newEvent.event = e;
        newEvent.clock = clock;
        newEvent.nodeID = nodeID;
        logEvents.add(newEvent);
    }

    public ArrayList<LogEvent> getLog() {
        return logEvents;
    }
}
