package kr.dja.plciot;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import kr.dja.plciot.Database.DatabaseConnector;
import kr.dja.plciot.Device.DeviceManager;
import kr.dja.plciot.Log.Console;
import kr.dja.plciot.Task.MultiThread.MultiThreadTaskOperator;
import kr.dja.plciot.Task.MultiThread.NextTask;
import kr.dja.plciot.Task.TaskLock;
import kr.dja.plciot.Task.MultiThread.IMultiThreadTaskCallback;
import kr.dja.plciot.Task.MultiThread.TaskOption;
import kr.dja.plciot.UI.MainFrame;
import kr.dja.plciot.Web.WebServer;

public class PLC_IoT_Core implements IMultiThreadTaskCallback
{
	private static PLC_IoT_Core MainInstance;
	public static final Console CONS = new Console();
	
	public final MainFrame mainFrame;
	public final DatabaseConnector dbManager;
	public final DeviceManager deviceManager;
	public final WebServer webServer;
	
	private PLC_IoT_Core()
	{//TEST
		this.mainFrame = new MainFrame();
		this.dbManager = new DatabaseConnector();
		this.deviceManager = new DeviceManager();
		this.webServer = new WebServer();
		
		IMultiThreadTaskCallback[] startTaskArr = new IMultiThreadTaskCallback[]
				{this.dbManager, this.deviceManager, this.webServer, new TestClass(), this};
		MultiThreadTaskOperator serverStartOperator = new MultiThreadTaskOperator(TaskOption.START, startTaskArr);
		
		IMultiThreadTaskCallback[] shutdownTaskArr = new IMultiThreadTaskCallback[]
				{this.webServer, this.dbManager, CONS, this.mainFrame, this};
		MultiThreadTaskOperator serverShutdownOperator = new MultiThreadTaskOperator(TaskOption.SHUTDOWN, shutdownTaskArr);
		
		serverStartOperator.start();
		
		this.mainFrame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				serverShutdownOperator.start();
			}
		});
	}
	
	public static void main(String[] args)
	{
		MainInstance = new PLC_IoT_Core();
	}

	@Override
	public void executeTask(TaskOption option, NextTask next)
	{
		if(option == TaskOption.START)
		{
			CONS.push("���� ���� �Ϸ�");
		}
		if(option == TaskOption.SHUTDOWN)
		{
			System.out.println("���� ���� �Ϸ�");
			System.exit(0);
		}
	}
}

class TestClass implements IMultiThreadTaskCallback
{

	@Override
	public void executeTask(TaskOption option, NextTask nextTask)
	{
		TaskLock lock1 = nextTask.createLock();
		TaskLock lock2 = nextTask.createLock();
		
		new Thread(()->{
			try
			{
				Thread.sleep(3000);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lock1.unlock();
			
		}
				).start();
		new Thread(()->{
			try
			{
				Thread.sleep(2000);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lock2.unlock();
			
		}
				).start();
		
	}
	
}