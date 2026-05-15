package com.munashechipanga.eharvest.services;

import com.munashechipanga.eharvest.dtos.chat.*;
import com.munashechipanga.eharvest.entities.*;
import com.munashechipanga.eharvest.exceptions.ResourceNotFoundException;
import com.munashechipanga.eharvest.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ConversationDTO createConversation(CreateConversationRequest req) {
        if (!Boolean.TRUE.equals(req.getIsGroup()) && req.getMemberIds().size() == 2) {
            Long u1 = req.getMemberIds().get(0);
            Long u2 = req.getMemberIds().get(1);
            Optional<Conversation> existing = conversationRepository.findDirectConversation(u1, u2);
            if (existing.isPresent()) {
                return mapToDto(existing.get(), u1);
            }
        }

        Conversation conversation = new Conversation();
        conversation.setName(req.getName());
        conversation.setIsGroup(Boolean.TRUE.equals(req.getIsGroup()));
        conversation.setCreatedAt(LocalDateTime.now());
        Conversation saved = conversationRepository.save(conversation);

        for (Long userId : req.getMemberIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            ConversationMember member = new ConversationMember();
            member.setConversation(saved);
            member.setUser(user);
            member.setJoinedAt(LocalDateTime.now());
            member.setLastReadAt(LocalDateTime.now());
            memberRepository.save(member);
        }

        return mapToDto(conversationRepository.findById(saved.getId()).orElseThrow(),
                req.getMemberIds().get(0));
    }

    @Transactional
    public MessageDTO sendMessage(SendMessageRequest req) {
        Conversation conversation = conversationRepository.findById(req.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        memberRepository.findByConversation_IdAndUser_Id(req.getConversationId(), req.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this conversation"));

        User sender = userRepository.findById(req.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(req.getContent());
        message.setCreatedAt(LocalDateTime.now());
        Message saved = messageRepository.save(message);

        MessageDTO dto = mapMessageToDto(saved);

        memberRepository.findByConversation_Id(req.getConversationId())
                .stream()
                .filter(m -> !m.getUser().getId().equals(req.getSenderId()))
                .forEach(m -> messagingTemplate.convertAndSendToUser(
                        m.getUser().getId().toString(),
                        "/queue/messages",
                        dto));

        return dto;
    }

    @Transactional
    public void markRead(Long conversationId, Long userId) {
        ConversationMember member = memberRepository
                .findByConversation_IdAndUser_Id(conversationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not in conversation"));
        member.setLastReadAt(LocalDateTime.now());
        memberRepository.save(member);
    }

    public List<ConversationDTO> getUserConversations(Long userId) {
        return conversationRepository.findByUserId(userId)
                .stream()
                .map(c -> mapToDto(c, userId))
                .toList();
    }

    public List<MessageDTO> getMessages(Long conversationId, Long userId) {
        memberRepository.findByConversation_IdAndUser_Id(conversationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not in conversation"));
        return messageRepository.findByConversationId(conversationId)
                .stream().map(this::mapMessageToDto).toList();
    }

    @Transactional
    public void deleteMessage(Long messageId, Long requestingUserId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        if (!message.getSender().getId().equals(requestingUserId)) {
            throw new IllegalArgumentException("Cannot delete another user's message");
        }
        message.setDeletedAt(LocalDateTime.now());
        messageRepository.save(message);
    }

    private ConversationDTO mapToDto(Conversation c, Long currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setIsGroup(c.getIsGroup());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUnreadCount(messageRepository.countUnread(c.getId(), currentUserId));
        dto.setMembers(c.getMembers().stream().map(m -> {
            MemberDTO mdto = new MemberDTO();
            mdto.setUserId(m.getUser().getId());
            mdto.setFullName(m.getUser().getFirstName() + " " + m.getUser().getLastName());
            mdto.setJoinedAt(m.getJoinedAt());
            mdto.setLastReadAt(m.getLastReadAt());
            return mdto;
        }).toList());
        messageRepository.findByConversationId(c.getId())
                .stream().reduce((a, b) -> b)
                .ifPresent(m -> dto.setLastMessage(mapMessageToDto(m)));
        return dto;
    }

    private MessageDTO mapMessageToDto(Message m) {
        MessageDTO dto = new MessageDTO();
        dto.setId(m.getId());
        dto.setConversationId(m.getConversation().getId());
        dto.setSenderId(m.getSender().getId());
        dto.setSenderName(m.getSender().getFirstName() + " " + m.getSender().getLastName());
        dto.setContent(m.getContent());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
