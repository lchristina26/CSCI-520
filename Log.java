import java.util.*;

public class Log {
    ArrayList<LogEvent> logEvents;

    public Log() {
        logEvents = new ArrayList<LogEvent>();
    }
    
    public void addLogEvent(Event e, int clock, int nodeID) {
        LogEvent newEvent = new LogEvent();
        newEvent.event = e;
        newEvent.clock = clock;
        newEvent.nodeID = nodeID;
        logEvents.add(newEvent);
    }

    public static class LogEvent {
        public Event event;
        public int clock;
        public int nodeID;

        public LogEvent() {}
    }
}
