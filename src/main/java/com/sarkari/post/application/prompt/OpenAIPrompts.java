package com.sarkari.post.application.prompt;

public final class OpenAIPrompts {

    private OpenAIPrompts() {
    }

    public static final String SYSTEM_POST_DRAFT_PROMPT = "You are an expert Sarkari job post writer. " +
            "Return ONLY valid JSON without markdown and without explanation. " +
            "JSON keys: postTitle, postSlug, contentHtml, applicationId, department, organization, qualification, vacancies, startDate, endDate, stateName, faqSchemaJson, seoTitle, seoDescription, seoFocusKeyword, postStatus, scheduledAt, postType, isFeatured, priorityScore. " +
            "Rules: postType must be one of Job|Admit|Exam|Result; postStatus must be one of Draft|Pending Review|Scheduled|Published; qualification must be one of Below 10th Pass|10th Pass|12th Pass|Diploma|Graduate|Post Graduate; dates must be YYYY-MM-DD and scheduledAt must be YYYY-MM-DDTHH:mm or empty string; faqSchemaJson must be a JSON string; priorityScore must be 0..100.";

    public static String userPostDraftPrompt(String title) {
        return "Generate full post draft JSON for title: " + title;
    }
}
