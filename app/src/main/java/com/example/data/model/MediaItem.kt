package com.example.data.model

import com.example.R

data class MediaItem(
    val id: String,
    val title: String,
    val titleArabic: String? = null,
    val subtitleBadge: String = "Top 01 in TV Shows",
    val subtitleBadgeArabic: String? = null,
    val episodesFreeBadge: String? = "4 episodes Free for you",
    val year: String = "2026",
    val seasonsOrDuration: String = "2 Seasons",
    val seasonsOrDurationArabic: String? = null,
    val maturityRating: String = "16+",
    val qualityTag: String = "4K HDR",
    val genresText: String = "Social | Drama | Romance",
    val genresTextArabic: String? = null,
    val category: String = "TV Shows",
    val matchScore: Int = 98,
    val description: String = "",
    val descriptionArabic: String? = null,
    val cast: String = "",
    val posterRes: Int = R.drawable.img_hero_poster,
    val backdropRes: Int = R.drawable.img_hero_poster,
    val progress: Float = 0f, // For "Continue Watching"
    val isFeatured: Boolean = false,
    val isLive: Boolean = false,
    val uriString: String? = null,
    val initialSeekPositionMs: Long? = null,
    val episodes: List<Episode> = emptyList()
)

data class Episode(
    val episodeNumber: Int,
    val title: String,
    val duration: String,
    val description: String,
    val isFree: Boolean = false
)

data class LiveChannel(
    val id: String,
    val channelNumber: String,
    val channelName: String,
    val currentShow: String,
    val nextShow: String,
    val viewersCount: String,
    val category: String,
    val isLive: Boolean = true
)

data class ShortVideo(
    val id: String,
    val title: String,
    val creatorName: String,
    val handle: String,
    val soundTrack: String,
    val likesCount: String,
    val commentsCount: String,
    val sharesCount: String,
    val tag: String,
    val posterRes: Int
)

object MediaRepository {
    val heroItem = MediaItem(
        id = "hero_1",
        title = "El Sitt Oumour",
        titleArabic = "الست أمور",
        subtitleBadge = "Top 01 in TV Shows",
        subtitleBadgeArabic = "رقم 1 في المسلسلات",
        episodesFreeBadge = "4 episodes Free for you",
        year = "2026",
        seasonsOrDuration = "2 Seasons",
        seasonsOrDurationArabic = "موسمان",
        maturityRating = "16+",
        qualityTag = "4K HDR",
        genresText = "Social | Drama | Romance",
        genresTextArabic = "دراما • اجتماعي • رومانسي",
        category = "TV Shows",
        matchScore = 99,
        description = "An intricate web of family heritage, fierce social rivalries, and forbidden romances across Cairo and Alexandria. As old secrets unravel, the matriarch must choose between family honor and true devotion.",
        descriptionArabic = "شبكة معقدة من الإرث العائلي والمنافسات الاجتماعية الشرسة وقصص الحب المحرمة بين القاهرة والإسكندرية، حيث تنكشف أسرار الماضي.",
        cast = "Mona Zaki, Eyad Nassar, Amina Khalil, Ahmed Malek",
        posterRes = R.drawable.img_hero_poster,
        backdropRes = R.drawable.img_hero_poster,
        isFeatured = true,
        episodes = listOf(
            Episode(1, "S1:E1 The Arrival", "48m", "A high-society wedding in Alexandria is shattered when an estranged daughter returns with devastating proof.", true),
            Episode(2, "S1:E2 Whispers in Zamalek", "52m", "Tensions flare as rival factions compete for control of the family empire.", true),
            Episode(3, "S1:E3 Fragile Alliances", "46m", "A secret pact is forged under the cover of night, risking everyone involved.", true),
            Episode(4, "S1:E4 Point of No Return", "55m", "An explosive confrontation at the annual charity gala alters the dynasty's destiny.", true),
            Episode(5, "S1:E5 The Shadow Heir", "50m", "Secrets hidden for two decades are revealed.", false)
        )
    )

