package eng.nookipet.lab7.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import eng.nookipet.lab7.AppPreferences
import eng.nookipet.lab7.R
import kotlinx.coroutines.launch
import eng.nookipet.lab7.ui.theme.applyPetPalette

/**
 * Settings screen.
 *   - user name (greeting on Home updates instantly)
 *   - dark theme toggle (applied via AppCompatDelegate)
 *   - test notification button (WorkManager demo)
 *   - about section
 */
class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

    private lateinit var etName: TextInputEditText
    private lateinit var switchTheme: MaterialSwitch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyPetPalette(view)

        val btnBack: ImageButton = view.findViewById(R.id.btnBack)
        val btnSaveName: MaterialButton = view.findViewById(R.id.btnSaveName)
        val btnTest: MaterialButton = view.findViewById(R.id.btnTestNotification)

        etName = view.findViewById(R.id.etUserName)
        switchTheme = view.findViewById(R.id.switchDarkTheme)

        // Pre-fill name from SharedPreferences
        etName.setText(AppPreferences.getUserName(requireContext()))

        // Pre-fill theme state
        switchTheme.isChecked = viewModel.isDarkTheme()

        // Reactively update the field if name changes from elsewhere
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userName.collect { name ->
                    // Only update if the field is not currently being edited,
                    // to avoid caret jumps while typing.
                    if (etName.text?.toString() != name && !etName.hasFocus()) {
                        etName.setText(name)
                    }
                }
            }
        }

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnSaveName.setOnClickListener {
            val name = etName.text?.toString()?.trim().orEmpty()
            viewModel.setUserName(name)
            Toast.makeText(
                requireContext(),
                if (name.isBlank()) "Name cleared" else "Saved: $name",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Theme toggle → save + apply globally
        switchTheme.setOnCheckedChangeListener { _, checked ->
            viewModel.setDarkTheme(checked)
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        btnTest.setOnClickListener {
            viewModel.sendTestNotification {
                Toast.makeText(
                    requireContext(),
                    "Notification in 5 seconds",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}