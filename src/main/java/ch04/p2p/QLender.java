package ch04.p2p;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

public class QLender implements MessageListener {
	
	private QueueConnection qConnect = null;
	private QueueSession qSession = null;
	private Queue requestQ = null;
	
	public QLender (String queuecf, String requestQueue) {
		
		try {
			
			Context ctx = new InitialContext();
			QueueConnectionFactory qFactory = (QueueConnectionFactory) ctx.lookup(queuecf);
			qConnect = qFactory.createQueueConnection();
			
			qSession = qConnect.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			
			requestQ = (Queue) ctx.lookup(requestQueue);
			
			qConnect.start();
			
			QueueReceiver qReceiver = qSession.createReceiver(requestQ);
			qReceiver.setMessageListener(this);
			
			System.out.println("Waiting for loan requests...");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		
	}
	

	public void onMessage(Message message) {
		
		try {
			boolean accepted = false;
			
			System.out.println("接收到消息");
			Thread.sleep(10000);
			
			MapMessage msg = (MapMessage) message;
			double salary = msg.getDouble("Salary");
			double loanAmt = msg.getDouble("LoanAmount");
			
			if (loanAmt < 20000) {
				accepted = (salary / loanAmt) > 0.25;
			} else {
				accepted = (salary / loanAmt) > 0.33;
			}
			System.out.println("" + "%= " + (salary / loanAmt) + ",loan is " 
			+ (accepted ? "Accepted!" : "Declined"));
			
			TextMessage tmsg = qSession.createTextMessage();
			tmsg.setText(accepted ? "Accepted!" : "Declined");
			tmsg.setJMSCorrelationID(message.getJMSMessageID());
			
			QueueSender qSender = qSession.createSender((Queue) message.getJMSReplyTo());
			qSender.send(tmsg);
			
			System.out.println("\nWaiting for loan reuqests...");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void exit() {
		try {
			qConnect.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public static void main(String[] args) {

		String queueCF = null;
		String requestq = null;
		if (args.length == 2) {
			queueCF = args[0];
			requestq = args[1];
		} else {
			System.out.println("Invalid arguments. Should be:");
			System.out.println("java QLender factory request_queue");
			System.exit(0);
		}
		
		QLender lender = new QLender(queueCF, requestq);
		try {
			
			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("QLender Application Started");
			System.out.println("Please enter to quit application");
			stdin.readLine();
			lender.exit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
