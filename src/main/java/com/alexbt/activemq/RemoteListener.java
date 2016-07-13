package com.alexbt.activemq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

@Component
public class RemoteListener {
	
	/**
     * Get a copy of the application context
     */
    @Autowired
    ConfigurableApplicationContext context;
    
    @Autowired
	private JmsTemplate jmsTemplate;

	/**
     * When you receive a message, print it out, then shut down the application.
     * Finally, clean up any ActiveMQ server stuff.
	 * @throws JMSException 
     */
    @JmsListener(destination = "message-in-a-bottle", containerFactory = "myJmsContainerFactory")
    public void receiveMessage(Message msg) throws JMSException {
    	System.out.println("remote-receive");
    	jmsTemplate.setDefaultDestinationName("dummy");
		this.jmsTemplate.send(msg.getJMSReplyTo(), new MessageCreator() {
		      @Override
		      public Message createMessage(Session session) throws JMSException {
		        Message message = session.createTextMessage("<b>" + ((TextMessage) msg).getText() + "</b> received in " + msg.getJMSDestination().toString());     
		        msg.acknowledge();
		        message.setJMSDestination(msg.getJMSReplyTo());
		    	System.out.println("remote-sent");
		        return message;
		      }
		    });
    }
}
