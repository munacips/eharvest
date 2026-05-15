package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {
    Optional<ConversationMember> findByConversation_IdAndUser_Id(Long conversationId, Long userId);

    List<ConversationMember> findByConversation_Id(Long conversationId);
}
