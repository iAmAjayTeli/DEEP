package com.example.deep.view.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.deep.R
import com.example.deep.adapter.ManagePledgeAdapter
import com.example.deep.databinding.ActivityManagePledgeBinding

class ManagePledge : AppCompatActivity() {
    private lateinit var binding: ActivityManagePledgeBinding
    private lateinit var searchView: SearchView
    private  lateinit var adapter: ManagePledgeAdapter
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_pledge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor = resources.getColor(R.color.action_color, null)
            insets
        }

        searchView=binding.searchBar


        setupClickListener()

    }

    private fun setupRecyclerview(){
        recyclerView=binding.pledgeRecyclerview
        adapter=ManagePledgeAdapter{pledge->
            val intent= Intent(this@ManagePledge, ManagePledgeDetail::class.java)
//            intent.putExtra("pledge",pledge)
        }
    }

    private fun setupClickListener() {
        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }



//            search.setOnClickListener {
//                if (searchView.visibility == View.GONE) {
//                    searchView.visibility = View.VISIBLE
//                    searchView.requestFocus()
//                    setUpSearchView()
//
//                    val inputMethodManager =
//                        binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                    inputMethodManager.showSoftInput(searchView, InputMethodManager.SHOW_FORCED)
//                } else {
//                    searchView.visibility = View.GONE
//                    search.visibility = View.VISIBLE
//
//                    val inputMethodManager =
//                        binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                    inputMethodManager.hideSoftInputFromWindow(searchView.windowToken, 0)
//                }
//            }

        }
    }


//    private fun setUpSearchView() {
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                filterCategories(query.orEmpty())
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                filterCategories(newText.orEmpty())
//                return true
//            }
//        })
//    }

//    private fun filterCategories(query: String) {
//        val lowerCaseQuery = query.lowercase()
//
//        for (i in cardViewList.indices) {
//            val categoryText = categoryTextList[i].text.toString().lowercase()
//
//            if (categoryText.contains(lowerCaseQuery)) {
//                cardViewList[i].visibility = View.VISIBLE
//            } else {
//                cardViewList[i].visibility = View.GONE
//            }
//        }
//    }
}