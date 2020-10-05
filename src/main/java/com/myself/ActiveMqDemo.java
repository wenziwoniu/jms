package com.myself;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ActiveMqDemo implements MessageListener {
	
	private TopicSession pubSession;
	private TopicPublisher publisher;
	private TopicConnection connection;
	private String userName;
	
	/** 用于初始化Chat（聊天）的构造函数  
	 * @throws NamingException 
	 * @throws JMSException */
	public ActiveMqDemo(String topicFactory, String topicName, String userName) throws NamingException, JMSException {
		
		// 使用jndi.properties文件获得一个jndi连接
		InitialContext ctx = new InitialContext();
		
		// 查找一个JMS连接工厂并创建连接
		TopicConnectionFactory conFactory = (TopicConnectionFactory) ctx.lookup(topicFactory);
		TopicConnection connection = conFactory.createTopicConnection();
		
		// 创建两个JMS会话对象
		TopicSession pubSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		TopicSession subSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		
		// 查找一个JMS主题
		Topic chatTopic = (Topic) ctx.lookup(topicName);
		
		// 创建一个JMS发布者和订阅者，createSubscriber中附加的参数是一个消息
		// 选择器(null)和noLocal标记一个真值，它表明这个发布者生产的消息不应被它自己消费
		TopicPublisher publisher = pubSession.createPublisher(chatTopic);
		TopicSubscriber subscriber = subSession.createSubscriber(chatTopic, null, true);
		
		// 设置一个JMS消息侦听器
		subscriber.setMessageListener(this);
		
		// 初始化Chat应用程序变量
		this.connection = connection;
		this.pubSession = pubSession;
		this.publisher = publisher;
		this.userName = userName;
		
		// 启动JMS连接，允许传送消息
		connection.start();
		
	}

	/** 接收来自TopicSubcriber的消息 */
	public void onMessage(Message message) {
		
		try {
			TextMessage textMessage = (TextMessage) message;
//			Topic des = (Topic) textMessage.getJMSDestination();
//			System.out.println("接收主题:" + textMessage.getJMSDestination());
//			System.out.println("接收消息id:" + message.getJMSMessageID());
//			System.out.println("时间：" + message.getJMSTimestamp());
//			System.out.println("过期时间:" + message.getJMSExpiration());
//			System.out.println("消息类型：" + message.getJMSType());
			System.out.println(message.getStringProperty("username") + ":" + textMessage.getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
	}
	
	/** 使用发布者创建并发布消息 
	 * @throws JMSException */
	protected void writeMessage(String text) throws JMSException {
		
		TextMessage message = pubSession.createTextMessage();
		Destination destination = message.getJMSDestination();
		System.out.println("发送主题:" + message.getJMSDestination());
		message.setJMSType("客户端设置的消息类型");
		message.setStringProperty("username", userName);
		message.setText(text);
		publisher.publish(message);
		
	}
	
	/** 关闭JMS连接 
	 * @throws JMSException */
	public void close() throws JMSException {
		connection.close();
	}
	
	/** 运行聊天客户端 
	 * @throws JMSException 
	 * @throws NamingException 
	 * @throws CloneNotSupportedException 
	 * @throws IOException */
	public static void main(String[] args) throws NamingException, JMSException, CloneNotSupportedException, IOException {
		
		if (args.length != 3) {
			System.out.println("Factory,Topic,or username missing");
		}
		// args[0]=topicFactory;args[1]=topicName;args[2]=username
		ActiveMqDemo chat = new ActiveMqDemo(args[0], args[1], args[2]);
		
		//从命令行读取
		BufferedReader commandLine = new BufferedReader(new InputStreamReader(System.in));
		
		// 循环，直到键入“exit”为止
		while (true) {
			String s = commandLine.readLine();
			if (s.equalsIgnoreCase("exit")) {
				chat.clone();
				System.exit(0);
			} else {
				chat.writeMessage(s);
			}
		}
		
	}
	

}
