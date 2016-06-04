package org.vaadin.example;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import java.util.Random;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import org.vaadin.viritin.button.PrimaryButton;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.layouts.MVerticalLayout;

@CDIUI("")
@Push
@Theme("valo")
public class MyUI extends UI {

    @Inject
    private JMSContext jmsContext;

    @Resource(lookup = Config.JMSTOPIC)
    private Topic topic;

    public void send(String msg) {
        jmsContext.createProducer().send(topic, msg);
    }

    @Inject
    MsgDistributor msgDistributor;

    MTable<String> messages = new MTable<>(String.class).withGeneratedColumn("Message", s -> s)
            .withProperties("Message")
            .withFullWidth();

    @Override
    protected void init(VaadinRequest vaadinRequest) {

        msgDistributor.register(this);

        final TextField msg = new MTextField().withAutocompleteOff();
        msg.setCaption("Send message:");

        Button button = new PrimaryButton("Send");
        button.addClickListener(e -> {
            send(msg.getValue());
            msg.selectAll();
        });

        messages.setImmediate(true);

        setContent(new MVerticalLayout(msg, button).expand(messages));
    }

    Random r = new Random(System.currentTimeMillis());

    /**
     * Public API for msgDistributor to post the message
     *
     * @param msg
     */
    public void showMessage(String msg) {
        access(() -> {
            messages.addBeans(msg);
            messages.setCurrentPageFirstItemIndex(messages.size());
        });
    }

}
