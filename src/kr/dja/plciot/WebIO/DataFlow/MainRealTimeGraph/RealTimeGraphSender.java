package kr.dja.plciot.WebIO.DataFlow.MainRealTimeGraph;

import java.util.Iterator;
import io.netty.channel.Channel;
import kr.dja.plciot.Device.IDeviceEventObserver;
import kr.dja.plciot.Device.IDeviceHandler;
import kr.dja.plciot.Device.AbsDevice.AbsDevice;
import kr.dja.plciot.Device.AbsDevice.DataFlow.AbsDataFlowDevice;
import kr.dja.plciot.WebConnector.WebServer;
import kr.dja.plciot.WebIO.WebIOProcess;
import kr.dja.plciot.WebIO.DataFlow.AbsWebFlowDataMember;

public class RealTimeGraphSender extends AbsWebFlowDataMember implements IDeviceEventObserver, Runnable
{
	private static final int SEND_DATA_INTERVAL = 200;
	private static final String SEND_KEY = "SERVER_REALTIME_DATA";
	
	private final IDeviceHandler deviceView;
	
	private final String dataKey;
	
	private boolean runFlag;
	
	private final Thread thread;
	
	private int sum;
	private int dataCount;
	
	private String sendData;
	
	public RealTimeGraphSender(Channel ch, String dataKey, IDeviceHandler deviceView)
	{
		super(ch);
		this.deviceView = deviceView;
		this.deviceView.addObserver(AbsDataFlowDevice.SENSOR_DATA_EVENT, this);
		
		this.dataKey = dataKey;
		
		this.runFlag = true;
		
		this.thread = new Thread(this);
		
		this.sum = 0;
		this.dataCount = 0;
		
		this.sendData = "0";
		
		this.thread.start();
	}
	
	@Override
	public void run()
	{
		while(this.runFlag && this.channel.isActive())
		{
			if(this.dataCount > 0)
			{
				this.sendData = Integer.toString(this.sum/this.dataCount);
				this.sum = 0;
				this.dataCount = 0;
			}
			
			this.channel.writeAndFlush(WebIOProcess.CreateDataPacket(SEND_KEY, sendData));
			try
			{
				Thread.sleep(SEND_DATA_INTERVAL);
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
		this.channel.close();
	}
	
	public void endTask()
	{
		this.thread.interrupt();
		try
		{
			this.thread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		this.deviceView.deleteObserver(this, AbsDataFlowDevice.SENSOR_DATA_EVENT);
		this.runFlag = false;
	}

	@Override
	public void deviceEvent(AbsDevice device, String key, String data)
	{
		if(!(device instanceof AbsDataFlowDevice)) return;
		AbsDataFlowDevice dataflowDevice = (AbsDataFlowDevice)device;
		int deviceData = dataflowDevice.getDeviceValue(this.dataKey);
		if(deviceData == -1) return;
		this.sum += deviceData;
		++this.dataCount;
	}
}