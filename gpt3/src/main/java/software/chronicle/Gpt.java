package software.chronicle;

import java.time.Duration;
import java.util.List;


import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;


public class Gpt {

    private static final String GPT_MODEL = "gpt-3.5-turbo";
    private OpenAiService openAiService = new OpenAiService(System.getenv("OPENAI_TOKEN"), Duration.ofSeconds(60));

    public static void main(String[] args) {
        new Gpt().callGpt("hi", "answer formally and factually");
    }

    public StringBuilder callGpt(String question, String system) {

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(GPT_MODEL)
                .temperature(0.1)
                .messages(
                        List.of(
                                new ChatMessage("system", system),
                                new ChatMessage("user", question)))
                .build();

        StringBuilder stringBuilder = new StringBuilder();
        openAiService.createChatCompletion(chatCompletionRequest).getChoices().forEach(choice -> {
            stringBuilder.append(choice.getMessage().getContent());
            stringBuilder.append("\n");
        });
        return stringBuilder;
    }

}

