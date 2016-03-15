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
    public byte[] convertTo1D(int[][] arr) {
        int count = 0;
        String str = "";
        for (int i = 0; i < (arr.length*4); i++) {
            while (count < 4) {
                str+= (arr[i][count] + " ");
                count++;
            }
            count = 0;
        }
        return str.getBytes();
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
}
