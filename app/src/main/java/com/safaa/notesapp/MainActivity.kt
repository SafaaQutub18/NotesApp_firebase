package com.safaa.notesapp

import android.app.Dialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.safaa.notesapp.Data.MainViewModel
import com.safaa.notesapp.Data.Note
import com.safaa.notesapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    val myViewModel by lazy{ ViewModelProvider(this).get(MainViewModel::class.java)}

    lateinit var myAdapter: RecyclerViewAdapter

    var deletedNote: Note? = null
    var selectedItem: Note? = null

    private var swipeBackground: ColorDrawable = ColorDrawable(Color.parseColor("#BD2828"))
    private lateinit var deleteIcon: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adapter setting
        myAdapter = RecyclerViewAdapter( this)
        binding.recyclerV.adapter = myAdapter
        binding.recyclerV.layoutManager = LinearLayoutManager(this)


        myViewModel.readNotes().observe(this, {
            noteList -> myAdapter.setNotesList(noteList)
        })


        //set delete swipe
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete)!!
        itemSwipe()

        binding.apply {
            addBtn.setOnClickListener {
                showDialog("")
            }
        }
    }


    fun itemSwipe() {
        val itemTouchHelperCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                viewHolder2: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                (myAdapter as RecyclerViewAdapter).removeItem(viewHolder)
                if (deletedNote != null) {
                    myViewModel.removeNote(deletedNote!!)
                    deletedNote = null
                }

            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean,
            ) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
                if (dX > 0) { // if swipe right
                    swipeBackground.setBounds(itemView.left,
                        itemView.top,
                        dX.toInt(),
                        itemView.bottom)
                    deleteIcon.setBounds(itemView.left + iconMargin,
                        itemView.top + iconMargin,
                        itemView.left + iconMargin +
                                deleteIcon.intrinsicWidth,
                        itemView.bottom - iconMargin)
                } else {
                    swipeBackground.setBounds(itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom)
                    deleteIcon.setBounds(itemView.right - iconMargin - deleteIcon.intrinsicWidth,
                        itemView.top + iconMargin,
                        itemView.right - iconMargin,
                        itemView.bottom - iconMargin)
                }
                swipeBackground.draw(c)
                deleteIcon.draw(c)
                super.onChildDraw(c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive)
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerV)
    }

    fun showDialog(noteOldText: String) {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alert_dialog)

        val updatBtn = dialog.findViewById(R.id.btn) as Button
        val textET = dialog.findViewById(R.id.noteET) as EditText

        //set the text of button
        if (selectedItem != null)
            updatBtn.setText("update")
        else
            updatBtn.setText("add")

        textET.setText(noteOldText)
        updatBtn.setOnClickListener {
            dialog.dismiss()
            if (selectedItem != null) { // update
                selectedItem!!.text = textET.text.toString()
                myViewModel.editNote(selectedItem!!)
                selectedItem = null

            } else { // add
                //add in data base:
                myViewModel.addNote(textET.text.toString())

            }

        }
        dialog.show()
    }


}

