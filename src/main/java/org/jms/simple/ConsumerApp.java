package org.jms.simple;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class ConsumerApp
{
	public static void main(String[] args) throws Exception
	{
		thread(new MessageConsumer(), false);
	}

	public static void thread(Runnable runnable, boolean daemon)
	{
		Thread brokerThread = new Thread(runnable);
		brokerThread.setDaemon(daemon);
		brokerThread.start();
	}

	public static class MessageConsumer implements Runnable, ExceptionListener
	{
		public void run()
		{
			try
			{
				// Create a ConnectionFactory
				ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://0.0.0.0:61816");

				// Create a Connection
				Connection connection = connectionFactory.createConnection();
				connection.start();

				connection.setExceptionListener(this);

				// Create a Session
				Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

				// Create the destination (Topic or Queue)
				Destination destination = session.createQueue("SAAS.REQUEST.1");

				// Create a MessageConsumer from the Session to the Topic or
				// Queue
				
				javax.jms.MessageConsumer consumer = session.createConsumer(destination);

				// Wait for a message
				
				int i	=	0;
				
				while (i < 100000)
				{
					Message message = consumer.receive(0);	
					
					if (message instanceof TextMessage)
					{
						TextMessage textMessage = (TextMessage) message;
						String text = textMessage.getText();
						System.out.println("Received: " + text);
					} else
					{
						System.out.println("Received non-text message: " + message);
					}
					
					i++;
				}
				
				
				consumer.close();
				session.close();
				connection.close();
				
			} catch (Exception e)
			{
				System.out.println("Caught: " + e);
				e.printStackTrace();
			}
		}

		public synchronized void onException(JMSException ex)
		{
			System.out.println("JMS Exception occured.  Shutting down client.");
		}
	}
}
