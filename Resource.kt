package eng.nookipet.lab7.data.common

/**
 * Generic wrapper for async data with three states.
 * Used by ViewModels to expose loading / success / error to the UI.
 *
 * Example:
 *   viewModel.state.collect { r ->
 *       when (r) {
 *           is Resource.Loading -> showLoading()
 *           is Resource.Success -> showContent(r.data)
 *           is Resource.Error -> showError(r.message)
 *       }
 *   }
 */
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}