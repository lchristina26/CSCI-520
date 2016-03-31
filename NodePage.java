import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

public class NodePage extends HttpServlet implements Servlet {

    private String[] dayStrings;
    private String addEvent;
    private String clearCal;
    private String day;
    private String time;
    private String duration;
    private HttpSession session;
    private Node node;
    private Event event;
    private String eventName;
    private String[] invitees;
    private String IP;
    private Run client;
    private int[] ports;
    private String[] ips;
    private String delName;
    private String delEvent;
    private int myPort;
    private int[] intInvitees;
    private String getCrash;
    private String getFail;
    private String backUp;

    public NodePage() {
        node = new Node(4, 4);
        dayStrings = new String[7];
        if (node.getID() == 1)
            myPort = 11111;
        else if (node.getID() == 2)
            myPort = 11112;
        else if (node.getID() == 3)
            myPort = 11113;
        else 
            myPort = 11114;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int ret = 0;
        boolean nodeDown = false;

        response.setContentType("text/html");
        // Set refresh, autoload time as 5 seconds
        // response.addHeader("Refresh", "2");

        session = request.getSession(true);
        
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<h1>Node "+ node.getID() +" Calendar</h1>");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" +
                "./css/style.css\">");
        out.println("<script src=\"./js/my_js.js\"></script>");
        out.println("</head>");
    //    out.println("<div class=\"content\">");
        out.println("<body>");
        if (getCrash != null || getFail != null) {
            nodeDown = true;
        }
        getCrash = null;
        getFail = null;

        if (backUp != null)
            nodeDown = false;
        backUp = null;
        if (clearCal != null) {
            clearStrings();
        }
        clearCal = null;

        if (!nodeDown) {
        //check if Add Event was clicked
        if (addEvent != null) {
            addEvent = null;
            if (day != null && duration != null && time != null &&
                    invitees != null && eventName != null) {
                double start = Double.parseDouble(time);
                double end = start + Double.parseDouble(duration);
                //check if multi-day event
                if (end > 23.5) {
                    out.println("<script>");
                    out.println("alert(\"Event Can't Be Multi-Day!\")");
                    out.println("</script>");
                } else {
                    out.println(addEventFunc(start, end));
                }
            }
        }
        invitees = null;

        // If the delete button was clicked, attempt to delete event
        if (delEvent != null) {
            Event toDelete = node.getEventByName(delName.trim());
            if (toDelete != null) {
                ret = node.removeCalEvent(toDelete);
                if (ret == -3) {
                    out.println("<p>BAD CONNECTION! Resend.</p>");
                }
            }
        }
        delEvent = null;
        addEvent = null;
        delName = null;
        day = null;
        duration = null;
        time = null;
        eventName = null;
        // read for incoming data
        String recvd = "";
        recvd = client.receivePackets(myPort);
        if (!recvd.equals("")) {
            //out.println("<p>RECEIVED: "+recvd+"</p>");
            String[] newStr = recvd.split("\\s+");
            ArrayList<String[]> recStr = getRecvStr(newStr);
            for (int i = 0; i < recStr.size(); i++) {
               // out.println(recStr.get(i)[0]);
                String tableStr = getTableStr(recStr.get(i));
               // out.println("<p>TABLE:"+tableStr+"</p>");
               // out.print("<p> My TABLE: ");
               // for(int x = 0; x < node.get2DTT()[0].length; x++) {
               //     for (int y = 0; y < node.get2DTT()[0].length; y++) {
               //         out.print(node.get2DTT()[x][y]+" ");
               //     }
               // }
               // out.println("</p>");
                out.println(getRecOutput(tableStr, recStr.get(i), recvd));
               // out.println("<p>HELLO</p>");
            }
        }
        dayStrings = node.getCalendar(); //update days to post to cal
        addEvent = null;
        }
        out.println("<h3>ADD NEW EVENT TO CALENDAR</h3>");

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
        out.println("<input type=hidden name=\"connect\" value=\"AddedEvent\">");
        out.println("<input type=\"submit\" class=\"conButton\"" + 
                " value=\"Add Event\">"); 
        out.println("</form>");
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
        // Delete event by name
        out.println("<P>");
        out.print("<form action=\"\"");
        out.println("method=POST>");
        out.println("<h3>REMOVE EVENT FROM CALENDAR</h3>");
        out.println("<p>Event Name:</p><input type=\"text\""+ 
                " name=\"deleteName\">");
        out.println("<input type=hidden name=\"delete\" value=\"deleteEvent\">");
        out.println("<input type=\"submit\" class=\"conButton\"" + 
                " value=\"Delete Event\">"); 
        out.println("</form>");
        // Crash
        out.println("<P>");
        out.print("<form action=\"\"");
        out.println("method=POST>");
        out.println("<input type=hidden name=\"crash\" value=\"crashNode\">");
        out.println("<input type=\"submit\" class=\"crashButton\"" + 
                " value=\"Crash\">"); 
        out.println("</form>");
        // Fail
        out.println("<P>");
        out.print("<form action=\"\"");
        out.println("method=POST>");
        out.println("<input type=hidden name=\"fail\" value=\"failNode\">");
        out.println("<input type=\"submit\" class=\"crashButton\"" + 
                " value=\"Fail\">"); 
        out.println("</form>");
        // Bring node online
        out.println("<P>");
        out.print("<form action=\"\"");
        out.println("method=POST>");
        out.println("<input type=hidden name=\"backOn\" value=\"onNode\">");
        out.println("<input type=\"submit\" class=\"conButton\"" + 
                " value=\"Make Live\">"); 
        out.println("</form>");