    val sportsFeatureItem = MediaItem(
        id = "sport_1",
        title = "Champions League: Semi-Final",
        titleArabic = "دوري أبطال أوروبا",
        subtitleBadge = "LIVE in 4K",
        subtitleBadgeArabic = "مباشر 4K",
        episodesFreeBadge = "Live Event",
        year = "2026",
        seasonsOrDuration = "LIVE NOW",
        seasonsOrDurationArabic = "مباشر الآن",
        maturityRating = "All",
        qualityTag = "UHD 60fps",
        genresText = "Sports | Football | Championship",
        genresTextArabic = "رياضة • كرة قدم • بث مباشر",
        category = "Sports",
        matchScore = 99,
        description = "Live high-octane football clash with multi-angle cameras, real-time statistics, and Dolby surround commentary.",
        descriptionArabic = "مواجهة أوروبية نارية بأعلى جودة بث مباشر 4K مع زوايا تصوير متعددة وتعليق صوتي محيطي.",
        cast = "Commentary: Issam Chaouali",
        posterRes = R.drawable.img_poster_sports,
        backdropRes = R.drawable.img_poster_sports,
        isFeatured = true,
        isLive = true
    )

    val nexusHorizonItem = MediaItem(
        id = "trend_3",
        title = "Nexus Horizon",
        titleArabic = "أفق نيكسوس",
        subtitleBadge = "Pulse Original",
        subtitleBadgeArabic = "إنتاج حصري",
        episodesFreeBadge = "All Episodes Available",
        year = "2026",
        seasonsOrDuration = "2 Seasons",
        seasonsOrDurationArabic = "موسمان",
        maturityRating = "13+",
        qualityTag = "HDR10+",
        genresText = "Sci-Fi | Adventure | Mystery",
        genresTextArabic = "خيال علمي • مغامرة • غموض",
        category = "TV Shows",
        matchScore = 98,
        description = "A generation starship encounters a sentient celestial gateway on the edge of the galaxy.",
        descriptionArabic = "بعثة فضائية تكتشف بوابة كونية غامضة على حافة المجرة تقلب موازين الزمن والوجود.",
        cast = "Salma Abu Deif, Nour",
        posterRes = R.drawable.img_poster_scifi,
        backdropRes = R.drawable.img_poster_scifi,
        isFeatured = true,
        episodes = listOf(
            Episode(1, "S1:E1 Beyond the Ring", "51m", "First contact telemetry triggers an emergency orbital shift.", true),
            Episode(2, "S1:E2 Echo Signal", "47m", "The crew decodes a rhythmic transmission from inside the portal.", true),
            Episode(3, "S1:E3 Event Horizon", "54m", "A daring EVA mission alters time aboard the station.", false)
        )
    )

    val shadowProtocolItem = MediaItem(
        id = "cw_3",
        title = "Shadow Protocol",
        titleArabic = "بروتوكول الظل",
        subtitleBadge = "Top Action Movie",
        subtitleBadgeArabic = "أفضل فيلم أكشن",
        episodesFreeBadge = "Full Movie Included",
        year = "2026",
        seasonsOrDuration = "2h 14m",
        seasonsOrDurationArabic = "2س 14د",
        maturityRating = "16+",
        qualityTag = "Dolby Vision",
        genresText = "Action | Espionage | Thriller",
        genresTextArabic = "أكشن • تجسس • إثارة",
        category = "Movies",
        matchScore = 95,
        description = "An undercover agent marked for termination goes off-grid across Istanbul and Vienna to protect a lethal decryption cipher.",
        descriptionArabic = "عميل سري يختفي عن الأنظار بين إسطنبول وفيينا لحماية شيفرة فك تشفير خطيرة.",
        cast = "Idris Elba, Rebecca Ferguson",
        posterRes = R.drawable.img_poster_action,
        backdropRes = R.drawable.img_poster_action,
        progress = 0.82f,
        isFeatured = true
    )

    val featuredCarousel = listOf(
        heroItem,
        sportsFeatureItem,
        nexusHorizonItem,
        shadowProtocolItem
    )

