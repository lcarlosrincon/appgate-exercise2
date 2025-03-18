package org.example.facebook;

import io.micronaut.core.util.StringUtils;
import org.example.common.Risk;
import org.example.common.BasicAnalyzer;
import org.example.common.SocialMention;

public class FacebookAnalyzerService implements BasicAnalyzer {

    public static final double HIGH_RISK = -100;
    public static final double MEDIUM_RISK = 50d;
    private FacebookAnalyzer externalAnalyzer;
    private FacebookDAO dbService;

    public Risk processMessage(SocialMention socialMention) {
        Double facebookScore = 0d; // General facebook score based on comments and message
        StringBuilder auxComments = new StringBuilder();
        socialMention.getFacebookComments().forEach(comment -> auxComments.append(comment).append(" "));
        String comments = auxComments.toString().trim();
        if (StringUtils.isNotEmpty(comments)) {
            double facebookCommentsScore = externalAnalyzer.calculateFacebookCommentsScore(comments);
            if (facebookCommentsScore < MEDIUM_RISK) {
                facebookScore = HIGH_RISK;
            }
        }
        if (facebookScore > HIGH_RISK) {
            String longMessage = "facebookMessage: " + socialMention.getMessage() + " || comments: " + comments;
            facebookScore = externalAnalyzer.analyzePost(longMessage, socialMention.getFacebookAccount());
            dbService.insertFBPost(facebookScore, longMessage, socialMention.getFacebookAccount());
        }
        if (facebookScore == HIGH_RISK) {
            return Risk.HIGH_RISK;
        } else if (facebookScore > HIGH_RISK && facebookScore < MEDIUM_RISK) {
            return Risk.MEDIUM_RISK;
        } else if (facebookScore >= MEDIUM_RISK) {
            return Risk.LOW_RISK;
        }
        return null;
    }

    @Override
    public boolean isApplicable(SocialMention socialMention) {
        return socialMention.getFacebookAccount() != null;
    }
}
