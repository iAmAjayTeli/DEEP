package com.example.deep.handler

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deep.R
import com.example.deep.adapter.DrawerAdapter
import com.example.deep.databinding.NavHeaderBinding
import com.example.deep.model.DrawerItem
import com.example.deep.view.admin.AdminLogin
import com.example.deep.view.admin.AdminSetting
import com.example.deep.view.admin.ManageHospital
import com.example.deep.view.admin.ManagePledge
import com.example.deep.view.admin.ManageSuccessStories
import com.example.deep.view.user.AboutUs
import com.example.deep.view.user.EyeBankScreen
import com.example.deep.view.user.HelpSupport
import com.example.deep.view.user.HospitalRegisteration
import com.example.deep.view.user.PledgeScreen
import com.example.deep.view.user.SuccessStories

class
NavigationDrawerSetup(
    private val isAdmin: Boolean,
    private val context: Context,
    private val drawerLayout: DrawerLayout,
    private val recyclerView: RecyclerView,
) {
    fun setupDrawer() {
        val headerView = drawerLayout.findViewById<View>(R.id.navHeader)
        val navBinding: NavHeaderBinding = DataBindingUtil.bind(headerView)!!

        val drawerItems = getDrawerItems()
        val drawerAdapter = DrawerAdapter(drawerItems) { item ->
            handleDrawerItemClick(item.title)
            drawerLayout.closeDrawers()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = drawerAdapter
    }

    private fun getDrawerItems(): List<DrawerItem> {
        return commonDrawerItem()
    }

    private fun commonDrawerItem(): List<DrawerItem> {

        val list: List<DrawerItem>

        if (!isAdmin) {
            list = listOf(
                DrawerItem(R.drawable.eye_bank, "Eye Bank"),
                DrawerItem(R.drawable.eye_pledge, "Pledge"),
                DrawerItem(R.drawable.terms_condition, "Help & Support"),
                DrawerItem(R.drawable.about_us, "About Us"),
                DrawerItem(R.drawable.success, "Success Stories"),
                DrawerItem(R.drawable.hospital, "Hospital Registration"),
                DrawerItem(R.drawable.login, "Admin Login")


            )
        } else {
            list = listOf(
                DrawerItem(R.drawable.eye_pledge, "Manage Pledge"),
                DrawerItem(R.drawable.success, "Add Success Stories"),
                DrawerItem(R.drawable.hospital, "Manage Hospital"),
                DrawerItem(R.drawable.setting, "Setting")
            )
        }
        return list
    }

    private fun handleDrawerItemClick(title: String) {
        when (title) {

            "Eye Bank" -> {
                navigateTo(EyeBankScreen())
            }

            "Pledge" -> {
                navigateTo(PledgeScreen())
            }

            "Help & Support" -> {
                navigateTo(HelpSupport())
            }

            "About Us" -> {
                navigateTo(AboutUs())
            }

            "Success Stories" -> {
                navigateTo(SuccessStories())
            }

            "Hospital Registration" -> {
                navigateTo(HospitalRegisteration())
            }

            "Admin Login" -> {
                navigateTo(AdminLogin())
            }

            "Manage Pledge" -> {
                navigateTo(ManagePledge())
            }

            "Add Success Stories" -> {
                navigateTo(ManageSuccessStories())
            }

            "Manage Hospital" -> {
                navigateTo(ManageHospital())
            }

            "Setting" -> {
                navigateTo(AdminSetting())
            }

        }
    }

    private fun navigateTo(activity: Activity) {
        context.startActivity(Intent(context, activity::class.java))
    }
}