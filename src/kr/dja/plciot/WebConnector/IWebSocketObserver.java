package kr.dja.plciot.WebConnector;

import io.netty.channel.Channel;

public interface IWebSocketObserver
{
	public void websocketEvent(Channel ch,String key, String data);
	public void channelDisconnect(Channel ch);
}
