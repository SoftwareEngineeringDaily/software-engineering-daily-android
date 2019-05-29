package com.koalatea.sedaily.feature.home

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.koalatea.sedaily.R
import com.koalatea.sedaily.feature.episodes.EpisodesFragment

class HomePagerAdapter(
        private val context: Context,
        fragmentManager: FragmentManager
) : FragmentStatePagerAdapter(fragmentManager) {

    private enum class Categories(val categoryId: String, @StringRes val nameStringRes: Int) {
        All("-1", R.string.tab_all),
        BusinessAndPhilosophy("1068", R.string.tab_businessAndPhilosophy),
        Blockchain("1082", R.string.tab_blockchain),
        CloudEngineering("1079", R.string.tab_cloudEngineering),
        Data("1081", R.string.tab_data),
        JavaScript("1084", R.string.tab_javaScript),
        MachineLearning("1080", R.string.tab_machineLearning),
        OpenSource("1078", R.string.tab_openSource),
        Security("1083", R.string.tab_security),
        Hackers("1085", R.string.tab_hackers),
        GreatestHits("1069", R.string.tab_greatestHits);
    }

    override fun getItem(position: Int): Fragment {
        return EpisodesFragment.newInstance(Categories.values()[position].categoryId)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(Categories.values()[position].nameStringRes)
    }

    override fun getCount(): Int {
        return Categories.values().size
    }
}