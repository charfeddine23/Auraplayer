package com.example.data.model

import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToLong

enum class VaultTier(
    val titleAr: String,
    val titleEn: String,
    val rangeText: String,
    val startLevel: Int,
    val endLevel: Int
) {
    FOUNDATION("المستوى التأسيسي", "Foundation Tier", "1 - 30", 1, 30),
    PRO("مستوى المحترفين", "Pro Tier", "31 - 100", 31, 100),
    EXPERT("مستوى الخبراء", "Expert Tier", "101 - 200", 101, 200),
    ELITE("القمة الأسطورية", "Elite Pinnacle", "201 - 365", 201, 365)
}

data class VaultLevelMilestone(
    val level: Int,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val shieldsReward: Int,
    val tier: VaultTier,
    val isMajor: Boolean = false,
    val badgeName: String? = null,
    val iconType: String = "shield",
    val requiredXp: Long = 0L
)

data class RoadmapXpState(
    val unlockedLevel: Int = 0,
    val currentLevelXp: Long = 0L,
    val watchXpSecondsAccum: Int = 0
) {
    val isMaxLevelReached: Boolean
        get() = unlockedLevel >= VaultProgressionRegistry.MAX_ROADMAP_LEVEL

    val targetLevel: Int
        get() = (unlockedLevel + 1).coerceIn(1, VaultProgressionRegistry.MAX_ROADMAP_LEVEL)

    val requiredLevelXp: Long
        get() = VaultProgressionRegistry.xpRequiredForLevel(targetLevel)

    val levelProgressFraction: Float
        get() = if (isMaxLevelReached) {
            1f
        } else {
            (currentLevelXp.toDouble() / requiredLevelXp.coerceAtLeast(1L).toDouble())
                .toFloat()
                .coerceIn(0f, 1f)
        }
}

object VaultProgressionRegistry {

    const val MAX_ROADMAP_LEVEL = 365
    const val LEVEL_1_XP = 50L
    const val LEVEL_365_XP = 50_000_000L
    const val WATCH_XP_PER_MINUTE = 10L
    const val STREAK_XP_PER_DAY_STEP = 100L
    const val MAX_STREAK_XP_DAYS = 365
    const val MAX_DAILY_STREAK_XP = 36_500L

    private val LEVEL_XP_TABLE: LongArray = LongArray(MAX_ROADMAP_LEVEL + 1).apply {
        this[0] = 0L
        this[1] = LEVEL_1_XP
        this[MAX_ROADMAP_LEVEL] = LEVEL_365_XP
        val ratio = LEVEL_365_XP.toDouble() / LEVEL_1_XP.toDouble()
        for (lvl in 2 until MAX_ROADMAP_LEVEL) {
            val t = (lvl - 1).toDouble() / (MAX_ROADMAP_LEVEL - 1).toDouble()
            val raw = LEVEL_1_XP.toDouble() * ratio.pow(t)
            val rounded = when {
                raw < 100.0 -> raw.roundToLong()
                raw < 1_000.0 -> (raw / 5.0).roundToLong() * 5L
                raw < 10_000.0 -> (raw / 50.0).roundToLong() * 50L
                raw < 100_000.0 -> (raw / 500.0).roundToLong() * 500L
                raw < 1_000_000.0 -> (raw / 5_000.0).roundToLong() * 5_000L
                raw < 10_000_000.0 -> (raw / 25_000.0).roundToLong() * 25_000L
                else -> (raw / 100_000.0).roundToLong() * 100_000L
            }
            val minAllowed = this[lvl - 1] + 1L
            val maxAllowed = LEVEL_365_XP - (MAX_ROADMAP_LEVEL - lvl).toLong()
            this[lvl] = rounded.coerceIn(minAllowed, maxAllowed)
        }
    }

    /**
     * Independent (non-cumulative) XP required to reach [level] (1..365).
     * Level 1 = 50 XP, Level 365 = 50,000,000 XP.
     */
    fun xpRequiredForLevel(level: Int): Long {
        return LEVEL_XP_TABLE[level.coerceIn(1, MAX_ROADMAP_LEVEL)]
    }

    /**
     * XP granted when completing daily goal on streak day [streakDay].
     * Day 1 = 100 XP, Day 2 = 200 XP, ..., Day 365 = 36,500 XP.
     * Above Day 365, Streak XP remains fixed at 36,500 XP.
     */
    fun streakDayXpReward(streakDay: Int): Long {
        if (streakDay <= 0) return 0L
        return streakDay.coerceAtMost(MAX_STREAK_XP_DAYS).toLong() * STREAK_XP_PER_DAY_STEP
    }

