package kr.dja.plciot.LowLevelConnection.PacketSend;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import kr.dja.plciot.PLC_IoT_Core;

public class UDPRawSocketSender
{
	private final DatagramSocket socket;
	
	public UDPRawSocketSender(DatagramSocket socket)
	{
		this.socket = socket;
		PLC_IoT_Core.CONS.push("�ο� ���� �۽��� ��Ʈ " + this.socket.getLocalPort() + " �� Ȱ��ȭ.");
	}
	
	public void sendData(InetAddress sendAddress, int port, byte[] data)
	{
		DatagramPacket packet = new DatagramPacket(data, data.length, sendAddress, port);
		try
		{
			this.socket.send(packet);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
