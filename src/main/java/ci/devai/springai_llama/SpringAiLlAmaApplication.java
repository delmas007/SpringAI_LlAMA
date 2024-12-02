package ci.devai.springai_llama;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class SpringAiLlAmaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiLlAmaApplication.class, args);
        OpenAiApi openAiApi = new OpenAiApi("sk-proj-GrElRn1zXqqqR3Ux7Uk1T3BlbkFJF8EH7AsQeu0ewOwUVQn");
        OpenAiChatModel openAiChatModel = new OpenAiChatModel(
                openAiApi,
                OpenAiChatOptions.builder()
                        .withModel("gpt-4-0")
                        .withTemperature(0.0)
                        .withMaxTokens(300)
                        .build()
        );

        String systemMessageText =
                """
                    Vous êtes un assistant spécialisé dans le domaine de l'analyse des sentiments.
                    Votre tâche est d'extraire, à partir d'un commentaire, le sentiment des différents aspects\s
                    des ordinateurs achetés par des clients. Les aspects qui nous sont intéressants sont :
                    l'écran,la souris,le clavier. le sentiment peut être positif, négatif ou neutre.
                   \s
                    Le résultat attendu sera au format JSON avec les champs suivants :
                    - clavier : le sentiment relatif au clavier
                    - souris : le sentiment relatif à la souris
                    - écran : le sentiment relatif à l'écran
               \s""";

        SystemMessage systemMessage = new SystemMessage(systemMessageText);
        String userInput1 =
                """
                    je suis satisfait par la qualité de l'écran,mais le clavier est de mauvais alors que pour la souris c'est moyen niveau qualité.
                    par ailleur je pense que cette ordinateur consomme beaucoup d'énergie.
                """;
        UserMessage userMessage = new UserMessage(userInput1);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        ChatResponse chatResponse = openAiChatModel.call(prompt);
        System.out.println(chatResponse.getResult().getOutput().getContent());
    }

}
