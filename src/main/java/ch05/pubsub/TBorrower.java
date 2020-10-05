package ch05.pubsub;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class TBorrower implements MessageListener {
	
	private TopicConnection tConnect = null;
	private TopicSession tSession = null;
	private Topic topic = null;
	private double currentRate;
	
	public TBorrower (String topicCF, String topicName, String rate) throws NamingException, JMSException {
		
		currentRate = Double.valueOf(rate);
		
		Context ctx = new InitialContext();
		TopicConnectionFactory qFactory = (TopicConnectionFactory) ctx.lookup(topicCF);
		tConnect = qFactory.createTopicConnection();
		
		tSession = tConnect.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		
		topic = (Topic) ctx.lookup(topicName);
		
		TopicSubscriber subscriber = tSession.createSubscriber(topic);
		subscriber.setMessageListener(this);
		
		tConnect.start();
		
		System.out.println("Waiting for loan requests...");
	}
	
	

	public static void main(String[] args) throws NamingException, JMSException {
		String topicCF = null;
		String topicName = null;
		String rate = null;
		if (args.length == 3) {
			topicCF = args[0];
			topicName = args[1];
			rate = args[2];
		} else {
			System.out.println("Invalid arguments. Should be:");
			System.out.println("java TLender factory topic rate");
			System.exit(0);
		}
		
		TBorrower borrower = new TBorrower(topicCF, topicName, rate);
		try {
			
			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("TLender Application Started");
			System.out.println("Please enter to quit application");
			stdin.readLine();
			borrower.exit();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onMessage(Message message) {

		try {
			BytesMessage msg = (BytesMessage) message;
			double newRate = msg.readDouble();
			if ((currentRate - newRate) >= 1.0) {
				System.out.println("New Rate = " + newRate + " - Consider refinancing loan");
			} else {
				System.out.println("New Rate = " + newRate + " - Keep existing loan");
			}
			
			System.out.println("\nWating for rate updates...");
			
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void exit() throws JMSException {
		tConnect.close();
	}
}
