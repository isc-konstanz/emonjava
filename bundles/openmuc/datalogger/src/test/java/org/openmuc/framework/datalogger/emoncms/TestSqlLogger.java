/**
 * 
 */
package org.openmuc.framework.datalogger.emoncms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoncms.sql.ScaleIntegerType;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.type.BasicType;
import org.junit.jupiter.api.Test;
import org.openmuc.framework.data.BooleanValue;
import org.openmuc.framework.data.ByteValue;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.FloatValue;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.LongValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.data.ShortValue;
import org.openmuc.framework.data.StringValue;
import org.openmuc.framework.data.Value;
import org.openmuc.framework.data.ValueType;
import org.openmuc.framework.datalogger.data.Channel;
import org.openmuc.framework.datalogger.data.Configuration;
import org.openmuc.framework.datalogger.data.Settings;
import org.openmuc.framework.datalogger.dynamic.ChannelHandler;
import org.openmuc.framework.datalogger.dynamic.TestChannelHandler;
import org.openmuc.framework.datalogger.spi.LogChannel;

/**
 * @author gb
 *
 */
public class TestSqlLogger {
	
	SqlLogger logger = new SqlLogger();

	@Test
	public void testDoLogChannel() {
		try {
			onActivateLogger();
			onConfigureLogger();
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Value value = new BooleanValue(true);
			Long time = System.currentTimeMillis();
			ChannelHandler channelHandler = createChannelHandler();
			channelHandler.update(new Record(value, time));
			logger.doLog(channelHandler, time);
			
			List<Record> recList = logger.getRecords(channelHandler, time, time-1);
			Record rec = recList.get(0);
			checkTimestamp(rec.getTimestamp(), time);
			assertEquals(rec.getValue().asBoolean(), true);			
		} 
		catch (ServiceException e) {
			e.printStackTrace();
			return;
		}
		catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		onDeactivateLogger();
	}

	@Test
	public void testDoLogListOfChannels() {
		try {
			onActivateLogger();
			
			Long time = System.currentTimeMillis();
//			Long time = new Long("1564743066000");
			List<Channel> list = createChannelHandlers(time);
			logger.onConfigure(list);
			logger.doLog(list, time);
			
			getRecords(list);
		} 
		catch (ServiceException e) {
			e.printStackTrace();
			return;
		}
		catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		onDeactivateLogger();
	}
	
	private void checkTimestamp(Long timestamp, Long time) {
		BasicType userType = logger.getUserType();
		if (userType instanceof  ScaleIntegerType) {
			Integer i = ((ScaleIntegerType)userType).getJavaTypeDescriptor().unwrap(time, Integer.class, null);
			time = ((ScaleIntegerType)userType).getJavaTypeDescriptor().wrap(i, null);
		}
		assertEquals(timestamp, time);
		
	}

	private void getRecords(List<Channel> list) throws IOException {
		for (Channel channel : list) {
			long time = channel.getTime();
			Value val = channel.getValue();
			ValueType type = channel.getValueType();
			List<Record> recList = logger.getRecords(channel, time, time-5);
			if (recList.size() > 0) {
				Record rec = recList.get(0);
				checkTimestamp(rec.getTimestamp(), time);
				switch (type) {
					case BOOLEAN:
						assertEquals(rec.getValue().asBoolean(), val.asBoolean());			
						break;
					case BYTE:
						assertEquals(rec.getValue().asByte(), val.asByte());			
						break;
					case DOUBLE:
						assertEquals(rec.getValue().asDouble(), val.asDouble(), 0.0001);			
						break;
					case FLOAT:
						assertEquals(rec.getValue().asFloat(), val.asFloat(), 0.0001);			
						break;
					case INTEGER:
						assertEquals(rec.getValue().asInt(), val.asInt());			
						break;
					case SHORT:
						assertEquals(rec.getValue().asShort(), val.asShort());			
						break;
					case STRING:
						assertEquals(rec.getValue().asString(), val.asString());			
						break;
					default:
						break;			
				}
			}
			else {
				if (!type.equals(ValueType.STRING)) {
					fail("No Records found!");
				}
			}
			
		}
		
	}

