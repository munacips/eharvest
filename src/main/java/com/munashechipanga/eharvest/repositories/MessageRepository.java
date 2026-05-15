package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
            select m from Message m
            where m.conversation.id = :conversationId
              and m.deletedAt is null
            order by m.createdAt asc
            """)
    List<Message> findByConversationId(@Param("conversationId") Long conversationId);

    @Query("""
            select count(m) from Message m
            join m.conversation c
            join c.members mem
            where c.id = :conversationId
              and mem.user.id = :userId
              and m.sender.id != :userId
              and m.createdAt > mem.lastReadAt
              and m.deletedAt is null
            """)
    long countUnread(@Param("conversationId") Long conversationId,
            @Param("userId") Long userId);
}
