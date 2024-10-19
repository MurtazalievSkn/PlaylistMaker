package com.example.playlistmaker.presentation.ui.search.fragment

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.domain.search.model.SearchStatus
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.domain.search.model.TrackSearchResult
import com.example.playlistmaker.presentation.isNightModeOn
import com.example.playlistmaker.presentation.ui.BindingFragment
import com.example.playlistmaker.presentation.ui.main.MainActivity
import com.example.playlistmaker.presentation.ui.search.common.TrackListAdapter
import com.example.playlistmaker.presentation.ui.search.view_model.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class SearchFragment : BindingFragment<FragmentSearchBinding>() {
    private var editTextValue = ""
    private lateinit var searchAdapter: TrackListAdapter
    private lateinit var historyAdapter: TrackListAdapter
    private lateinit var tracks: ArrayList<Track>

    private val viewModel by viewModel<SearchViewModel>()

    private val onClick: (track: Track) -> Unit = {
        if (viewModel.clickDebounce()) {
            viewModel.addTrackInSearchHistory(it)
            historyAdapter.notifyDataSetChanged()
            startPlayerActivity(it)
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tracks = ArrayList<Track>()
        searchAdapter = TrackListAdapter(tracks, onClick)
        historyAdapter = TrackListAdapter(viewModel.tracksHistory().value!!, onClick)

        viewModel.foundTracks().observe(viewLifecycleOwner) { it ->
            processingSearchStatus(it)
        }

        viewModel.tracksHistory().observe(viewLifecycleOwner) { it ->
            historyAdapter.notifyDataSetChanged()
        }

        binding.clearButton.setOnClickListener {
            binding.searchEditText.setText("")
            viewModel.deleteFoundTracks()
            hideKeyboard()
            changeStateWhenSearchBarIsEmpty()
        }

        binding.buttonUpdate.setOnClickListener {
            viewModel.changeRequestText(binding.searchEditText.text.toString())
            viewModel.searchDebounce()
            hideErrorElements()
        }

        binding.clearHistoryButton.setOnClickListener {   //кнопка очистить историю поиска
            showAndHideHistoryLayout(false)
            viewModel.cleanHistory()
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearButton.isVisible = !s.isNullOrEmpty()
                binding.historyLayout.isVisible =
                    binding.searchEditText.hasFocus()
                            && s?.isEmpty() == true
                            && viewModel.tracksHistory().value!!.isNotEmpty()
//

                editTextValue = binding.searchEditText.text.toString()
                if (editTextValue.isEmpty()) {
                    changeStateWhenSearchBarIsEmpty()
                    viewModel.removeCallbacks()
                } else {
                    viewModel.changeRequestText(binding.searchEditText.text.toString())
                    viewModel.searchDebounce()
                    hideErrorElements()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        binding.searchEditText.addTextChangedListener(simpleTextWatcher)

        binding.rvTracks.adapter = searchAdapter
        binding.rvSearchHistory.adapter = historyAdapter
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus
                && binding.searchEditText.text.isEmpty()
                && viewModel.tracksHistory().value!!.isNotEmpty()
            ) {
                updateRecyclerViewSearchHistory()
            } else {
                showAndHideHistoryLayout(false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showNavBar()
        viewModel.updateTrackHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeCallbacks()
    }

    private fun startPlayerActivity(track: Track) {

        (activity as? MainActivity)?.hideNavBar()

        val bundle = Bundle()
        bundle.putParcelable(TRACK_KEY, track)
        findNavController().navigate(R.id.action_searchFragment_to_playerFragment, bundle)
    }

    private fun updateRecyclerViewSearchHistory() {
        showAndHideHistoryLayout(true)
        viewModel.updateTrackHistory()
        historyAdapter.notifyDataSetChanged()
    }


    private fun showImageError(typeError: SearchStatus) {
        if (typeError == SearchStatus.LIST_IS_EMPTY) {
            showNotFoundError()
        } else {
            showNetworkError()
            binding.buttonUpdate.isVisible = true
        }
        binding.messageImage.isVisible = true
        binding.tvMessageError.isVisible = true
    }

    private fun showNotFoundError() {
        binding.tvMessageError.text = getString(R.string.not_found_error)
        if (requireContext().isNightModeOn()) {
            Glide.with(this)
                .load(R.drawable.not_found_dark)
                .into(binding.messageImage)
        } else {
            Glide.with(this)
                .load(R.drawable.not_found_light)
                .into(binding.messageImage)
        }
    }

    private fun showNetworkError() {
        binding.tvMessageError.text = getString(R.string.network_error)
        if (requireContext().isNightModeOn()) {
            Glide.with(this)
                .load(R.drawable.network_problem_dark)
                .into(binding.messageImage)
        } else {
            Glide.with(this)
                .load(R.drawable.network_problem_light)
                .into(binding.messageImage)
        }
    }

    private fun hideErrorElements() {
        binding.messageImage.isVisible = false
        binding.tvMessageError.isVisible = false
        binding.buttonUpdate.isVisible = false
    }

    private fun hideRecyclerView() {
        binding.rvTracks.isVisible = false
    }

    private fun changeStateWhenSearchBarIsEmpty() {
        hideErrorElements()
        hideRecyclerView()
        updateRecyclerViewSearchHistory()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun showAndHideHistoryLayout(action: Boolean) {
        if (action && viewModel.tracksHistory().value!!.isNotEmpty() && binding.searchEditText.hasFocus()) {
            binding.historyLayout.isVisible = true
        } else {
            binding.historyLayout.isVisible = false
        }
    }

    private fun processingSearchStatus(trackSearchResult: TrackSearchResult) {
        tracks.clear()
        hideRecyclerView()
        hideErrorElements()
        when (trackSearchResult.status) {
            SearchStatus.DEFAULT -> {
                binding.progressBar.isVisible = false
            }

            SearchStatus.RESPONSE_RECEIVED -> {
                binding.progressBar.isVisible = false
                binding.rvTracks.isVisible = true
                tracks.addAll(trackSearchResult.results)
                searchAdapter.notifyDataSetChanged()
            }

            SearchStatus.LIST_IS_EMPTY -> {
                binding.progressBar.isVisible = false
                showImageError(SearchStatus.LIST_IS_EMPTY)
            }

            SearchStatus.NETWORK_ERROR -> {
                binding.progressBar.isVisible = false
                showImageError(SearchStatus.NETWORK_ERROR)
            }

            SearchStatus.LOADING -> {
                binding.progressBar.isVisible = true
            }
        }
    }

    companion object {
        private const val TRACK_KEY = "track"
    }
}