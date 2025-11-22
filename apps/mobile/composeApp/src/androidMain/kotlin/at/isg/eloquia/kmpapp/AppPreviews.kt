package at.isg.eloquia.kmpapp

import androidx.compose.runtime.Composable
import at.isg.eloquia.core.theme.EloquiaTheme
import at.isg.eloquia.core.theme.components.EloquiaPreview
import at.isg.eloquia.kmpapp.data.MuseumObject
import at.isg.eloquia.kmpapp.screens.detail.DetailScreenContent
import at.isg.eloquia.kmpapp.screens.list.ListScreenContent

/**
 * Android-specific previews for commonMain composables.
 * Following KMP best practices: previews live in androidMain and call shared composables.
 */

// Sample data for previews
private val sampleMuseumObject = MuseumObject(
    objectID = 1,
    title = "Starry Night",
    artistDisplayName = "Vincent van Gogh",
    medium = "Oil on canvas",
    dimensions = "73.7 cm × 92.1 cm",
    objectURL = "https://example.com/object/1",
    objectDate = "1889",
    primaryImage = "https://via.placeholder.com/400",
    primaryImageSmall = "https://via.placeholder.com/200",
    repository = "Museum of Modern Art",
    department = "European Paintings",
    creditLine = "Gift of Anonymous Donor"
)

private val sampleMuseumObjects = listOf(
    sampleMuseumObject,
    sampleMuseumObject.copy(
        objectID = 2,
        title = "The Scream",
        artistDisplayName = "Edvard Munch",
        objectDate = "1893"
    ),
    sampleMuseumObject.copy(
        objectID = 3,
        title = "Girl with a Pearl Earring",
        artistDisplayName = "Johannes Vermeer",
        objectDate = "1665"
    )
)

@EloquiaPreview
@Composable
fun ListScreenPreview() {
    EloquiaTheme {
        ListScreenContent(
            objects = sampleMuseumObjects,
            onObjectClick = {}
        )
    }
}

@EloquiaPreview
@Composable
fun ListScreenEmptyPreview() {
    EloquiaTheme {
        ListScreenContent(
            objects = emptyList(),
            onObjectClick = {}
        )
    }
}

@EloquiaPreview
@Composable
fun DetailScreenPreview() {
    EloquiaTheme {
        DetailScreenContent(
            museumObject = sampleMuseumObject,
            onBackClick = {}
        )
    }
}

@EloquiaPreview
@Composable
fun DetailScreenLoadingPreview() {
    EloquiaTheme {
        DetailScreenContent(
            museumObject = null,
            onBackClick = {}
        )
    }
}
