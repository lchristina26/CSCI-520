import java.util.*;

public class Event {
    private String name;
    private int day;
    private double startTime;
    private double endTime;
    private int[] participants;

    public Event(String name, int day, double start, double end,
                    int[] participants) {
        this.name = name;
        this.day = day;
        startTime = start;
        endTime = end;
        this.participants = participants;
    }

    public String getName() {
        return name;
    }
    public int getDay() {
        return day;
    }
    public double getStart() {
        return startTime;
    }
    public double getEnd() {
        return endTime;
    }
    public int[] getParticipants() {
        return participants;
    }
    public double getEventLength() {
        return endTime - startTime;
    }
}