	private List<Channel> createChannelHandlers(Long time) {
		List<Channel> retVal = new ArrayList<Channel>();
		Value feedId = new IntValue(777);
		ChannelHandler channelHandler = createChannelHandler(feedId, ValueType.BOOLEAN, new BooleanValue(false), time);
		retVal.add(channelHandler);
		
		feedId = new IntValue(778);
		channelHandler = createChannelHandler(feedId, ValueType.BYTE, new ByteValue((byte)10), time);
		retVal.add(channelHandler);
		
		feedId = new IntValue(779);
		channelHandler = createChannelHandler(feedId, ValueType.DOUBLE, new DoubleValue(125.6), time);
		retVal.add(channelHandler);
		
		feedId = new IntValue(780);
		channelHandler = createChannelHandler(feedId, ValueType.FLOAT, new FloatValue((float) 125.7), time);
		retVal.add(channelHandler);
		
		feedId = new IntValue(781);
		channelHandler = createChannelHandler(feedId, ValueType.INTEGER, new IntValue(333), time);
		retVal.add(channelHandler);
		
		feedId = new IntValue(782);
		channelHandler = createChannelHandler(feedId, ValueType.LONG, new LongValue(666666), time);
		retVal.add(channelHandler);

		feedId = new IntValue(783);
		channelHandler = createChannelHandler(feedId, ValueType.SHORT, new ShortValue((short) 7), time);
		retVal.add(channelHandler);
		
		feedId = new IntValue(784);
		channelHandler = createChannelHandler(feedId, ValueType.STRING, new StringValue("999"), time);
		retVal.add(channelHandler);
		return retVal;
	}

	private ChannelHandler createChannelHandler(Value feedId, ValueType type, Value value, long time) {
		String name = feedId.asString();
		LogChannel logChannel = new LogChannelTestImpl(name, type);
		Settings settings = new Settings(logChannel);
		settings.put(SqlLogger.FEED_ID, feedId);
		settings.put(SqlLogger.NODE, new StringValue(name));
		ChannelHandler channelHandler = TestChannelHandler.createChannelHandler(logChannel, settings);
		channelHandler.update(new Record(value, time));
		return channelHandler;
	}

	private void onActivateLogger() throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		map.put(SqlLogger.CONNECTION_URL, SqlLogger.CONNECTION_URL_DEFAULT);
		map.put(SqlLogger.CONNECTION_DRIVER_CLASS, SqlLogger.CONNECTION_DRIVER_CLASS_DEFAULT);
		map.put(SqlLogger.USER, "root");
		map.put(SqlLogger.PASSWORD, "");
		
		Configuration config = new Configuration(map);
		logger.onActivate(config);
	}

	private void onConfigureLogger() throws IOException {
		List<Channel> channels = new ArrayList<Channel>();
		Value feedId = new IntValue(777);
		String tableName = feedId.asString();
		// Attention ValueType are not relevant
		LogChannel logChannel = new LogChannelTestImpl(tableName, ValueType.FLOAT);
		Settings settings = new Settings(logChannel);
		settings.put(SqlLogger.FEED_ID, feedId);
		Channel channel = TestChannelHandler.createChannelHandler(logChannel, settings);
		channels.add(channel);
		logger.onConfigure(channels);
	}

	private ChannelHandler createChannelHandler() {
		String name = "testSqlLoggerBoolean";
		// Attention tableName is not relevant
		LogChannel logChannel = new LogChannelTestImpl(name, ValueType.BOOLEAN);
		Settings settings = new Settings(logChannel);
		settings.put(SqlLogger.FEED_ID, new IntValue(777));
		settings.put(SqlLogger.NODE, new StringValue(name));
		ChannelHandler channelHandler = TestChannelHandler.createChannelHandler(logChannel, settings);
		return channelHandler;
	}

	private void onDeactivateLogger() {
		logger.onDeactivate();
	}

}
