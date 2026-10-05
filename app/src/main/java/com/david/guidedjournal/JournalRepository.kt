package com.david.guidedjournal

/**
 * JournalRepository - Manages all data operations between the UI and database.
 *
 * @param dao The JournalDao used to perform database operations
 */
class JournalRepository(private val dao: JournalDao) {

    /**
     * Daily rotating quotes shown on the home screen.
     * Cycles through based on the day of the year.
     */
    val dailyQuotes = listOf(

        // Original quotes
        "The unexamined life is not worth living. — Socrates",
        "Write hard and clear about what hurts. — Ernest Hemingway",
        "Journal writing is a voyage to the interior. — Christina Baldwin",
        "One day or day one. You decide. — Unknown",
        "Your life is your story. Write well. Edit often. — Unknown",
        "Journaling is like whispering to one's self and listening to oneself at the same time. — Mina Murray",
        "Fill your paper with the breathings of your heart. — William Wordsworth",
        "Start where you are. Use what you have. Do what you can. — Arthur Ashe",
        "Every day is a new beginning. Take a deep breath and start again. — Unknown",
        "In the journal I do not just express myself more openly than I could to any person; I create myself. — Susan Sontag",
        "Be yourself; everyone else is already taken. — Oscar Wilde",
        "It does not matter how slowly you go as long as you do not stop. — Confucius",
        "Gratitude turns what we have into enough. — Unknown",
        "The secret of getting ahead is getting started. — Mark Twain",
        "Believe you can and you're halfway there. — Theodore Roosevelt",
        "In the middle of every difficulty lies opportunity. — Albert Einstein",
        "You are never too old to set another goal or to dream a new dream. — C.S. Lewis",
        "The only way to do great work is to love what you do. — Steve Jobs",
        "Act as if what you do makes a difference. It does. — William James",
        "Well done is better than well said. — Benjamin Franklin",
        "Nothing is impossible. The word itself says I'm possible. — Audrey Hepburn",
        "Keep your face always toward the sunshine and shadows will fall behind you. — Walt Whitman",
        "You have been assigned this mountain to show others it can be moved. — Unknown",
        "With God all things are possible. — Matthew 19:26",
        "I can do all things through Christ who strengthens me. — Philippians 4:13",
        "Trust in the Lord with all your heart. — Proverbs 3:5",
        "The Lord is my shepherd; I shall not want. — Psalm 23:1",
        "Be still and know that I am God. — Psalm 46:10",
        "This is the day the Lord has made; let us rejoice and be glad in it. — Psalm 118:24",
        "For I know the plans I have for you, declares the Lord. — Jeremiah 29:11",

        // Additional quotes
        "The journey of a thousand miles begins with one step. — Lao Tzu",
        "What we think, we become. — Buddha",
        "Knowing yourself is the beginning of all wisdom. — Aristotle",
        "The present moment is filled with joy and happiness. If you are attentive, you will see it. — Thich Nhat Hanh",
        "Happiness depends upon ourselves. — Aristotle",
        "The future depends on what you do today. — Mahatma Gandhi",
        "Do what you can, with what you have, where you are. — Theodore Roosevelt",
        "It always seems impossible until it's done. — Nelson Mandela",
        "The greatest glory in living lies not in never falling, but in rising every time we fall. — Nelson Mandela",
        "Turn your wounds into wisdom. — Oprah Winfrey",
        "Nothing can dim the light that shines from within. — Maya Angelou",
        "You yourself, as much as anybody in the entire universe, deserve your love and affection. — Buddha",
        "Almost everything will work again if you unplug it for a few minutes, including you. — Anne Lamott",
        "Sometimes the most productive thing you can do is rest. — Mark Black",
        "Small steps every day add up to big results. — Unknown",
        "Progress, not perfection. — Unknown",
        "Give yourself permission to begin again. — Unknown",
        "Your pace does not matter as long as you keep moving forward. — Unknown",
        "Be gentle with yourself. You are doing the best you can. — Unknown",
        "Every morning is a chance to begin again. — Unknown",
        "Every sunset is an opportunity to reset. — Unknown",
        "You cannot pour from an empty cup. Take care of yourself first. — Unknown",
        "The quieter you become, the more you can hear. — Ram Dass",
        "Faith is taking the first step even when you don't see the whole staircase. — Martin Luther King Jr.",
        "Let your faith be bigger than your fear. — Unknown",
        "Commit your work to the Lord, and your plans will be established. — Proverbs 16:3",
        "Cast all your anxiety on Him because He cares for you. — 1 Peter 5:7",
        "The Lord is near to the brokenhearted. — Psalm 34:18",
        "I will give you rest. — Matthew 11:28",
        "Do not be anxious about tomorrow. — Matthew 6:34"
    )

