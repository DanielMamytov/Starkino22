package co.nisari.katisnar.presentation.ui.stararticle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nisari.katisnar.R
import co.nisari.katisnar.presentation.data.local.ArticleEntity
import co.nisari.katisnar.presentation.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class StarArticleListViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val articles: List<ArticleListItem> = emptyList()
    )

    sealed interface UiEvent {
        data class NavigateToArticle(val articleId: Long) : UiEvent
    }

    private data class ArticleSeed(
        val title: String,
        val content: String,
        val image: Int
    )

    private val isSeeding = AtomicBoolean(false)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    init {
        observeArticles()
    }

    fun onArticleClicked(item: ArticleListItem) {
        viewModelScope.launch {
            _events.emit(UiEvent.NavigateToArticle(item.id))
        }
    }

    private fun observeArticles() {
        viewModelScope.launch {
            repository.getAll().collectLatest { entities ->
                if (entities.isEmpty()) {
                    if (isSeeding.compareAndSet(false, true)) {
                        val seededEntities = seedDefaultArticles()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                articles = seededEntities.map(ArticleEntity::toListItem)
                            )
                        }
                    } else {
                        _state.update { it.copy(isLoading = false, articles = emptyList()) }
                    }
                    return@collectLatest
                }

                if (backfillDefaultCovers(entities)) {
                    return@collectLatest
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        articles = entities.map(ArticleEntity::toListItem)
                    )
                }
            }
        }
    }

    private suspend fun seedDefaultArticles(): List<ArticleEntity> {
        return DEFAULT_ARTICLES.map { seed ->
            val entity = seed.toEntity()
            val id = repository.insert(entity)
            entity.copy(id = id)
        }
    }

    private suspend fun backfillDefaultCovers(entities: List<ArticleEntity>): Boolean {
        var didUpdate = false
        entities.forEach { entity ->
            val defaultCover = DEFAULT_ARTICLES_BY_TITLE[entity.title]?.image ?: return@forEach
            if (entity.coverUri != defaultCover) {
                repository.updateCover(entity.id, defaultCover)
                didUpdate = true
            }
        }
        return didUpdate
    }

    private fun ArticleEntity.toListItem(): ArticleListItem {
        return ArticleListItem(
            id = id,
            title = title,
            preview = content.createPreview(),
            coverResId = coverUri ?: DEFAULT_ARTICLES_BY_TITLE[title]?.image ?: DEFAULT_LIST_COVER_RES
        )
    }

    private fun ArticleSeed.toEntity(): ArticleEntity = ArticleEntity(
        title = title,
        content = content,
        coverUri = image
    )

    private fun String.createPreview(): String {
        val normalized = trim()
        if (normalized.length <= PREVIEW_LIMIT) {
            return normalized
        }
        val truncated = normalized.take(PREVIEW_LIMIT).trimEnd()
        return "$truncated\u2026"
    }

    private companion object {
        private const val PREVIEW_LIMIT = 180
        private val DEFAULT_LIST_COVER_RES = R.drawable.img_night_city

        private val DEFAULT_ARTICLES = listOf(
            ArticleSeed(
                title = "The City That Never Sleeps Under the Stars",
                content = """
                    There is a moment each evening when the city begins to glow with the light of thousands of stars, both above and around you. Neon reflections ripple on wet streets, music drifts from rooftop bars, and every building seems to hum with hidden stories. This is the true pulse of the night, and Starkino invites you to capture it. Every coordinate you save becomes a portal to memory, a digital echo of where you once stood.
                    
                    The modern traveler is not guided by maps alone. Instead, we navigate emotions, experiences, and atmosphere. The street corner where you shared a secret, the terrace where the skyline felt infinite, the park where the wind whispered something only you could understand. Each of these deserves a place in your personal constellation of memories.
                    
                    Technology often distances us from reality, but in this context it draws us closer. With each pin you drop, you are claiming a star for yourself. The map becomes a reflection of your journey, a story told through light and space.
                    
                    As you explore, remember to look up once in a while. The same stars above your city shimmer over oceans and deserts. They have seen civilizations rise and fade, and now they illuminate your path. The beauty of night is not only in its mystery but in its infinite capacity for rediscovery. With Starkino, your story joins that timeless sky.
                """.trimIndent(),
                R.drawable.article_img1
            ),
            ArticleSeed(
                title = "Luxury of the Moment",
                content = """
                    Luxury is not defined by wealth or possessions but by attention. To live luxuriously is to notice the details that others overlook. It is to slow down long enough to taste, feel, and truly exist within a single moment. Starkino was designed for those who wish to record such moments before they fade.
                    
                    Imagine standing on a balcony overlooking a quiet coastline. The city hum is replaced by the rhythm of waves, the glow of your screen softly illuminating your face. You open Starkino, mark your position, write a thought, and close your phone. That single act transforms an instant into something eternal.
                    
                    In a world of constant movement, mindfulness becomes the rarest treasure. The more you move, the easier it is to forget. Yet with Starkino, every journey is anchored. The digital becomes poetic, the simple act of saving a note turns into a ritual of presence.
                    
                    Luxury today is connected. To the world, to oneself, to memory. When you travel, do not rush through the experience. Breathe, record, observe. The beauty of this era lies in the ability to blend technology with emotion, to build a collection of luminous memories that reflect who you are.
                """.trimIndent(),
                R.drawable.article_img2
            ),
            ArticleSeed(
                title = "The Hidden Geometry of Travel",
                content = """
                    Every journey forms a pattern across the globe. If you could trace your movements from space, you would see elegant shapes emerging from randomness. Triangles between cities, spirals of return trips, intersecting lines that form a portrait of your life. Starkino is the tool that reveals this invisible geometry.
                    
                    Each saved route is more than a record. It is a symbol of curiosity. Every coordinate you capture adds a vertex to your personal constellation. This pattern belongs only to you.
                    
                    Think of explorers who once drew maps by hand. Their lines were guesses, born of intuition and discovery. You do the same today with digital precision. The difference is that your map is not meant for others. It is for reflection, to show how far you have traveled and how much of yourself you have discovered along the way.
                    
                    Sometimes you will find that the most interesting points are not distant at all. They are close, hidden in familiar streets. The café you visit when you need to think, the bridge that catches the last sunset, the bench where silence feels safe. By saving them, you are drawing meaning into your daily routine.
                    
                    Travel is not about distance. It is about awareness. With Starkino, you can turn your life into a living map, filled with the geometry of experience.
                """.trimIndent(),
                R.drawable.article_img3
            ),
            ArticleSeed(
                title = "Digital Constellations",
                content = """
                    Once, people looked at the sky to navigate. Now, we look at screens. But perhaps these two actions are not so different. Both involve reading patterns of light, searching for meaning, and finding direction. Starkino merges these worlds.
                    
                    Each coordinate becomes a star. Each note, a story. Together, they form constellations that reflect your personal universe. Unlike the fixed constellations of the sky, yours are dynamic, ever-changing. They grow as you move, expand as you live.
                    
                    There is beauty in this digital cosmos. Every pin glows with significance. When you open your map, you are looking at more than points and numbers. You are witnessing your own evolution.
                    
                    The secret to building a meaningful constellation lies in intention. Do not mark places just for the sake of it. Choose them because they hold something special. A memory, an idea, an emotion. The more authentic your entries, the brighter your constellation will shine.
                    
                    When you share stories with others, show them your sky. Each star is a reminder that life is not measured by destinations but by the light you leave behind.
                """.trimIndent(),
                R.drawable.article_img4

            ),
            ArticleSeed(
                title = "The Art of Silence",
                content = """
                    In the noise of daily life, silence becomes a luxury. We fill every moment with notifications, conversations, and constant stimulation. Yet silence has its own rhythm, and within it lies creativity. Starkino can be used as a tool for that kind of mindful silence.
                    
                    Imagine walking alone at night. The city glows softly, and your footsteps echo in narrow streets. You stop, mark your position, and write one quiet thought. This simple action is a dialogue with yourself.
                    
                    Your coordinates become anchors for reflection. You begin to associate places not just with activity but with emotion. The park where you made a decision, the hill where you felt peace, the corner that changed your mind about something important.
                    
                    When life feels too loud, open your notebook of locations. Revisit those coordinates. Each entry will whisper something different, something you might have forgotten.
                    
                    Silence is not emptiness. It is clear. It is the sound of presence. Use Starkino to find and preserve those rare moments when the world slows down enough for you to hear yourself again.
                """.trimIndent(),
                R.drawable.article_img5
            ),
            ArticleSeed(
                title = "Stars, Stories, and Screens",
                content = """
                    Humanity has always been drawn to light. From ancient fires to neon cities, we gather around illumination and turn it into meaning. Starkino is a continuation of that instinct. It allows you to capture light in a digital form through coordinates, notes, and memories.
                    
                    Every story begins somewhere. A star above a skyline, a light in a distant window, the reflection of your phone screen on glass. We record these fragments to remind ourselves that beauty exists in detail.
                    
                    When you save a location, you are not just saving geography. You are saving an emotion tied to that exact latitude and longitude. You are building a story that can only be told through the lens of your experiences.
                    
                    Screens are often blamed for distraction, yet in the right hands they become instruments of awareness. Starkino transforms your screen into a mirror of your journey. It turns technology into storytelling.
                    
                    Your story deserves to shine. The sky has room for infinite stars, and your digital map is no different. Let it glow.
                """.trimIndent(),
                R.drawable.article_img6
            ),
            ArticleSeed(
                title = "The Light of Privacy",
                content = """
                    In an era where everything is shared, privacy feels rare and precious. The most meaningful experiences are often the ones you keep to yourself. Starkino was designed with that philosophy in mind. Your data stays on your device, and your memories belong only to you.
                    
                    This is not about secrecy but about ownership. It is about reclaiming control over your personal story. When you record a thought or location, you can trust that it remains yours. No algorithms analyze it, no cloud copies it without permission.
                    
                    There is elegance in solitude. Knowing that your notes exist purely for your reflection adds authenticity to your journaling. It transforms each entry into an honest moment.
                    
                    Privacy nurtures creativity. Without the pressure of an audience, you are free to write, explore, and experiment. Your routes become poetic, your notes sincere.
                    
                    In this way, Starkino represents more than technology. It represents freedom. A space where your story shines privately, like a single light in the vast night sky.
                """.trimIndent(),
                R.drawable.article_img7
            ),
            ArticleSeed(
                title = "Modern Nomads and Digital Maps",
                content = """
                    The new traveler is both physical and digital. We wander through landscapes while staying connected to invisible networks of information. Our devices have become part of our identity. Starkino celebrates that dual existence.
                    
                    The modern nomad no longer carries paper maps or heavy journals. The world fits into a pocket, yet the meaning of exploration remains unchanged. Curiosity drives us. The desire to see, to feel, to understand.
                    
                    Each place you mark is more than a destination. It is a dialogue between you and the planet. When you drop a pin, you are saying I was here and this moment mattered.
                    
                    Even if you return to the same city a year later, the coordinates stay. They remind you how you have changed. The café may be different, the air colder, but the memory endures.
                    
                    Technology is often criticized for making us detached. Yet when used with purpose, it deepens our connection. Starkino helps modern nomads rediscover the art of presence in motion.
                """.trimIndent(),
                R.drawable.article_img8
            ),
            ArticleSeed(
                title = "The Eternal Glow of Memory",
                content = """
                    Memories are fragile. They fade, blur, and rearrange themselves over time. Yet when you attach them to a specific place, they become tangible again. Starkino transforms your geography into a museum of emotions.
                    
                    Think of each saved location as a lamp in the dark. When you open the app, you are lighting up your past. Every point is alive with feeling.
                    
                    Sometimes a single note can bring back an entire season. The smell of summer rain, the sound of laughter, the golden glow of streetlights. These sensory memories return because they were anchored in space and time.
                    
                    The more entries you make, the brighter your personal map becomes. It turns into a constellation of who you are, who you were, and who you are becoming.
                    
                    In the end, technology cannot replace memory, but it can preserve it. Starkino gives you the tools to protect your light. And in doing so, it ensures that your story continues to shine, even when the night grows long.
                """.trimIndent(),
                R.drawable.article_img9
            )
        )

        private val DEFAULT_ARTICLES_BY_TITLE = DEFAULT_ARTICLES.associateBy { it.title }
    }
}

data class ArticleListItem(
    val id: Long,
    val title: String,
    val preview: String,
    val coverResId: Int,
)
