package org.example.common;

public interface BasicAnalyzer {

    Risk processMessage(SocialMention socialMention);

    default boolean isApplicable(SocialMention socialMention) {
        return false;
    }
}
