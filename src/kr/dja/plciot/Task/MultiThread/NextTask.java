package kr.dja.plciot.Task.MultiThread;

import kr.dja.plciot.Task.Lockable;
import kr.dja.plciot.Task.TaskLock;

public class NextTask extends Lockable
{// ���� �۾����� �ش� �۾� �����ڿ��� �۾��� ��Ʈ�� �� �� �ִ� �������̽� ����.
	
	private final MultiThreadTaskOperator oper;
	private boolean executeTaskFlag;
	private boolean lockTaskFlag;
	
	NextTask(MultiThreadTaskOperator oper)
	{
		this.oper = oper;
		this.executeTaskFlag = false;
		this.lockTaskFlag = false;
	}
	
	public void insertTask(IMultiThreadTaskCallback callback)
	{
		this.oper.insertTask(callback);
	}
	
	public void nextTask()
	{// �� �۾� �������� �۾� ������ ����ȭ �Ǿ� ������� ����մϴ�.
	 // �� �޼ҵ带 ȣ���� ��� ���� �۾��� ready ���·� �ٲߴϴ�.
		
		if(this.executeTaskFlag)
		{// �۾��� �̹� ���� �۾����� �Ѿ����.
			
			new Exception("Task Already Executed").printStackTrace();
			return;
		}
		if(this.lockTaskFlag)
		{// ���� �۾� ������ �� Ŭ������ ���� �Ǿ�����.
			
			new Exception("Task Is Lock Task").printStackTrace();
			return;
		}
		this.privateNextTask();
	}
	
	@Override
	public TaskLock createLock()
	{// �� Ŭ������ ���� �۾� ������ �����ϴ� �޼ҵ� �Դϴ�.
	 // ���� Ǯ���� �ڵ����� ���� �۾��� �����ϸ�, nextTask�� ���� �۾��� ������ �� �����ϴ�.
	 // �� �۾� �������� ���� �������� �۾��� ��� �������� ���� �۾����� �Ѿ�� �Ҷ� ����մϴ�.
		
		this.lockTaskFlag = true;
		TaskLock lock = super.createLock();
		lock.lock();
		return lock;
	}
	
	private void privateNextTask()
	{
		this.oper.nextTask();
		this.executeTaskFlag = true;
	}
	
	@Override
	protected void unLock()
	{// ���� �۾��� �����մϴ�.
		this.privateNextTask();
	}
	
	public void error(Exception e, String message)
	{
		this.oper.error(e, message);
	}
}