    val continueWatchingList = listOf(
        heroItem.copy(id = "cw_1", progress = 0.65f),
        MediaItem(
            id = "cw_2",
            title = "Cyber Pulse 2088",
            titleArabic = "نبض السايبر",
            subtitleBadge = "Trending Worldwide",
            subtitleBadgeArabic = "رائج عالمياً",
            episodesFreeBadge = "Episode 1 Free",
            year = "2026",
            seasonsOrDuration = "1 Season",
            seasonsOrDurationArabic = "موسم واحد",
            maturityRating = "18+",
            qualityTag = "IMAX Enhanced",
            genresText = "Sci-Fi | Thriller | Action",
            genresTextArabic = "خيال علمي • تشويق • أكشن",
            category = "TV Shows",
            matchScore = 96,
            description = "In a rain-slicked mega-city powered by neural interfaces, a rogue detective uncovers a syndicate controlling human memories.",
            descriptionArabic = "في مدينة مستقبلية تتحكم بها الواجهات العصبية، يكتشف محقق متمرد منظمة تسرق ذكريات البشر.",
            cast = "Alexander Skarsgård, Bae Doona",
            posterRes = R.drawable.img_poster_cyberpunk,
            backdropRes = R.drawable.img_poster_cyberpunk,
            progress = 0.38f
        ),
        shadowProtocolItem
    )

    val pulseOriginals = listOf(
        heroItem,
        MediaItem(
            id = "orig_2",
            title = "Neon Mirage",
            titleArabic = "سراب النيون",
            subtitleBadge = "Pulse Original",
            subtitleBadgeArabic = "إنتاج أصلي",
            episodesFreeBadge = "3 episodes Free",
            year = "2026",
            seasonsOrDuration = "Miniseries",
            seasonsOrDurationArabic = "مسلسل قصير",
            maturityRating = "16+",
            qualityTag = "4K HDR",
            genresText = "Mystery | Cyberpunk | Drama",
            genresTextArabic = "غموض • سايبر بانك • دراما",
            category = "TV Shows",
            matchScore = 97,
            description = "A clandestine investigation into vanishing virtual reality designers leads to a city beneath the dunes.",
            descriptionArabic = "تحقيق سري في اختفاء مصممي الواقع الافتراضي يقود إلى مدينة خفية تحت الرمال.",
            cast = "Asser Yassin, Tara Emad",
            posterRes = R.drawable.img_poster_cyberpunk,
            backdropRes = R.drawable.img_poster_cyberpunk
        ),
        MediaItem(
            id = "orig_3",
            title = "Redline Beirut",
            titleArabic = "بيروت الحمراء",
            subtitleBadge = "Top 05 in Movies",
            subtitleBadgeArabic = "ضمن أفضل 5 أفلام",
            episodesFreeBadge = "VIP Premier",
            year = "2026",
            seasonsOrDuration = "1h 58m",
            seasonsOrDurationArabic = "1س 58د",
            maturityRating = "18+",
            qualityTag = "Dolby Atmos",
            genresText = "Heist | Crime | High Octane",
            genresTextArabic = "جريمة • سرقة • إثارة",
            category = "Movies",
            matchScore = 95,
            description = "An elite crew attempts a high-stakes maritime vault heist along the Mediterranean coast.",
            descriptionArabic = "فريق محترف يخطط لعملية سطو بحرية عالية الخطورة على ساحل المتوسط.",
            cast = "Karim Abdel Aziz, Nelly Karim",
            posterRes = R.drawable.img_poster_action,
            backdropRes = R.drawable.img_poster_action
        ),
        nexusHorizonItem
    )