        //out.println("</div>");
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
        getCrash = request.getParameter("crash");
        getFail = request.getParameter("fail");
        backUp = request.getParameter("backOn");
        doGet(request, response);
    }
    public void clearStrings() {
        node.resetCalendar();
        node.resetLog();
        node.resetTable();
        dayStrings = node.getCalendar();
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

    public int[][] convertTo2D(String str) {
        String[] splitStr = str.split("\\s+");
        int[][] table = new int[4][4];
        int count = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                table[i][j] = Integer.parseInt(splitStr[count].trim());
                count++;
            }
        }
        return table;
    }
    public String addEventFunc(double start, double end) {
        int ret;
        String str = "";
        if (!invitees[0].contains("None")) {
            intInvitees = new int[invitees.length+1];
            intInvitees[0] = node.getID();
            for (int i = 1; i < invitees.length+1; i++) {
                intInvitees[i] = Integer.parseInt(invitees[i-1]);
            }

        } else {
            intInvitees = new int[1];
            intInvitees[0] = node.getID();
        }
        event = new Event(eventName, day, start, end, intInvitees);
        // try to add event to the node's calendar
        if (!node.containsEvent(event)) {
            ret = node.addCalEvent(event);
            if (ret == -1) {
                str +=("<script>");
                str += ("alert(\"COLLISION! Updating calendar...\")");
                str += ("</script>");
                //       node.removeCalEvent(event);
            } else if (ret == -2) {
                str += ("<p>DAY OVERLAPS NOT ALLOWED!<br></p>");
                //node.removeCalEvent(event);
            } else if (ret == -3) {
                str += ("<p>BAD CONNECTION! Resend.</p>");
                node.removeCalEvent(event, "r");
            }
        }
        return str;
    }

    public ArrayList<String[]> getRecvStr(String[] rec) {
        ArrayList <String> recStr = new ArrayList<String>();
        ArrayList <String[]> recArrs = new ArrayList<String[]>();
        int count = 0;
        int arrCount = 0;
        for (int i = 0; i < rec.length; i++) {
            if (rec[i].equals("ID")) {
                count = i+1;
                while (!rec[count].contains("EVENT")) {
                    recStr.add(rec[count]);
                    count--;
                }
                //recArrs.get(arrCount) = new String[recStr.size()];
                Collections.reverse(recStr);
                recArrs.add(recStr.toArray(new String[recStr.size()]));
                arrCount++;
                recStr = new ArrayList<String>();
            }
        }
        return recArrs;
    }
    public String getRecOutput(String tableStr, String[] rec, String collide) {
        String outStr = "";
        int[][] table;
        int ret;
            if (collide.contains("COLLISION")) {
               outStr += ("<script>alert(\"COLLISION! Updating "+
                        "Calendar\")</script>");
                Event remEvent = node.getEventByName(rec[1].trim());
                node.removeCalEvent(remEvent, "r");
            } else if (rec.length >= 7) {
                ArrayList<Event> eList = node.readLog(rec);
                for (int i = 0; i < eList.size(); i++) {
                    ret = node.addCalEvent(eList.get(i), "r");
                    if (ret == 0) {
                        table = convertTo2D(tableStr);
                        if (table[0].length != 4) {
                            outStr += ("<h3 class=\"red\">BAD TABLE!</h3>");
                        } else {
                            node.updateTT(table, Integer.parseInt(
                                        rec[rec.length-1].trim()));
                        }
                    } else if (ret == -1) {
                        outStr += ("<script>alert(\"COLLISION!" +
                                "SENDING NOTIFICATION\")</script>");
                        String collision = "COLLISION "+eList.get(i).getName();
                        byte[] coll = collision.getBytes();
                        String id = rec[rec.length-1].trim();
                        String[] singleIP = {getIP(id)};
                        int[] singlePort = {getPort(Integer.parseInt(id.trim()))};
                        node.removeCalEvent(eList.get(i), "r");
                        try {
                            client.sendPacket(singleIP, singlePort, coll);
                        } catch (Exception e) {
                            outStr +=("<h3 class=\"red\">CANT SEND</h3>");
                        }
                    } else {
                        outStr += ("<script>");
                        outStr += ("alert(\"REMOVING\")");
                        outStr += ("</script>");
                        node.removeCalEvent(eList.get(i), "r");
                    }

                }
            } 
        return outStr;
    }
    public String getTableStr(String[] rec) {
        String table = "";
        int count = 0;
        for (int i = 0; i < rec.length; i++) {
            if (rec[i].equals("TABLE:")) {
                count = i+1;
                while (count < (i+1+16)) {
                    table += (rec[count] + " ");
                    count++;
                }
            }
        }
        return table;
    }
}
