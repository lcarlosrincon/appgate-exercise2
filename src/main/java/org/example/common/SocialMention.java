package org.example.common;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SocialMention {
    private String message;
    private String facebookAccount;
    private String tweeterAccount;
    private String creationDate;
    private String tweeterUrl;
    private List<String> facebookComments;
}
