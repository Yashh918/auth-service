package expensetracker.authservice.kafka;

import expensetracker.authservice.events.AuthUserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthEventProducer {

    private final KafkaTemplate<String, AuthUserRegisteredEvent> kafkaTemplate;

    public void sendUserRegisteredEvent(AuthUserRegisteredEvent event) {
        kafkaTemplate.send(KafkaTopics.USER_REGISTER, event);
    }
}
