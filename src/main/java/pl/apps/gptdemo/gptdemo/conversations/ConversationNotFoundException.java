package pl.apps.gptdemo.gptdemo.conversations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConversationNotFoundException extends RuntimeException {

    private final Long id;
}
