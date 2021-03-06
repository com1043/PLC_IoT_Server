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
		PLC_IoT_Core.CONS.push("로우 레벨 송신자 포트 " + this.socket.getLocalPort() + " 번 활성화.");
	}
	
	public void sendData(InetAddress sendAddress, int port, byte[] data)
	{
		System.out.println("패킷 전송 호출");
		DatagramPacket packet = new DatagramPacket(data, data.length, sendAddress, port);
		try
		{
			System.out.println(sendAddress.getHostAddress().toString() + " " + port + " " + data.toString());
			this.socket.send(packet);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
