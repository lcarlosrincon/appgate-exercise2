package org.example;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;

@Controller
public class SocialMentionController {
    public static final String ANALYZED_TWEETS_TABLE = "analyzed_tweets";
    public static final String ANALYZED_FB_TABLE = "analyzed_fb_posts";
    private DBService dbService = new DBService("localhost", 5432); // database host and port
    @Post("/AnalyzeSocialMention")
    @Produces(MediaType.TEXT_PLAIN)
    public String analyze(@Body SocialMention socialMention) {
        double facebookCommentsScore = 0;
        boolean isFacebook = false;
        boolean isTweeter = false;
        Double facebookScore = 0d; // General facebook score based on comments and message
        Double tweeterScore = 0d; // General facebook score based on comments and message
        if (socialMention.getFacebookAccount() != null) {
            isFacebook = true;
        } else if (socialMention.getTweeterAccount() != null) {
            isTweeter = true;
        }
        if (isFacebook || isTweeter) {
            if (isFacebook) {
                socialMention.setMessage("facebookMessage: " + socialMention.getMessage());
                String comments = socialMention
                        .getFacebookComments()
                        .stream()
                        .reduce("", (h, c) -> h + " " + c);
                socialMention.setMessage(socialMention.getMessage() + " || comments: " +
                        comments);
            } else {
                socialMention.setMessage("tweeterMessage: " + socialMention.getMessage());
            }
            // Analyze and score facebook comments if present
            if (socialMention.getMessage().contains("comments:")) {
                facebookCommentsScore = FacebookAnalyzer.calculateFacebookCommentsScore(
                        socialMention
                                .getMessage()
                                .substring(socialMention.getMessage().indexOf("comments:"))
                );
                if (facebookCommentsScore < 50d){
                    facebookScore = Double.sum(facebookScore, -100d);
                }
            }
            // Analyze facebook post (if facebook is already low then skip this analysis)
            if (isFacebook && facebookScore > -100) {
                facebookScore = FacebookAnalyzer.analyzePost(
                        socialMention.getMessage(),
                        socialMention.getFacebookAccount()
                );
                dbService.insertFBPost(
                        ANALYZED_FB_TABLE,
                        facebookScore,
                        socialMention.getMessage(),
                        socialMention.getFacebookAccount()
                );
            }
            // Analyze tweet
            if (isTweeter) {
                tweeterScore = TweeterAnalyzer.analyzeTweet(
                        socialMention.getMessage(),
                        socialMention.getTweeterUrl(),
                        socialMention.getTweeterAccount()
                );
                dbService.insertTweet(
                        ANALYZED_TWEETS_TABLE,
                        tweeterScore,
                        socialMention.getMessage(),
                        socialMention.getTweeterUrl(),
                        socialMention.getTweeterAccount()
                );
            }
            if (isFacebook) {
                if (facebookScore == -100d) {
                    return "HIGH_RISK";
                } else if (facebookScore > -100d && facebookScore < 50d) {
                    return "MEDIUM_RISK";
                } else if (facebookScore >= 50d) {
                    return "LOW_RISK";
                }
            }
            if (isTweeter) {
                if (tweeterScore >= -1 && tweeterScore <= -0.5d) {
                    return "HIGH_RISK";
                } else if (tweeterScore > -0.5d && tweeterScore < 0.7d) {
                    return "MEDIUM_RISK";
                } else if (facebookScore >= 0.7d) {
                    return "LOW_RISK";
                }
            }
        } else {
            return "Error, Tweeter or Facebook account must be present";
        }
        return socialMention.getMessage();
    }
}