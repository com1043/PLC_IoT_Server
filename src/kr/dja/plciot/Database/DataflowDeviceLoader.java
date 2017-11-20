package kr.dja.plciot.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.dja.plciot.PLC_IoT_Core;
import kr.dja.plciot.Device.IDeviceEventObserver;
import kr.dja.plciot.Device.IDeviceHandler;
import kr.dja.plciot.Device.AbsDevice.AbsDevice;
import kr.dja.plciot.Task.MultiThread.IMultiThreadTaskCallback;
import kr.dja.plciot.Task.MultiThread.NextTask;
import kr.dja.plciot.Task.MultiThread.TaskOption;

public class DataflowDeviceLoader implements IMultiThreadTaskCallback, Runnable, IDeviceEventObserver
{
	private final int DB_STORE_INTEVAL = 2000;
	
	private final IDatabaseHandler dbHandler;
	private final IDeviceHandler deviceHandler;
	
	private boolean runFlag;
	private Thread thread;

	private NextTask nextTask;
	
	private Map<String, List<String>> deviceValueMap;
	
	public DataflowDeviceLoader(IDatabaseHandler dbHandler, IDeviceHandler deviceHandler)
	{
		this.dbHandler = dbHandler;
		this.deviceHandler = deviceHandler;
		
		this.thread = new Thread(this);
		
		this.deviceValueMap = new HashMap<String, List<String>>();
	}
	
	@Override
	public void run()
	{
		PLC_IoT_Core.CONS.push("�ǽð� ������ Ǫ�� ������ Ȱ��ȭ.");
		this.nextTask.nextTask();// ���۽� �帧 �ѱ�.
		while(this.runFlag)
		{
			PLC_IoT_Core.CONS.push("��� �Ϸ�.");
			try
			{
				Thread.sleep(DB_STORE_INTEVAL);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		PLC_IoT_Core.CONS.push("�ǽð� ������ Ǫ�� ������ ��Ȱ��ȭ.");
		this.nextTask.nextTask();// ������ �帧 �ѱ�.
	}
	
	@Override
	public void deviceEvent(AbsDevice device, String key, String data)
	{
		
	}
	
	private void start(NextTask nextTask)
	{
		PLC_IoT_Core.CONS.push("�ǽð� ������ Ǫ�� ������ ����.");
		ResultSet rs = this.dbHandler.sqlQuery("select * from value_by_device_type;");
		try
		{
			while(rs.next())
			{
				String deviceType = rs.getString(0);
				System.out.println(deviceType);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		this.runFlag = true;
		this.nextTask = nextTask;
		this.thread.start();
	}
	
	private void shutdown(NextTask nextTask)
	{
		PLC_IoT_Core.CONS.push("�ǽð� ������ Ǫ�� ������ ���� ����.");
		this.nextTask = nextTask;
		this.runFlag = false;
		this.thread.interrupt();
	}
	
	@Override
	public void executeTask(TaskOption option, NextTask nextTask)
	{
		if(option == TaskOption.START)
		{
			this.start(nextTask);
		}
		else if(option == TaskOption.SHUTDOWN)
		{
			this.shutdown(nextTask);
		}
	}
}
class DeviceValue
{
	private int[] values;
}