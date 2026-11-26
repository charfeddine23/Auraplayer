package com.example.data.model

enum class ForgeKeyTier(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val rarityAr: String,
    val rarityEn: String,
    val requiredShields: Int,
    val requiredKeys: Int,
    val durationHours: Int
) {
    BRONZE(
        id = "bronze",
        nameAr = "المفتاح البرونزي",
        nameEn = "Bronze Key",
        rarityAr = "برونزي • شائع",
        rarityEn = "BRONZE • COMMON",
        requiredShields = 10,
        requiredKeys = 0,
        durationHours = 3
    ),
    SILVER(
        id = "silver",
        nameAr = "المفتاح الفضي",
        nameEn = "Silver Key",
        rarityAr = "فضي • نادر",
        rarityEn = "SILVER • RARE",
        requiredShields = 20,
        requiredKeys = 0,
        durationHours = 6
    ),
    GOLD(
        id = "gold",
        nameAr = "المفتاح الذهبي",
        nameEn = "Golden Key",
        rarityAr = "ذهبي • ملحمي",
        rarityEn = "GOLD • EPIC",
        requiredShields = 0,
        requiredKeys = 30,
        durationHours = 9
    ),
    DIAMOND(
        id = "diamond",
        nameAr = "المفتاح الماسي",
        nameEn = "Diamond Key",
        rarityAr = "ماسي • أسطوري",
        rarityEn = "DIAMOND • MYTHIC",
        requiredShields = 0,
        requiredKeys = 100,
        durationHours = 100
    );

    val durationSeconds: Int
        get() = durationHours * 3600

    val usesShields: Boolean
        get() = requiredShields > 0
}

