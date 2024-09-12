package pl.apps.gptdemo.gptdemo.conversations.infrastructure;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.apps.gptdemo.gptdemo.conversations.Conversation;
import pl.apps.gptdemo.gptdemo.conversations.ConversationItems;

@Mapper
interface ConversationRepositoryMapper {
    Conversation mapToConversation(ConversationEntity saved);

    @Mapping(target = "items", ignore = true)
    Conversation mapToConversationWithoutItems(ConversationEntity saved);

    @Mapping(target = "items", ignore = true)
    ConversationEntity mapToConversationEntity(Conversation conversation);

    @Mapping(target = "id", source = "item.id")
    ConversationItemsEntity mapToConversationItemsEntity(ConversationItems item, ConversationEntity conversation);

    ConversationItems mapToConversationItems(ConversationItemsEntity entity);
}
