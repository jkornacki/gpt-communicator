package pl.apps.gptdemo.gptdemo.conversations;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.apps.gptdemo.gptdemo.api.ConversationApiResponse;
import pl.apps.gptdemo.gptdemo.api.ConversationItemDto;
import pl.apps.gptdemo.gptdemo.api.ConversationWithItemsApiResponse;

import java.util.List;

@Mapper
interface ConversationMapper {
    @Mapping(target = "conversationId", source = "id")
    @Mapping(target = "conversationTitle", source = "title")
    ConversationApiResponse mapToConversationApiResponse(Conversation conversation);

    @Mapping(target = "conversationId", source = "conversationWithItems.id")
    @Mapping(target = "conversationTitle", source = "conversationWithItems.title")
    ConversationWithItemsApiResponse mapToConversationWithItemsApiResponse(Conversation conversationWithItems, List<ConversationItemDto> items);

    ConversationItemDto mapToConversationWithItemsApiResponseItem(ConversationItems conversationItem);
}