data class ForgeSlotState(
    val tier: ForgeKeyTier,
    val isActive: Boolean = false,
    val elapsedSeconds: Int = 0,
    val refundShields: Int = 0,
    val refundBronzeKeys: Int = 0,
    val refundSilverKeys: Int = 0,
    val refundGoldKeys: Int = 0
) {
    val totalDurationSeconds: Int
        get() = tier.durationSeconds

    val clampedElapsedSeconds: Int
        get() = elapsedSeconds.coerceIn(0, totalDurationSeconds)

    val remainingSeconds: Int
        get() = (totalDurationSeconds - clampedElapsedSeconds).coerceAtLeast(0)

    val progress: Float
        get() = if (totalDurationSeconds > 0) {
            (clampedElapsedSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val isReadyToClaim: Boolean
        get() = isActive && clampedElapsedSeconds >= totalDurationSeconds
}

enum class FlameShieldCraftOption(
    val id: String,
    val labelAr: String,
    val labelEn: String,
    val shieldsCost: Int = 0,
    val bronzeKeysCost: Int = 0,
    val silverKeysCost: Int = 0,
    val diamondKeysCost: Int = 0
) {
    SHIELDS_100(
        id = "shields_100",
        labelAr = "100 درع",
        labelEn = "100 Shields",
        shieldsCost = 100
    ),
    BRONZE_3(
        id = "bronze_3",
        labelAr = "3 مفاتيح برونزية",
        labelEn = "3 Bronze Keys",
        bronzeKeysCost = 3
    ),
    SILVER_2(
        id = "silver_2",
        labelAr = "مفتاحان فضيان",
        labelEn = "2 Silver Keys",
        silverKeysCost = 2
    ),
    DIAMOND_1(
        id = "diamond_1",
        labelAr = "مفتاح واحد ماسي",
        labelEn = "1 Diamond Key",
        diamondKeysCost = 1
    );

    fun canAfford(shieldsCount: Int, forgeState: ForgeUiState): Boolean = when (this) {
        SHIELDS_100 -> shieldsCount >= shieldsCost
        BRONZE_3 -> forgeState.bronzeKeys >= bronzeKeysCost
        SILVER_2 -> forgeState.silverKeys >= silverKeysCost
        DIAMOND_1 -> forgeState.diamondKeys >= diamondKeysCost
    }
}

const val FLAME_SHIELD_CRAFT_DURATION_MS: Long = 24L * 60L * 60L * 1000L
const val FLAME_SHIELD_LABS_COUNT: Int = 2

data class FlameShieldForgeSlotState(
    val slotIndex: Int = 0,
    val isActive: Boolean = false,
    val selectedOption: FlameShieldCraftOption? = null,
    val startedAtEpochMs: Long = 0L,
    val durationMs: Long = FLAME_SHIELD_CRAFT_DURATION_MS,
    val refundShields: Int = 0,
    val refundBronzeKeys: Int = 0,
    val refundSilverKeys: Int = 0,
    val refundDiamondKeys: Int = 0
) {
    fun elapsedMs(nowEpochMs: Long = System.currentTimeMillis()): Long {
        if (!isActive || startedAtEpochMs <= 0L) return 0L
        return (nowEpochMs - startedAtEpochMs).coerceIn(0L, durationMs)
    }

    fun remainingMs(nowEpochMs: Long = System.currentTimeMillis()): Long {
        if (!isActive) return durationMs
        return (durationMs - elapsedMs(nowEpochMs)).coerceAtLeast(0L)
    }

    fun progress(nowEpochMs: Long = System.currentTimeMillis()): Float {
        if (!isActive || durationMs <= 0L) return 0f
        return (elapsedMs(nowEpochMs).toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }

    fun isReadyToClaim(nowEpochMs: Long = System.currentTimeMillis()): Boolean {
        return isActive && startedAtEpochMs > 0L && (nowEpochMs - startedAtEpochMs) >= durationMs
    }
}

data class ForgeUiState(
    val bronzeKeys: Int = 0,
    val silverKeys: Int = 0,
    val goldenKeys: Int = 0,
    val diamondKeys: Int = 0,
    val flameShieldsCount: Int = 0,
    val flameShieldSlot: FlameShieldForgeSlotState = FlameShieldForgeSlotState(slotIndex = 0),
    val flameShieldSlots: List<FlameShieldForgeSlotState> = listOf(
        flameShieldSlot.copy(slotIndex = 0),
        FlameShieldForgeSlotState(slotIndex = 1)
    ),
    val slots: Map<ForgeKeyTier, ForgeSlotState> = ForgeKeyTier.entries.associateWith { ForgeSlotState(it) }
) {
    val totalKeys: Int
        get() = bronzeKeys + silverKeys + goldenKeys + diamondKeys

    fun getSlot(tier: ForgeKeyTier): ForgeSlotState = slots[tier] ?: ForgeSlotState(tier)

    fun getFlameShieldSlot(slotIndex: Int): FlameShieldForgeSlotState =
        flameShieldSlots.getOrNull(slotIndex) ?: FlameShieldForgeSlotState(slotIndex = slotIndex)

    fun getKeyCount(tier: ForgeKeyTier): Int = when (tier) {
        ForgeKeyTier.BRONZE -> bronzeKeys
        ForgeKeyTier.SILVER -> silverKeys
        ForgeKeyTier.GOLD -> goldenKeys
        ForgeKeyTier.DIAMOND -> diamondKeys
    }

    fun availableKeysFor(tier: ForgeKeyTier): Int = when (tier) {
        ForgeKeyTier.BRONZE, ForgeKeyTier.SILVER -> 0
        ForgeKeyTier.GOLD -> bronzeKeys + silverKeys
        ForgeKeyTier.DIAMOND -> bronzeKeys + silverKeys + goldenKeys
    }

    fun canStartForging(tier: ForgeKeyTier, currentShields: Int): Boolean {
        if (getSlot(tier).isActive) return false
        return if (tier.usesShields) {
            currentShields >= tier.requiredShields
        } else {
            availableKeysFor(tier) >= tier.requiredKeys
        }
    }

    val activeFlameLabsCount: Int
        get() = flameShieldSlots.count { it.isActive && !it.isReadyToClaim() }

    val readyFlameLabsCount: Int
        get() = flameShieldSlots.count { it.isReadyToClaim() }

    val activeSlotsCount: Int
        get() = slots.values.count { it.isActive && !it.isReadyToClaim } + activeFlameLabsCount

    val readySlotsCount: Int
        get() = slots.values.count { it.isReadyToClaim } + readyFlameLabsCount
}

enum class StoreItemRarity(
    val labelAr: String,
    val labelEn: String
) {
    FREE("أساسي • مجاني", "CORE • FREE"),
    COMMON("شائع", "COMMON"),
    RARE("نادر", "RARE"),
    EPIC("ملحمي", "EPIC"),
    MYTHIC("أسطوري", "MYTHIC")
}

enum class PlayerSeekbarStyle(
    val id: String,
    val displayName: String,
    val rarity: StoreItemRarity,
    val shieldsCost: Int,
    val costTier: ForgeKeyTier?,
    val costAmount: Int,
    val displayNameAr: String = displayName,
    val subtitleEn: String = "",
    val subtitleAr: String = "",
    val primaryAccentHex: Long = 0xFF00F2FE,
    val secondaryAccentHex: Long = 0xFF3B82F6,
    val isNextGenLiquidGlass: Boolean = false
) {
    CLASSIC(
        id = "classic",
        displayName = "Classic Seekbar",
        rarity = StoreItemRarity.FREE,
        shieldsCost = 0,
        costTier = null,
        costAmount = 0,
        displayNameAr = "الشريط الكلاسيكي",
        subtitleEn = "Minimalist precision scrubber bar",
        subtitleAr = "شريط تمرير كلاسيكي دقيق وبسيط",
        primaryAccentHex = 0xFF00F2FE,
        secondaryAccentHex = 0xFF3B82F6,
        isNextGenLiquidGlass = false
    ),
    WAVEFORM(
        id = "waveform",
        displayName = "Waveform Audio Seekbar",
        rarity = StoreItemRarity.COMMON,
        shieldsCost = 15,
        costTier = ForgeKeyTier.BRONZE,
        costAmount = 2,
        displayNameAr = "شريط الموجة الصوتية",
        subtitleEn = "60-bar animated sine-wave equalizer timeline",
        subtitleAr = "شريط تمرير موجي متحرك (60 عموداً) بسلاسة فائقة",
        primaryAccentHex = 0xFF00F2FE,
        secondaryAccentHex = 0xFF3B82F6,
        isNextGenLiquidGlass = false
    ),
    SHOOTING_STAR(
        id = "shooting_star",
        displayName = "Shooting Star Seekbar",
        rarity = StoreItemRarity.RARE,
        shieldsCost = 30,
        costTier = ForgeKeyTier.SILVER,
        costAmount = 3,
        displayNameAr = "شريط الشهاب الليزري",
        subtitleEn = "Blazing comet tip with glowing neon laser trail",
        subtitleAr = "رأس شهاب متوهج مع ذيل ليزري ساطع",
        primaryAccentHex = 0xFF00F0FF,
        secondaryAccentHex = 0xFFFF4FD8,
        isNextGenLiquidGlass = false
    ),
    ANGRY_SNAKE(
        id = "angry_snake",
        displayName = "Angry Snake Seekbar",
        rarity = StoreItemRarity.MYTHIC,
        shieldsCost = 55,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 3,
        displayNameAr = "شريط الأفعى الغاضبة",
        subtitleEn = "26-segment cyber viper slithering across the timeline",
        subtitleAr = "أفعى سيبرانية من 26 مفصلاً تتحرك وتغضب أثناء التقديم",
        primaryAccentHex = 0xFF10B981,
        secondaryAccentHex = 0xFFFF1744,
        isNextGenLiquidGlass = false
    ),
    PLASMA_KATANA(
        id = "plasma_katana",
        displayName = "Plasma Katana Seekbar",
        rarity = StoreItemRarity.RARE,
        shieldsCost = 20,
        costTier = ForgeKeyTier.BRONZE,
        costAmount = 1,
        displayNameAr = "شريط سيف البلازما (Katana)",
        subtitleEn = "Glowing liquid glass neon blade with spark bursts & scrub light trail",
        subtitleAr = "نصل نيون من الزجاج السائل يمتد عبر الشريط مع شرارات ضوئية ومسار متوهج",
        primaryAccentHex = 0xFF00F2FE,
        secondaryAccentHex = 0xFF3B82F6,
        isNextGenLiquidGlass = true
    ),
    QUANTUM_COLLIDER(
        id = "quantum_collider",
        displayName = "Quantum Collider Seekbar",
        rarity = StoreItemRarity.EPIC,
        shieldsCost = 35,
        costTier = ForgeKeyTier.SILVER,
        costAmount = 2,
        displayNameAr = "شريط المصادم الكمي",
        subtitleEn = "Transparent glass accelerator tube with colliding particles & photon flashes",
        subtitleAr = "أنبوب زجاجي شفاف تتصادم داخله جسيمات الطاقة مع ومضات فوتون عند المؤشر",
        primaryAccentHex = 0xFF00F2FE,
        secondaryAccentHex = 0xFFA855F7,
        isNextGenLiquidGlass = true
    ),
    DOUBLE_HELIX_GENE(
        id = "double_helix_gene",
        displayName = "Double Helix Gene Seekbar",
        rarity = StoreItemRarity.EPIC,
        shieldsCost = 40,
        costTier = ForgeKeyTier.SILVER,
        costAmount = 2,
        displayNameAr = "شريط الحلزون الجيني (DNA)",
        subtitleEn = "Twin intertwined luminous DNA strands expanding & spinning faster on scrub",
        subtitleAr = "خيطا DNA مضيئان متشابكان يتسعان وتتسارع حركتهما ديناميكياً عند التمرير",
        primaryAccentHex = 0xFF00F2FE,
        secondaryAccentHex = 0xFFFF2E93,
        isNextGenLiquidGlass = true
    ),
    EVENT_HORIZON(
        id = "event_horizon",
        displayName = "Event Horizon Seekbar",
        rarity = StoreItemRarity.MYTHIC,
        shieldsCost = 50,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 2,
        displayNameAr = "شريط أفق الحدث الجاذبي",
        subtitleEn = "Gravitational lensing ring pulling star particles toward your touch point",
        subtitleAr = "عدسة جاذبية سوداء تشوّه المحيط وتسحب جسيمات النجوم نحو نقطة اللمس",
        primaryAccentHex = 0xFFA855F7,
        secondaryAccentHex = 0xFF00F2FE,
        isNextGenLiquidGlass = true
    ),
    CYBER_MATRIX_CODE(
        id = "cyber_matrix_code",
        displayName = "Cyber Matrix Code Seekbar",
        rarity = StoreItemRarity.MYTHIC,
        shieldsCost = 60,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 3,
        displayNameAr = "شريط شيفرة الماتريكس الرقمية",
        subtitleEn = "Cascading green & cyan binary code stream accelerating along the timeline",
        subtitleAr = "سيل متساقط من الشيفرات الثنائية الخضراء والسماوية يتسارع مع التمرير",
        primaryAccentHex = 0xFF10B981,
        secondaryAccentHex = 0xFF00F2FE,
        isNextGenLiquidGlass = true
    );

    companion object {
        fun fromId(id: String?): PlayerSeekbarStyle =
            entries.firstOrNull { it.id == id } ?: CLASSIC

        val nextGenLiquidGlassStyles: List<PlayerSeekbarStyle>
            get() = entries.filter { it.isNextGenLiquidGlass }
    }
}

enum class HomeMimicItem(
    val id: String,
    val displayNameEn: String,
    val displayNameAr: String,
    val subtitleEn: String,
    val subtitleAr: String,
    val rarity: StoreItemRarity,
    val shieldsCost: Int,
    val costTier: ForgeKeyTier?,
    val costAmount: Int,
    val isUnlockedByDefault: Boolean,
    val primaryAccentHex: Long,
    val secondaryAccentHex: Long,
    val isNextGenCenterpiece: Boolean = false
) {
    STREAK_FLAME(
        id = "streak_flame",
        displayNameEn = "Home Streak Flame",
        displayNameAr = "شعلة صفحة الـ Home",
        subtitleEn = "Core Home centerpiece • Synced with Streak (0 - 60 consecutive days)",
        subtitleAr = "العنصر الأساسي في الصفحة الرئيسية • يرتبط بالسلسلة (0 - 60 يوماً متتالياً)",
        rarity = StoreItemRarity.FREE,
        shieldsCost = 0,
        costTier = null,
        costAmount = 0,
        isUnlockedByDefault = true,
        primaryAccentHex = 0xFFFF6D00,
        secondaryAccentHex = 0xFFFFD700,
        isNextGenCenterpiece = false
    ),
    DNA_CHROMOSOME(
        id = "dna_chromosome",
        displayNameEn = "DNA & Chromosomes Helix",
        displayNameAr = "حمض الـ DNA والكروموزومات",
        subtitleEn = "3D self-rotating chromosome helix synced to Streak (0 → 60 consecutive days)",
        subtitleAr = "يدور ثلاثي الأبعاد حول نفسه ويرتبط بالسترايك (0 → 60 يوماً متتالياً)",
        rarity = StoreItemRarity.MYTHIC,
        shieldsCost = 60,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 3,
        isUnlockedByDefault = false,
        primaryAccentHex = 0xFF00F2FE,
        secondaryAccentHex = 0xFFA855F7,
        isNextGenCenterpiece = false
    ),
    QUANTUM_REACTOR(
        id = "quantum_reactor",
        displayNameEn = "Quantum Reactor Core",
        displayNameAr = "نواة المفاعل الكمي",
        subtitleEn = "Central energy sphere with counter-rotating liquid glass rings pulsed by playback",
        subtitleAr = "كرة طاقة مركزية بحلقات زجاجية سائلة متعاكسة الدوران تنبض مع نشاط المشغّل",
        rarity = StoreItemRarity.EPIC,
        shieldsCost = 35,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 2,
        isUnlockedByDefault = false,
        primaryAccentHex = 0xFF00F2FE,
        secondaryAccentHex = 0xFF7C3AED,
        isNextGenCenterpiece = true
    ),
    ACOUSTIC_SINGULARITY(
        id = "acoustic_singularity",
        displayNameEn = "Acoustic Singularity",
        displayNameAr = "الثقب الأسود الصوتي",
        subtitleEn = "Pure OLED black event horizon with swirling accretion particles & wave distortion",
        subtitleAr = "نواة OLED سوداء خالصة مع قرص جسيمات نيون دوّار وتموّجات تفاعلية",
        rarity = StoreItemRarity.MYTHIC,
        shieldsCost = 50,
        costTier = ForgeKeyTier.DIAMOND,
        costAmount = 1,
        isUnlockedByDefault = false,
        primaryAccentHex = 0xFFA855F7,
        secondaryAccentHex = 0xFFFF2E93,
        isNextGenCenterpiece = true
    ),
    HOLOGRAPHIC_CYBER_VINYL(
        id = "holographic_cyber_vinyl",
        displayNameEn = "Holographic Cyber-Vinyl",
        displayNameAr = "أسطوانة الفينيل الهولوغرافية",
        subtitleEn = "3D-angled futuristic vinyl with holographic shaders that accelerates during playback",
        subtitleAr = "أسطوانة مستقبلية ثلاثية الأبعاد بلمعان هولوغرافي تتسارع أثناء التشغيل",
        rarity = StoreItemRarity.EPIC,
        shieldsCost = 40,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 3,
        isUnlockedByDefault = false,
        primaryAccentHex = 0xFF00F2FE,
        secondaryAccentHex = 0xFFFFD700,
        isNextGenCenterpiece = true
    ),
    LIQUID_TIME_CRYSTAL(
        id = "liquid_time_crystal",
        displayNameEn = "Liquid Time Crystal Hourglass",
        displayNameAr = "الساعة الرملية الكريستالية السائلة",
        subtitleEn = "Geometric liquid-glass hourglass flowing with neon droplets synced to Streak & Watch goal",
        subtitleAr = "ساعة زجاجية هندسية تتدفق منها قطرات نيون مرتبطة بالسلسلة وتقدّم المشاهدة اليومي",
        rarity = StoreItemRarity.RARE,
        shieldsCost = 25,
        costTier = ForgeKeyTier.SILVER,
        costAmount = 2,
        isUnlockedByDefault = false,
        primaryAccentHex = 0xFF10B981,
        secondaryAccentHex = 0xFF00F2FE,
        isNextGenCenterpiece = true
    ),
    CYBERNETIC_NEURAL_WEB(
        id = "cybernetic_neural_web",
        displayNameEn = "Cybernetic Neural Web",
        displayNameAr = "الشبكة العصبية السيبرانية",
        subtitleEn = "Interactive synaptic matrix of glowing nodes pulsing with cybernetic signals",
        subtitleAr = "مصفوفة تفاعلية من العقد الرقمية والروابط العصبية النابضة بإشارات سيبرانية",
        rarity = StoreItemRarity.MYTHIC,
        shieldsCost = 65,
        costTier = ForgeKeyTier.DIAMOND,
        costAmount = 2,
        isUnlockedByDefault = false,
        primaryAccentHex = 0xFF00F2FE,
        secondaryAccentHex = 0xFFEC4899,
        isNextGenCenterpiece = true
    ),
    SUPERNOVA_NEBULA_CORE(
        id = "supernova_nebula_core",
        displayNameEn = "Supernova Nebula Core",
        displayNameAr = "نواة سديم السوبرنوفا",
        subtitleEn = "Miniature stellar core veiled in breathing purple & turquoise cosmic nebula clouds",
        subtitleAr = "نواة نجمية مصغّرة محاطة بسحب سديمية أرجوانية وفيروزية تتنفس بضوء محيطي ناعم",
        rarity = StoreItemRarity.RARE,
        shieldsCost = 30,
        costTier = ForgeKeyTier.SILVER,
        costAmount = 2,
        isUnlockedByDefault = false,
        primaryAccentHex = 0xFF00F2FE,
        secondaryAccentHex = 0xFFA855F7,
        isNextGenCenterpiece = true
    ),
    QUANTUM_ORACLE_SPHERE(
        id = "quantum_oracle_sphere",
        displayNameEn = "Quantum Oracle Sphere",
        displayNameAr = "كرة العرّاف الكمي",
        subtitleEn = "Floating frosted glass orb enclosing a shifting geometric rune-matrix",
        subtitleAr = "كرة زجاجية عائمة شبه شفافة تحتضن مصفوفة رونية هندسية دوّارة ومتغيّرة",
        rarity = StoreItemRarity.EPIC,
        shieldsCost = 42,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 2,
        isUnlockedByDefault = false,
        primaryAccentHex = 0xFF38BDF8,
        secondaryAccentHex = 0xFFA855F7,
        isNextGenCenterpiece = true
    ),
    SUPERCONDUCTING_PLASMA_LATTICE(
        id = "superconducting_plasma_lattice",
        displayNameEn = "Superconducting Plasma Lattice",
        displayNameAr = "شبكة البلازما فائقة التوصيل",
        subtitleEn = "Interlocking dark obsidian rings enclosing humming cyan cyber-lightning nodes",
        subtitleAr = "حلقات أوبسيديان داكنة متداخلة تحتضن نواة معلقة من عقد البرق السيبراني الفيروزي",
        rarity = StoreItemRarity.EPIC,
        shieldsCost = 48,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 3,
        isUnlockedByDefault = false,
        primaryAccentHex = 0xFF00F2FE,
        secondaryAccentHex = 0xFF3B82F6,
        isNextGenCenterpiece = true
    ),
    FERROFLUIDIC_MAGNETIC_VORTEX(
        id = "ferrofluidic_magnetic_vortex",
        displayNameEn = "Ferrofluidic Magnetic Vortex",
        displayNameAr = "دوّامة المعدن السائل المغناطيسي",
        subtitleEn = "Organic morphing black liquid metal reacting fluidly to media rhythm",
        subtitleAr = "حلقة مركزية من المعدن السائل الأسود المتشكل عضوياً يتفاعل بانسيابية مع إيقاع الوسائط",
        rarity = StoreItemRarity.MYTHIC,
        shieldsCost = 58,
        costTier = ForgeKeyTier.DIAMOND,
        costAmount = 1,
        isUnlockedByDefault = false,
        primaryAccentHex = 0xFFE2E8F0,
        secondaryAccentHex = 0xFF00F2FE,
        isNextGenCenterpiece = true
    ),
    DIMENSIONAL_STARGATE_MONOLITH(
        id = "dimensional_stargate_monolith",
        displayNameEn = "Dimensional Stargate Monolith",
        displayNameAr = "بوابة الأبعاد البلورية",
        subtitleEn = "Parting frosted glass shards revealing an infinite digital horizon synced to roadmap",
        subtitleAr = "شظايا زجاجية عمودية تنفرج لتكشف أفقاً رقمياً لانهائياً مرتبطاً بتقدّم خريطة الطريق",
        rarity = StoreItemRarity.MYTHIC,
        shieldsCost = 75,
        costTier = ForgeKeyTier.DIAMOND,
        costAmount = 2,
        isUnlockedByDefault = false,
        primaryAccentHex = 0xFF00F2FE,
        secondaryAccentHex = 0xFFFFD700,
        isNextGenCenterpiece = true
    );

    companion object {
        fun fromId(id: String?): HomeMimicItem? =
            entries.firstOrNull { it.id == id }

        val nextGenMimics: List<HomeMimicItem>
            get() = entries.filter { it.isNextGenCenterpiece }
    }
}

enum class StoreAvatarItem(
    val id: String,
    val displayNameEn: String,
    val displayNameAr: String,
    val subtitleEn: String,
    val subtitleAr: String,
    val frameNameEn: String,
    val frameNameAr: String,
    val rarity: StoreItemRarity,
    val shieldsCost: Int,
    val costTier: ForgeKeyTier,
    val costAmount: Int,
    val primaryAccentHex: Long,
    val secondaryAccentHex: Long,
    val tertiaryAccentHex: Long
) {
    CYBER_SAMURAI(
        id = "cyber_samurai",
        displayNameEn = "Cyber Samurai",
        displayNameAr = "الساموراي السيبراني",
        subtitleEn = "Tactical mask with glowing blue/orange eyes and rotating plasma-spark cyber frame",
        subtitleAr = "قناع تكتيكي بعيون زرقاء وبرتقالية متوهجة مع إطار شرارات البلازما الدوّار",
        frameNameEn = "Plasma-Spark Cyber Frame",
        frameNameAr = "إطار شرارات البلازما السيبراني",
        rarity = StoreItemRarity.COMMON,
        shieldsCost = 12,
        costTier = ForgeKeyTier.BRONZE,
        costAmount = 1,
        primaryAccentHex = 0xFF00F2FE,
        secondaryAccentHex = 0xFFFF6D00,
        tertiaryAccentHex = 0xFF38BDF8
    ),
    NEURAL_HACKER(
        id = "neural_hacker",
        displayNameEn = "Neural Hacker",
        displayNameAr = "المخترق العصبي",
        subtitleEn = "Dark digital portrait overlaid with matrix code streams and holographic HUD glasses",
        subtitleAr = "بورتريه رقمي داكن مع سيول شيفرة الماتريكس الخضراء ونظارات HUD هولوغرافية",
        frameNameEn = "Matrix Telemetry Halo",
        frameNameAr = "هالة قياسات الماتريكس",
        rarity = StoreItemRarity.COMMON,
        shieldsCost = 15,
        costTier = ForgeKeyTier.BRONZE,
        costAmount = 2,
        primaryAccentHex = 0xFF10B981,
        secondaryAccentHex = 0xFF00F2FE,
        tertiaryAccentHex = 0xFF34D399
    ),
    QUANTUM_GHOST(
        id = "quantum_ghost",
        displayNameEn = "Quantum Ghost",
        displayNameAr = "الشبح الكمي",
        subtitleEn = "Frosted liquid glass translucent silhouette with internal floating energy particles",
        subtitleAr = "طيف شفاف من الزجاج السائل المصنفر تتطاير بداخله جسيمات طاقة كمية متوهجة",
        frameNameEn = "Cryo-Glass Phase Ring",
        frameNameAr = "حلقة الطور الزجاجي الكمي",
        rarity = StoreItemRarity.RARE,
        shieldsCost = 22,
        costTier = ForgeKeyTier.SILVER,
        costAmount = 2,
        primaryAccentHex = 0xFF38BDF8,
        secondaryAccentHex = 0xFFE0F2FE,
        tertiaryAccentHex = 0xFF818CF8
    ),
    DEEP_SPACE_PILOT(
        id = "deep_space_pilot",
        displayNameEn = "Deep Space Pilot",
        displayNameAr = "طيار الفضاء العميق",
        subtitleEn = "Dark futuristic helmet reflecting moving star nebulae and subtle starlight twinkles",
        subtitleAr = "خوذة مستقبلية داكنة تعكس سديم النجوم المتحرك وبريق النجوم المتلألئ",
        frameNameEn = "Orbital Starlight Ring",
        frameNameAr = "حلقة مدار النجوم الكونية",
        rarity = StoreItemRarity.RARE,
        shieldsCost = 26,
        costTier = ForgeKeyTier.SILVER,
        costAmount = 2,
        primaryAccentHex = 0xFF7C3AED,
        secondaryAccentHex = 0xFF00F2FE,
        tertiaryAccentHex = 0xFFF472B6
    ),
    NEON_GLITCH(
        id = "neon_glitch",
        displayNameEn = "Neon Glitch",
        displayNameAr = "خلل النيون الطيفي",
        subtitleEn = "Chromatic aberration split portrait with sharp pulsing neon pink/cyan energy lines",
        subtitleAr = "بورتريه منقسم بالانزياح اللوني مع خطوط طاقة وردية وسماوية نابضة",
        frameNameEn = "Chromatic Split Frame",
        frameNameAr = "إطار الانزياح اللوني النبضي",
        rarity = StoreItemRarity.RARE,
        shieldsCost = 30,
        costTier = ForgeKeyTier.SILVER,
        costAmount = 3,
        primaryAccentHex = 0xFFFF2E93,
        secondaryAccentHex = 0xFF00F2FE,
        tertiaryAccentHex = 0xFFA855F7
    ),
    MECHA_ANDROID(
        id = "mecha_android",
        displayNameEn = "Mecha Android",
        displayNameAr = "الأندرويد الميكانيكي",
        subtitleEn = "Matte black steel mechanical faceplate revealing glowing orange nuclear core joints",
        subtitleAr = "واجهة فولاذية سوداء مطفية تكشف مفاصل نواة نووية برتقالية متوهجة",
        frameNameEn = "Hex-Reactor Armor Frame",
        frameNameAr = "إطار الدرع النووي السداسي",
        rarity = StoreItemRarity.EPIC,
        shieldsCost = 38,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 2,
        primaryAccentHex = 0xFFFF6D00,
        secondaryAccentHex = 0xFFFFD700,
        tertiaryAccentHex = 0xFFFF3D00
    ),
    VOID_WANDERER(
        id = "void_wanderer",
        displayNameEn = "Void Wanderer",
        displayNameAr = "جوال الفراغ الكوني",
        subtitleEn = "Hooded dark figure with a miniature event-horizon black hole core & accretion disk",
        subtitleAr = "شخصية غامضة بقلنسوة داكنة ونواة ثقب أسود محاطة بقرص تراكم متوهج",
        frameNameEn = "Event Horizon Accretion Halo",
        frameNameAr = "هالة قرص أفق الحدث",
        rarity = StoreItemRarity.EPIC,
        shieldsCost = 45,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 3,
        primaryAccentHex = 0xFFA855F7,
        secondaryAccentHex = 0xFFFF2E4C,
        tertiaryAccentHex = 0xFF00F2FE
    ),
    BIO_LUMINESCENT_SYNTH(
        id = "bio_luminescent_synth",
        displayNameEn = "Bio-Luminescent Synth",
        displayNameAr = "الاصطناعي الحيوي المضيء",
        subtitleEn = "Cybernetic face interwoven with pulsing organic DNA-luminescent fiber strands",
        subtitleAr = "وجه سيبراني متشابك مع ألياف ضوئية عضوية تنبض كخيوط الـ DNA الحيوية",
        frameNameEn = "Bio-Helix Pulse Aura",
        frameNameAr = "هالة نبض الحلزون الحيوي",
        rarity = StoreItemRarity.EPIC,
        shieldsCost = 48,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 3,
        primaryAccentHex = 0xFF00FF88,
        secondaryAccentHex = 0xFFEC4899,
        tertiaryAccentHex = 0xFF00F2FE
    ),
    APEX_CYBER_HUNTER(
        id = "apex_cyber_hunter",
        displayNameEn = "Apex Cyber-Hunter",
        displayNameAr = "صياد القمة السيبراني",
        subtitleEn = "Sharp-featured face with an active targeting HUD visor and dynamic tactical grid lines",
        subtitleAr = "ملامح حادة مع منظار HUD نشط لتحديد الأهداف وخطوط شبكية تكتيكية متحركة",
        frameNameEn = "Apex Targeting Reticle Ring",
        frameNameAr = "حلقة تصويب القمة التكتيكية",
        rarity = StoreItemRarity.MYTHIC,
        shieldsCost = 60,
        costTier = ForgeKeyTier.DIAMOND,
        costAmount = 1,
        primaryAccentHex = 0xFFFF1744,
        secondaryAccentHex = 0xFF00F0FF,
        tertiaryAccentHex = 0xFFFFD700
    ),
    AURA_SOVEREIGN(
        id = "aura_sovereign",
        displayNameEn = "Aura Sovereign",
        displayNameAr = "سيّد الهالة الملكي",
        subtitleEn = "Regal geometric glass-shard crown floating and glowing with royal gold/blue light",
        subtitleAr = "تاج هندسي من شظايا الزجاج الملكي يطفو ويتوهج بالذهب والأزرق الإمبراطوري",
        frameNameEn = "Sovereign Prism Crown Halo",
        frameNameAr = "هالة التاج المنشوري الملكية",
        rarity = StoreItemRarity.MYTHIC,
        shieldsCost = 80,
        costTier = ForgeKeyTier.DIAMOND,
        costAmount = 2,
        primaryAccentHex = 0xFFFFD700,
        secondaryAccentHex = 0xFF38BDF8,
        tertiaryAccentHex = 0xFFA855F7
    );

    companion object {
        fun fromId(id: String?): StoreAvatarItem? =
            entries.firstOrNull { it.id == id }
    }
}