    val trendingNow = listOf(
        MediaItem(
            id = "trend_1",
            title = "Midnight Odyssey",
            titleArabic = "رحلة منتصف الليل",
            subtitleBadge = "#2 in Trending",
            subtitleBadgeArabic = "#2 في الرائج",
            episodesFreeBadge = "Watch for Free",
            year = "2026",
            seasonsOrDuration = "3 Seasons",
            seasonsOrDurationArabic = "3 مواسم",
            maturityRating = "16+",
            qualityTag = "4K Ultra HD",
            genresText = "Drama | Supernatural | Fantasy",
            genresTextArabic = "دراما • غموض • فانتازيا",
            category = "TV Shows",
            matchScore = 93,
            description = "An ancient artifact discovered in Upper Egypt unlocks nocturnal visions in seven strangers.",
            descriptionArabic = "قطعة أثرية غامضة في صعيد مصر توقظ رؤى ليلية مشتركة لدى سبعة غرباء.",
            cast = "Ahmed Dawood, Mayan El Sayed",
            posterRes = R.drawable.img_poster_drama,
            backdropRes = R.drawable.img_poster_drama
        ),
        MediaItem(
            id = "trend_2",
            title = "Echoes of Syndicate",
            titleArabic = "أصداء النقابة",
            subtitleBadge = "#3 in Movies",
            subtitleBadgeArabic = "#3 في الأفلام",
            episodesFreeBadge = "4K HDR Free",
            year = "2026",
            seasonsOrDuration = "2h 05m",
            seasonsOrDurationArabic = "2س 05د",
            maturityRating = "16+",
            qualityTag = "4K HDR",
            genresText = "Action | Thriller | Crime",
            genresTextArabic = "أكشن • إثارة • جريمة",
            category = "Movies",
            matchScore = 91,
            description = "A tactical operator defends a high-value whistleblower against ruthless global syndicates.",
            descriptionArabic = "عميل تكتيكي يحمي شاهداً رئيسياً ضد شبكات إجرامية عابرة للقارات.",
            cast = "Amr Youssef, Dhaffer L'Abidine",
            posterRes = R.drawable.img_poster_action,
            backdropRes = R.drawable.img_poster_action
        ),
        nexusHorizonItem,
        MediaItem(
            id = "trend_4",
            title = "Nile Crown",
            titleArabic = "تاج النيل",
            subtitleBadge = "#4 in Drama",
            subtitleBadgeArabic = "#4 في الدراما",
            episodesFreeBadge = "New Episode",
            year = "2026",
            seasonsOrDuration = "1 Season",
            seasonsOrDurationArabic = "موسم واحد",
            maturityRating = "16+",
            qualityTag = "4K HDR",
            genresText = "History | Drama | Luxury",
            genresTextArabic = "تاريخي • دراما • ملحمي",
            category = "TV Shows",
            matchScore = 96,
            description = "Power, betrayal, and legacy collide inside Cairo's grandest historic estate.",
            descriptionArabic = "صراع النفوذ والإرث والولاء داخل أعرق القصور التاريخية على ضفاف النيل.",
            cast = "Yousra, Khaled El Nabawy",
            posterRes = R.drawable.img_poster_drama,
            backdropRes = R.drawable.img_poster_drama
        )
    )

    val sportsAndLiveList = listOf(
        sportsFeatureItem,
        MediaItem(
            id = "sport_2",
            title = "Pulse Sound Sessions",
            titleArabic = "جلسات نبض الموسيقى",
            subtitleBadge = "Hi-Res Audio",
            subtitleBadgeArabic = "صوت فائق النقاء",
            episodesFreeBadge = "Live Stage",
            year = "2026",
            seasonsOrDuration = "LIVE",
            seasonsOrDurationArabic = "مباشر",
            maturityRating = "All",
            qualityTag = "Spatial Audio",
            genresText = "Listen | Acoustic | Live Concert",
            genresTextArabic = "حفل مباشر • موسيقى حية",
            category = "Sports",
            matchScore = 97,
            description = "Intimate studio acoustic sessions featuring legendary artists performing reimagined orchestral arrangements.",
            descriptionArabic = "حفلات صوتية حية بتوزيع أوركسترالي فاخر وجودة صوت محيطية.",
            cast = "Orchestra of Cairo & Special Guests",
            posterRes = R.drawable.img_poster_drama,
            backdropRes = R.drawable.img_poster_drama,
            isLive = true
        ),
        MediaItem(
            id = "sport_3",
            title = "Premier Matchday: El Clasico",
            titleArabic = "الكلاسيكو • البث المباشر",
            subtitleBadge = "UHD Broadcast",
            subtitleBadgeArabic = "بث فائق الدقة",
            episodesFreeBadge = "Pre-Match Studio",
            year = "2026",
            seasonsOrDuration = "21:00",
            seasonsOrDurationArabic = "21:00 مباشر",
            maturityRating = "All",
            qualityTag = "4K 60fps",
            genresText = "Football | Live Studio | Analysis",
            genresTextArabic = "كرة قدم • استوديو تحليلي",
            category = "Sports",
            matchScore = 99,
            description = "Full matchday coverage with tactical pitch cameras and instant replay telemetry.",
            descriptionArabic = "تغطية شاملة لمباراة القمة مع استوديو تحليلي وتقنيات إعادة فورية.",
            cast = "Live Broadcast Studio",
            posterRes = R.drawable.img_poster_sports,
            backdropRes = R.drawable.img_poster_sports,
            isLive = true
        )
    )

