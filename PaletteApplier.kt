package eng.nookipet.lab7.ui.theme

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RadioButton
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch

/**
 * Recursively repaints a view tree with the active pet's palette.
 *
 * What it recolors:
 *   - FloatingActionButton  → background
 *   - MaterialButton        → background (only "filled" style; outlined/text skipped)
 *   - ProgressBar           → indeterminate tint
 *   - MaterialSwitch        → thumb + track
 *   - Chip (Filter)         → checked = filled primary, unchecked = outlined primary
 *   - RadioButton           → button tint
 *
 * Called from every Fragment that owns a view tree — see PaletteExtensions.
 */
object PaletteApplier {

    private const val SWITCH_OFF_THUMB = 0xFFE0E0E0.toInt()
    private const val SWITCH_OFF_TRACK = 0xFFBDBDBD.toInt()

    fun applyToTree(root: View, palette: PetPalette) {
        when (root) {
            is FloatingActionButton -> {
                root.backgroundTintList = ColorStateList.valueOf(palette.primary)
            }

            is com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton -> {
                root.backgroundTintList = ColorStateList.valueOf(palette.primary)
            }

            is MaterialButton -> {
                // Only recolor "filled" buttons. Outlined/Text buttons
                // have a TRANSPARENT backgroundTint — leave them alone.
                val bg = root.backgroundTintList
                if (bg != null && bg.defaultColor != Color.TRANSPARENT) {
                    root.backgroundTintList = ColorStateList.valueOf(palette.primary)
                }
            }

            is ProgressBar -> {
                root.indeterminateTintList = ColorStateList.valueOf(palette.primary)
            }

            is MaterialSwitch -> {
                val checked = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf()
                )
                root.thumbTintList = ColorStateList(
                    checked,
                    intArrayOf(palette.primary, SWITCH_OFF_THUMB)
                )
                root.trackTintList = ColorStateList(
                    checked,
                    intArrayOf(palette.primaryLight, SWITCH_OFF_TRACK)
                )
            }

            is Chip -> {
                val checked = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf()
                )
                root.chipBackgroundColor = ColorStateList(
                    checked,
                    intArrayOf(palette.primary, Color.TRANSPARENT)
                )
                root.chipStrokeColor = ColorStateList(
                    checked,
                    intArrayOf(palette.primary, palette.primary)
                )
                root.setTextColor(
                    ColorStateList(
                        checked,
                        intArrayOf(Color.WHITE, palette.primary)
                    )
                )
            }

            is RadioButton -> {
                root.buttonTintList = ColorStateList.valueOf(palette.primary)
            }
        }

        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                applyToTree(root.getChildAt(i), palette)
            }
        }
    }
}