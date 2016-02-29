import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.*;

public class Node 
{
    private static boolean beginning = true;
    private static int countUpdates = 0;
    private static DatagramSocket recvSock = null;
    private static long totalTime = 30000000000L;
    private static int timeCount = 0;

    public Node() {}

    public static void main(String args[]) throws Exception {
        int port_num;
        char routerId;
        
        routerId = getRouterId();
        port_num = getPortNum();

        System.out.println("Router " + routerId + " is running on port "
                + port_num);
        runRouter(routerId, port_num);
    }


    private static int getPortNum() throws IOException {
        while (true) {
            BufferedReader inFromUser = new BufferedReader(
                    new InputStreamReader(System.in));
            System.out.print("Please enter the port to connect to: ");
            String input = inFromUser.readLine();
            if (input.length() > 0)
                return Integer.parseInt(input);
        }
    }
    private static char getRouterId() throws IOException {
        while (true) {
            BufferedReader inFromUser = new BufferedReader(
                    new InputStreamReader(System.in));
            System.out.print("Please enter the router's ID: ");
            String input = inFromUser.readLine();
            if (input.length() > 0)
                return input.charAt(0);
        }
    }

    // Main method; If first time, or bellmanford indicates dv update, send
    // packets
    // Always listen for packets to receive
    private static void runRouter(int routerId, int port) throws Exception {
        String content = "stuff to send";
        boolean first = true;

        long startTime = System.nanoTime();
        while (true) {
            countUpdates++;
            char tempRouterID = receivePackets(routerId, port);
            if (beginning) {
                Thread.sleep(7000);
                beginning = false;
            } else {
                Thread.sleep(1000);
            }    
            sendPacket(routerId, port, content.getBytes());
            if (System.nanoTime() - startTime >= totalTime && timeCount == 0) {
                timeCount++;
            }
        }
    }

    private static char receivePackets(int routerId, int port)
        throws IOException {
            char toRet = 'N'; //the information to be returned from Node X
            while (true) {
                try {
                    if (recvSock == null)
                        recvSock = new DatagramSocket(port);
                    byte[] data = new byte[1024];
                    DatagramPacket recvPacket = new DatagramPacket(data,
                            data.length);
                    recvSock.setSoTimeout(500);
                    recvSock.receive(recvPacket);

                    if (recvPacket.getLength() != 4)
                        System.out
                            .println("You are reading more than 4 bytes. Bytes read = "
                                    + recvPacket.getLength());

                    int[] distanceRecvd = new int[recvPacket.getLength()-1];
                    for (int i = 0; i < recvPacket.getLength()-1; i++)
                        distanceRecvd[i] = (int) data[i];

                    char routerChar = (char)data[recvPacket.getLength()-1];
                } catch (SocketTimeoutException se) {
                    // timeout expired
                    return toRet;
                }
            }
        }

    private static void sendPacket(int routerId, int port, byte[] data)
        throws Exception {
            DatagramSocket sendSock = new DatagramSocket();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length,
                    InetAddress.getByName("localhost"), port);
            sendSock.send(sendPacket);
            sendSock.close();
        }

    private static byte[] toByteArray(int[] convertMe) {
        byte[] ret = new byte[convertMe.length];
        for (int i = 0; i < convertMe.length; i++)
            ret[i] = (byte) convertMe[i];
        return ret;
    }
}
