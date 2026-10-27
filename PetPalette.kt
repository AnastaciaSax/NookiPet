package eng.nookipet.lab7.ui.theme

import androidx.annotation.ColorInt

/**
 * Palette for one pet. Colors are stored as ColorInt values,
 * so we can switch them at runtime without recreating the Activity.
 * Each pet in the database has a paletteId — see PetPaletteCatalog.
 */
data class PetPalette(
    val id: String,
    val displayName: String,

    // Primary colors used for cards, FAB, chip background, avatar gradient
    @ColorInt val primary: Int,
    @ColorInt val primaryLight: Int,
    @ColorInt val primaryDark: Int,

    // Secondary — for gradients, links, secondary highlights
    @ColorInt val secondary: Int,
    @ColorInt val secondaryLight: Int,

    // Accent — for FAB, badges, important buttons
    @ColorInt val accent: Int,

    // Orb colors for the atmospheric background (already with alpha)
    @ColorInt val orbA: Int,
    @ColorInt val orbB: Int,
    @ColorInt val orbC: Int
)