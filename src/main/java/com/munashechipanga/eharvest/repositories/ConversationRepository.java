package com.munashechipanga.eharvest.repositories;

import com.munashechipanga.eharvest.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
            select c from Conversation c
            join c.members m
            where m.user.id = :userId
            order by c.createdAt desc
            """)
    List<Conversation> findByUserId(@Param("userId") Long userId);

    @Query("""
            select c from Conversation c
            join c.members m1
            join c.members m2
            where c.isGroup = false
              and m1.user.id = :userId1
              and m2.user.id = :userId2
            """)
    Optional<Conversation> findDirectConversation(@Param("userId1") Long userId1,
            @Param("userId2") Long userId2);
}
