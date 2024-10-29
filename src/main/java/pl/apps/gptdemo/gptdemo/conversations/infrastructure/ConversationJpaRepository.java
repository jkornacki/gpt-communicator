package pl.apps.gptdemo.gptdemo.conversations.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.apps.gptdemo.gptdemo.conversations.SystemPrompt;

import java.util.Collection;
import java.util.List;

@Repository
interface ConversationJpaRepository extends JpaRepository<ConversationEntity, Long> {

    @Query("""
            SELECT conversation FROM ConversationEntity conversation
            WHERE conversation.deleteDate is null
            ORDER BY conversation.createDate, conversation.id desc
            """)
    List<ConversationEntity> findAllNotDeleted();

    @Query("""
            select new pl.apps.gptdemo.gptdemo.conversations.SystemPrompt(
                conversation.id, conversation.systemPrompt
            )
            FROM ConversationEntity conversation
            WHERE conversation.systemPrompt is not null
            """)
    List<SystemPrompt> findSystemPromptsForAllConversations();
}