    val topRankedList = listOf(
        heroItem,
        sportsFeatureItem,
        nexusHorizonItem,
        trendingNow[0],
        shadowProtocolItem,
        pulseOriginals[1],
        trendingNow[1],
        pulseOriginals[2]
    )

    val allCatalogItems: List<MediaItem> by lazy {
        (featuredCarousel + continueWatchingList + pulseOriginals + trendingNow + sportsAndLiveList)
            .distinctBy { it.id }
    }

    val liveChannels = listOf(
        LiveChannel("ch_1", "01", "Pulse Cinema HD", "El Sitt Oumour (S2 E04)", "Midnight Odyssey", "342K Watching", "Cinema"),
        LiveChannel("ch_2", "02", "Pulse Sports 1 UHD", "Live: UEFA Champions League", "Sports Pulse Post-Match", "1.2M Watching", "Sports"),
        LiveChannel("ch_3", "03", "Pulse Documentaries", "Wild Nile: Secret Predators", "Cosmic Horizons 4K", "185K Watching", "Documentary"),
        LiveChannel("ch_4", "04", "Pulse Music Hits", "Top 20 Arabian Pop Countdown", "Late Night Vinyl Sessions", "220K Watching", "Music"),
        LiveChannel("ch_5", "05", "Pulse Action One", "Shadow Protocol: Extended Cut", "Cyber Matrix Reloaded", "510K Watching", "Action")
    )

    val shortsFeed = listOf(
        ShortVideo(
            id = "sh_1",
            title = "Behind the Scenes: That jaw-dropping palace climax in El Sitt Oumour! 🎬✨",
            creatorName = "Video Pulse Studios",
            handle = "@videopulse_official",
            soundTrack = "Original Score • El Sitt Oumour Theme",
            likesCount = "142.8K",
            commentsCount = "3,210",
            sharesCount = "18.4K",
            tag = "BehindTheScenes",
            posterRes = R.drawable.img_hero_poster
        ),
        ShortVideo(
            id = "sh_2",
            title = "Unreal stunt choreography with zero CGI in Shadow Protocol 🔥⚡",
            creatorName = "Pulse Action Hub",
            handle = "@pulse_action",
            soundTrack = "High Adrenaline Synth • Pulse Records",
            likesCount = "289.4K",
            commentsCount = "5,420",
            sharesCount = "41.2K",
            tag = "ActionStunts",
            posterRes = R.drawable.img_poster_action
        ),
        ShortVideo(
            id = "sh_3",
            title = "Cyber Pulse 2088: Building holographic Cairo 🌆 Future vision!",
            creatorName = "VFX Masterclass",
            handle = "@vfxpulse",
            soundTrack = "Synthwave Neon Dream • Cairo 2088",
            likesCount = "95.1K",
            commentsCount = "1,880",
            sharesCount = "12.3K",
            tag = "SciFiArt",
            posterRes = R.drawable.img_poster_cyberpunk
        )
    )

    val categories = listOf("Videos", "Movies", "Listen", "Sports", "Watch for Free")

    val defaultLocalVideos: List<LocalDeviceVideo> = emptyList()
}

data class LocalDeviceVideo(
    val id: String,
    val title: String,
    val durationText: String,
    val sizeText: String,
    val resolutionText: String = "1080p FHD",
    val dateAddedText: String = "Today",
    val dateAddedTimestampMs: Long = 0L,
    val uriString: String? = null,
    val thumbnailRes: Int = 0,
    val bitmap: android.graphics.Bitmap? = null,
    val isFromGallery: Boolean = false,
    val folderName: String = "Device Storage",
    val relativePath: String? = null
)

fun LocalDeviceVideo.isNewWithinDays(
    days: Int,
    nowMs: Long = System.currentTimeMillis()
): Boolean {
    if (days <= 0 || dateAddedTimestampMs <= 0L) return false
    val windowMs = days.toLong() * 86_400_000L
    val ageMs = nowMs - dateAddedTimestampMs
    return ageMs in 0L..windowMs
}

data class LocalAudioTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val albumCoverRes: Int,
    val lyricsLines: List<String> = emptyList(),
    val audioUri: String? = null,
    val albumArtUri: String? = null,
    val isFromDevice: Boolean = false,
    val folderName: String = "Device Music",
    val dateAddedMs: Long = 0L
)
