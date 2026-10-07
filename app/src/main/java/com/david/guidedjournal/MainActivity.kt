package com.david.guidedjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.david.guidedjournal.ui.theme.GuidedJournalTheme
import com.david.guidedjournal.ui.theme.SplashBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

// Stores the name of the currently displayed screen.
// The JournalApp composable uses its own local currentScreen state for navigation.
lateinit var currentScreen: String

/**
 * MainActivity - The single activity that hosts all screens of the app.
 */
class MainActivity : ComponentActivity() {

    // Repository used by the activity to provide database access to the Compose UI.
    private lateinit var repository: JournalRepository

    /**
     * Called when the activity is created.
     * Sets up the database and launches the Compose UI.
     *
     * @param savedInstanceState Previously saved activity state, if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the singleton Room database instance for the application.
        val database = JournalDatabase.getDatabase(this)

        // Create the repository using the DAO supplied by the database.
        repository = JournalRepository(database.journalDao())

        // Start the Jetpack Compose UI and apply the app's theme.
        setContent {
            GuidedJournalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the shared repository into the root navigation composable.
                    JournalApp(repository = repository)
                }
            }
        }
    }
}

/**
 * JournalApp - Root composable managing navigation between all screens.
 *
 * @param repository Shared data repository used by all screens.
 */
@Composable
fun JournalApp(repository: JournalRepository) {

    // Stores which screen should currently be displayed.
    var currentScreen by remember { mutableStateOf("splash") }

    // Stores the entry selected for editing.
    var selectedEntry by remember { mutableStateOf<Entry?>(null) }

    // Stores all entries belonging to the selected day.
    var selectedDayEntries by remember { mutableStateOf<List<Entry>>(emptyList()) }

    // Stores the formatted label for the selected day.
    var selectedDayLabel by remember { mutableStateOf("") }

    // Stores the prompt selected for the user to answer.
    var selectedPrompt by remember { mutableStateOf<Prompt?>(null) }

    // Incrementing this triggers HomeScreen to reload its entries.
    var refreshKey by remember { mutableIntStateOf(0) }

    // Display the appropriate screen based on the current navigation state.
    when (currentScreen) {

        // Initial screen shown when the app starts.
        "splash" -> SplashScreen(onFinished = { currentScreen = "home" })

        // Main home screen containing the quote, prompt, and journal records.
        "home" -> HomeScreen(
            repository = repository,
            refreshKey = refreshKey,
            onOpenPrompt = { prompt ->
                // Store the selected prompt before opening the writing screen.
                selectedPrompt = prompt
                currentScreen = "write_entry"
            },
            onOpenPastDay = { dayEntries, dayLabel ->
                // Store the selected day's entries and label before opening the day screen.
                selectedDayEntries = dayEntries
                selectedDayLabel = dayLabel
                currentScreen = "past_day"
            },
            onBrowsePrompts = {
                // Open the screen containing all available prompts.
                currentScreen = "browse_prompts"
            }
        )

        // Screen used to write and save a new journal entry.
        "write_entry" -> WriteEntryScreen(
            repository = repository,
            prompt = selectedPrompt,
            onBack = {
                // Increment key so HomeScreen reloads entries.
                refreshKey++
                currentScreen = "home"
            }
        )

        // Screen showing all entries belonging to a selected day.
        "past_day" -> PastDayScreen(
            repository = repository,
            entries = selectedDayEntries,
            dayLabel = selectedDayLabel,
            onBack = {
                // Refresh the home screen when returning.
                refreshKey++
                currentScreen = "home"
            },
            onEntryDeleted = {
                // Refresh the home screen after the final entry is deleted.
                refreshKey++
                currentScreen = "home"
            },
            onEditEntry = { entry ->
                // Store the selected entry before opening the edit screen.
                selectedEntry = entry
                currentScreen = "edit_entry"
            }
        )

        // Screen used to edit an existing journal entry.
        "edit_entry" -> EditEntryScreen(
            repository = repository,
            entry = selectedEntry,
            onBack = {
                // Refresh the records after returning from editing.
                refreshKey++
                currentScreen = "past_day"
            }
        )

        // Screen allowing the user to browse and select any available prompt.
        "browse_prompts" -> BrowsePromptsScreen(
            repository = repository,
            onSelectPrompt = { prompt ->
                // Store the chosen prompt and open the writing screen.
                selectedPrompt = prompt
                currentScreen = "write_entry"
            },
            onBack = { currentScreen = "home" }
        )
    }
}

/**
 * SplashScreen - Opening screen with logo, app name and spinner.
 * Stays for 6 seconds then transitions to the home screen.
 *
 * @param onFinished Callback triggered when splash is done.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {

    // Run the splash timer once when this composable first enters the composition.
    LaunchedEffect(Unit) {
        delay(6000.milliseconds)

        // Notify the parent composable that the splash screen is finished.
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display the application logo.
            Image(
                painter = painterResource(id = R.drawable.gj_logo),
                contentDescription = "Guided Journal Logo",
                modifier = Modifier.size(220.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display the application name using two different font weights.
            Row {
                Text("Guided", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Journal", fontSize = 30.sp, fontWeight = FontWeight.Normal, color = Color.White)
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Show a small loading indicator while the splash screen is displayed.
            CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.8f),
                strokeWidth = 2.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * HomeScreen - Main screen with rotating quote, today's prompt, and past records.
 *
 * @param repository Data repository used to load prompts, quotes, and entries.
 * @param refreshKey Value that changes when the screen should reload its data.
 * @param onOpenPrompt Callback to open a prompt for writing.
 * @param onOpenPastDay Callback to open a past day's entries.
 * @param onBrowsePrompts Callback to open the complete prompt browser.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: JournalRepository,
    refreshKey: Int,
    onOpenPrompt: (Prompt?) -> Unit,
    onOpenPastDay: (List<Entry>, String) -> Unit,
    onBrowsePrompts: () -> Unit
) {
    // Coroutine scope used for database operations triggered by the UI.
    val scope = rememberCoroutineScope()

    // Holds all journal entries loaded from the database.
    var allEntries by remember { mutableStateOf<List<Entry>>(emptyList()) }

    // Holds the mandatory prompt currently selected for the user.
    var currentPrompt by remember { mutableStateOf<Prompt?>(null) }

    // Holds the quote currently displayed on the home screen.
    var todayQuote by remember { mutableStateOf("") }

    // Tracks which quote is currently being displayed.
    var quoteIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    // Determines whether Morning, Afternoon, or Night prompts should be displayed.
    val timeOfDay = repository.getCurrentTimeOfDay()

    // Reload entries and prompt data whenever refreshKey changes.
    LaunchedEffect(refreshKey) {
        scope.launch {

            // Make sure the prompt database has been initialized.
            repository.initializePrompts()

            // Load all saved journal entries.
            allEntries = repository.getEntries()

            // Count today's entries so the next mandatory prompt can be selected.
            val todayDoneCount = allEntries
                .filter { isSameDay(it.date, System.currentTimeMillis()) }.size

            // Select the next mandatory prompt based on today's progress.
            currentPrompt = repository.getNextMandatoryPrompt(todayDoneCount)

            // Load today's starting quote.
            todayQuote = repository.getTodayQuote()

            // Find the quote's position so quote rotation can continue from it.
            quoteIndex = repository.dailyQuotes.indexOf(todayQuote)
        }
    }

    // Rotate quotes every 30 seconds while the home screen is active.
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000.milliseconds)
            quoteIndex = (quoteIndex + 1) % repository.dailyQuotes.size
            todayQuote = repository.dailyQuotes[quoteIndex]
        }
    }

    // Filter the complete entry list to only entries written today.
    val todayEntries = allEntries.filter { isSameDay(it.date, System.currentTimeMillis()) }

    // Include ALL entries in records (today + past), grouped by date.
    // Today shows at the top labeled "Today".
    val allEntriesByDay: Map<String, List<Entry>> = buildMap {
        if (todayEntries.isNotEmpty()) {
            put("Today — ${formatTodayFull()}", todayEntries)
        }

        allEntries
            .filter { !isSameDay(it.date, System.currentTimeMillis()) }
            .groupBy { formatDateLabel(it.date) }
            .toSortedMap(compareByDescending { it })
            .forEach { (label, entries) -> put(label, entries) }
    }

    Scaffold(
        topBar = {
            // Main application bar displayed at the top of the home screen.
            TopAppBar(
                title = {
                    Text(
                        "Guided Journal",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            // ── Rotating Quote Card ─────────────────────────────────
            // Displays the current motivational or spiritual quote.
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Quote of the Moment",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "\"$todayQuote\"",
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // ── Today's Journal Header ──────────────────────────────
            // Displays the journal section title and today's date.
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Journal",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatTodayFull(),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Time of Day Label + Progress Counter ───────────────
            // Shows the current part of the day and the user's daily progress.
            item {
                val emoji = when (timeOfDay) {
                    "Morning" -> "🌅"
                    "Afternoon" -> "☀️"
                    else -> "🌙"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$emoji $timeOfDay Prompts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Show X/10 progress toward daily mandatory target.
                    val mandatoryCount = todayEntries.size.coerceAtMost(10)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (todayEntries.size >= 10)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.primaryContainer
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${todayEntries.size}/10 prompts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (todayEntries.size >= 10)
                                Color.White
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // ── Today's Prompt Card (clickable) ─────────────────────
            // Displays the next mandatory prompt and opens the writing screen when tapped.
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOpenPrompt(currentPrompt) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentPrompt?.category ?: "Reflection",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = timeOfDay,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentPrompt?.text ?: "Tap to load today's prompt",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tap to respond →",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )

                            // Opens the screen where all prompts can be browsed.
                            TextButton(
                                onClick = { onBrowsePrompts() },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "Browse all prompts",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // ── Records Header ──────────────────────────────────────
            // Heading for the user's saved journal records.
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Records",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // ── All Records (today + past) ──────────────────────────
            // Displays either an empty-state message or a card for each journal day.
            if (allEntriesByDay.isEmpty()) {
                item {
                    Text(
                        text = "Your journal entries will appear here after writing.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                items(allEntriesByDay.entries.toList()) { (dayLabel, dayEntries) ->
                    val isToday = dayLabel.startsWith("Today")

                    // Each card represents one day's journal entries.
                    PastDayCard(
                        dayLabel = dayLabel,
                        entryCount = dayEntries.size,
                        preview = dayEntries.first().content,
                        isToday = isToday,
                        onClick = { onOpenPastDay(dayEntries, dayLabel) }
                    )
                }
            }
        }
    }
}

/**
 * PastDayCard - Summary card for a past day's journal entries.
 *
 * @param dayLabel Formatted date label.
 * @param entryCount Number of entries that day.
 * @param preview Text preview from the first entry.
 * @param isToday Whether this card represents today's entries.
 * @param onClick Callback when tapped.
 */
@Composable
fun PastDayCard(
    dayLabel: String,
    entryCount: Int,
    preview: String,
    isToday: Boolean = false,
    onClick: () -> Unit
) {
    // Card opens the selected day's complete journal entries.
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isToday)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isToday) 4.dp else 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isToday)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                // Displays how many journal entries were written on this day.
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$entryCount ${if (entryCount == 1) "entry" else "entries"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Show a shortened preview of the first entry.
            Text(
                text = if (preview.length > 80) preview.substring(0, 80) + "..." else preview,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * WriteEntryScreen - Screen for responding to a prompt and saving a journal entry.
 * Shows the prompt, allows response, and lets user request more prompts.
 *
 * @param repository Data repository used to save and retrieve entries/prompts.
 * @param prompt The prompt to respond to.
 * @param onBack Callback to return home after saving or leaving the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteEntryScreen(
    repository: JournalRepository,
    prompt: Prompt?,
    onBack: () -> Unit
) {
    // Coroutine scope used for database operations.
    val scope = rememberCoroutineScope()

    // Stores the user's written journal response.
    var content by remember { mutableStateOf("") }

    // Stores the original prompt passed into the screen.
    var activePrompt by remember { mutableStateOf(prompt) }

    // Stores additional prompts loaded when the user requests more.
    var extraPrompts by remember { mutableStateOf<List<Prompt>>(emptyList()) }

    // Stores the additional prompt currently selected by the user.
    var selectedExtraPrompt by remember { mutableStateOf<Prompt?>(null) }

    // Determines the current time-of-day category.
    val timeOfDay = repository.getCurrentTimeOfDay()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Write Entry",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            "$timeOfDay • ${formatTodayFull()}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    // Allows the user to return without saving.
                    TextButton(onClick = onBack) {
                        Text("Back", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Active Prompt Card ──────────────────────────────────
            // Shows the prompt currently selected for the journal response.
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = (selectedExtraPrompt ?: activePrompt)?.category ?: "Reflection",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Mark mandatory prompts so the user knows they are required.
                            if ((selectedExtraPrompt ?: activePrompt)?.isMandatory == true) {
                                Text(
                                    text = "★ Required",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = (selectedExtraPrompt ?: activePrompt)?.text
                                ?: "What is on your mind?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            // ── Text Input ──────────────────────────────────────────
            // Text field where the user writes the answer to the selected prompt.
            item {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = { Text("Write your response here...") },
                    label = { Text("Your Response") }
                )
            }

            // ── Save Button ─────────────────────────────────────────
            // Saves the response as a new Entry in the database.
            item {
                Button(
                    onClick = {
                        if (content.isNotBlank()) {
                            // Use the selected extra prompt if one was chosen;
                            // otherwise use the original active prompt.
                            val usedPrompt = selectedExtraPrompt ?: activePrompt

                            scope.launch {
                                repository.addEntry(
                                    Entry(
                                        content = content,
                                        category = usedPrompt?.category ?: "Reflection",
                                        promptText = usedPrompt?.text ?: "",
                                        promptTimeOfDay = timeOfDay,
                                        date = System.currentTimeMillis()
                                    )
                                )

                                // Return to the home screen after saving.
                                onBack()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Save Entry", fontSize = 16.sp, color = Color.White)
                }
            }

            // ── More Prompts Button ─────────────────────────────────
            // Loads additional prompts matching the current time of day.
            item {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            extraPrompts = repository.getMorePrompts(5)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Request More Prompts", fontSize = 14.sp)
                }
            }

            // ── Extra Prompts List ──────────────────────────────────
            // Only display this section after additional prompts have been loaded.
            if (extraPrompts.isNotEmpty()) {
                item {
                    Text(
                        text = "More prompts for $timeOfDay — tap one to use it:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(extraPrompts) { extraPrompt ->
                    // Determine whether this prompt is currently selected.
                    val isSelected = selectedExtraPrompt?.id == extraPrompt.id

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            // Toggle selection of the additional prompt.
                            selectedExtraPrompt = if (isSelected) null else extraPrompt

                            // Clear the previous response when switching prompts.
                            content = ""
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected)
                            CardDefaults.outlinedCardBorder()
                        else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = extraPrompt.category,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = extraPrompt.text,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )

                            // Display a visual confirmation when this prompt is selected.
                            if (isSelected) {
                                Text(
                                    text = "✓ Selected - write your response above",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * PastDayScreen - Shows all entries for a specific past day.
 * Entries within 24 hours are editable; older entries are read-only.
 *
 * @param repository Data repository used to check editability and delete entries.
 * @param entries List of entries for this day.
 * @param dayLabel Formatted date label.
 * @param onBack Callback to return home.
 * @param onEntryDeleted Callback after all entries are deleted.
 * @param onEditEntry Callback to open edit screen for an entry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastDayScreen(
    repository: JournalRepository,
    entries: List<Entry>,
    dayLabel: String,
    onBack: () -> Unit,
    onEntryDeleted: () -> Unit,
    onEditEntry: (Entry) -> Unit
) {
    // Coroutine scope used for deleting entries from the database.
    val scope = rememberCoroutineScope()

    // Local copy of the entries so the screen can immediately update after deletion.
    var currentEntries by remember { mutableStateOf(entries) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Journal Entry",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            dayLabel,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Edit window notice at the top.
            item {
                // Check whether at least one entry can still be edited.
                val anyEditable = currentEntries.any { repository.isEditable(it) }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (anyEditable)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = if (anyEditable)
                            "✎  Entries written within 24 hours can still be edited."
                        else
                            "🔒  These entries are past their 24-hour edit window and are read-only.",
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp),
                        color = if (anyEditable)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Display each journal entry belonging to the selected day.
            items(currentEntries) { entry ->

                // Determine whether this specific entry is still within its edit window.
                val canEdit = repository.isEditable(entry)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Category + edit window badge.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = entry.category,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                // Show the time of day associated with the prompt.
                                if (entry.promptTimeOfDay.isNotEmpty()) {
                                    Text(
                                        text = entry.promptTimeOfDay,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Edit/Delete buttons.
                            Row {

                                // The edit button only appears while the entry is editable.
                                if (canEdit) {
                                    IconButton(
                                        onClick = { onEditEntry(entry) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Delete remains available for the displayed entry.
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            repository.deleteEntry(entry)

                                            // Remove the deleted entry from the current screen immediately.
                                            currentEntries =
                                                currentEntries.filter { it.id != entry.id }

                                            // Return home if there are no entries left for this day.
                                            if (currentEntries.isEmpty()) onEntryDeleted()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Prompt question the user answered.
                        if (entry.promptText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = entry.promptText,
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = DividerDefaults.Thickness,
                            color = DividerDefaults.color
                        )

                        // The user's written response.
                        Text(
                            text = entry.content,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )

                        // Edit window notice.
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (canEdit)
                                "✎ Editable for 24hrs from creation"
                            else
                                "🔒 Read-only after 24 hours",
                            fontSize = 11.sp,
                            color = if (canEdit)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * EditEntryScreen - Edits an existing entry within its 24-hour window.
 *
 * @param repository Data repository used to update the entry.
 * @param entry The Entry being edited.
 * @param onBack Callback to return to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    repository: JournalRepository,
    entry: Entry?,
    onBack: () -> Unit
) {
    // Coroutine scope used for the database update.
    val scope = rememberCoroutineScope()

    // Initialize the text field with the existing entry content.
    var content by remember { mutableStateOf(entry?.content ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Entry", color = Color.White) },
                navigationIcon = {
                    // Return to the previous screen without updating the entry.
                    TextButton(onClick = onBack) {
                        Text("Back", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // Show the original prompt question for context while editing.
            if (entry?.promptText?.isNotEmpty() == true) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = entry.promptText,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            // Text field containing the user's existing response.
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("Edit your response...") },
                label = { Text("Your Response") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save the edited response back to the database.
            Button(
                onClick = {
                    if (content.isNotBlank() && entry != null) {
                        scope.launch {
                            // Create an updated copy while preserving the rest of the entry data.
                            repository.updateEntry(entry.copy(content = content))

                            // Return to the previous screen after updating.
                            onBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Update Entry", fontSize = 16.sp)
            }
        }
    }
}

/**
 * BrowsePromptsScreen - Shows ALL prompts organized by category.
 * User can tap any prompt to respond to it directly.
 *
 * @param repository Data repository for loading prompts.
 * @param onSelectPrompt Callback when user picks a prompt to answer.
 * @param onBack Callback to return home.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowsePromptsScreen(
    repository: JournalRepository,
    onSelectPrompt: (Prompt) -> Unit,
    onBack: () -> Unit
) {
    // Stores prompts grouped by their category.
    var promptsByCategory by remember {
        mutableStateOf<Map<String, List<Prompt>>>(emptyMap())
    }

    // Stores which category is currently expanded.
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    // Load all prompts when this screen is first displayed.
    LaunchedEffect(Unit) {
        promptsByCategory = repository.getAllPromptsByCategory()

        // Auto-expand the first category.
        expandedCategory = promptsByCategory.keys.firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "All Prompts",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    // Return to the home screen.
                    TextButton(onClick = onBack) {
                        Text("Back", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Total prompt count header.
            item {
                // Calculate the number of prompts across every category.
                val totalCount = promptsByCategory.values.sumOf { it.size }

                Text(
                    text = "$totalCount prompts across ${promptsByCategory.size} categories",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // One section per category.
            promptsByCategory.forEach { (category, prompts) ->

                // Determine whether this category is currently expanded.
                val isExpanded = expandedCategory == category

                // Category header (tap to expand/collapse).
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            // Toggle the category between expanded and collapsed states.
                            expandedCategory = if (isExpanded) null else category
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpanded)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = category,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isExpanded)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )

                                // Show the number of prompts and the time periods represented.
                                Text(
                                    text = "${prompts.size} prompts  •  " +
                                            prompts.map { it.timeOfDay }
                                                .distinct()
                                                .joinToString(", "),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Display an arrow indicating whether the category is expanded.
                            Text(
                                text = if (isExpanded) "▲" else "▼",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Prompts under this category (shown only when expanded).
                if (isExpanded) {
                    items(prompts) { prompt ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp),
                            onClick = { onSelectPrompt(prompt) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    // Time of day badge.
                                    val timeEmoji = when (prompt.timeOfDay) {
                                        "Morning" -> "🌅"
                                        "Afternoon" -> "☀️"
                                        else -> "🌙"
                                    }

                                    Text(
                                        text = "$timeEmoji ${prompt.timeOfDay}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Mark prompts that are part of the required daily rotation.
                                    if (prompt.isMandatory) {
                                        Text(
                                            text = "★ Required",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Display the complete prompt text.
                                Text(
                                    text = prompt.text,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Indicates that tapping the card opens the writing screen.
                                Text(
                                    text = "Tap to respond →",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── DATE HELPER FUNCTIONS ──────────────────────────────────────────────────────

/**
 * Checks whether two timestamps fall on the same calendar day.
 *
 * @param timestamp1 First timestamp in milliseconds.
 * @param timestamp2 Second timestamp in milliseconds.
 * @return True if both timestamps belong to the same calendar day.
 */
fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
    // Create calendar objects from both timestamps.
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }

    // Compare both the year and day-of-year to determine whether they are the same day.
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

/**
 * Formats a timestamp to a short date label like "Oct 3, 2026".
 *
 * @param timestamp Milliseconds timestamp.
 * @return Formatted date string.
 */
fun formatDateLabel(timestamp: Long): String {
    // Use the device's default locale when formatting the date.
    val format = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    // Convert the timestamp into a Date and format it as text.
    return format.format(Date(timestamp))
}

/**
 * Returns today's date as a readable string like "Saturday, Oct 3".
 *
 * @return Formatted today date string.
 */
fun formatTodayFull(): String {
    // Use the device's default locale for the day and month names.
    val format = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

    // Format the current date as a readable string.
    return format.format(Date())
}
