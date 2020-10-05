package ch05.pubsub;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;

public class TLender {
	
	private TopicConnection tConnection = null;
	private TopicSession tSession = null;
	private Topic topic = null;
	
	public TLender (String topicCf, String topicName) {
		
		try {
			Context ctx = new InitialContext();
			TopicConnectionFactory qFactory = (TopicConnectionFactory) ctx.lookup(topicCf);
			tConnection = qFactory.createTopicConnection();
			tSession = tConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			
			topic = (Topic) ctx.lookup(topicName);
			
			tConnection.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void publishRate(Double newRate) {
		
		try {
			BytesMessage msg = tSession.createBytesMessage();
			msg.writeDouble(newRate);
			
			TopicPublisher publisher = tSession.createPublisher(topic);
			publisher.publish(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void exit() throws JMSException {
		tConnection.close();
	}
	

	public static void main(String[] args) {
		
		String topicCF = null;
		String topicName = null;
		if (args.length == 2) {
			topicCF = args[0];
			topicName = args[1];
		} else {
			System.out.println("Invalid arguments. Should be:");
			System.out.println("java TLender factory topic");
			System.exit(0);
		}
		
		TLender lender = new TLender(topicCF, topicName);
		try {
			
			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("TLender Application Started");
			System.out.println("Please enter to quit application");
			System.out.println("Enter:rate");
			System.out.println("\ne.g. 6.8");
			
			while (true) {
				System.out.println("> ");
				String rate = stdin.readLine();
				if (rate == null || rate.trim().length() <= 0) {
					lender.exit();
				}
				
				double newRate = Double.valueOf(rate);
				lender.publishRate(newRate);

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
