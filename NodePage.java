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
    private String[] dayStrings;
    private String addEvent;
    private String clearCal;
    private String day;
    private String time;
    private String duration;
    private HttpSession session;
    private Node node;
    private Event event;
    private Event newE;
    private String eventName;
    private double start;
    private double end;
    private String[] invitees;
    private int[] intInvitees;
    private String IP;
    private Run client;
    private int[] ports;
    private String[] ips;
    private String recvd = "";
    private boolean collision;
    private boolean sameDay;
    byte[] collisionBytes = ("COLLISION").getBytes();
    private int[][] table;
    private String tableStr;
    private String delName;
    private String delEvent;
    private String check;
    private String startAvail;
    private String durAvail;
    private String idAvail;
    private String availability;

    public NodePage() {
        node = new Node(4, 4);
        invitees = new String[3];
        intInvitees = new int[3];
        collision = false;
        sameDay = true;
        dayStrings = new String[7];
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
        {
            int ret = 0;
            response.setContentType("text/html");
            session = request.getSession(true);

            PrintWriter out = response.getWriter();
            out.println("<html>");
            out.println("<head>");
            out.println("<h1>Node 4 Calendar</h1>");
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
                    ports = new int[invitees.length];
                    ips = new String[invitees.length];
                    for (int i = 0; i < invitees.length; i++) {
                        intInvitees[i] = Integer.parseInt(invitees[i]);
                        ips[i] = getIP(invitees[i]);
                        if (!ips[0].equals("None"))
                            ports[i] = getPort(intInvitees[i]);
                    }
                    event = new Event(eventName, day, start, end, intInvitees);
                    // try to add event to the node's calendar
                    if (!node.containsEvent(event)) {
                        ret = node.addCalEvent(event);
                        if (ret == 0) {
                            updateCalendar(event);
                            tableStr = convertTo1D(node.get2DTT());
                            // convert event to string for sending
                            if (!ips[0].contains("None"))
                            {
                                byte[] byteStr = node.toString(event, tableStr).
                                    getBytes();
                                try{
                                    client.sendPacket(ips, ports, byteStr);
                                } catch(Exception e) {
                                    out.println("<h3 class=\"red\">BAD" +
                                            " CONNECTION!</h3>");
                                }
                            }
                        } else if (ret == -1) {
                            out.println("<script>");
                            out.println("alert(\"Time Slot Full!" +
                                    " Pick new time.\")");
                            out.println("</script>");
                            node.removeCalEvent(event);
                        } else {
                            node.removeCalEvent(event);
                        }
                    }
                }
            }
            // check availability was clicked
            if (check != null) {
                String[] ids = idAvail.split("\\s+");
                double s = Double.parseDouble(startAvail.trim());
                double d = Double.parseDouble(durAvail.trim());
                for (int i = 0; i < node.getOtherCals().length; i++) {
                    if (node.getOtherCals()[i] != null) {
                        if (node.checkOtherAvail(i, s, d))
                            availability += (i + " is available!\n");
                        else
                            availability += (i + " is NOT available!\n");
                    }
                }
            }
            // If the delete button was clicked, attempt to delete event
            if (delEvent != null) {
                Event toDelete = node.getEventByName(delName);
                if (toDelete != null) {
                    int[] delIDs = toDelete.getParticipants();
                    String[] delIPs = new String[delIDs.length];
                    int[] delPorts = new int[delIDs.length];
                    for (int i = 0; i < delIDs.length; i++) {
                        delIPs[i] = getIP(Integer.toString(delIDs[i]));
                        delPorts[i] = getPort(delIDs[i]);
                    }
                    String del = toDelete.toDelString();
                    node.removeCalEvent(toDelete);
                    try{
                        client.sendPacket(delIPs, delPorts, del.getBytes());
                    } catch(Exception e) {
                        out.println("<h3 class=\"red\">BAD" +
                                " CONNECTION: Delete Event Action!</h3>");
                    }
                }
            }
            addEvent = null;
            day = null;
            duration = null;
            time = null;
            invitees = null;
            eventName = null;
            // read for incoming data
            recvd = client.receivePackets(11114);
            if (recvd != null) {
                String[] newStr = recvd.split("\\s+");
                if (recvd.contains("COLLISION")) {
                    node.removeCalEvent(newStr[1].trim());
                } else if (recvd.contains("DELETE")) {
                    Event delEvent = node.getEventByName(newStr[1].trim());
                    if (delEvent != null) {
                        node.removeCalEvent(delEvent);
                    }
                } else if (newStr.length >= 7) {
                    intInvitees[0] = Integer.parseInt(newStr[4].trim());
                    intInvitees[1] = Integer.parseInt(newStr[5].trim());
                    intInvitees[2] = Integer.parseInt(newStr[6].trim());
                    newE = new Event(newStr[0], newStr[1],
                            Double.parseDouble(newStr[2]),
                            Double.parseDouble(newStr[3]), intInvitees);
                    ret = node.addCalEvent(newE);
                    if (ret == 0) {
                        updateCalendar(newE);
                        out.println("<p>"+ newStr[0] + " " + newStr[1] + 
                                " " + newStr[2]+" "+newStr[3]+" "+newStr[4]+" "+
                                newStr[5]+ " " + newStr[6]+" "+newStr[7]+"</p>");
                        table = convertTo2D(recvd);
                        if (table[0].length != 4) {
                            out.println("<h3 class=\"red\">BAD TABLE!</h3>");
                        } else {
                            node.updateTT(table, Integer.parseInt(newStr[4]
                                        .trim()));
                        }
                    } else if (ret == -1) {
                        String collision = "COLLISION " + newE.getName();
                        byte[] coll = collision.getBytes();
                        String id = newStr[7];
                        String[] singleIP = {getIP(id)};
                        int[] singlePort = {getPort(Integer.parseInt(id.trim()))};
                        try {
                            client.sendPacket(singleIP, singlePort, coll);
                            node.removeCalEvent(newE);
                        } catch (Exception e) {
                            out.println("<h3 class=\"red\">CANT SEND</h3>");
                        }
                    } else {
                        node.removeCalEvent(newE);
                    }
                } 
            }
            dayStrings = node.getCalendar(); //update days to post to cal
            out.println("<P>");
            out.print("<form action=\"\"");
            out.println("method=POST>");
            
            // check if users are available to add event
            out.println("<p>Event Start Time:</p><input type=\"text\"" +
                            " name=\"checkStart\">");
            out.println("<p>Event Duration:</p><input type=\"text\"" +
                            " name=\"checkDuration\">");
            out.println("<p>Participants(space sep):</p><input type=\"text\"" +
                            " name=\"checkIDs\">");
            out.println("<input type=hidden name=\"checkAvail\" value=\"checkAvail\">");
            out.println("<input type=\"submit\" class=\"conButton\"" + 
                    " value=\"Check Availability\">"); 
            out.println(availability);
            availability = "";
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
            out.println("<option value=\"None\">None</option>");
            out.println("<option value=\"1\">1</option>");
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
            /*if (collision)
              out.println("<h3 red>COLLISION DETECTED! PICK NEW TIME.</h3>");
              if (!sameDay)
              out.println("<h3 red>Event cannot span multiple days!</h3>");*/
            out.println("<input type=hidden name=\"connect\" value=\"AddedEvent\">");
            out.println("<input type=\"submit\" class=\"conButton\"" + 
                    " value=\"Add Event\">"); 
            out.println("<hr />");
            // Delete event by name
            out.println("<h3>REMOVE EVENT FROM CALENDAR</h3>");
            out.println("<p>Event Name:</p><input type=\"text\""+ 
                    " name=\"deleteName\">");
            out.println("<input type=hidden name=\"delete\" value=\"deleteEvent\">");
            out.println("<input type=\"submit\" class=\"conButton\"" + 
                    " value=\"Delete Event\">"); 
            out.println("</form>");
            /*collision = false;
              sameDay = true;*/

            out.println("<hr />");
            out.println("<p class=\"cal\">Sunday</p>");
            out.print(dayStrings[0]);
            out.println("<p class=\"cal\">Monday</p>");
            out.print(dayStrings[1]);
            out.println("<p class=\"cal\">Tuesday</p>");
            out.print(dayStrings[2]);
            out.println("<p class=\"cal\">Wednesday</p>");
            out.print(dayStrings[3]);
            out.println("<p class=\"cal\">Thursday</p>");
            out.print(dayStrings[4]);
            out.println("<p class=\"cal\">Friday</p>");
            out.print(dayStrings[5]);
            out.println("<p class=\"cal\">Saturday</p>");
            out.print(dayStrings[6]);

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
        delName = request.getParameter("deleteName");
        delEvent = request.getParameter("delete");
        check = request.getParameter("checkAvail");
        startAvail = request.getParameter("checkStart");
        durAvail = request.getParameter("checkDuration");
        idAvail = request.getParameter("checkIDs");
        //IP = request.getRemoteAddr();
        doGet(request, response);
    }
    public void clearStrings() {
        node.resetCalendar();
        dayStrings = node.getCalendar();
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
    public String getIP(String idStr) {
        String ip;
        int id;

        if (idStr.trim().equals("None")) {
            return "None";
        }
        id = Integer.parseInt(idStr.trim());
        switch(id) {
            case 1:
                ip = "52.87.126.232";
                break;
            case 2:
                ip = "52.87.110.153";
                break;
            case 3:
                ip = "52.23.44.51";
                break;
            case 4:
                ip = "52.200.9.240";
                break;
            default:
                ip = "badIP";
                break;
        }
        return ip;
    }
    public int getPort(int id) {
        int port;
        switch(id) {
            case 1:
                port = 11111;
                break;
            case 2:
                port = 11112;
                break;
            case 3:
                port = 11113;
                break;
            case 4:
                port = 11114;
                break;
            default:
                port = 0000;
                break;
        }
        return port;

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
    public int[][] convertTo2D(String str) {
        String[] splitStr = str.split("\\s+");
        int[][] table = new int[4][4];
        if (!str.contains("Table") || splitStr.length < 25) {
            table = new int[1][1];
            table[0][0] = -1;
        } else {
            int count = 9;
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    table[i][j] = Integer.parseInt(splitStr[count].trim());
                    count++;
                }
            }
        }
        return table;
    }
}
