package at.isg.eloquia.features.support.model

import androidx.compose.ui.graphics.Color

/**
 * Represents an external organization that offers resources or support.
 */
data class SupportResource(
    val name: String,
    val description: String,
    val url: String,
    val accentColor: Color,
    val iconGlyph: String,
)

/**
 * Simple grouping used to render the Support page sections.
 */
data class SupportSection(
    val title: String,
    val resources: List<SupportResource>,
)

object SupportContent {
    val professionalResources: List<SupportResource> = listOf(
        SupportResource(
            name = "National Stuttering Association",
            description = "Support, resources, and community for people who stutter",
            url = "https://westutter.org/",
            accentColor = Color(0xFF4CAF50),
            iconGlyph = "NS",
        ),
        SupportResource(
            name = "The Stuttering Foundation",
            description = "Resources for therapy, research, and support",
            url = "https://www.stutteringhelp.org/",
            accentColor = Color(0xFFB388FF),
            iconGlyph = "SF",
        ),
        SupportResource(
            name = "American Speech-Language-Hearing Association",
            description = "Find a certified speech-language pathologist",
            url = "https://www.asha.org/",
            accentColor = Color(0xFF81C784),
            iconGlyph = "AS",
        ),
        SupportResource(
            name = "International Stuttering Association",
            description = "Global network of support and advocacy",
            url = "https://www.isastutter.org/",
            accentColor = Color(0xFFFF8A80),
            iconGlyph = "IS",
        ),
    )

    val sections: List<SupportSection> = listOf(
        SupportSection(
            title = "Professional Resources",
            resources = professionalResources,
        ),
    )
}
