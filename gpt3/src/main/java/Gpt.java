import java.time.Duration;
import java.util.List;


import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;


import java.time.Duration;
import java.util.Arrays;
import java.util.List;


public class Gpt {

    private static final String GPT_MODEL = "gpt-3.5-turbo";

    public static void main(String[] args) {
        new Gpt().callIt();

    }

    private void callIt() {
        OpenAiService openAiService = new OpenAiService("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",
                Duration.ofSeconds(40));
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(GPT_MODEL)
                .temperature(0.1)
                .messages(
                        List.of(
                                new ChatMessage("system", "answer formally and factually"),
                                new ChatMessage("user", "what is the 5*5")))
                .build();

        openAiService.createChatCompletion(chatCompletionRequest).getChoices().forEach(choice -> {
            System.out.print(choice.getMessage().getContent());
        });
        System.out.println();
    }

}

