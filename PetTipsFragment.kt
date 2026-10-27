package eng.nookipet.lab7.ui.tips

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eng.nookipet.lab7.R
import kotlinx.coroutines.launch

/**
 * Pet care tips loaded via Retrofit.
 *   - Loading → ProgressBar
 *   - Success → RecyclerView with tips
 *   - Error   → message + Retry button
 */
class PetTipsFragment : Fragment() {

    private val viewModel: PetTipsViewModel by viewModels()

    private lateinit var adapter: PetTipAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var errorContainer: LinearLayout
    private lateinit var tvError: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_post_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv: RecyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        errorContainer = view.findViewById(R.id.errorContainer)
        tvError = view.findViewById(R.id.tvError)
        val btnRetry: Button = view.findViewById(R.id.btnRetry)

        adapter = PetTipAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnRetry.setOnClickListener { viewModel.loadTips() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is TipsUiState.Loading -> showLoading()
                        is TipsUiState.Success -> {
                            adapter.submitList(state.tips)
                            showContent()
                        }
                        is TipsUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        errorContainer.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        errorContainer.visibility = View.VISIBLE
        tvError.text = message
    }
}