package kr.dja.plciot.DependManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import kr.dja.plciot.Task.ITaskCallback;

public class DependencyTaskOperator
{
	private final TaskOption option;
	private ConcurrentLinkedQueue<IDependencyTask> taskQueue;
	private List<TaskLock> lock;
	
	public DependencyTaskOperator(TaskOption option)
	{
		this.option = option;
		this.taskQueue = new ConcurrentLinkedQueue<IDependencyTask>();
		this.lock = Collections.synchronizedList(new ArrayList<TaskLock>());
	}
	
	public DependencyTaskOperator(TaskOption option, IDependencyTask[] callbackArr)
	{
		this(option);
		
		for(IDependencyTask task : callbackArr)
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
	
	public TaskLock createLock()
	{
		TaskLock lock = new TaskLock();
		lock.unlock();
		this.lock.add(lock);
		return lock;
	}
}
