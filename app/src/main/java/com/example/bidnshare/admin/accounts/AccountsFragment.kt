package com.example.bidnshare.admin.accounts

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bidnshare.R
import com.example.bidnshare.databinding.FragmentAccountsBinding
import com.example.bidnshare.user.homeitemtabs.EndingSoonFragment
import com.example.bidnshare.user.homeitemtabs.NewlyListedFragment
import com.example.bidnshare.user.homeitemtabs.UpcomingFragment
import com.google.android.material.tabs.TabLayout

class AccountsFragment : Fragment() {
    private lateinit var binding: FragmentAccountsBinding
    private lateinit var tabLayout: TabLayout
    private var selectedTabIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountsBinding.inflate(layoutInflater)
        tabLayout = binding.tbLayout
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            selectedTabIndex = it.getInt("selectedTabIndex", 0)
        }

        showFragment(selectedTabIndex)
        addTabs()
        binding.tbLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    showFragment(it.position)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedTabIndex", selectedTabIndex)
    }

    private fun showFragment(position: Int) {
        val fragment = when (position) {
            0 -> AccountsListsFragment()
            1 -> AccountRequestsFragment()
            else -> AccountsListsFragment() // Default to AccountsListsFragment
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.viewPager1, fragment)
            .commit()

        selectedTabIndex = position
    }

    private fun addTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Accounts"))
        tabLayout.addTab(tabLayout.newTab().setText("Requests"))
    }
}


