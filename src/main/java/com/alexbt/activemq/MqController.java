package com.alexbt.activemq;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mq")
public class MqController {

	private static final String DESTINATION_TOPIC = "message-in-a-bottle";
	private static final String RETURN_QUEUE = "back-with-love";

	@Autowired
	private JmsTemplate jmsTemplate;
	private Message receive = null;
	private static final Object LOCK = new Object();

	@RequestMapping(path = "/{msg}", method = RequestMethod.GET)
	public String put(@PathVariable String msg) throws JMSException, InterruptedException, ExecutionException {
		jmsTemplate.setDefaultDestinationName("dumy");
		receive = null;

		this.jmsTemplate.send(new ActiveMQTopic(DESTINATION_TOPIC), new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				Message message = session.createTextMessage(msg);
				message.setJMSReplyTo(new ActiveMQTopic(RETURN_QUEUE));
				System.out.println("local-sent");
				return message;
			}
		});
		synchronized (LOCK) {
			while (receive == null) {
				LOCK.wait();
			}
		}
		System.out.println("after lock");
		return ((TextMessage) receive).getText() + " and back in '" + receive.getJMSDestination().toString() + "'";
	}

	@Async
	public Future<Message> receive() throws InterruptedException {
		return new AsyncResult<Message>(jmsTemplate.receive(RETURN_QUEUE));
	}

	@JmsListener(destination = RETURN_QUEUE)
	public void receiveMessage(Message receive) throws JMSException {
		synchronized (LOCK) {
			receive.acknowledge();
			this.receive = receive;
			System.out.println("local-receive");
			LOCK.notifyAll();
		}
	}

}
