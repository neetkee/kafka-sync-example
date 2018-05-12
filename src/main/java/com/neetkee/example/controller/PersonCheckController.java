package com.neetkee.example.controller;

import com.neetkee.example.event.PersonCheckInitiated;
import com.neetkee.example.event.PersonChecked;
import com.neetkee.example.model.Person;
import com.neetkee.example.model.PersonCheckResult;
import com.neetkee.example.repository.PersonCheckResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PersonCheckController {
    private final KafkaTemplate<Integer, PersonCheckInitiated> kafkaTemplate;
    private final PersonCheckResultRepository checkResultRepository;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);

    @PostMapping("/check")
    //you may want to try/catch this
    @SneakyThrows({InterruptedException.class, ExecutionException.class, TimeoutException.class})
    public PersonCheckResult checkPerson(@RequestBody Person person) {
        kafkaTemplate.send("Person.Check.Initiated", person.getId(), new PersonCheckInitiated(person));
        return pollForCheckResult(person.getId()).get(50, TimeUnit.SECONDS);
    }

    private CompletableFuture<PersonCheckResult> pollForCheckResult(Integer personId) {
        CompletableFuture<PersonCheckResult> checkResultCompletableFuture = new CompletableFuture<>();
        final ScheduledFuture<?> checkResultScheduledFuture = executor.scheduleAtFixedRate(() -> {
            log.info("Checking result for person with id: {}", personId);
            Optional<PersonCheckResult> optionalCheckResult = checkResultRepository.findByPersonId(personId);
            optionalCheckResult.ifPresent(checkResultCompletableFuture::complete);
        }, 1, 1, TimeUnit.SECONDS);
        //we don't want to run this future indefinitely
        executor.schedule(() -> {
            log.info("Cancelling check for person with id: {}", personId);
            checkResultScheduledFuture.cancel(true);
        }, 65, TimeUnit.SECONDS);
        //cancel polling when result is received
        checkResultCompletableFuture.whenComplete((personCheckResult, throwable) -> checkResultScheduledFuture.cancel(true));
        return checkResultCompletableFuture;
    }

    @KafkaListener(topics = "Person.Checked")
    public void personCheckedReceived(PersonChecked personChecked) {
        log.info("Received personCheckedEvent. Id:{}, CheckResult: {}", personChecked.getPersonId(), personChecked.getCheckResult());
        PersonCheckResult personCheckResult = new PersonCheckResult();
        personCheckResult.setPersonId(personChecked.getPersonId());
        personCheckResult.setCheckResult(personChecked.getCheckResult());
        checkResultRepository.save(personCheckResult);
    }
}
