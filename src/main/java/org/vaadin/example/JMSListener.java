package org.vaadin.example;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "clientId", propertyValue = Config.JMSTOPIC),
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = Config.JMSTOPIC),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = Config.JMSTOPIC),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
})
public class JMSListener implements MessageListener {
    
    @Inject
    MsgDistributor distributor;
    
    public JMSListener() {
    }
    
    @Override
    public void onMessage(Message message) {
        try {
            String body = message.getBody(String.class);
            distributor.distribute(body);
        } catch (JMSException ex) {
            Logger.getLogger(JMSListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
