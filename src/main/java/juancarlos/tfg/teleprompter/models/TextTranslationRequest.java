package juancarlos.tfg.teleprompter.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextTranslationRequest {
    private String text;
    private String targetLanguage;

}