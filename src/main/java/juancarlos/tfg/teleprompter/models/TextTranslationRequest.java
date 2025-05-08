package juancarlos.tfg.teleprompter.models;

import lombok.Data;

@Data
public class TextTranslationRequest {
    private String text;
    private String targetLanguage;

}