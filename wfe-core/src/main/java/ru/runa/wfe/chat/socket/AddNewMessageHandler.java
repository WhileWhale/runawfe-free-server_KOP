package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.ChatFileDto;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.chat.utils.DtoConverters;
import ru.runa.wfe.chat.utils.MentionedExecutorsExtractor;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@Component
public class AddNewMessageHandler implements ChatSocketMessageHandler<AddMessageRequest> {

    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private DtoConverters converter;
    @Autowired
    private MentionedExecutorsExtractor extractor;

    @Transactional
    @Override
    public void handleMessage(Session session, AddMessageRequest dto, User user) throws IOException {
        if (executionLogic.getProcess(user, dto.getProcessId()).isEnded()) {
            return;
        }
        boolean isPrivate = dto.isPrivate();
        Actor actor = user.getActor();
        ChatMessage newMessage = converter.convertAddMessageRequestToChatMessage(dto, actor);
        Set<Executor> mentionedExecutors = extractor.extractMentionedExecutors(dto.getPrivateNames(), newMessage, user);
        Collection<Long> recipientIds = extractor.extractRecipientIds(mentionedExecutors, isPrivate);
        MessageAddedBroadcast messageAddedBroadcast;
        long processId = dto.getProcessId();
        if (dto.getFiles() != null) {
            ArrayList<ChatMessageFile> chatMessageFiles = new ArrayList<>();
            for (Map.Entry<String, byte[]> entry : dto.getFiles().entrySet()) {
                ChatMessageFile chatMessageFile = new ChatMessageFile(entry.getKey(), entry.getValue());
                chatMessageFiles.add(chatMessageFile);
            }
            newMessage = chatLogic.saveMessageAndBindFiles(processId, newMessage, mentionedExecutors,
                    isPrivate, chatMessageFiles);
            messageAddedBroadcast = converter.convertChatMessageToAddedMessageBroadcast(newMessage);
            for (ChatMessageFile file : chatMessageFiles) {
                messageAddedBroadcast.getFilesDto().add(new ChatFileDto(file.getId(), file.getFileName()));
            }
        } else {
            Long newMessId = chatLogic.saveMessage(actor, processId, newMessage, mentionedExecutors, isPrivate);
            newMessage.setId(newMessId);
            messageAddedBroadcast = converter.convertChatMessageToAddedMessageBroadcast(newMessage);
        }
        messageAddedBroadcast.setOld(false);
        messageAddedBroadcast.setCoreUser(messageAddedBroadcast.getAuthor().getId().equals(user.getActor().getId()));
        sessionHandler.sendMessage(recipientIds, messageAddedBroadcast);
    }

    @Override
    public boolean isSupports(Class<? extends MessageRequest> messageType) {
        return messageType.equals(AddMessageRequest.class);
    }
}
