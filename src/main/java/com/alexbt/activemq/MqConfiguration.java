package com.alexbt.activemq;

import java.net.URI;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

@Configuration
public class MqConfiguration {

	@Bean
	public BrokerService brokerService() throws Exception {
		BrokerService brokerService = new BrokerService();
		brokerService.setVmConnectorURI(new URI("vm://localhost:61616"));
		brokerService.start();
		return brokerService;
	}

	@Bean
	public DefaultJmsListenerContainerFactory myJmsContainerFactory() throws JMSException {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setPubSubDomain(true);
		factory.setConnectionFactory(connectionFactory());
		return factory;
	}

	@Bean
	public ConnectionFactory connectionFactory() throws JMSException {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost:61616");
		return activeMQConnectionFactory;
	}
}
