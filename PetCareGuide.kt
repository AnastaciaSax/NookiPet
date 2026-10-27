package eng.nookipet.lab7.data.content

/**
 * Real, actionable care advice for each pet type.
 * Not lorem ipsum — these are practical tips a pet owner can use.
 *
 * The guide is shown on PetProfileFragment (Care guide section).
 * If a pet's species isn't in the catalog, the fragment shows a friendly
 * "No specific guide yet" message.
 */
data class CareTip(
    val category: TipCategory,
    val title: String,
    val body: String
)

enum class TipCategory(val emoji: String, val label: String) {
    FEEDING("🍽", "Feeding"),
    HABITAT("🏠", "Habitat"),
    HEALTH("🩺", "Health"),
    SAFETY("⚠️", "Safety")
}

data class CareGuide(
    val speciesKey: String,   // matches Pet.species or Pet.breed, lowercase
    val displayName: String,
    val emoji: String,
    val tips: List<CareTip>
)

object PetCareGuide {

    // ──────────────────────────────────────────────────────────────
    // SNAILS (Achatina)
    // ──────────────────────────────────────────────────────────────
    private val SNAIL = CareGuide(
        speciesKey = "snail",
        displayName = "Snail (Achatina)",
        emoji = "🐌",
        tips = listOf(
            CareTip(
                TipCategory.FEEDING,
                "Safe vegetables",
                "Cucumber, zucchini, lettuce, pumpkin, carrot, sweet potato. " +
                        "Wash thoroughly and remove leftovers after 24h to prevent mold."
            ),
            CareTip(
                TipCategory.FEEDING,
                "Fruit (in moderation)",
                "Apple, pear, banana, melon. Give 2–3 times a week — too much sugar " +
                        "causes obesity and shortens lifespan."
            ),
            CareTip(
                TipCategory.FEEDING,
                "Calcium is critical",
                "Offer cuttlebone or powdered eggshell at all times. Without calcium " +
                        "the shell becomes thin, cracks, and the snail stops growing."
            ),
            CareTip(
                TipCategory.FEEDING,
                "NEVER feed",
                "Citrus fruits, onion, garlic, salty or spicy food, pasta, bread. " +
                        "These are toxic and can kill the snail within hours."
            ),
            CareTip(
                TipCategory.HABITAT,
                "Humidity 70–90%",
                "Mist the terrarium twice a day with room-temperature water. Low humidity " +
                        "triggers hibernation — the snail seals itself in its shell and may not wake up."
            ),
            CareTip(
                TipCategory.HABITAT,
                "Temperature 22–28°C",
                "Room temperature is fine. Avoid direct sunlight and heat sources — " +
                        "above 30°C the snail can dry out and die."
            ),
            CareTip(
                TipCategory.HABITAT,
                "Substrate 5–7 cm deep",
                "Coconut coir is ideal. Change once a month, spot-clean weekly. " +
                        "Never use sand or gravel — it damages the shell."
            ),
            CareTip(
                TipCategory.HEALTH,
                "Shell cracks",
                "Small cracks heal with enough calcium. Large breaks need isolation " +
                        "in a clean container with high humidity until the shell hardens."
            ),
            CareTip(
                TipCategory.SAFETY,
                "Wash your hands",
                "Snails can carry bacteria. Wash hands before and after handling, " +
                        "especially before eating."
            )
        )
    )

    // ──────────────────────────────────────────────────────────────
    // CATS
    // ──────────────────────────────────────────────────────────────
    private val CAT = CareGuide(
        speciesKey = "cat",
        displayName = "Cat",
        emoji = "🐱",
        tips = listOf(
            CareTip(
                TipCategory.FEEDING,
                "Taurine is essential",
                "Cats cannot synthesize taurine — it must come from meat. " +
                        "Deficiency leads to blindness and heart disease."
            ),
            CareTip(
                TipCategory.FEEDING,
                "NEVER feed",
                "Onion, garlic, chocolate, grapes, raisins, avocado, alcohol, " +
                        "raw yeast dough. All are toxic — some are fatal even in tiny amounts."
            ),
            CareTip(
                TipCategory.FEEDING,
                "Water matters",
                "Cats drink little by nature. Wet food or a running fountain " +
                        "reduces the risk of kidney disease."
            ),
            CareTip(
                TipCategory.HEALTH,
                "Annual vaccination",
                "FVRCP + rabies every 1–3 years depending on lifestyle. " +
                        "Even indoor cats need core vaccines."
            ),
            CareTip(
                TipCategory.HEALTH,
                "Spay/neuter at 6–8 months",
                "Reduces cancer risk and prevents unwanted litters. " +
                        "Ask your vet for the right timing for your cat."
            ),
            CareTip(
                TipCategory.SAFETY,
                "Toxic houseplants",
                "Lilies, dieffenbachia, philodendron, poinsettia. Even a few " +
                        "lily petals can cause acute kidney failure."
            ),
            CareTip(
                TipCategory.SAFETY,
                "Window safety",
                "Install screens on all windows. Fall from 3rd floor+ causes " +
                        "high-rise syndrome — fractures and internal injuries."
            )
        )
    )

