package org.vaadin.example;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

/**
 * @author mstahv
 */
@Stateless
public class JmsMessageSender {

    @Resource(name = Config.JMSTOPIC)
    private Topic topic;

    @Resource(name = "java:/ConnectionFactory")
    private TopicConnectionFactory connectionFactory;
    
    private TopicConnection connection;
    private TopicSession session;

    public void sendMessage(String msg) {
        try {
            TopicPublisher publisher = session.createPublisher(null);
            TextMessage textMessage = session.createTextMessage(msg);
            publisher.send(textMessage);
        } catch (JMSException ex) {
            Logger.getLogger(JmsMessageSender.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @PostConstruct
    public void init() {
        try {
            connection = connectionFactory.createTopicConnection();
            session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
        } catch (JMSException ex) {
            Logger.getLogger(JmsMessageSender.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
