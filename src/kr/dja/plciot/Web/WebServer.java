package kr.dja.plciot.Web;

import kr.dja.plciot.PLC_IoT_Core;
import kr.dja.plciot.Log.Console;
import kr.dja.plciot.Task.MultiThread.MultiThreadTaskOperator;
import kr.dja.plciot.Task.MultiThread.NextTask;
import kr.dja.plciot.Task.MultiThread.IMultiThreadTaskCallback;
import kr.dja.plciot.Task.MultiThread.TaskOption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
 

public class WebServer implements Runnable, IMultiThreadTaskCallback
{
	public static final String ROOT_DOC = "/Users/kyoungil_lee/Desktop/web";
	
	private NextTask startNextTask;

	private boolean stop = false;

	private Thread webServerThread;
	
	public WebServer()
	{
	}

	@Override
	public void run()
	{
		PLC_IoT_Core.CONS.push("�� ���� ����");
		this.startNextTask.nextTask();
		
		ServerSocket serverSocket = null;
		int port = 80;
 
		try
		{
			serverSocket = new ServerSocket(port, 1);
		}
		catch (IOException ie)
		{
			ie.printStackTrace();
			System.exit(1);
		}
 
		while (!this.stop)
		{
			Socket socket = null;
			InputStream input = null;
			OutputStream output = null;
 
			try
			{
				socket = serverSocket.accept();
				PLC_IoT_Core.CONS.push("�� ���� ��û");
				input = socket.getInputStream();
				output = socket.getOutputStream();
 
				Request request = new Request(input);
				request.parse();

				Response response = new Response(output);
				response.setRequest(request);
				response.sendStaticResource();
 
				socket.close();
 
				System.out.println(request.getUrl());
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				continue;
			}
		}
		PLC_IoT_Core.CONS.push("���� �ߴ�");
	}


	@Override
	public void executeTask(TaskOption option, NextTask nextTask)
	{
		if(option == TaskOption.START)
		{
			this.startNextTask = nextTask;
			this.webServerThread = new Thread(this);
			this.webServerThread.start();
		}
		if(option == TaskOption.SHUTDOWN)
		{
			nextTask.nextTask();
		}
	}
}
