package kr.dja.plciot.Device;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class PacketProcess
{// ��ġ�� �ְ�޴� ��Ŷ�� ���õ� �Լ��� ����� ĸ��ȭ.
	
	private static final int BYTE = 8;// 8bit = byte
	
	private static final int FIELD_UUID = 4;// 0 ~ 3 Byte
	private static final int FIELD_MACADDR = 10;// 4 ~ 9 Byte
	private static final int FIELD_PHASE = 11; // 10 ~ 11 Byte
	private static final int FIELD_NAME = 32; // 12 ~ 32 Byte
	private static final int FIELD_TOTAL = 1024; // total 1024 Byte
	
	private static final byte DATAMAP_KEYVALUE = '=';
	private static final byte DATAMAP_NEXTKV = '\n';
	
	public static final byte PHASE_SEND = 0b00010110; // 0x16 SYN
	public static final byte PHASE_CHECK = 0b00000110; // 0x06 ACK
	public static final byte PHASE_EXECUTE = 0b00000101; // 0x05 ENQ
	
	private static final byte NULL_VALUE = 0b00000000;
	
	public static final int TIMEOUT = 2000;
	public static final int MAX_RESEND = 3;
	
	public static void SendDataByteCreation(String name, Map<String, String> sendData)
	{
		byte[] dataPacketByteFirst = new byte[FIELD_TOTAL];
		
		StringBuffer sendDataSerialStr = new StringBuffer();
		
		long time = System.currentTimeMillis();
		System.out.printf("%x\n", time);
		for(int i = 0; i < 4 * 8; i += 8)
		{
			System.out.printf("%x\n", (byte)time);
			time = time >> 8;
		}
		
		
		int dataPutIndex = 0;
		
		byte[] dataTypeNameByte = name.getBytes();
		for(int nameIndex = 0; nameIndex < FIELD_NAME; ++nameIndex)
		{
			if(dataTypeNameByte.length > nameIndex)
			{
				dataPacketByteFirst[dataPutIndex++] = dataTypeNameByte[nameIndex];
			}
			else
			{
				dataPacketByteFirst[dataPutIndex++] = NULL_VALUE;
			}
		}
		
		dataPacketByteFirst[dataPutIndex] = PHASE_SEND;		
		dataPutIndex += FIELD_PHASE;
		

		
	}
	
	public static byte[] createDataSet()
	{// ����Ÿ �� ����.
		return new byte[FIELD_TOTAL];
	}
	
	public static byte[] createUUID()
	{// uuid ����.
		byte[] uuid = new byte[FIELD_UUID];
		long time = System.currentTimeMillis();
		
		for(int uuidIndex = 0; uuidIndex < FIELD_UUID; ++uuidIndex)
		{
			uuid[uuidIndex] = (byte)time;
			time = time >> BYTE;
		}

		return uuid;
	}
	
	public static byte[] GetPacketUUID(byte[] packet)
	{// ��Ŷ uuid �� �����ɴϴ�.
		
		byte[] uuid = new byte[FIELD_UUID];
		
		for(int uuidIndex = 0; uuidIndex < FIELD_UUID; ++uuidIndex)
		{
			uuid[uuidIndex] = packet[uuidIndex];
		}
		
		return uuid;
	}

	public static byte[] GetpacketMacAddr(byte[] packet)
	{// ��Ŷ ���ּҸ� �����ɴϴ�.
		
		byte[] macAddr = new byte[FIELD_MACADDR - FIELD_UUID];
		int macAddrIndex = 0;
		
		for(int macAddrPacket = FIELD_UUID; macAddrPacket < FIELD_MACADDR; ++macAddrPacket)
		{
			macAddr[macAddrIndex] = packet[macAddrPacket];
			++macAddrIndex;
		}
		
		return macAddr;
	}
	
	public static byte GetPacketPhase(byte[] packet)
	{// ��Ŷ ����� �����ɴϴ�.
		byte phase = packet[FIELD_PHASE - 1];
		return phase;				
	}
	
	public static Map<String, String> GetPacketData(byte[] dataByte)
	{// ������ ����Ʈ�� ������ ������ ���ڵ�.
		
		Map<String, String> dataMap = new HashMap<String, String>();
		
		return dataMap;
	}
	
	public static byte[] dataMapToByte(Map<String, String> dataMap)
	{// ������ ���� ���̳ʸ��� ���ڵ�.
		
		StringBuffer sendDataSerialStr = new StringBuffer();
		for(String key : dataMap.keySet())
		{
			String appendStr = key+DATAMAP_KEYVALUE+dataMap.get(key)+DATAMAP_NEXTKV;
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
	
	public static void InputPacketHeader(byte[] dataSet, byte[] uuid, byte[] macAddr, byte phase)
	{// ��Ŷ ����� ����ϴ�.
		
		int dataSetIndex = 0;
		
		while(dataSetIndex < FIELD_MACADDR)
		{// ��ȣ uuid ����.
			dataSet[dataSetIndex] = uuid[dataSetIndex];
			++dataSetIndex;
		}
		
		for(int macAddrIndex = 0; dataSetIndex < FIELD_UUID; ++macAddrIndex)
		{// ��ȣ macAddr ����.
			dataSet[dataSetIndex] = macAddr[macAddrIndex];
			++dataSetIndex;
		}
		
		dataSet[dataSetIndex] = phase; // ��ȣ ������ �ֱ� (index 10)
		
	}
	
	public static void InputPacketData(byte[] dataSet, String name, byte[] sendData)
	{// ����Ÿ�� ��Ŷ ���ۿ� �ֽ��ϴ�.
		
		int dataSetIndex = FIELD_PHASE; // (index 11~)
		
		byte[] nameByte = name.getBytes();
		
		for(int dataNameIndex = 0; dataNameIndex < nameByte.length && dataSetIndex < FIELD_NAME; ++dataNameIndex)
		{
			dataSet[dataSetIndex] = nameByte[dataNameIndex];
			++dataSetIndex;
		}
		
		while(dataSetIndex <  FIELD_NAME)
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
	
}
