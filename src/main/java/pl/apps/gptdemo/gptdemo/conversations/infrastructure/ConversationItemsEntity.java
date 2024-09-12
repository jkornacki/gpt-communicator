package pl.apps.gptdemo.gptdemo.conversations.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.apps.gptdemo.gptdemo.conversations.SendBy;

import java.time.LocalDateTime;

@Slf4j
@Builder
@Getter
@Entity
@Table(name = "conversation_items")
@AllArgsConstructor
@NoArgsConstructor
class ConversationItemsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @JoinColumn(name = "conversation_id")
    @ManyToOne
    private ConversationEntity conversation;

    @Column(name = "send_by")
    @Enumerated(EnumType.STRING)
    private SendBy sendBy;

    @Lob
    @Column(name = "prompt", columnDefinition = "text")
    private String prompt;

    @Lob
    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
