package org.example.common;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import org.example.facebook.FacebookAnalyzerService;
import org.example.twitter.TwitterAnalyzerService;

import java.util.List;
import java.util.Optional;

@Controller("/social-mentions")
public class SocialMentionController {

    private final List<BasicAnalyzer> analyzers = List.of(
            new FacebookAnalyzerService(),
            new TwitterAnalyzerService());

    @Post("/risks")
    @Produces(MediaType.TEXT_PLAIN)
    public String analyzeRisk(@Body SocialMention socialMention) {
        Optional<BasicAnalyzer> analyzer = analyzers.stream()
                .filter(basicAnalyzer -> basicAnalyzer.isApplicable(socialMention))
                .findFirst();
        Risk risk = analyzer.map(basicAnalyzer -> basicAnalyzer.processMessage(socialMention))
                .orElseThrow(() -> new ClientException("Error, Account must be present"));
        return risk != null ? risk.name() : socialMention.getMessage();
    }
}