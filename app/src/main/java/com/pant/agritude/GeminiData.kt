package com.pant.agritude.gemini

// இது Gemini API-க்கு அனுப்பும் JSON கோரிக்கையை குறிக்கிறது.
// This data class represents the JSON request body for the Gemini API.
data class GeminiRequest(
    val contents: List<Content>
)

// ஒரு உரையாடல் உள்ளடக்கத்தை குறிக்கிறது.
// Represents a piece of conversational content.
data class Content(
    val parts: List<Part>
)

// உரையாடலின் ஒரு பகுதியை குறிக்கிறது (உரையாடல், படம், போன்றவை).
// Represents a part of the content, like text or an image.
data class Part(
    val text: String
)

// இது Gemini API-யிடமிருந்து வரும் JSON பதிலைக் குறிக்கிறது.
// This data class represents the JSON response body from the Gemini API.
data class GeminiResponse(
    val candidates: List<Candidate>
)

// சாத்தியமான பதில்களில் ஒன்று.
// Represents a possible response candidate.
data class Candidate(
    val content: Content
)
