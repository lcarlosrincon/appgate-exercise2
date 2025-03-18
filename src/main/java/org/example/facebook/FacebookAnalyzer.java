package org.example.facebook;

public interface FacebookAnalyzer {
    public double calculateFacebookCommentsScore(String comments) ;

    public Double analyzePost(String message, String facebookAccount) ;
}
