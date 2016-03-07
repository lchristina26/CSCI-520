import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.*;

public class Run 
{
    private static boolean beginning = true;
    private static int countUpdates = 0;
    private static DatagramSocket recvSock = null;
    private static long totalTime = 30000000000L;
    private static int timeCount = 0;

    public Node() {}

    public static void main(String args[]) throws Exception {
        String nodeID = args[0];
        int port = Integer.parseInt(args[1]);

        nodeID = getRouterId();
        port = getPortNum();

        System.out.println("Router " + nodeID + " is running on port "
                + port);
        runRouter(nodeID, port);
    }
    // packets
    // Always listen for packets to receive
    private static void runRouter(String nodeID, int port) throws Exception {
        String content = "stuff to send";
        boolean first = true;

        long startTime = System.nanoTime();
        while (true) {
            countUpdates++;
            char tempRouterID = receivePackets(nodeID, port);
            if (beginning) {
                Thread.sleep(7000);
                beginning = false;
            } else {
                Thread.sleep(1000);
            }    
            sendPacket(nodeID, port, content.getBytes());
            if (System.nanoTime() - startTime >= totalTime && timeCount == 0) {
                timeCount++;
            }
        }
    }

    private static char receivePackets(String nodeID, int port)
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

    private static void sendPacket(String nodeID, int port, byte[] data)
        throws Exception {
            DatagramSocket sendSock = new DatagramSocket();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length,
                    InetAddress.getByName(nodeID, port);
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