    fun formatXpNumber(xp: Long): String {
        return String.format(Locale.US, "%,d", xp.coerceAtLeast(0L))
    }

    fun formatXpCompact(xp: Long): String {
        val safe = xp.coerceAtLeast(0L)
        return when {
            safe >= 1_000_000L -> {
                val m = safe.toDouble() / 1_000_000.0
                val formatted = if (m >= 10.0 && m % 1.0 < 0.05) {
                    String.format(Locale.US, "%.0fM", m)
                } else {
                    String.format(Locale.US, "%.2fM", m).replace(".00M", "M")
                }
                formatted
            }
            safe >= 1_000L -> {
                val k = safe.toDouble() / 1_000.0
                val formatted = if (k >= 100.0 || k % 1.0 < 0.05) {
                    String.format(Locale.US, "%.0fK", k)
                } else {
                    String.format(Locale.US, "%.1fK", k).replace(".0K", "K")
                }
                formatted
            }
            else -> safe.toString()
        }
    }

    fun getMilestone(level: Int): VaultLevelMilestone {
        val clampedLevel = level.coerceIn(1, MAX_ROADMAP_LEVEL)
        return buildMilestoneInternal(clampedLevel).copy(
            requiredXp = xpRequiredForLevel(clampedLevel)
        )
    }

