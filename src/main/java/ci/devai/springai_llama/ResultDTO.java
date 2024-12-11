package ci.devai.springai_llama;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultDTO {
    private Long id;

    private String question;

    private String answer;

    private String correctAnswer;

    private AnswerStatus answerStatus;
}
