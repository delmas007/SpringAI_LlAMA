package ci.devai.springai_llama;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
public class chatRestController {

    private ChatClient chatClient;

    @Value("classpath:/prompts/system-message.st")
    private Resource systemMessageRessource;

    @Value("classpath:/prompts/system-message-objet.st")
    private Resource systemMessageRessourceObject;

    public chatRestController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping(value = "/chat", produces = MediaType.TEXT_PLAIN_VALUE)
    public String chat(String message) {
        String content = chatClient.prompt()
                .system("tu reponds en francais")
                .user(message)
                .call()
                .content();
        return content;
    }

    @GetMapping(value = "/chat2", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> chat2(String message) {
        Flux<String> content = chatClient.prompt()
                .system("tu reponds en francais")
                .user(message)
                .stream()
                .content();
        return content;
    }

    @GetMapping(value = "/sentiment", produces = MediaType.TEXT_PLAIN_VALUE)
    public String sentiment(String review) {
        String content = chatClient.prompt()
                .system(systemMessageRessource)
                .user(review)
                .call()
                .content();
        return content;
    }

    @GetMapping(value = "/sentimentObject")
    public Sentiment sentimentReturnObject(String review) {
        return  chatClient.prompt()
                .system(systemMessageRessource)
                .user(review)
                .call()
                .entity(Sentiment.class);
    }
}
