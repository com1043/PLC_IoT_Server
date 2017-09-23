package kr.dja.plciot.DeviceConnection.Cycle;

import java.net.InetAddress;
import java.util.Map;

import kr.dja.plciot.Device.Device;
import kr.dja.plciot.DeviceConnection.PacketProcess;
import kr.dja.plciot.DeviceConnection.PacketReceive.IPacketReceiveObservable;
import kr.dja.plciot.DeviceConnection.PacketReceive.IPacketReceiveObserver;
import kr.dja.plciot.DeviceConnection.PacketReceive.ReceiveController;
import kr.dja.plciot.DeviceConnection.PacketSend.IPacketSender;
import kr.dja.plciot.DeviceConnection.PacketSend.SendController;

public class ReceiveCycle implements Runnable, IPacketReceiveObserver
{
	private final IPacketSender sender;
	private final IPacketReceiveObservable receiver;
	private final IPacketCycleController deviceCallback;
	
	private final InetAddress addr;
	private final String uuid;
	private byte[] receivePacket;
	private int resendCount;
	private boolean taskState;
	
	private Thread resiveTaskThread;
	
	public ReceiveCycle(IPacketSender sender, IPacketReceiveObservable receiver, InetAddress addr
			, byte[] data, IPacketCycleController deviceCallback)
	{
		this.resendCount = 0;
		this.taskState = false;
		
		this.sender = sender;
		this.receiver = receiver;
		this.deviceCallback = deviceCallback;
		
		this.addr = addr;
		this.receivePacket = data;
		this.uuid = PacketProcess.GetPacketFULLUID(data);
		
		// ���� �޴����� �ش� ����Ŭ�� ���ε� �մϴ�.
		this.receiver.addObserver(this.uuid, this);
		
		// �߽��ڷκ��� ��Ŷ �� ��ȯ�Ǿ� �ö����� ������ϴ�.
		this.resiveWaitTask();
		
		// �߽��ڿ��� ��Ŷ�� ��ȯ�մϴ�.
		this.reSendPhase();
	}

	@Override
	public synchronized void packetReceive(byte[] resiveData)
	{
		this.receivePacket = resiveData;
		this.resiveTaskThread.interrupt();
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
			byte phase = PacketProcess.GetPacketPhase(this.receivePacket);
			
			if(phase == CycleProcess.PHASE_EXECUTE)
			{// ������ ���� ���� ����.
				this.taskState = true;
				this.endProcess();// ����Ŭ�� ���������� �Ϸ�Ǿ����ϴ�.
				return;
			}
			else if(phase == CycleProcess.PHASE_START)
			{// ������ �ִ� ����.
				if(this.resendCount < CycleProcess.MAX_RESEND)
				{// ��ġ���� ������ �ִٴ� ��ȣ�� ���� ���� - ������ �ʿ�.
					++this.resendCount;
					
					// �߽��ڷκ��� ��Ŷ �� ��ȯ�Ǿ� �ö����� ������ϴ�.
					this.resiveWaitTask();
					// �߽��ڿ��� ��Ŷ�� ��ȯ �մϴ�.
					this.reSendPhase();
					return;
				}
				else
				{// �߽��ڰ� ���� �������� �ʴ� ����. (������ ���� Ƚ�� �ʰ�)
					new Exception("Device is not responding").printStackTrace();
					this.endProcess();// task ERROR.
					return;
				}
			}
		}
		
		this.taskState = false;
		this.endProcess();// ����Ŭ �ð� ���� ����.
	}

	private void resiveWaitTask()
	{
		this.resiveTaskThread = new Thread(this);
		this.resiveTaskThread.start();
	}
	
	private void reSendPhase()
	{// ������.
		this.sender.sendData(this.addr, this.receivePacket);
	}
	
	private void endProcess()
	{
		String receiveName = PacketProcess.GetPacketName(this.receivePacket);
		String receiveData = PacketProcess.GetPacketData(this.receivePacket);
		
		// ��ġ���� ������ ������ �˸��ϴ�.
		this.deviceCallback.packetReceiveCallback(receiveName, receiveData);
		
		// ���� �޴��� ���ε� ����.
		this.receiver.deleteObserver(this.uuid);
	}
}
