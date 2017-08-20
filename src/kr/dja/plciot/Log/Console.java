package kr.dja.plciot.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import kr.dja.plciot.Task.MultiThread.MultiThreadTaskOperator;
import kr.dja.plciot.Task.MultiThread.IMultiThreadTaskCallback;
import kr.dja.plciot.Task.MultiThread.TaskOption;

public class Console extends Observable implements IMultiThreadTaskCallback
{// �ܼ� �α� ���� Ŭ����.
	private LogSaver logSaver;
	
	private int numOfLog;
	private List<ConsoleMessage> consoleList;
	
	public Console()
	{
		this.logSaver = new LogSaver(this);
		this.numOfLog = 0;
		this.consoleList = Collections.synchronizedList(new ArrayList<ConsoleMessage>());
	}
	
	public void push(String message)
	{
		this.push(message, null);
	}
	
	public void push(String message, Object sender)
	{
		ConsoleMessage messageObj = new ConsoleMessage(sender, message, this.numOfLog++);
		this.consoleList.add(messageObj);
		
		System.out.println(messageObj.getMessage());
		
		this.setChanged();
		this.notifyObservers(messageObj);
	}

	@Override
	public void executeTask(TaskOption option, MultiThreadTaskOperator operator)
	{
		if(option == TaskOption.SHUTDOWN)
		{//TODO �α� ���� ó��
			operator.nextTask();
		}
	}

}