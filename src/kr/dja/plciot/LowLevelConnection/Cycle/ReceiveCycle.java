package kr.dja.plciot.LowLevelConnection.Cycle;

import java.net.InetAddress;
import kr.dja.plciot.PLC_IoT_Core;
import kr.dja.plciot.LowLevelConnection.PacketProcess;
import kr.dja.plciot.LowLevelConnection.PacketReceive.IPacketReceiveObservable;
import kr.dja.plciot.LowLevelConnection.PacketSend.IPacketSender;

public class ReceiveCycle extends AbsCycle implements Runnable
{	
	private byte[] receivePacket;
	private int resendCount;
	
	private Thread resiveTaskThread;
	
	private ReceiveCycle(IPacketSender sender, IPacketReceiveObservable receiver, InetAddress addr
			,int port, byte[] data, IPacketCycleUser userCallback, IEndCycleCallback endCycleCallback)
	{
		super(sender, receiver, addr, port, endCycleCallback, userCallback);
		this.resendCount = 0;
		this.receivePacket = data;
	}
	
	@Override
	public void start()
	{
		this.startTask(PacketProcess.GetPacketFULLUID(this.receivePacket));
		// �߽��ڷκ��� ��Ŷ �� ��ȯ�Ǿ� �ö����� ������ϴ�.
		this.resiveWaitTask();
		
		// �߽��ڿ��� ��Ŷ�� ��ȯ�մϴ�.
		this.reSendPhase(CycleProcess.PHASE_CHECK);
	}

	@Override
	public synchronized void packetReceive(byte[] resiveData)
	{
		this.receivePacket = resiveData;
		this.resiveTaskThread.interrupt();
		
		byte phase = PacketProcess.GetPacketPhase(this.receivePacket);
		
		if(phase == CycleProcess.PHASE_EXECUTE)
		{// ������ ���� ���� ����.
			this.endProcess();// ����Ŭ�� ���������� �Ϸ�Ǿ����ϴ�.
			return;
		}
		else if(phase == CycleProcess.PHASE_START)
		{// ������ �ִ� ����.
			if(this.resendCount > CycleProcess.MAX_RESEND)
			{// ��ġ���� ������ �ִٴ� ��ȣ�� ���� ���� - ������ �ʿ�.
				
				this.errorHandling("Too many resend error.");
				return;
			}
			++this.resendCount;
			
			// �߽��ڷκ��� ��Ŷ �� ��ȯ�Ǿ� �ö����� ������ϴ�.
			this.resiveWaitTask();
			// �߽��ڿ��� ��Ŷ�� ��ȯ �մϴ�.
			this.reSendPhase(CycleProcess.PHASE_CHECK);
			return;
		}
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
		
		this.errorHandling("Device is not responding.");
	}

	private void resiveWaitTask()
	{
		this.resiveTaskThread = new Thread(this);
		this.resiveTaskThread.start();
	}
	
	private void reSendPhase(byte phase)
	{// ������.
		PacketProcess.SetPacketPhase(this.receivePacket, phase);
		this.sender.sendData(this.addr, this.port, this.receivePacket);
	}
	
	private void endProcess()
	{
		this.notifyEndCycle();
		
		String receiveName = PacketProcess.GetPacketName(this.receivePacket);
		String receiveData = PacketProcess.GetPacketData(this.receivePacket);
		
		// ��ġ���� ������ ������ �˸��ϴ�.
		this.user.packetReceiveCallback(receiveName, receiveData);
		
		// ���� �޴��� ���ε� ����.
		
	}
	
	private void errorHandling(String str)
	{
		new Exception(str).printStackTrace();
		PLC_IoT_Core.CONS.push("Packet Send ERROR " + str);
		this.endProcess();
	}
	
	public static class ReceiveCycleBuilder extends AbsCycleBuilder
	{
		private byte[] data;
		
		public ReceiveCycleBuilder(){}
		
		public ReceiveCycleBuilder setPacketData(byte[] data)
		{
			this.data = data;
			return this;
		}
		
		
		public ReceiveCycle getInstance()
		{
			return new ReceiveCycle(sender, receiver, addr, port, data, user, endCycleCallback);
		}
	}
}
