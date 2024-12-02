package ci.devai.springai_llama;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.Collections;
import java.util.List;

public class test_llma {
    public static void main(String[] args) {
        OllamaApi ollamaApi = new OllamaApi();
        OllamaOptions ollamaOptions = OllamaOptions.builder()
                .withModel("gpt-4-0")
                .withTemperature(0.0)
                .build();

        FunctionCallbackContext functionCallbackContext = null;

        java.util.List<org.springframework.ai.model.function.FunctionCallback> toolFunctionCallbacks = Collections.emptyList();

        ObservationRegistry observationRegistry = ObservationRegistry.create();

        ModelManagementOptions modelManagementOptions = new ModelManagementOptions(null, null, null, null);
        OllamaChatModel ollamaChatModel = new OllamaChatModel(
                ollamaApi,
                OllamaOptions.builder()
                        .withModel("llama3")
                        .withTemperature(0.0),null,null,observationRegistry,modelManagementOptions
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
                    je ne suis pas satisfait par la qualité de l'écran,mais le clavier est de mauvais alors que pour la souris c'est plutot assez bon niveau qualité.
                    par ailleur je pense que cette ordinateur consomme beaucoup d'énergie.
                """;
        UserMessage userMessage = new UserMessage(userInput1);

        String userInputText1 =
                """
                    e suis satisfait par la qualité de l'écran,mais le clavier est de mauvais alors que pour la souris c'est moyen niveau qualité.
                    par ailleur je pense que cette ordinateur consomme beaucoup d'énergie.
                """;

        UserMessage userMessage1 = new UserMessage(userInputText1);

        String response1 =
                """
                    {
                        "clavier": "négatif",
                        "souris": "neutre",
                        "écran": "positif"
                    }
                """;
        AssistantMessage assistantMessage1 = new AssistantMessage(response1);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        ChatResponse chatResponse = ollamaChatModel.call(prompt);
        System.out.println(chatResponse.getResult().getOutput().getContent());
    }
}
