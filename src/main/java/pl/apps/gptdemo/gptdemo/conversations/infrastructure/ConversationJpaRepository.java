package pl.apps.gptdemo.gptdemo.conversations.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ConversationJpaRepository extends JpaRepository<ConversationEntity, Long> {
}