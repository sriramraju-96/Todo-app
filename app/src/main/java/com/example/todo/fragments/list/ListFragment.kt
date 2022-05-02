package com.example.todo.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.todo.R
import com.example.todo.data.models.ToDoData
import com.example.todo.data.viewmodel.ToDoViewModel
import com.example.todo.databinding.FragmentListBinding
import com.example.todo.fragments.SharedViewModel
import com.example.todo.fragments.list.adapter.ListAdapter
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.fragment_list.view.*

class ListFragment : Fragment() , SearchView.OnQueryTextListener{

    private var _binding : FragmentListBinding? = null
    private val binding get() = _binding!!

    private val adapter : ListAdapter by lazy { ListAdapter() }
    private val mToDoViewModel : ToDoViewModel by viewModels()
    private val mSharedViewModel : SharedViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater,container,false)
        binding.mSharedViewModel = mSharedViewModel
        binding.lifecycleOwner = this


        //setUpRecyclerView
       setUpRecyclerView()

        mToDoViewModel.getAllData.observe(viewLifecycleOwner, Observer {data ->
            mSharedViewModel.checkDataBase(data)
            adapter.setData(data)
        })

        mSharedViewModel.emptyDataBase.observe(viewLifecycleOwner, Observer {
            showEmptyDataBaseViews(it)
        })


        setHasOptionsMenu(true)
        return binding.root
    }

    private fun setUpRecyclerView() {
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 300
        }
        swipeToDelete(recyclerView)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu,menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.delete_all ->confirmRemoval()
            R.id.menu_high_priority -> mToDoViewModel.sortByHighPriority.observe(this, Observer {
                adapter.setData(it)
            })
            R.id.menu_low_priority -> mToDoViewModel.sortByLowPriority.observe(this, Observer {
                adapter.setData(it)
            })

        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmRemoval() {

        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") {_,_->
            mToDoViewModel.deleteAll()
            Toast.makeText(requireContext(),"Successfully deleted everything", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No"){_,_->
        }
        builder.setTitle("Delete everything?")
        builder.setMessage("Are you sure you want to remove everything")
        builder.create().show()
    }

    private fun showEmptyDataBaseViews(emptyDataBase : Boolean) {
        if(emptyDataBase){
            view?.no_data_textview?.visibility = View.VISIBLE
            view?.no_data_imageview?.visibility = View.VISIBLE
        }
        else{
            view?.no_data_textview?.visibility = View.INVISIBLE
            view?.no_data_imageview?.visibility = View.INVISIBLE
        }

    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query != null){
            searchThroughDataBase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if(query != null){
            searchThroughDataBase(query)
        }
        return true
    }

    private fun searchThroughDataBase(query: String) {
        val searchQuery = "%$query%"
        mToDoViewModel.searchDataBase(searchQuery).observe(this, Observer { list->
            adapter.setData(list)
        })
    }
////////////////////////////////////////////////////////////////UNDO AND REDO/////////////////////////////////
    private fun swipeToDelete(recyclerView: RecyclerView){
        val swipeToDeleteCallBack = object  : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = adapter.dataList[viewHolder.adapterPosition]
                mToDoViewModel.deleteItem(deletedItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                restoreDeletedItem(viewHolder.itemView,deletedItem,viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)

    }

    private fun restoreDeletedItem(view:View,deletedItem : ToDoData,position:Int){
        val snackbar  = Snackbar.make(
            view,"Deleted '${deletedItem.title}'",Snackbar.LENGTH_LONG
        )
        snackbar.setAction("Undo"){
            mToDoViewModel.insertData(deletedItem)
        }
        snackbar.show()
    }
///////////////////////////////////////////////////////////////////UNDO AND REDO/////////////////////////////////


    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
    }



}