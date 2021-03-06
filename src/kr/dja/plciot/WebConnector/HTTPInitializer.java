package kr.dja.plciot.WebConnector;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

public class HTTPInitializer extends ChannelInitializer<SocketChannel>
{
	private final IWebSocketRawTextObserver observer;

	public HTTPInitializer(IWebSocketRawTextObserver observer)
	{
		this.observer = observer;
	}
	
	protected void initChannel(SocketChannel socketChannel) throws Exception
	{
		ChannelPipeline pipeline = socketChannel.pipeline();
		pipeline.addLast("httpServerCodec", new HttpServerCodec());
		pipeline.addLast("httpHandler", new HttpServerHandler(this.observer));

	}
}