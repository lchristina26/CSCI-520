import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.*;
import java.nio.charset.Charset;


public class Run 
{
    private static boolean beginning = true;
    private static int countUpdates = 0;
    private static DatagramSocket recvSock = null;
    private static long totalTime = 30000000000L;
    private static int timeCount = 0;

    public Run() {}

    // packets
    // Always listen for packets to receive
    public static String receivePackets(int port)
        throws IOException {
            StringBuilder str = new StringBuilder();
            while (true) {
                try {
                    if (recvSock == null)
                        recvSock = new DatagramSocket(port);
                    byte[] data = new byte[1024];
                    DatagramPacket recvPacket = new DatagramPacket(data,
                            data.length);
                    recvSock.setSoTimeout(500);
                    recvSock.receive(recvPacket);

                    if (recvPacket.getLength() != 4) {
                        InputStreamReader input = new InputStreamReader(
                                new ByteArrayInputStream(data), Charset.forName("UTF-8"));
                        for (int value; (value = input.read()) != -1; )
                            str.append((char) value);
                    }
                } catch (SocketTimeoutException se) {
                    // timeout expired
                    return str.toString(); //toRet;
                }
            }
        }

    public static void sendPacket(int nodeID, byte[] data)
        throws Exception 
        {
            int port;
            String ip;
            if (nodeID == 1) {
                port = 11111;
                ip = "52.87.126.232";
            } else if (nodeID == 2) {
                port = 11112;
                ip = "52.87.110.153";
            }else if (nodeID == 3) {
                port = 11113;
                ip = "52.23.44.51";
            } else {
                port = 11114;
                ip = "52.200.9.240";
            }
            DatagramSocket sendSock = new DatagramSocket();

            DatagramPacket sendPacket = new DatagramPacket(data, 
                    data.length, InetAddress.getByName(ip), port);
            sendSock.send(sendPacket);
            sendSock.close();
        }
    public static void sendPacket(String[] nodeIDs, int[] ports, byte[] data)
        throws Exception 
        {
            DatagramSocket sendSock = new DatagramSocket();

            for (int i = 0; i < nodeIDs.length; i++) {
                DatagramPacket sendPacket = new DatagramPacket(data, 
                        data.length, InetAddress.getByName(nodeIDs[i]), ports[i]);
                sendSock.send(sendPacket);
                sendSock.close();
            }
        }
}
