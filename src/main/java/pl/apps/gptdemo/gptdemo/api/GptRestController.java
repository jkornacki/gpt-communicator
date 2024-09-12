package pl.apps.gptdemo.gptdemo.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pl.apps.gptdemo.gptdemo.conversations.ConversationService;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:7070", "http://localhost:5175", "http://**", "https://**"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS, RequestMethod.DELETE, RequestMethod.PUT})
@RestController
@RequiredArgsConstructor
public class GptRestController {

    private final ConversationService conversationService;

    @GetMapping("/api/conversations")
    List<ConversationApiResponse> fetchAllConversations() {
        return conversationService.fetchAllConversations();
    }

    @GetMapping("/api/conversation/{id}")
    ConversationWithItemsApiResponse fetchConversationWithItems(@PathVariable("id") Long conversationId) {
        return conversationService.fetchConversationWithItems(conversationId);
    }

    @PostMapping("/api/anthropic")
    PromptApiResponse callAnthropicApi(@RequestBody ApiRequest request) {
        return handleCallAnthropicApi(() -> conversationService.sendPromptForNewConversation(request.prompt()), request.prompt());
    }

    @PostMapping("/api/conversation/{id}/anthropic")
    PromptApiResponse callAnthropicApiInConversation(@PathVariable("id") Long conversationId, @RequestBody ApiRequest request) {
        return handleCallAnthropicApi(() -> conversationService.sendPromptForExistingConversation(conversationId, request.prompt()), request.prompt());
    }

    private PromptApiResponse handleCallAnthropicApi(Supplier<PromptApiResponse> apiCall, String prompt) {
        PromptApiResponse response = apiCall.get();
        log.info("claudeApiClientResponse for prompt: {}\n{}", prompt, response);
        return response;
    }

}
