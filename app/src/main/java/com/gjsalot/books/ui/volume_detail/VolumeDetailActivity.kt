package com.gjsalot.books.ui.volume_detail

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.gjsalot.books.R
import com.gjsalot.books.app.Application
import com.gjsalot.books.utils.visible
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_volume_detail.*
import android.content.Intent
import android.net.Uri
import android.support.design.widget.Snackbar

class VolumeDetailActivity : AppCompatActivity() {

    companion object {
        const val ARG_VOLUME_ID = "volume_id"
    }

    private val viewModel: VolumeDetailViewModel by lazy {
        val volumeId = intent.getStringExtra(ARG_VOLUME_ID)
        val factory = VolumeDetailViewModel.Factory(volumeId, application as Application)
        ViewModelProviders.of(this, factory).get(VolumeDetailViewModel::class.java)
    }

    private val errorLoadingSnackbar by lazy {
        val snackback = Snackbar.make(scrollView, R.string.error_loading_detail, Snackbar.LENGTH_INDEFINITE)
        snackback.setAction(R.string.retry) {
            viewModel.onRetryClicked()
            snackback.dismiss()
        }
        snackback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volume_detail)

        setupToolbar()
        setupListeners()
        setupObservers()
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    private fun setupListeners() {
        buyButton.setOnClickListener {
            viewModel.onBuyOrRentClicked()
        }
    }

    private fun setupObservers() {
        viewModel.viewState().observe(this, Observer {
            it?.let {
                if (it.thumbnailUrl.isNullOrBlank())
                    thumbnail.setImageResource(0)
                else
                    Picasso
                            .with(this)
                            .load(it.thumbnailUrl)
                            .into(thumbnail)

                volumeTitle.text = it.title
                authors.text = it.authors
                publishedDetails.text = it.publishDetails
                pages.text = it.pages
                description.text = it.description

                if (it.averageRating != null && it.numRatings != null && it.numRatings > 0) {
                    rating.visible = true
                    rating.rating = it.averageRating
                    ratingDescription.visible = true
                    ratingDescription.text = getString(R.string.rating_description, it.numRatings.toString())
                } else {
                    rating.visible = false
                    ratingDescription.visible = false
                }

                buyButton.visible = it.isBuyVisible

                // Hide loading, show content
                progressBar.visible = false
                scrollView.visible = true
            }
        })

        viewModel.event().observe(this, Observer {
            when (it) {

                is VolumeDetailViewModel.Event.LaunchWebBrowser -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.url))
                    startActivity(intent)
                }

                is VolumeDetailViewModel.Event.ShowErrorLoading -> {
                    scrollView.postDelayed( {
                        errorLoadingSnackbar.show()
                    }, 2000)
                }

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()

            return true
        }

        return super.onOptionsItemSelected(item)
    }

}
