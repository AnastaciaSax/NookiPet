package eng.nookipet.lab7.ui.theme

import android.graphics.Color

/**
 * All available palettes. Each pet in DB stores paletteId.
 * Default palette is "mint" (matches the NookiPet mockup for Finley the snail).
 */
object PetPaletteCatalog {

    val MINT = PetPalette(
        id = "mint",
        displayName = "Mint (snails, mollusks)",
        primary = Color.parseColor("#4CC9A0"),
        primaryLight = Color.parseColor("#7FD8BE"),
        primaryDark = Color.parseColor("#2FA37E"),
        secondary = Color.parseColor("#5AA9E6"),
        secondaryLight = Color.parseColor("#8FC7EE"),
        accent = Color.parseColor("#FF8A65"),
        orbA = Color.parseColor("#38B6EBD8"),
        orbB = Color.parseColor("#33C3E2F7"),
        orbC = Color.parseColor("#4DDCF6EA")
    )

    val PEACH = PetPalette(
        id = "peach",
        displayName = "Peach (cats, dogs)",
        primary = Color.parseColor("#FFB088"),
        primaryLight = Color.parseColor("#FFCBA8"),
        primaryDark = Color.parseColor("#D98E64"),
        secondary = Color.parseColor("#FF8A65"),
        secondaryLight = Color.parseColor("#FFB299"),
        accent = Color.parseColor("#4CC9A0"),
        orbA = Color.parseColor("#38FFD8C2"),
        orbB = Color.parseColor("#33FFE0C7"),
        orbC = Color.parseColor("#4DFFD0B2")
    )

    val FOREST = PetPalette(
        id = "forest",
        displayName = "Forest (reptiles, amphibians)",
        primary = Color.parseColor("#6DBE45"),
        primaryLight = Color.parseColor("#98D473"),
        primaryDark = Color.parseColor("#4A8F2D"),
        secondary = Color.parseColor("#3FA37E"),
        secondaryLight = Color.parseColor("#6FC5A1"),
        accent = Color.parseColor("#FFC371"),
        orbA = Color.parseColor("#38C8E8B8"),
        orbB = Color.parseColor("#33B8DCE0"),
        orbC = Color.parseColor("#4DDAEFC5")
    )

    val OCEAN = PetPalette(
        id = "ocean",
        displayName = "Ocean (fish, aquatics)",
        primary = Color.parseColor("#5AA9E6"),
        primaryLight = Color.parseColor("#8FC7EE"),
        primaryDark = Color.parseColor("#3579B8"),
        secondary = Color.parseColor("#7CC0F0"),
        secondaryLight = Color.parseColor("#B0D8F5"),
        accent = Color.parseColor("#4CC9A0"),
        orbA = Color.parseColor("#38C2E0F7"),
        orbB = Color.parseColor("#33D8E8FB"),
        orbC = Color.parseColor("#4DC7EDF5")
    )

    val SUNSET = PetPalette(
        id = "sunset",
        displayName = "Sunset (birds)",
        primary = Color.parseColor("#FF9A76"),
        primaryLight = Color.parseColor("#FFBCA0"),
        primaryDark = Color.parseColor("#E0744F"),
        secondary = Color.parseColor("#FFC371"),
        secondaryLight = Color.parseColor("#FFD79B"),
        accent = Color.parseColor("#5AA9E6"),
        orbA = Color.parseColor("#38FFC7A8"),
        orbB = Color.parseColor("#33FFE0B8"),
        orbC = Color.parseColor("#4DFFD9C4")
    )

    val ALL = listOf(MINT, PEACH, FOREST, OCEAN, SUNSET)

    /** Returns palette by id, falls back to MINT for unknown ids. */
    fun byId(id: String?): PetPalette = ALL.firstOrNull { it.id == id } ?: MINT
}