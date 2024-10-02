package pl.apps.gptdemo.gptdemo.conversations.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}