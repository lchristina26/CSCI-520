import java.util.*;

public class Event {
    private String name;
    private String day;
    private double startTime;
    private double endTime;
    private int[] participants;

    // constructor to take in individual items of event
    public Event(String name, String day, double start, double end,
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
    public String getDay() {
        return day;
    }
    public double getStart() {
        return startTime;
    }
    public String toTime(double timeDouble) {
        String timeStr;
        if ((timeDouble - 0.5) == (double)((int)(timeDouble))) {
            timeStr = (int)timeDouble + ":30";
        } else {
            timeStr = (int)timeDouble + ":00";
        }
        return timeStr;
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
