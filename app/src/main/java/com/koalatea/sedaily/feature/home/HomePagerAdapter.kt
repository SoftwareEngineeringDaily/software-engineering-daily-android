package com.koalatea.sedaily.feature.home

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.koalatea.sedaily.R
import com.koalatea.sedaily.feature.episodes.EpisodesFragment

private val TAB_TITLES_STRING_RES = arrayOf(
    R.string.home_title,
    R.string.home_title
)

class HomePagerAdapter(private val context: Context, fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return EpisodesFragment()//.newInstance(category)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES_STRING_RES[position])
    }

    override fun getCount(): Int {
        return 2
    }
}