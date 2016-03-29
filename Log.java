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

    public void removeLE(Event e) {
        for (int i = 0; i < logEvents.size(); i++) {
            if (logEvents.get(i).event.getName().contains(e.getName())) {
                logEvents.remove(i);
            }
        }
    }
    public ArrayList<LogEvent> getLog() {
        return logEvents;
    }
}
