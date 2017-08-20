package kr.dja.plciot.Task.MultiThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import kr.dja.plciot.Task.TaskLock;
import kr.dja.plciot.Task.Serial.ISerialTaskCallback;

public class MultiThreadTaskOperator
{
	private final TaskOption option;
	private ConcurrentLinkedQueue<IMultiThreadTaskCallback> taskQueue;
	private List<TaskLock> lock;
	
	public MultiThreadTaskOperator(TaskOption option)
	{
		this.option = option;
		this.taskQueue = new ConcurrentLinkedQueue<IMultiThreadTaskCallback>();
		this.lock = Collections.synchronizedList(new ArrayList<TaskLock>());
	}
	
	public MultiThreadTaskOperator(TaskOption option, IMultiThreadTaskCallback[] callbackArr)
	{
		this(option);
		
		for(IMultiThreadTaskCallback task : callbackArr)
		{
			this.taskQueue.add(task);
		}
	}
	
	public void nextTask()
	{// ������ �������� �� �޼ҵ� ȣ��.
		new Thread(()->
		{
			if(!this.taskQueue.isEmpty())
			{
				this.taskQueue.poll().executeTask(this.option, this);
			}
		}).start();
	}
	
	public void error(Exception e, String message)
	{
		System.out.println("���� �۾��� ���� �߻�");
		System.out.println(message);
		e.printStackTrace();
	}
}
