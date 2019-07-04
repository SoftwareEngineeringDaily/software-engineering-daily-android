package com.koalatea.sedaily.feature.home

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import com.koalatea.sedaily.MainActivity
import com.koalatea.sedaily.R
import com.koalatea.sedaily.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager.adapter = HomePagerAdapter(view.context, childFragmentManager)
        tabs.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)

        context?.let { context ->
            val searchView = menu.findItem(R.id.search).actionView as? SearchView
            val searchManager = context.getSystemService(Context.SEARCH_SERVICE) as? SearchManager
            val componentName = ComponentName(context, MainActivity::class.java)

            searchView?.setSearchableInfo(searchManager?.getSearchableInfo(componentName))
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

}