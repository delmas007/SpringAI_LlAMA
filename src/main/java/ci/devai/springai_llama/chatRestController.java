package ci.devai.springai_llama;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@RestController
public class chatRestController {

    private ChatClient chatClient;

    @Value("classpath:/prompts/system-message.st")
    private Resource systemMessageRessource;

    @Value("classpath:/prompts/system-message-objet.st")
    private Resource systemMessageRessourceObject;

    @Value("classpath:img.png")
    private Resource image;

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

    @GetMapping(value = "/describeImage")
    public String describeImage() throws IOException {
        Resource imageResource = new ClassPathResource("img.png");
        if (!imageResource.exists()) {
            throw new FileNotFoundException("Image introuvable dans le chemin spécifié.");
        }

        System.out.println("Image Path: " + imageResource.getFilename());
        System.out.println("Image Size: " + imageResource.contentLength());

        String userMessageText = """
        Voici une image contenant du texte que vous devez analyser et transcrire.
        Effectue une reconnaissance optique (OCR) pour extraire le texte visible sur l'image
        et retourne uniquement le texte extrait sans aucun commentaire.
        """;

        UserMessage userMessage = new UserMessage(userMessageText, List.of(
                new Media(MimeTypeUtils.IMAGE_PNG, imageResource)
        ));

        Prompt prompt = new Prompt(userMessage);
        var response = chatClient.prompt(prompt).call();

        System.out.println("API Response: " + response.content());
        return response.content();
    }


//    public String describeImage() throws IOException {
//        byte[] data = new ClassPathResource("img.png").getContentAsByteArray();
//        Resource imageResource = new ClassPathResource("img.png");
//        System.out.println(imageResource.exists());
//        String userMessageText = """
//                Ton role est de faire de la reconnaissance optique du texte
//                qui se trouve sur l'image fournie.
//                repond en francais
//                """;
//
//        UserMessage userMessage = new UserMessage(userMessageText, List.of(
//                new Media(MimeTypeUtils.IMAGE_PNG, imageResource)
//        ));
//        Prompt prompt = new Prompt(userMessage);
//        return chatClient.prompt(prompt).call().content();
//    }
}
