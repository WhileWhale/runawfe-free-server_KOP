package ru.runa.wfe.chat.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDao;
import java.util.HashSet;
import java.util.Set;

@CommonsLog
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RecipientCalculator {

    private final ExecutorDao executorDao;
    private final ExecutionLogic executionLogic;

    public Set<Long> mapToRecipientIds(Set<Actor> recipients) {
        Set<Long> recipientIds = new HashSet<>(recipients.size());
        for (Actor actor : recipients) {
            recipientIds.add(actor.getId());
        }
        return recipientIds;
    }

    public Set<Actor> calculateRecipients(User user, boolean isPrivate, String messageText, Long processId) {
        return isPrivate
                ? findMentionedActorsInMessageText(messageText)
                : executionLogic.getAllExecutorsByProcessId(user, processId, true);
    }

    /**
     * @return Mentioned actors, defined by '@username' pattern
     */
    private Set<Actor> findMentionedActorsInMessageText(String messageText) {
        Set<Actor> recipients = new HashSet<>();
        int dogIndex = -1;
        while (true) {
            dogIndex = messageText.indexOf('@', dogIndex + 1);
            if (dogIndex != -1) {
                int spaceIndex = messageText.indexOf(' ', dogIndex);
                String login = (spaceIndex != -1)
                        ? messageText.substring(dogIndex + 1, spaceIndex)
                        : messageText.substring(dogIndex + 1);
                try {
                    Executor executor = executorDao.getExecutor(login);
                    if (executor instanceof Actor) {
                        recipients.add((Actor) executor);
                    } else if (executor instanceof Group) {
                        recipients.addAll(executorDao.getGroupActors((Group) executor));
                    }
                } catch (ExecutorDoesNotExistException e) {
                    log.error("Executor with login \"" + login + "\" does not exist");
                }
            } else {
                break;
            }
        }
        log.info(recipients.size() + " mentioned actors were found");
        return recipients;
    }
}