    /**
     * Returns today's quote based on the day of the year.
     */
    fun getTodayQuote(): String {
        val dayOfYear = java.util.Calendar.getInstance()
            .get(java.util.Calendar.DAY_OF_YEAR)

        return dailyQuotes[dayOfYear % dailyQuotes.size]
    }

    /**
     * Determines the current time of day period.
     *
     * Morning: 6am-11:59am
     * Afternoon: 12pm-5:59pm
     * Night: 6pm-5:59am
     */
    fun getCurrentTimeOfDay(): String {
        val hour = java.util.Calendar.getInstance()
            .get(java.util.Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 6..11 -> "Morning"
            in 12..17 -> "Afternoon"
            else -> "Night"
        }
    }

    /**
     * Saves a new journal entry to the database.
     */
    suspend fun addEntry(entry: Entry) {
        dao.insertEntry(entry)
    }

    /**
     * Retrieves all journal entries from the database.
     */
    suspend fun getEntries(): List<Entry> {
        return dao.getAllEntries()
    }

    /**
     * Updates an existing journal entry.
     */
    suspend fun updateEntry(entry: Entry) {
        dao.updateEntry(entry)
    }

    /**
     * Deletes an existing journal entry.
     */
    suspend fun deleteEntry(entry: Entry) {
        dao.deleteEntry(entry)
    }

    /**
     * Gets a random prompt for the current time of day.
     */
    suspend fun getCurrentPrompt(): Prompt? {
        val timeOfDay = getCurrentTimeOfDay()
        return dao.getPromptByTimeOfDay(timeOfDay)
    }

    /**
     * Gets multiple prompts for the current time of day.
     *
     * @param count How many prompts to load
     */
    suspend fun getMorePrompts(count: Int = 3): List<Prompt> {
        val timeOfDay = getCurrentTimeOfDay()
        return dao.getPromptsByTimeOfDay(timeOfDay, count)
    }

    /**
     * Gets the next mandatory prompt the user should answer.
     *
     * Rotates through all mandatory prompts using the user's
     * current daily count.
     *
     * @param todayCount How many mandatory prompts the user has answered today
     * @return The next mandatory Prompt to show
     */
    suspend fun getNextMandatoryPrompt(todayCount: Int): Prompt? {
        val allMandatory = dao.getAllMandatoryPrompts()

        if (allMandatory.isEmpty()) {
            return null
        }

        val index = todayCount % allMandatory.size

        return allMandatory[index]
    }

    /**
     * Retrieves all prompts grouped by category.
     *
     * @return Map of category name to list of prompts
     */
    suspend fun getAllPromptsByCategory(): Map<String, List<Prompt>> {
        return dao.getAllPrompts()
            .groupBy { it.category }
    }

    /**
     * Checks whether an entry is still within its 24-hour edit window.
     */
    fun isEditable(entry: Entry): Boolean {
        val twentyFourHours = 24 * 60 * 60 * 1000L

        return System.currentTimeMillis() - entry.date < twentyFourHours
    }

