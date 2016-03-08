import java.util.*;

public class Log {
    ArrayList<LogEvent> logEvents;

    public Log() {
        logEvents = new ArrayList<LogEvent>();
    }
    
    public void updateLog(int indel, Event e, int clock, int nodeID) {
        LogEvent newEvent = new LogEvent();
        newEvent.indel = indel;
        newEvent.event = e;
        newEvent.clock = clock;
        newEvent.nodeID = nodeID;
        logEvents.add(newEvent);
    }

    public static class LogEvent {
        public int indel;
        public Event event;
        public int clock;
        public int nodeID;

        public LogEvent() {}
    }
}