    // ──────────────────────────────────────────────────────────────
    // DOGS
    // ──────────────────────────────────────────────────────────────
    private val DOG = CareGuide(
        speciesKey = "dog",
        displayName = "Dog",
        emoji = "🐶",
        tips = listOf(
            CareTip(
                TipCategory.FEEDING,
                "Portion by weight",
                "Adult dogs need 20–30 g of dry food per kg of body weight per day, " +
                        "split into 2 meals. Puppies eat 3–4 times a day."
            ),
            CareTip(
                TipCategory.FEEDING,
                "NEVER feed",
                "Chocolate, grapes, raisins, xylitol (sugar-free gum), onion, garlic, " +
                        "macadamia nuts, alcohol. Xylitol is fatal at 0.1 g/kg."
            ),
            CareTip(
                TipCategory.HEALTH,
                "Deworming every 3–6 months",
                "Even indoor dogs. Also monthly flea/tick prevention in warm seasons."
            ),
            CareTip(
                TipCategory.HEALTH,
                "Watch for these signs",
                "Excessive drinking, sudden weight loss, limping, coughing at night. " +
                        "Any of these needs a vet visit within 48 hours."
            ),
            CareTip(
                TipCategory.SAFETY,
                "Never leave in a car",
                "In +25°C heat the interior reaches +50°C in 10 minutes. " +
                        "Heatstroke kills within 20 minutes."
            ),
            CareTip(
                TipCategory.SAFETY,
                "Microchip + tag",
                "Legally required in many countries. Increases return rate after " +
                        "escape from 20% to over 90%."
            )
        )
    )

    // ──────────────────────────────────────────────────────────────
    // REPTILES (lizards, geckos, snakes)
    // ──────────────────────────────────────────────────────────────
    private val REPTILE = CareGuide(
        speciesKey = "reptile",
        displayName = "Reptile",
        emoji = "🦎",
        tips = listOf(
            CareTip(
                TipCategory.HABITAT,
                "UVB lamp is not optional",
                "Most reptiles need UVB light 10–12 h/day to synthesize vitamin D3. " +
                        "Replace the bulb every 6 months — UVB fades even if the light still works."
            ),
            CareTip(
                TipCategory.HABITAT,
                "Temperature gradient",
                "Provide a warm basking spot (30–38°C) and a cooler side (22–25°C). " +
                        "Reptiles regulate body temperature by moving between zones."
            ),
            CareTip(
                TipCategory.FEEDING,
                "Calcium + D3 supplement",
                "Dust feeder insects with calcium powder 2–3 times a week. " +
                        "For juveniles — every feeding."
            ),
            CareTip(
                TipCategory.HEALTH,
                "Metabolic Bone Disease (MBD)",
                "Signs: soft jaw, curved spine, tremors. Caused by calcium/UVB deficiency. " +
                        "Reversible only in early stages — see a vet immediately."
            ),
            CareTip(
                TipCategory.HEALTH,
                "Shedding problems",
                "Stuck shed on toes or tail tip cuts off circulation. " +
                        "Soak in warm water 15 min, help gently if needed."
            ),
            CareTip(
                TipCategory.SAFETY,
                "Wash hands after handling",
                "Reptiles can carry Salmonella. Always wash hands after contact, " +
                        "and never let them near food preparation areas."
            )
        )
    )

