import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.*;
import javax.servlet.http.*;


import java.io.IOException;
import java.io.PrintWriter;

public class NodePage extends HttpServlet implements Servlet {

    private String sundayString = "";
    private String mondayString = "";
    private String tuesdayString = "";
    private String wednesdayString = "";
    private String thursdayString = "";
    private String fridayString = "";
    private String saturdayString = "";
    private String addEvent;
    private String clearCal;
    private String day;
    private String time;
    private String duration;
    private HttpSession session;
    private Node node;
    private Event event;
    private String eventName;
    private double start;
    private double end;
    private String[] invitees;
    private int[] intInvitees;
    private String IP;

    public NodePage() {
        node = new Node(1, 3);
        invitees = new String[3];
        intInvitees = new int[3];
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
        {
            response.setContentType("text/html");
            session = request.getSession(true);

            PrintWriter out = response.getWriter();
            out.println("<html>");
            out.println("<head>");
            out.println("<h1>Node 1 Calendar</h1>");
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" +
                    "./css/style.css\">");
            out.println("</head>");
            out.println("<div class=\"content\">");
            out.println("<h3>ADD NEW EVENT TO CALENDAR</h3>");
            out.println("<body>");
            if (clearCal != null) {
                clearStrings();
            }
            String ipAddress = request.getHeader("X-FORWARDED-FOR");  
            if (ipAddress == null) {  
                   ipAddress = request.getRemoteAddr();  
            }
            out.println("<h3>IP: " + ipAddress + "</h3>");

            if (addEvent != null) {
                if (day != null && duration != null && time != null &&
                        invitees != null && eventName != null) {
                    start = Double.parseDouble(time);
                    end = start + Double.parseDouble(duration);
                    for (int i = 0; i < invitees.length; i++) {
                        intInvitees[i] = Integer.parseInt(invitees[i]);
                    }
                    event = new Event(eventName, day, start, end, intInvitees);
                    int ret = node.addCalEvent(event);
                    if (ret == 0) {
                        updateCalendar(event);
                    }
                }
            }
            addEvent = null;
            day = null;
            duration = null;
            time = null;
            invitees = null;
            eventName = null;

            out.println("<P>");
            out.print("<form action=\"\"");
            out.println("method=POST>");

            // dropdown for the day to add event
            out.println("<p>Day: <select id=\"theDay\" name=\"theDay\">");
            out.println("<option class=\"op\" value=\"Sunday\">Sunday</option>");
            out.println("<option class=\"op\" value=\"Monday\">Monday</option>");
            out.println("<option class=\"op\" value=\"Tuesday\">Tuesday</option>");
            out.println("<option class=\"op\" value=\"Wednesday\">Wednesday</option>");
            out.println("<option class=\"op\" value=\"Thursday\">Thursday</option>");
            out.println("<option class=\"op\" value=\"Friday\">Friday</option>");
            out.println("<option class=\"op\" value=\"Saturday\">Saturday</option>");
            out.println("</select>");
            // times dropdown
            out.println("Start Time: <select id=\"times\" name=\"times\">");
            for (double i = 0; i < 23.5; i+=0.5) {
                if ((i-0.5) ==(double)((int)(i))) {
                    out.println("<option class=\"op\" value="+i+">"+(int)i+":30"+
                            "</option>");
                } else {
                    out.println("<option class=\"op\" value="+i+">"+(int)i+":00"+
                            "</option>");
                }
            }
            out.println("</select><br>");
            // invitees dropdown
            out.println("Invitees:<br> <select multiple id=\"invitees\" name=\"invitees\">");
            out.println("<option value=\"2\">2</option>");
            out.println("<option value=\"3\">3</option>");
            out.println("<option value=\"4\">4</option>");
            out.println("</select><br>");
            //get name of event
            out.println("Event Title: <input type=\"text\""+ 
                    " name=\"eventName\">");
            // get duration
            out.println("Duration: <input type=\"text\" name=\"duration\"></p>");
            // button to create event
            out.println("<input type=hidden name=\"connect\" value=\"AddedEvent\">");
            out.println("<input type=\"submit\" class=\"conButton\"" + 
                    " value=\"Add Event\">"); 
            out.println("</form>");

            out.println("<hr />");
            out.println("<p class=\"cal\">Sunday</p>");
            out.print(sundayString);
            out.println("<p class=\"cal\">Monday</p>");
            out.print(mondayString);
            out.println("<p class=\"cal\">Tuesday</p>");
            out.print(tuesdayString);
            out.println("<p class=\"cal\">Wednesday</p>");
            out.print(wednesdayString);
            out.println("<p class=\"cal\">Thursday</p>");
            out.print(thursdayString);
            out.println("<p class=\"cal\">Friday</p>");
            out.print(fridayString);
            out.println("<p class=\"cal\">Saturday</p>");
            out.print(saturdayString);

            // button to reset session
            out.println("<P>");
            out.print("<form action=\"");
            out.print("\"");
            out.println("method=POST>");
            out.println("<input type=hidden name=\"clear\" value=\"clear\">");
            out.println("<input type=\"submit\" class=\"conButton\"" + 
                    " value=\"Clear Calendar\">"); 
            out.println("</form>");

            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
            out.close();
        }
    protected void doPost(HttpServletRequest request, HttpServletResponse 
            response) throws ServletException, IOException {
        addEvent = request.getParameter("connect");
        clearCal = request.getParameter("clear");
        day = request.getParameter("theDay");
        time = request.getParameter("times");
        duration = request.getParameter("duration");
        eventName = request.getParameter("eventName");
        invitees = request.getParameterValues("invitees");
        //IP = request.getRemoteAddr();
        doGet(request, response);
    }
    public void clearStrings() {
        sundayString = "";
        mondayString = "";
        tuesdayString = "";
        wednesdayString = "";
        thursdayString = "";
        fridayString = "";
        saturdayString = "";
    }

    public void updateCalendar(Event e) {
        switch(e.getDay()) {
            case "Sunday":
                sundayString += ("<p class=\"event\">" + e.getName() + "<br>" +  
                        e.toTime(e.getStart()) + " - " + e.toTime(e.getEnd()) + "</p>");
                break;
            case "Monday":
                mondayString += ("<p class=\"event\">" + e.getName() + "<br>" +  
                        e.toTime(e.getStart()) + " - " + e.toTime(e.getEnd()) + "</p>");
                break;
            case "Tuesday":
                tuesdayString += ("<p class=\"event\">" + e.getName() + "<br>" +  
                        e.toTime(e.getStart()) + " - " + e.toTime(e.getEnd()) + "</p>");
                break;
            case "Wednesday":
                wednesdayString += ("<p class=\"event\">" + e.getName() + "<br>" +  
                        e.toTime(e.getStart()) + " - " + e.toTime(e.getEnd()) + "</p>");
                break;
            case "Thursday":
                thursdayString += ("<p class=\"event\">" + e.getName() + "<br>" +  
                        e.toTime(e.getStart()) + " - " + e.toTime(e.getEnd()) + "</p>");
                break;
            case "Friday":
                fridayString += ("<p class=\"event\">" + e.getName() + "<br>" +  
                        e.toTime(e.getStart()) + " - " + e.toTime(e.getEnd()) + "</p>");
                break;
            case "Saturday":
                saturdayString += ("<p class=\"event\">" + e.getName() + "<br>" +  
                        e.toTime(e.getStart()) + " - " + e.toTime(e.getEnd()) + "</p>");
                break;
            default:
                break;
        }


    }
}