    private fun buildMilestoneInternal(clampedLevel: Int): VaultLevelMilestone {

        // Tier 1: Levels 1 to 30 (Foundation Milestones & Shield Rewards)
        if (clampedLevel in 1..30) {
            return when (clampedLevel) {
                1 -> VaultLevelMilestone(
                    level = 1,
                    titleAr = "بداية رحلة الشعلة",
                    titleEn = "Flame Journey Kickoff",
                    descriptionAr = "انطلاقة السلسلة اليومية الأولى مع حزمة +10 دروع حماية ترحيبية.",
                    descriptionEn = "Begin your daily watch streak with a +10 protection shields starter pack.",
                    shieldsReward = 10,
                    tier = VaultTier.FOUNDATION,
                    iconType = "flame"
                )
                2 -> VaultLevelMilestone(
                    level = 2,
                    titleAr = "شرارة الاستمرارية الأولى",
                    titleEn = "First Persistence Spark",
                    descriptionAr = "إثبات التزام اليوم الثاني مع مكافأة +10 دروع إضافية.",
                    descriptionEn = "Day-2 consistency milestone awarding +10 Streak Shields.",
                    shieldsReward = 10,
                    tier = VaultTier.FOUNDATION,
                    iconType = "shield"
                )
                3 -> VaultLevelMilestone(
                    level = 3,
                    titleAr = "تعزيز الشعلة الثلاثي",
                    titleEn = "Triple Flame Momentum",
                    descriptionAr = "ثلاثة أيام متواصلة من التغذية المستمرة للشعلة (+10 دروع).",
                    descriptionEn = "Three consecutive days keeping the daily watch flame alive (+10 Shields).",
                    shieldsReward = 10,
                    tier = VaultTier.FOUNDATION,
                    iconType = "flame"
                )
                4 -> VaultLevelMilestone(
                    level = 4,
                    titleAr = "حارس البوابة الأول",
                    titleEn = "Gatekeeper Rank I",
                    descriptionAr = "تعزيز مخزون الحماية بـ +10 دروع إضافية قبل محطة اليوم الخامس.",
                    descriptionEn = "Reinforce your inventory with +10 Shields ahead of Day 5.",
                    shieldsReward = 10,
                    tier = VaultTier.FOUNDATION,
                    iconType = "shield"
                )
                5 -> VaultLevelMilestone(
                    level = 5,
                    titleAr = "درع البداية المتوهج (محطة كبرى)",
                    titleEn = "Glowing Starter Shield Milestone",
                    descriptionAr = "إنجاز الأيام الخمسة الأولى مع منحة مضاعفة (+20 درع حماية).",
                    descriptionEn = "First 5-day milestone completed with a +20 Shields boost.",
                    shieldsReward = 20,
                    tier = VaultTier.FOUNDATION,
                    isMajor = true,
                    badgeName = "Starter Flame",
                    iconType = "trophy"
                )
                in 6..9 -> VaultLevelMilestone(
                    level = clampedLevel,
                    titleAr = "توهج النيون المتصاعد للمستوى $clampedLevel",
                    titleEn = "Rising Neon Aura Lvl $clampedLevel",
                    descriptionAr = "شارة تقدم مستمرة مع +10 دروع حماية تضاف إلى رصيدك.",
                    descriptionEn = "Continuous streak progression awarding +10 Shields.",
                    shieldsReward = 10,
                    tier = VaultTier.FOUNDATION,
                    iconType = "sparkle"
                )
                10 -> VaultLevelMilestone(
                    level = 10,
                    titleAr = "حارس الشعلة الفضي (محطة كبرى)",
                    titleEn = "Silver Flame Guardian Milestone",
                    descriptionAr = "إتمام العشرة أيام الأولى بنجاح مع جائزة 30 درع حماية.",
                    descriptionEn = "10-day streak milestone awarding Silver Guardian badge + 30 Shields.",
                    shieldsReward = 30,
                    tier = VaultTier.FOUNDATION,
                    isMajor = true,
                    badgeName = "Silver Guardian",
                    iconType = "shield_pack"
                )
                in 11..14 -> VaultLevelMilestone(
                    level = clampedLevel,
                    titleAr = "صمود السلسلة للمستوى $clampedLevel",
                    titleEn = "Streak Resilience Lvl $clampedLevel",
                    descriptionAr = "تواصل يومي يحفظ السلسلة ويمنحك +10 دروع حماية إضافية.",
                    descriptionEn = "Daily consistency milestone granting +10 Protection Shields.",
                    shieldsReward = 10,
                    tier = VaultTier.FOUNDATION,
                    iconType = "shield"
                )
                15 -> VaultLevelMilestone(
                    level = 15,
                    titleAr = "حزمة دروع منتصف الشهر",
                    titleEn = "Mid-Month Shield Pack",
                    descriptionAr = "نصف شهر من الالتزام المتواصل ومكافأة 30 درع حماية.",
                    descriptionEn = "Half-month milestone unlocked with a +30 Shields drop.",
                    shieldsReward = 30,
                    tier = VaultTier.FOUNDATION,
                    isMajor = true,
                    badgeName = "Half-Month Vanguard",
                    iconType = "trophy"
                )
                in 16..19 -> VaultLevelMilestone(
                    level = clampedLevel,
                    titleAr = "وهج المثابرة للمستوى $clampedLevel",
                    titleEn = "Persistence Glow Lvl $clampedLevel",
                    descriptionAr = "خطوة إضافية نحو الدرع الذهبي مع مكافأة +10 دروع حماية.",
                    descriptionEn = "One step closer to the Golden Shield with +10 Shields.",
                    shieldsReward = 10,
                    tier = VaultTier.FOUNDATION,
                    iconType = "sparkle"
                )
                20 -> VaultLevelMilestone(
                    level = 20,
                    titleAr = "الدرع الذهبي الملكي (محطة كبرى)",
                    titleEn = "Golden Royal Shield Milestone",
                    descriptionAr = "إتمام 20 يوماً واستحقاق وسام الدرع الذهبي مع +50 درعاً.",
                    descriptionEn = "20-day mastery unlocking the Golden Shield crest + 50 Shields.",
                    shieldsReward = 50,
                    tier = VaultTier.FOUNDATION,
                    isMajor = true,
                    badgeName = "Golden Guardian",
                    iconType = "crown"
                )
                in 21..24 -> VaultLevelMilestone(
                    level = clampedLevel,
                    titleAr = "طاقة النخبة للمستوى $clampedLevel",
                    titleEn = "Elite Momentum Lvl $clampedLevel",
                    descriptionAr = "تعزيز إضافي لرصيد الدروع (+10) مع الحفاظ على اشتعال الشعلة.",
                    descriptionEn = "Keep your daily flame burning to collect +10 Shields.",
                    shieldsReward = 10,
                    tier = VaultTier.FOUNDATION,
                    iconType = "shield"
                )
                25 -> VaultLevelMilestone(
                    level = 25,
                    titleAr = "وسام الهالة المشعّة (محطة كبرى)",
                    titleEn = "Radiant Aura Crest Milestone",
                    descriptionAr = "إنجاز الـ 25 يوماً مع منحة 50 درعاً وشارة الشرف الزجاجية.",
                    descriptionEn = "25-day achievement granting Radiant Crest + 50 Shields.",
                    shieldsReward = 50,
                    tier = VaultTier.FOUNDATION,
                    isMajor = true,
                    badgeName = "Radiant Aura",
                    iconType = "aura"
                )
                in 26..29 -> VaultLevelMilestone(
                    level = clampedLevel,
                    titleAr = "توهج أورا النيوني المتطور M${clampedLevel - 25}",
                    titleEn = "Neon Aura Flame Boost Lvl ${clampedLevel - 25}",
                    descriptionAr = "تعزيز الشعلة اليومية مع مكافأة +20 درع حماية للمخزون.",
                    descriptionEn = "Vibrant neon aura flair awarding +20 Streak Shields.",
                    shieldsReward = 20,
                    tier = VaultTier.FOUNDATION,
                    iconType = "aura"
                )
                else -> VaultLevelMilestone(
                    level = 30,
                    titleAr = "مكافأة الشهر الأول الكبرى (+100 درع)",
                    titleEn = "Month-1 Grand Milestone (+100 Shields)",
                    descriptionAr = "اكتمال 30 يوماً من الحماسة! لقب حارس الشهر الأول مع مكافأة كبرى 100 درع.",
                    descriptionEn = "30-day mastery unlocked! Month-1 Guardian title and massive +100 shields drop.",
                    shieldsReward = 100,
                    tier = VaultTier.FOUNDATION,
                    isMajor = true,
                    badgeName = "Month-1 Guardian",
                    iconType = "trophy"
                )
            }
        }

        // Tier 2: Levels 31 to 100 (Pro Tier Progress)
        if (clampedLevel in 31..100) {
            return when (clampedLevel) {
                50 -> VaultLevelMilestone(
                    level = 50,
                    titleAr = "معادل الصوت الاحترافي والصوت المكاني ثلاثي الأبعاد",
                    titleEn = "Advanced Audio Equalizer Integration",
                    descriptionAr = "دمج 5 نطاقات ترددية مع تضخيم الباس Bass Boost ومكافأة +100 درع.",
                    descriptionEn = "Full 5-band studio equalizer with dynamic bass boost & +100 Shields.",
                    shieldsReward = 100,
                    tier = VaultTier.PRO,
                    isMajor = true,
                    badgeName = "Acoustic Master",
                    iconType = "equalizer"
                )
                75 -> VaultLevelMilestone(
                    level = 75,
                    titleAr = "حزمة إدارة العلامات الزمنية والفصول الذكية",
                    titleEn = "Smart Bookmarks Management Suite",
                    descriptionAr = "إدارة كاملة للنقاط المرجعية الفورية على شريط التقدم مع +120 درعاً.",
                    descriptionEn = "Deep bookmark cataloging and chapter jumping + 120 Shields.",
                    shieldsReward = 120,
                    tier = VaultTier.PRO,
                    isMajor = true,
                    badgeName = "Timeline Navigator",
                    iconType = "bookmark"
                )
                100 -> VaultLevelMilestone(
                    level = 100,
                    titleAr = "وسام الشرف الماسي 'Aura Pro' الملكي",
                    titleEn = "'Aura Pro' Glass Title Badge & Shield Drop",
                    descriptionAr = "شعار النخبة الزجاجي على الملف الشخصي مع إسقاط أضخم للدروع (+200 درع).",
                    descriptionEn = "Prestigious 'Aura Pro' profile badge plus monumental +200 shields drop.",
                    shieldsReward = 200,
                    tier = VaultTier.PRO,
                    isMajor = true,
                    badgeName = "Aura Pro",
                    iconType = "crown"
                )
                else -> {
                    val isOdd = clampedLevel % 2 != 0
                    if (isOdd) {
                        VaultLevelMilestone(
                            level = clampedLevel,
                            titleAr = "مكافأة الصمود الاحترافي للمستوى $clampedLevel",
                            titleEn = "Pro Resilience Level $clampedLevel",
                            descriptionAr = "منح +10 دروع حماية إضافية لدعم السلسلة المستمرة.",
                            descriptionEn = "Award +10 Shields for unwavering daily engagement.",
                            shieldsReward = 10,
                            tier = VaultTier.PRO,
                            iconType = "shield"
                        )
                    } else {
                        VaultLevelMilestone(
                            level = clampedLevel,
                            titleAr = "شارة زجاجية وجزيئات لهب للمستوى $clampedLevel",
                            titleEn = "Glass UI Badge & Flame Particles Lvl $clampedLevel",
                            descriptionAr = "فتح تأثير جزيئات النار المتصاعدة وشارة تميز زجاجية مع +10 دروع.",
                            descriptionEn = "Dynamic flame particle animation, glass UI badge & +10 Shields.",
                            shieldsReward = 10,
                            tier = VaultTier.PRO,
                            badgeName = "Pro Lvl $clampedLevel",
                            iconType = "flame"
                        )
                    }
                }
            }
        }

        // Tier 3: Levels 101 to 200 (Expert Tier)
        if (clampedLevel in 101..200) {
            return when (clampedLevel) {
                150 -> VaultLevelMilestone(
                    level = 150,
                    titleAr = "طبقة التشفير المتقدمة لخزنة الوسائط السرية",
                    titleEn = "Enhanced Vault Encryption Layer",
                    descriptionAr = "تعزيز خوارزمية التشفير المحلي للفيديوهات والصوتيات مع مكافأة 150 درعاً.",
                    descriptionEn = "Military-grade local cryptographic obfuscation for vault files + 150 Shields.",
                    shieldsReward = 150,
                    tier = VaultTier.EXPERT,
                    isMajor = true,
                    badgeName = "Cipher Sentinel",
                    iconType = "encryption"
                )
                200 -> VaultLevelMilestone(
                    level = 200,
                    titleAr = "وسام خبير النظام المحلي 'Local System Master'",
                    titleEn = "'Local System Master' Badge + 250 Shields",
                    descriptionAr = "تعبئة ضخمة لمخزون الدروع (+250 درعاً) ووسام الخبير الملكي.",
                    descriptionEn = "Massive Shield inventory drop (+250 shields) and master system badge.",
                    shieldsReward = 250,
                    tier = VaultTier.EXPERT,
                    isMajor = true,
                    badgeName = "Local System Master",
                    iconType = "master"
                )
                else -> {
                    val isEvery10 = clampedLevel % 10 == 0
                    if (isEvery10) {
                        VaultLevelMilestone(
                            level = clampedLevel,
                            titleAr = "تدرج RGB مميز للمستوى $clampedLevel (+30 درعاً)",
                            titleEn = "Custom RGB Aura Preset & 30 Shields Lvl $clampedLevel",
                            descriptionAr = "فتح تدرج لوني جديد للمحيط النيوني مع مكافأة +30 درع حماية.",
                            descriptionEn = "Bespoke RGB accent tone unlocked alongside +30 Shields drop.",
                            shieldsReward = 30,
                            tier = VaultTier.EXPERT,
                            iconType = "rgb"
                        )
                    } else {
                        VaultLevelMilestone(
                            level = clampedLevel,
                            titleAr = "محطة الخبير للمستوى $clampedLevel",
                            titleEn = "Expert Milestone Level $clampedLevel",
                            descriptionAr = "تعزيز استقرار السلسلة وكسب +10 دروع حماية إضافية للمخزون.",
                            descriptionEn = "Expert streak progression reward with +10 Shields allocation.",
                            shieldsReward = 10,
                            tier = VaultTier.EXPERT,
                            iconType = "shield"
                        )
                    }
                }
            }
        }

        // Tier 4: Levels 201 to 365 (Elite Pinnacle)
        return when (clampedLevel) {
            365 -> VaultLevelMilestone(
                level = 365,
                titleAr = "الأسطورة السنوية الذهبية 'Yearly Aura Legend'",
                titleEn = "Golden 'Yearly Aura Legend' Trophy Badge",
                descriptionAr = "إنجاز 365 يوماً من الحماسة الكاملة! نقش درع الأسطورة الذهبي ومكافأة +300 درع وفتح جميع الميزات.",
                descriptionEn = "Ultimate mastery unlocked! Golden engraved trophy badge, +300 shields drop, and lifetime glass styling.",
                shieldsReward = 300,
                tier = VaultTier.ELITE,
                isMajor = true,
                badgeName = "Yearly Aura Legend 👑",
                iconType = "legend"
            )
            else -> {
                val isEvery15 = clampedLevel % 15 == 0
                if (isEvery15) {
                    VaultLevelMilestone(
                        level = clampedLevel,
                        titleAr = "إسقاط دروع النخبة للمستوى $clampedLevel (+50 درعاً)",
                        titleEn = "Elite Shield Drop Level $clampedLevel (+50 Shields)",
                        descriptionAr = "مكافأة كبرى لرواد النخبة: 50 درع حماية إضافية فورية.",
                        descriptionEn = "Prestigious elite drop: +50 Shields added straight to your inventory.",
                        shieldsReward = 50,
                        tier = VaultTier.ELITE,
                        isMajor = true,
                        iconType = "shield_pack"
                    )
                } else {
                    VaultLevelMilestone(
                        level = clampedLevel,
                        titleAr = "محطة النخبة للمستوى $clampedLevel",
                        titleEn = "Elite Milestone Level $clampedLevel",
                        descriptionAr = "خطوة نحو عرش أسطورة الـ 365 يوماً مع +10 دروع إضافية.",
                        descriptionEn = "One step closer to the 365-day peak with +10 Shields protection.",
                        shieldsReward = 10,
                        tier = VaultTier.ELITE,
                        iconType = "shield"
                    )
                }
            }
        }
    }
}
