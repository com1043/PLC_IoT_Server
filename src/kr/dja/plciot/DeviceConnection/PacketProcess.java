package kr.dja.plciot.DeviceConnection;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class PacketProcess
{// ��ġ�� �ְ�޴� ��Ŷ�� ���õ� �Լ��� ����� ĸ��ȭ.

	public static void main(String args[])
    {
		String macAddr = "001F1F1F1FAA";
		String fulluid = PacketProcess.CreateFULLUID(macAddr);
		String packetName = "testPacket";
		
		HashMap<String, String> sendData = new HashMap<String, String>();
		sendData.put("key", "value");
		sendData.put("key1", "value1");
		
		byte[] data = PacketProcess.CreateDataSet();
		

		
		PacketProcess.InputPacketData(data, packetName, PacketProcess.DataMapToByte(sendData));
		
		PacketProcess.PrintDataPacket(data);
    }
	
	private static final int BYTE = 8;// 8bit = byte
	
	private static final int FIELD_TIMEID = 3;// 0 ~ 3 Byte
	private static final int FIELD_FULLUID = 9;// 4 ~ 9 Byte
	private static final int FIELD_PHASE = 10; // 10 Byte
	private static final int FIELD_NAME_SIZE = 11; // 12 ~ 32 Byte
	private static final int FIELD_TOTAL_SIZE = 12; // total 1024 Byte
	
	private static final byte DATAMAP_SYNTEX_KEY = '=';
	private static final byte DATAMAP_SYNTEX_VALUE = '\n';
	
	private static final byte NULL_VALUE = 0b00000000;
	
	public static final int TIMEOUT = 2000;
	public static final int MAX_RESEND = 3;
	
	public static void PrintDataPacket(byte[] data)
	{
		System.out.println("FullAddr   - " + PacketProcess.GetPacketFULLUID(data));
		System.out.println("MacAddr    - " + PacketProcess.GetpacketMacAddr(data));
		System.out.println("PacketName - " + PacketProcess.GetPacketName(data));
		System.out.println("Phase      - " + String.format("%02X", PacketProcess.GetPacketPhase(data)));
		Map<String, String> valueSet = PacketProcess.GetPacketData(data);
		
		System.out.println("-- DATA --");
		for(String key : valueSet.keySet())
		{
			System.out.println("key:" + key + " value:" + valueSet.get(key));
		}
	}
	
	public static byte[] CreateDataSet()
	{// ����Ÿ �� ����.
		return new byte[FIELD_TOTAL];
	}
	
	public static String CreateFULLUID(String macAddr)
	{// uuid ����.
		StringBuffer returnUUID = new StringBuffer();
		long time = System.currentTimeMillis();
		
		for(int uuidTime = 0; uuidTime < FIELD_TIMEID; ++uuidTime)
		{
			returnUUID.append(String.format("%02X", (byte)time));
			time = time >> BYTE;
		}
		
		for(int macAddrIndex = 0; macAddrIndex / 2 < FIELD_FULLUID - FIELD_TIMEID; macAddrIndex += 2)
		{
			returnUUID.append(macAddr.charAt(macAddrIndex));
			returnUUID.append(macAddr.charAt(macAddrIndex + 1));
		}
		
		return returnUUID.toString();
	}
	
	public static byte[] CreatePacketHeader(String fullUID, byte phase)
	{// ��Ŷ ����� ��Ŷ�� �����մϴ�.
		
		byte[] dataSet = new byte[FIELD_PHASE + 1];
		
		int dataSetIndex = 0;
		
		for(int uuidIndex = 0; uuidIndex / 2 < FIELD_FULLUID; uuidIndex += 2)
		{
			dataSet[dataSetIndex] = (byte)Integer.parseInt(fullUID.substring(uuidIndex, uuidIndex + 2), 16);
			++dataSetIndex;
		}
		dataSet[dataSetIndex] = phase; // ��ȣ ������ �ֱ� (index 10)
		
		return dataSet;
	}
	
	public static byte[] CreatePacketData(byte[] header, String name, String data)
	{// ��� ������ ��ģ ������ ��Ŷ�� �����մϴ�
		
		if(name.length() > Byte.MAX_VALUE)
		{
			new Exception("name is too long");
		}
		if(data.length() > Byte.MAX_VALUE)
		{
			new Exception("data is too long");
		}
		
		byte nameFieldSize = (byte)name.length();
		byte dataFieldSize = (byte)data.length();
		
		byte[] packet = new byte[header.length + 2 + nameFieldSize + dataFieldSize];
		
		for(packet)
		
		for(int dataNameIndex = 0; dataNameIndex < name.length() && dataSetIndex < FIELD_NAME; ++dataNameIndex)
		{
			dataSet[dataSetIndex] = (byte)name.charAt(dataNameIndex);
			++dataSetIndex;
		}
		
		while(dataSetIndex < FIELD_NAME)
		{
			dataSet[dataSetIndex] = NULL_VALUE;
			++dataSetIndex;
		}

		for(int sendDataPointer = 0; sendDataPointer < sendData.length && dataSetIndex < FIELD_TOTAL; ++sendDataPointer)
		{
			dataSet[dataSetIndex] = sendData[sendDataPointer];
			++dataSetIndex;
		}
		
		while(dataSetIndex < FIELD_TOTAL)
		{// ������ ���� ��� 0���� �ʱ�ȭ.
			dataSet[dataSetIndex] = NULL_VALUE;
			++dataSetIndex;
		}
	}
	
	public static byte[] DataMapToByte(Map<String, String> dataMap)
	{// ������ ���� ���̳ʸ��� ���ڵ�.
		
		StringBuffer sendDataSerialStr = new StringBuffer();
		for(String key : dataMap.keySet())
		{
			String appendStr = key+String.valueOf((char)DATAMAP_SYNTEX_KEY)+dataMap.get(key)+String.valueOf((char)DATAMAP_SYNTEX_VALUE);
			
			if(appendStr.length() + sendDataSerialStr.length() < FIELD_TOTAL - FIELD_NAME)
			{
				sendDataSerialStr.append(appendStr);
			}
			else
			{
				break;
			}
		}
		return sendDataSerialStr.toString().getBytes();
	}
	
	public static String GetPacketFULLUID(byte[] packet)
	{// ��Ŷ uuid �� �����ɴϴ�.
		
		StringBuffer fullUID = new StringBuffer();
		
		for(int uuidIndex = 0; uuidIndex  < FIELD_FULLUID; ++uuidIndex)
		{
			fullUID.append(String.format("%02X", packet[uuidIndex]));
		}
		
		return fullUID.toString();
	}

	public static String GetpacketMacAddr(byte[] packet)
	{// ��Ŷ ���ּҸ� �����ɴϴ�.
		StringBuffer macAddr = new StringBuffer();
		
		for(int macAddrPacket = FIELD_TIMEID; macAddrPacket < FIELD_FULLUID; ++macAddrPacket)
		{
			macAddr.append(String.format("%02X", packet[macAddrPacket]));
		}
		
		return macAddr.toString();
	}
	
	public static byte GetPacketPhase(byte[] packet)
	{// ��Ŷ ����� �����ɴϴ�.
		byte phase = packet[FIELD_PHASE - 1];
		return phase;				
	}
	
	public static String GetPacketName(byte[] packet)
	{// ��Ŷ ���ּҸ� �����ɴϴ�.
		StringBuffer packetName = new StringBuffer();
		
		for(int packetNameIndex = FIELD_PHASE; packetNameIndex < FIELD_NAME; ++packetNameIndex)
		{
			packetName.append((char)packet[packetNameIndex]);
		}
		
		return packetName.toString();
	}
	
	/*public static Map<String, String> GetPacketData(byte[] dataByte)
	{// ������ ����Ʈ�� ������ ������ ���ڵ�.
		Map<String, String> dataMap = new HashMap<String, String>();
		
		StringBuffer strBuf = new StringBuffer();
		String key = null, value = null;
		
		for(int dataIndex = FIELD_NAME; dataIndex < FIELD_TOTAL; ++dataIndex)
		{
			if(dataByte[dataIndex] == DATAMAP_SYNTEX_KEY)
			{
				key = strBuf.toString();
				strBuf.setLength(0);
			 	continue;
			}
			else if(dataByte[dataIndex] == DATAMAP_SYNTEX_VALUE)
			{
				value = strBuf.toString();
				strBuf.setLength(0);
				
				dataMap.put(key, value);
				continue;
			}
			strBuf.append((char)dataByte[dataIndex]);
			
		}
		return dataMap;
	}*/
	
}
