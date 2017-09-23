package kr.dja.plciot.DeviceConnection.Cycle;

import java.net.InetAddress;

import kr.dja.plciot.PLC_IoT_Core;
import kr.dja.plciot.DeviceConnection.PacketProcess;
import kr.dja.plciot.DeviceConnection.PacketReceive.IPacketReceiveObservable;
import kr.dja.plciot.DeviceConnection.PacketReceive.IPacketReceiveObserver;
import kr.dja.plciot.DeviceConnection.PacketSend.IPacketSender;

public class SendCycle implements Runnable, IPacketReceiveObserver
{
	private final IPacketSender sender;
	private final IPacketReceiveObservable receiver;
	private final IPacketCycleController deviceCallback;
	
	private final InetAddress addr;
	private final String uuid;
	private byte[] fullPacket;
	private byte[] packetHeader;
	private int resendCount;
	private boolean taskState;
	
	private Thread resiveTaskThread;
	
	public SendCycle(IPacketSender sender, IPacketReceiveObservable receiver,InetAddress addr
			,String macAddr, String name, String data, IPacketCycleController deviceCallback)
	{
		this.resendCount = 0;
		this.taskState = false;
		
		this.sender = sender;
		this.receiver = receiver;
		this.deviceCallback = deviceCallback;
		
		this.addr = addr;
		
		this.uuid = PacketProcess.CreateFULLUID(macAddr);
		this.packetHeader = PacketProcess.CreatePacketHeader(this.uuid);
		this.fullPacket = PacketProcess.CreateFullPacket(this.packetHeader, name, data);
		
		// ���� �޴����� �ش� ����Ŭ�� ���ε� �մϴ�.
		this.receiver.addObserver(this.uuid, this);
		
		// �߽��ڷκ��� ��Ŷ �� ��ȯ�Ǿ� �ö����� ������ϴ�.
		this.sendWaitTask();
		
		// ��Ŷ�� �����մϴ�.
		this.reSendPhase(this.fullPacket, CycleProcess.PHASE_START);
	}
	
	@Override
	public synchronized void packetReceive(byte[] receivePacket)
	{// ��Ŷ�� ���� �����϶� ��Ŷ�� �˻�.
		this.resiveTaskThread.interrupt();
		
		int receivePacketSize = PacketProcess.GetPacketSize(receivePacket);
		if(receivePacketSize != this.fullPacket.length)
		{
			this.errorHandling();
			return;
		}
		
		if(PacketProcess.GetPacketPhase(receivePacket) != CycleProcess.PHASE_CHECK)
		{
			this.errorHandling();
			return;
		}
		
		for(int i = 0; i < receivePacketSize; ++i)
		{
			if(receivePacket[i] != this.fullPacket[i])
			{
				if(this.resendCount > CycleProcess.MAX_RESEND)
				{
					this.errorHandling();
					return;
				}
				++this.resendCount;
				
				this.reSendPhase(this.fullPacket, CycleProcess.PHASE_START);
				return;
			}
		}
		
		this.reSendPhase(this.packetHeader, CycleProcess.PHASE_EXECUTE);
	}
	
	@Override
	public void run()
	{
		try
		{
			// ������ ���ͷ�Ʈ�� �ɸ� ������ ����մϴ�.
			// ���� ���ͷ�Ʈ�� �ɸ��� ������ �ð� �ʰ�.
			Thread.sleep(CycleProcess.TIMEOUT);
		}
		catch (InterruptedException e)
		{
			return;
		}
		
		this.taskState = false;
		this.endProcess();// ����Ŭ �ð� ���� ����.
	}
	
	private void sendWaitTask()
	{
		this.resiveTaskThread = new Thread(this);
		this.resiveTaskThread.start();
	}
	
	private void reSendPhase(byte[] packet, byte phase)
	{// ������.
		PacketProcess.SetPacketPhase(packet, phase);
		this.sender.sendData(this.addr, packet);
	}
	
	private void endProcess()
	{
		String receiveName = PacketProcess.GetPacketName(this.fullPacket);
		String receiveData = PacketProcess.GetPacketData(this.fullPacket);
		
		// ��ġ���� ������ ������ �˸��ϴ�.
		this.deviceCallback.packetSendCallback(this.taskState, receiveName, receiveData);
		
		// ���� �޴��� ���ε� ����.
		this.receiver.deleteObserver(this.uuid);
	}
	
	private void errorHandling()
	{
		PLC_IoT_Core.CONS.push("Packet Send ERROR");
	}

}
