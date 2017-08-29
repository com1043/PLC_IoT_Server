package kr.dja.plciot.Device.Connection;

import kr.dja.plciot.Device.Connection.PacketReceive.IPacketReceiveObservable;

import java.util.Map;

import kr.dja.plciot.Device.IDevicePacketReceiveObserver;
import kr.dja.plciot.Device.Connection.PacketReceive.IPacketReceiveObserver;

public class ReceiveCycle implements Runnable, IPacketReceiveObserver
{
	private final IPacketReceiveObservable receiveManager;
	private final IDevicePacketReceiveObserver deviceCallback;
	
	private final String uuid;
	private byte[] ReceivePacket;
	private int resendCount;
	private boolean taskState;
	
	private Thread resiveTaskThread;
	
	public ReceiveCycle(IPacketReceiveObservable receiveManager, byte[] data, IDevicePacketReceiveObserver deviceCallback)
	{
		this.resendCount = 0;
		this.taskState = false;
		
		this.receiveManager = receiveManager;
		this.deviceCallback = deviceCallback;
		
		this.ReceivePacket = data;
		this.uuid = PacketProcess.GetPacketFULLUID(data);
		
		// ���� �޴����� �ش� ����Ŭ�� ���ε� �մϴ�.
		this.receiveManager.addObserver(this.uuid, this);
		
		// �߽��ڷκ��� ��Ŷ �� ��ȯ�Ǿ� �ö����� ������ϴ�.
		this.resiveWaitTask();
		
		// �߽��ڿ��� ��Ŷ�� ��ȯ�մϴ�.
		this.reSendPhase();
	}
	
	@Override
	public void packetResive(byte[] resivePacket)
	{// ��Ŷ�� ���� �����϶�.
		this.ReceivePacket = resivePacket;
		this.resiveTaskThread.interrupt();
	}
	
	@Override
	public void run()
	{
		try
		{
			// ������ ���ͷ�Ʈ�� �ɸ� ������ ����մϴ�.
			// ���� ���ͷ�Ʈ�� �ɸ��� ������ �ð� �ʰ�.
			Thread.sleep(PacketProcess.TIMEOUT);
		}
		catch (InterruptedException e)
		{
			byte phase = PacketProcess.GetPacketPhase(this.ReceivePacket);
			
			if(phase == PacketProcess.PHASE_EXECUTE)
			{// ������ ���� ���� ����.
				this.taskState = true;
				this.endProcess();// ����Ŭ�� ���������� �Ϸ�Ǿ����ϴ�.
				return;
			}
			else if(phase == PacketProcess.PHASE_SEND)
			{// ������ �ִ� ����.
				if(this.resendCount < PacketProcess.MAX_RESEND)
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
	
	public boolean getResiveState()
	{
		return this.taskState;
	}
	
	private void resiveWaitTask()
	{
		this.resiveTaskThread = new Thread(this);
		this.resiveTaskThread.start();
	}
	
	private void reSendPhase()
	{// ������.
		
	}
	
	private void endProcess()
	{
		String receiveName = PacketProcess.GetPacketName(this.ReceivePacket);
		Map<String, String> receiveData = PacketProcess.GetPacketData(this.ReceivePacket);
		
		// ��ġ���� ������ ������ �˸��ϴ�.
		this.deviceCallback.ReceiveData(receiveName, receiveData, this.taskState);
		
		// ���� �޴��� ���ε� ����.
		this.receiveManager.deleteObserver(this.uuid);
	}
}
