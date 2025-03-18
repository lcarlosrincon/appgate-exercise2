package org.example.twitter;

import org.example.common.Risk;
import org.example.SocialMention;
import org.example.common.BasicAnalyzer;

public class TwitterAnalyzerService implements BasicAnalyzer {

    public static final double HIGH = -1d;
    public static final double MEDIUM = -0.5d;
    public static final double LOW = 0.7d;
    private TwitterAnalyzer externalAnalyzer;
    private TwitterDAO dbService;

    public Risk processMessage(SocialMention socialMention) {
        // It's not clear why it's using this format but in order to avoid changing the logic, it keeps for the rest of usages
        String longMessage = "tweeterMessage: " + socialMention.getMessage();
        double score = externalAnalyzer.analyzeTweet(
                longMessage,
                socialMention.getTweeterUrl(),
                socialMention.getTweeterAccount()
        );
        dbService.insertTweet(
                score,
                longMessage,
                socialMention.getTweeterUrl(),
                socialMention.getTweeterAccount()
        );
        if (score >= HIGH && score <= MEDIUM) {
            return Risk.HIGH_RISK;
        } else if (score > MEDIUM && score < LOW) {
            return Risk.MEDIUM_RISK;
        } else if (score >= LOW) {
            return Risk.LOW_RISK;
        }
        return null;
    }

    @Override
    public boolean isApplicable(SocialMention socialMention) {
        return socialMention.getTweeterAccount() != null;
    }
}
