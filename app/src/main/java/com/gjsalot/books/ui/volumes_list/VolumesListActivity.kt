package com.gjsalot.books.ui.volumes_list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.inputmethod.EditorInfo
import com.gjsalot.books.app.Application
import com.gjsalot.books.utils.SimpleTextWatcher
import kotlinx.android.synthetic.main.activity_volumes_list.*
import android.view.inputmethod.InputMethodManager
import com.gjsalot.books.R
import com.gjsalot.books.ui.volume_detail.VolumeDetailActivity

class VolumesListActivity : AppCompatActivity() {

    private val viewModel: VolumesListViewModel by lazy {
        val factory = VolumesListViewModel.Factory(application as Application)
        ViewModelProviders.of(this, factory).get(VolumesListViewModel::class.java)
    }

    private val adapter: VolumesListAdapter by lazy {
        VolumesListAdapter(viewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volumes_list)

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        // Hide keyboard when Search/Return clicked
        search.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                search.clearFocus()

                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        search.addTextChangedListener(SimpleTextWatcher {
            viewModel.onSearchQueryChanged(it)
        })
    }

    private fun setupObservers() {
        viewModel.viewState().observe(this, Observer {
            it?.let {
                adapter.updateVolumes(it.volumes, it.diffResult)
            }
        })

        viewModel.event().observe(this, Observer {
            when (it) {

                is VolumesListViewModel.Event.ShowVolumeDetail -> {
                    val intent = Intent(this, VolumeDetailActivity::class.java)
                    intent.putExtra(VolumeDetailActivity.ARG_VOLUME_ID, it.volumeId)
                    startActivity(intent)
                }

            }
        })
    }

}
