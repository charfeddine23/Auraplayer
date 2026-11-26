package com.example.data.local

import com.example.data.model.ForgeKeyTier
import com.example.data.model.StoreItemRarity

/**
 * 10 Luxury "Blur & Fade Ambient Glow" cinema modes persisted via Room DB.
 * Zero neon, zero sharp edges, zero particles — pure Gaussian blurs,
 * low-saturation dark tints, and smooth alpha fades into OLED #000000.
 * Rarity & pricing align with the Store economy (Shields + Forge Keys, or Free via Premium).
 */
enum class AmbientBlackBlurMode(
    val titleEn: String,
    val titleAr: String,
    val subtitleEn: String,
    val subtitleAr: String,
    val shortBadge: String,
    val rarity: StoreItemRarity,
    val shieldsCost: Int,
    val costTier: ForgeKeyTier?,
    val costAmount: Int,
    val isUnlockedByDefault: Boolean = false
) {
    DEEP_OBSIDIAN_BLUR(
        titleEn = "Silent Obsidian Eclipse",
        titleAr = "كسوف السبج الصامت",
        subtitleEn = "Circular dark eclipse halo fading bright edges into OLED black",
        subtitleAr = "هالة كسوف دائرية داكنة تمتص الحواف الساطعة نحو الأسود",
        shortBadge = "01",
        rarity = StoreItemRarity.COMMON,
        shieldsCost = 6,
        costTier = ForgeKeyTier.BRONZE,
        costAmount = 1,
        isUnlockedByDefault = false
    ),
    CINEMA_MATTE_VIGNETTE(
        titleEn = "Smoked Crystal Diffusion",
        titleAr = "انتشار الكريستال المدخن",
        subtitleEn = "Ultra-wide frosted smoked blur with 10% desaturated reflection",
        subtitleAr = "تمويه زجاجي مدخن فائق الاتساع بانعكاس لوني هادئ 10%",
        shortBadge = "02",
        rarity = StoreItemRarity.COMMON,
        shieldsCost = 10,
        costTier = ForgeKeyTier.BRONZE,
        costAmount = 1
    ),
    SMOKED_LIQUID_GLASS(
        titleEn = "Matte Theater Curtain Fade",
        titleAr = "تلاشي ستائر المسرح المخملية",
        subtitleEn = "Horizontal shadow blur & dark velvet curtain fade on side borders",
        subtitleAr = "تمويه أفقي وتلاشي مخملي داكن يشبه ستائر السينما الجانبية",
        shortBadge = "03",
        rarity = StoreItemRarity.COMMON,
        shieldsCost = 15,
        costTier = ForgeKeyTier.BRONZE,
        costAmount = 2
    ),
    OLED_MIDNIGHT_DIFFUSION(
        titleEn = "Still Arctic Mist",
        titleAr = "ضباب القطب الساكن",
        subtitleEn = "Luminance-only monochrome shadow/light cloud mist diffusion",
        subtitleAr = "ضباب سحابي أحادي اللون يعتمد على الإضاءة والظل فقط",
        shortBadge = "04",
        rarity = StoreItemRarity.RARE,
        shieldsCost = 22,
        costTier = ForgeKeyTier.SILVER,
        costAmount = 2
    ),
    VELVET_SHADOW_MIST(
        titleEn = "Deep Velvet Melt",
        titleAr = "ذوبان المخمل العميق",
        subtitleEn = "High-density edge blur with dark velvet mask dissolving borders",
        subtitleAr = "تمويه عالي الكثافة بقناع مخملي داكن يذيب حدود الفيديو تماماً",
        shortBadge = "05",
        rarity = StoreItemRarity.RARE,
        shieldsCost = 28,
        costTier = ForgeKeyTier.SILVER,
        costAmount = 3
    ),
    EDGE_FADE_GAUSSIAN_BLUR(
        titleEn = "Midnight Horizon Gradient",
        titleAr = "تدرج أفق منتصف الليل",
        subtitleEn = "Vertical-weighted soft blur fading smoothly from top & bottom edges",
        subtitleAr = "تمويه عمودي ناعم يتلاشى بسلاسة نحو الخارج من الأعلى والأسفل",
        shortBadge = "06",
        rarity = StoreItemRarity.EPIC,
        shieldsCost = 35,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 2
    ),
    RADIAL_VIGNETTE_DISSOLVE(
        titleEn = "Dark Ink Ripple Blur",
        titleAr = "تمويه تموج الحبر الداكن",
        subtitleEn = "Ultra-slow fluid diffusion like dark ink dispersing in water",
        subtitleAr = "انتشار مائع فائق البطء يشبه ذوبان الحبر الداكن بنعومة في الماء",
        shortBadge = "07",
        rarity = StoreItemRarity.EPIC,
        shieldsCost = 40,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 3
    ),
    CINEMATIC_SMOKED_BLUR(
        titleEn = "Frosted Noir Marble",
        titleAr = "رخام النوار الضبابي",
        subtitleEn = "Multi-toned charcoal & deep slate frosted blur with zero eye strain",
        subtitleAr = "تمويه ضبابي متعدد الدرجات من الفحم والرمادي الداكن لراحة العين",
        shortBadge = "08",
        rarity = StoreItemRarity.EPIC,
        shieldsCost = 45,
        costTier = ForgeKeyTier.GOLD,
        costAmount = 3
    ),
    VELVET_SHADOW_MELT(
        titleEn = "Anamorphic Bokeh Fade",
        titleAr = "تلاشي البوكيه السينمائي",
        subtitleEn = "Soft out-of-focus lens blur turning highlights into faint fading orbs",
        subtitleAr = "تمويه عدسة سينمائية يحوّل الإضاءة إلى هالات خافتة متلاشية",
        shortBadge = "09",
        rarity = StoreItemRarity.MYTHIC,
        shieldsCost = 55,
        costTier = ForgeKeyTier.DIAMOND,
        costAmount = 2
    ),
    MULTI_LAYER_LAYERED_FADE(
        titleEn = "Dimmed Charcoal Halo",
        titleAr = "هالة الفحم الخافتة",
        subtitleEn = "Warm muted charcoal blur with wide radial alpha fade for night viewing",
        subtitleAr = "تمويه فحمي دافئ خافت بتلاشي إشعاعي واسع للمشاهدة الليلية",
        shortBadge = "10",
        rarity = StoreItemRarity.MYTHIC,
        shieldsCost = 70,
        costTier = ForgeKeyTier.DIAMOND,
        costAmount = 3
    );

    companion object {
        fun fromName(name: String?): AmbientBlackBlurMode {
            return entries.firstOrNull { it.name == name } ?: DEEP_OBSIDIAN_BLUR
        }
    }
}