    // ──────────────────────────────────────────────────────────────
    // BIRDS
    // ──────────────────────────────────────────────────────────────
    private val BIRD = CareGuide(
        speciesKey = "bird",
        displayName = "Bird",
        emoji = "🐦",
        tips = listOf(
            CareTip(
                TipCategory.FEEDING,
                "Not just seeds",
                "A seed-only diet causes fatty liver disease. Base: pellets (60–70%), " +
                        "fresh vegetables (20–30%), fruit (10%), seeds as treats."
            ),
            CareTip(
                TipCategory.FEEDING,
                "Safe vegetables",
                "Carrot, broccoli, spinach, bell pepper, zucchini. " +
                        "Avoid: avocado, chocolate, caffeine, alcohol, salty snacks."
            ),
            CareTip(
                TipCategory.SAFETY,
                "Teflon fumes are deadly",
                "Overheated non-stick pans release fumes that kill birds within minutes. " +
                        "Also avoid aerosol sprays, scented candles, air fresheners near the cage."
            ),
            CareTip(
                TipCategory.HABITAT,
                "Sleep 10–12 hours",
                "Cover the cage or move it to a dark room at night. " +
                        "Chronic sleep deprivation causes hormonal and behavioral problems."
            ),
            CareTip(
                TipCategory.HEALTH,
                "Watch the droppings",
                "Change in color, consistency, or amount is often the first sign " +
                        "of illness. Birds hide symptoms until it's serious."
            )
        )
    )

    // ──────────────────────────────────────────────────────────────
    // FISH / AQUARIUM
    // ──────────────────────────────────────────────────────────────
    private val FISH = CareGuide(
        speciesKey = "fish",
        displayName = "Fish",
        emoji = "🐠",
        tips = listOf(
            CareTip(
                TipCategory.HABITAT,
                "Cycle the tank first",
                "New tank needs 4–6 weeks to build beneficial bacteria. " +
                        "Adding fish immediately causes ammonia spikes that kill them."
            ),
            CareTip(
                TipCategory.HABITAT,
                "Volume matters",
                "Minimum tank size depends on species. Goldfish need 40+ liters each, " +
                        "a betta needs 10+ liters. Bowls are not suitable for any fish."
            ),
            CareTip(
                TipCategory.FEEDING,
                "Feed what they eat in 2–3 min",
                "Overfeeding is the #1 killer — leftover food rots and spikes ammonia. " +
                        "Feed 1–2 times a day, skip one day per week."
            ),
            CareTip(
                TipCategory.HEALTH,
                "Water changes weekly",
                "Change 20–30% weekly. Test pH, ammonia, nitrite, nitrate. " +
                        "Any ammonia above 0 is dangerous."
            ),
            CareTip(
                TipCategory.HEALTH,
                "Signs of illness",
                "Clamped fins, gasping at surface, white spots, hiding constantly. " +
                        "Quarantine sick fish immediately to protect the rest."
            )
        )
    )

    private val ALL = listOf(SNAIL, CAT, DOG, REPTILE, BIRD, FISH)

    /**
     * Finds a guide by matching speciesKey against:
     *   1. Pet.species (lowercased, e.g. "achatina fulica" contains "snail"? no — species field is "Achatina fulica")
     *   2. Pet.breed (lowercased, e.g. "snail", "cat", "dog")
     * If nothing matches, returns null and the fragment shows a fallback message.
     */
    fun forPet(species: String, breed: String): CareGuide? {
        val haystack = "$species $breed".lowercase()
        return ALL.firstOrNull { guide ->
            guide.speciesKey in haystack ||
                    when (guide.speciesKey) {
                        "snail"   -> haystack.contains("achatina") || haystack.contains("улитк")
                        "cat"     -> haystack.contains("feline") || haystack.contains("кошк")
                        "dog"     -> haystack.contains("canine") || haystack.contains("собак")
                        "reptile" -> haystack.contains("lizard") || haystack.contains("gecko") ||
                                haystack.contains("snake") || haystack.contains("turtle")
                        "bird"    -> haystack.contains("parrot") || haystack.contains("canary") ||
                                haystack.contains("попугай")
                        "fish"    -> haystack.contains("aquarium") || haystack.contains("рыб")
                        else -> false
                    }
        }
    }

    fun allGuides(): List<CareGuide> = ALL
}