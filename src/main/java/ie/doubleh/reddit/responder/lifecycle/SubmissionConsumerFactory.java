package ie.doubleh.reddit.responder.lifecycle;

import ie.doubleh.reddit.responder.bot.SubmissionConsumer;
import ie.doubleh.reddit.responder.bot.model.ResponseModel;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Component
public class SubmissionConsumerFactory {

    private final ResponseModel model;
    private final Set<SubmissionConsumer> consumers;
    private final BeanFactory beanFactory;

    @Autowired
    public SubmissionConsumerFactory(ResponseModel model, BeanFactory beanFactory) {
        this.model = model;
        this.beanFactory = beanFactory;
        consumers = new HashSet<>();
    }

    @PostConstruct
    public void createConsumers() {
        for (String searchTerm : model.getSubmissionResponses().keySet()) {
              consumers.add(beanFactory.getBean(SubmissionConsumer.class, searchTerm));
        }
    }
}