    /**
     * Seeds the database with categorized prompts on first launch.
     * Skips initialization if prompts already exist.
     */
    suspend fun initializePrompts() {

        val count = dao.getPromptCount()

        if (count > 0) {
            return
        }

        val prompts = listOf(

            // =========================================================
            // MORNING PROMPTS
            // =========================================================

            Prompt(
                text = "Good morning! What are three things you are grateful for today?",
                category = "Gratitude",
                timeOfDay = "Morning",
                isMandatory = true
            ),

            Prompt(
                text = "What is your main intention or goal for today?",
                category = "Goals",
                timeOfDay = "Morning",
                isMandatory = true
            ),

            Prompt(
                text = "How are you feeling as you start this day? Describe your mood.",
                category = "Mood",
                timeOfDay = "Morning",
                isMandatory = true
            ),

            Prompt(
                text = "What is one thing you are looking forward to today?",
                category = "Anticipation",
                timeOfDay = "Morning",
                isMandatory = true
            ),

            Prompt(
                text = "Write a short morning prayer or affirmation to start your day.",
                category = "Spiritual",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "What did you dream about last night?",
                category = "Reflection",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "If today were perfect, what would it look like?",
                category = "Goals",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "Who is someone you want to show kindness to today?",
                category = "Relationships",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "What is one habit you want to practice today?",
                category = "Growth",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "What scripture or quote is inspiring you this morning?",
                category = "Spiritual",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "How did you sleep and how does your body feel this morning?",
                category = "Wellness",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "What is one fear you want to overcome today?",
                category = "Growth",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "What is one small thing you can do today that your future self will appreciate?",
                category = "Growth",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "What is something you want to learn or understand better today?",
                category = "Learning",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "What would make you feel proud of yourself by the end of today?",
                category = "Goals",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "Who are you praying for today?",
                category = "Spiritual",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "What is one thing you need to let go of before starting your day?",
                category = "Reflection",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "What positive quality do you want to bring into your interactions today?",
                category = "Relationships",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "What is one thing about your life that you sometimes take for granted?",
                category = "Gratitude",
                timeOfDay = "Morning"
            ),

            Prompt(
                text = "Complete this sentence: Today I choose to...",
                category = "Intention",
                timeOfDay = "Morning"
            ),

            // =========================================================
            // AFTERNOON PROMPTS
            // =========================================================

            Prompt(
                text = "How has your day been going so far? What is standing out?",
                category = "Reflection",
                timeOfDay = "Afternoon",
                isMandatory = true
            ),

            Prompt(
                text = "What challenge have you faced today and how did you handle it?",
                category = "Growth",
                timeOfDay = "Afternoon",
                isMandatory = true
            ),

            Prompt(
                text = "What is the highlight of your day so far?",
                category = "Highlights",
                timeOfDay = "Afternoon",
                isMandatory = true
            ),

            Prompt(
                text = "Have you been kind to yourself today? In what way?",
                category = "Wellness",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "What conversation stood out to you today and why?",
                category = "Relationships",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "Are you on track with your morning intention? If not, why?",
                category = "Goals",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "What made you smile or laugh today?",
                category = "Highlights",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "What is draining your energy today and how can you address it?",
                category = "Wellness",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "Describe one moment of beauty or peace you noticed today.",
                category = "Gratitude",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "What is God teaching you through today's experiences?",
                category = "Spiritual",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "What have you accomplished today that deserves recognition?",
                category = "Highlights",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "What is one thing that did not go according to plan today?",
                category = "Reflection",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "What have you learned about yourself today?",
                category = "Growth",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "Is there someone you should check in on today?",
                category = "Relationships",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "What is something you can stop worrying about for the rest of today?",
                category = "Wellness",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "What is one thing you can simplify right now?",
                category = "Wellness",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "What are you grateful for at this exact moment?",
                category = "Gratitude",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "What has given you energy today?",
                category = "Wellness",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "What would you tell a friend who was having the same kind of day?",
                category = "Reflection",
                timeOfDay = "Afternoon"
            ),

            Prompt(
                text = "What is one thing you want to accomplish before the day ends?",
                category = "Goals",
                timeOfDay = "Afternoon"
            ),

            // =========================================================
            // NIGHT PROMPTS
            // =========================================================

            Prompt(
                text = "What was the best moment of your entire day?",
                category = "Reflection",
                timeOfDay = "Night",
                isMandatory = true
            ),

            Prompt(
                text = "What is one lesson you learned or something that surprised you today?",
                category = "Growth",
                timeOfDay = "Night",
                isMandatory = true
            ),

            Prompt(
                text = "What are you looking forward to tomorrow? Say a short prayer or affirmation.",
                category = "Intentions",
                timeOfDay = "Night",
                isMandatory = true
            ),

            Prompt(
                text = "How did you show love or kindness to someone today?",
                category = "Relationships",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What would you do differently if you could repeat today?",
                category = "Growth",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What are you proud of yourself for today, big or small?",
                category = "Highlights",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "Write a short thank-you prayer for today.",
                category = "Spiritual",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "How did you take care of your body and mind today?",
                category = "Wellness",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "Who impacted your life today and how?",
                category = "Relationships",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What worries are you carrying into the night? Give them to God.",
                category = "Spiritual",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "Rate your day from 1 to 10 and explain why.",
                category = "Reflection",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What are three words that describe today?",
                category = "Reflection",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What is one thing you forgive yourself for today?",
                category = "Self-Compassion",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What did today teach you about patience?",
                category = "Growth",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What moment from today would you like to remember?",
                category = "Memories",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What are three things that went well today?",
                category = "Gratitude",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What is something you accomplished today that you almost overlooked?",
                category = "Highlights",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "Is there anything you need to apologize for or make right tomorrow?",
                category = "Relationships",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What are you grateful for that you did not expect today?",
                category = "Gratitude",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What can you release tonight so that you can rest peacefully?",
                category = "Wellness",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What prayer would you like to carry into tomorrow?",
                category = "Spiritual",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What is one thing you want to remember when you wake up tomorrow?",
                category = "Intentions",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "Where did you see God's goodness today?",
                category = "Spiritual",
                timeOfDay = "Night"
            ),

            Prompt(
                text = "What was difficult today, and what helped you get through it?",
                category = "Growth",
                timeOfDay = "Night"
            )
        )

        prompts.forEach { prompt ->
            dao.insertPrompt(prompt)
        }
    }
}
