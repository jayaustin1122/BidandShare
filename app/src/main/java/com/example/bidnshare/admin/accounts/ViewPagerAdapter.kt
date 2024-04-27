package com.example.bidnshare.admin.accounts

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter


class ViewPagerAdapter(fm : FragmentManager) : FragmentStatePagerAdapter(fm){
    private val mFrgmentList = ArrayList<Fragment>()
    private val mFrgmentTitleList = ArrayList<String>()
    override fun getCount() = mFrgmentList.size
    override fun getItem(position: Int) = mFrgmentList[position]
    override fun getPageTitle(position: Int) = mFrgmentTitleList[position]
    fun addFragment(fragment:Fragment,title:String){
        mFrgmentList.add(fragment)
        mFrgmentTitleList.add(title)
    }
    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        try {
            super.restoreState(state, loader)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}