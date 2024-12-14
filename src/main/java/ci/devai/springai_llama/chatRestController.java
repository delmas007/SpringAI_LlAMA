package ci.devai.springai_llama;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class chatRestController {

    private final ChatClient chatClient;

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
    public List<Sentiment> sentimentReturnObject(String review) {
        return Collections.singletonList(chatClient.prompt()
                .system(systemMessageRessource)
                .user(review)
                .call()
                .entity(Sentiment.class));
    }

    @GetMapping(value = "/sentimentObjects")
    public List<Question> sentimentReturnObjecte(String review) {
        return Collections.singletonList(chatClient.prompt()
                .system(systemMessageRessourceObject)
                .user(review)
                .call()
                .entity(Question.class));
    }

    @GetMapping(value = "/generateQuestions")
    public List<Question> generateQuestions(@RequestParam String sujet) {
        String prompt = "Génère 2 questions aléatoires sur le sujet suivant : " + sujet + ". Chaque question doit avoir trois réponses possibles, et un niveau de difficulté (Facile, Moyen, Difficile) doit être spécifié pour chaque question. La réponse doit être structurée en format JSON, où chaque question doit inclure un texte de question et trois propositions de réponse.";

        String systemMessage = "Vous êtes un générateur de questions à choix multiples (QCM). Pour chaque question, vous proposez trois réponses possibles et attribuez un niveau de difficulté (Facile, Moyen, Difficile). en francais";

        ResponseEntity<ChatResponse, List<Question>> response = chatClient.prompt()
                .system(systemMessage)
                .user(prompt)
                .call()
                .responseEntity(new ParameterizedTypeReference<>() {});

        return response.entity();
    }

    @PostMapping(value = "/responses")
    public List<ResultDTO> responses(@RequestBody List<Response> responses) {
        // Préparer le prompt en convertissant les réponses dans un format lisible
        StringBuilder promptBuilder = new StringBuilder("La liste des questions et des réponses est la suivante :\n");
        for (Response response : responses) {
            promptBuilder.append("Question: ").append(response.question()).append("\n");
            promptBuilder.append("Réponse du candidat: ").append(response.response()).append("\n");
            promptBuilder.append("Propositions: ").append(response.propostionReponse()).append("\n");
        }
        String prompt = promptBuilder.toString();

        String systemMessage = "Tu es un correcteur de QCM. La réponse du candidat sera dans 'response'. Pour chaque question, " +
                "retourne la question, la réponse du candidat, la réponse correcte et le statut de la réponse (CORRECT ou INCORRECT)";

        ResponseEntity<ChatResponse, List<ResultDTO>> response = chatClient.prompt()
                .system(systemMessage)
                .user(prompt)
                .call()
                .responseEntity(new ParameterizedTypeReference<>() {});

        System.out.println("API Response: " + response.entity().get(0).getCorrectAnswer());
        System.out.println("API Response: " + response.entity().get(1).getCorrectAnswer());

        List<ResultDTO> results = response.entity().stream().map(result -> {
            if (result.getAnswer().equals(result.getCorrectAnswer())) {
                result.setAnswerStatus(AnswerStatus.CORRECT);
            } else {
                result.setAnswerStatus(AnswerStatus.INCORRECT);
            }
            return result;
        }).collect(Collectors.toList());

        return results;
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
