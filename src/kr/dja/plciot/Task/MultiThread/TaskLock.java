package kr.dja.plciot.Task.MultiThread;

public class TaskLock
{
	private boolean lock;
	
	public void lock()
	{
		this.lock = true;
	}
	
	public void unlock()
	{
		this.lock = false;
	}
	
	public boolean getState()
	{
		return this.lock;
	}
}