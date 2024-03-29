package com.tutorial.bracusocial.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.tutorial.bracusocial.adapters.AddedListAdapter
import com.tutorial.bracusocial.adapters.CourseListAdapter
import com.tutorial.bracusocial.FetchData
import com.tutorial.bracusocial.ListItemChange
import com.tutorial.bracusocial.R
import com.tutorial.bracusocial.data.entities.CourseData

import com.tutorial.bracusocial.data.entities.User
import com.tutorial.bracusocial.data.UserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PrePreRegFragment : Fragment(), ListItemChange {

    private lateinit var context: Context
    private lateinit var listView: RecyclerView
    private lateinit var addedView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var refresh: Button
    private lateinit var save: Button
    private lateinit var listAdapter: CourseListAdapter
    private lateinit var addedAdapter: AddedListAdapter
    private lateinit var dataList: MutableList<String>
    private lateinit var tempDataList: MutableList<String>
    private lateinit var progressBarIndeterminate: ProgressBar
    private lateinit var table: Array<Array<TextView>>
    private val addedCourseString = mutableListOf<String>()
    private val addedCourseMap = mutableMapOf<String, MutableMap<String, MutableList<Int>>>()


    override fun onDestroy() {
        super.onDestroy()
        // Perform cleanup tasks here, if needed
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_pre_pre_reg, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context = requireContext()
        listView = view.findViewById(R.id.listView)
        addedView = view.findViewById(R.id.added_view)
        searchView = view.findViewById(R.id.searchView)
        refresh = view.findViewById(R.id.refresh)
        save = view.findViewById(R.id.saveRoutine)
        progressBarIndeterminate = view.findViewById(R.id.progressBarIndefinite)
        progressBarIndeterminate.visibility = View.GONE

        table = arrayOf(
            arrayOf(view.findViewById(R.id.slot1_1), view.findViewById(R.id.slot1_2), view.findViewById(R.id.slot1_3), view.findViewById(R.id.slot1_4), view.findViewById(R.id.slot1_5), view.findViewById(R.id.slot1_6), view.findViewById(R.id.slot1_7)),
            arrayOf(view.findViewById(R.id.slot2_1), view.findViewById(R.id.slot2_2), view.findViewById(R.id.slot2_3), view.findViewById(R.id.slot2_4), view.findViewById(R.id.slot2_5), view.findViewById(R.id.slot2_6), view.findViewById(R.id.slot2_7)),
            arrayOf(view.findViewById(R.id.slot3_1), view.findViewById(R.id.slot3_2), view.findViewById(R.id.slot3_3), view.findViewById(R.id.slot3_4), view.findViewById(R.id.slot3_5), view.findViewById(R.id.slot3_6), view.findViewById(R.id.slot3_7)),
            arrayOf(view.findViewById(R.id.slot4_1), view.findViewById(R.id.slot4_2), view.findViewById(R.id.slot4_3), view.findViewById(R.id.slot4_4), view.findViewById(R.id.slot4_5), view.findViewById(R.id.slot4_6), view.findViewById(R.id.slot4_7)),
            arrayOf(view.findViewById(R.id.slot5_1), view.findViewById(R.id.slot5_2), view.findViewById(R.id.slot5_3), view.findViewById(R.id.slot5_4), view.findViewById(R.id.slot5_5), view.findViewById(R.id.slot5_6), view.findViewById(R.id.slot5_7)),
            arrayOf(view.findViewById(R.id.slot6_1), view.findViewById(R.id.slot6_2), view.findViewById(R.id.slot6_3), view.findViewById(R.id.slot6_4), view.findViewById(R.id.slot6_5), view.findViewById(R.id.slot6_6), view.findViewById(R.id.slot6_7)),
            arrayOf(view.findViewById(R.id.slot7_1), view.findViewById(R.id.slot7_2), view.findViewById(R.id.slot7_3), view.findViewById(R.id.slot7_4), view.findViewById(R.id.slot7_5), view.findViewById(R.id.slot7_6), view.findViewById(R.id.slot7_7)),
            arrayOf(view.findViewById(R.id.slot8_1), view.findViewById(R.id.slot8_2), view.findViewById(R.id.slot8_3), view.findViewById(R.id.slot8_4), view.findViewById(R.id.slot8_5), view.findViewById(R.id.slot8_6), view.findViewById(R.id.slot8_7))
        )


        dataList = getCourseKey()

        tempDataList = dataList


        listAdapter = CourseListAdapter(this, tempDataList)
        addedAdapter = AddedListAdapter(this, addedCourseString)
        listView.adapter = listAdapter
        addedView.adapter = addedAdapter

        refresh.setOnClickListener {
            refresh.isEnabled = false

            populateCourseDatabase()

        }

        save.setOnClickListener {
            progressBarIndeterminate.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                val deferredFlag = async{ saveUserCourse(addedCourseString) }
                val flag = deferredFlag.await()

                if (flag){
                    Toast.makeText(requireContext(), "Coursed added successfully", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(requireContext(), "Could not be added", Toast.LENGTH_SHORT).show()
                }
                progressBarIndeterminate.visibility = View.GONE
            }

        }


        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // called upon entering a new character
                filterList(newText, tempDataList, listAdapter)
                return true
            }

        })

        // room
        //https://www.youtube.com/watch?v=bOd3wO0uFr8
    }

    private fun filterList(query: String?, data: MutableList<String>, adapter: CourseListAdapter) {
        if (query!=null){
            val filteredList = mutableListOf<String>()
            for (i in data){
                if(i.lowercase().contains(query.lowercase())){
                    filteredList.add(i)
                }
            }
            if(filteredList.isNotEmpty()){
                adapter.setFilteredList(filteredList)
            }
        }
    }


    override fun onItemAdded(item: String) {
        val courseName = item.substring(0,6)
        var isCourseAdded = false
        for (i in addedCourseString){
            if (i.contains(courseName)){
                isCourseAdded = true
                break
            }
        }
        if(!isCourseAdded && addedCourseString.size < 6){
            addedCourseString.add(item)
            addedCourseMap[item] = mutableMapOf()
            println(addedCourseString.toString())
            setClassSlot(item)
            addedAdapter.notifyDataSetChanged()

        }
    }

    override fun onItemRemoved(item: String) {
        addedCourseString.remove(item)
        addedCourseMap.remove(item)
        removeClassSlot(item)
        addedAdapter.notifyDataSetChanged()
    }

    private fun populateCourseDatabase(){
        Toast.makeText(requireContext(), "Refreshing Database", Toast.LENGTH_SHORT).show()
        val fetchData = FetchData()
        progressBarIndeterminate.visibility = View.VISIBLE
         CoroutineScope(Dispatchers.IO).launch {
             val deferredResult = async { fetchData.executeAsyncTask() }
             val json = deferredResult.await()

             val dao = UserDatabase.getInstance(requireContext()).dao

             for (i in 0 until json.length()) {
                 var timer = 0
                 val data: CourseData

                 val jsonObject = json.getJSONObject(i)
                 val courseName = jsonObject.getString("Course")
                 val section = jsonObject.getString("Section")


                 val classDayJson = jsonObject.getJSONArray("ClassDay")
                 var classDay: String = ""
                 for (i in 0 until classDayJson.length()) {
                     classDay += classDayJson.getString(i) + " "

                 }
                 val classTime = jsonObject.getString("ClassTime")
                 val classRoom = jsonObject.getString("ClassRoom")
                 val lab = jsonObject.getBoolean("Lab")

                 if (lab) {
                     val labDayJson = jsonObject.getJSONArray("LabDay")
                     var labDay = ""
                     for (i in 0 until labDayJson.length()) {
                         labDay += (labDayJson.getString(i)) + " "
                     }
                     val labTime = jsonObject.getString("LabTime")
                     val labRoom = jsonObject.getString("LabRoom")

                     data = CourseData(
                         courseName = courseName,
                         section = section,
                         classDay = classDay,
                         classTime = classTime,
                         classRoom = classRoom,
                         labDay = labDay,
                         labTime = labTime,
                         labRoom = labRoom
                     )
                 } else {
                     data = CourseData(
                         courseName = courseName,
                         section = section,
                         classDay = classDay,
                         classTime = classTime,
                         classRoom = classRoom
                     )
                 }


                 val id = dao.getCourseByKey(courseName, section)
                 if (id != null) {
                     data.id = id.id
                 }
                 dao.upsertCourse(data)

             }
             //populateDatabaseTimer = 1
             dataList = getCourseKey()
             tempDataList = dataList
             withContext(Dispatchers.Main){

                 refresh.isEnabled = true
                 Toast.makeText(requireContext(), "Database Refreshed", Toast.LENGTH_SHORT).show()
                 listAdapter.setData(tempDataList)
                 listAdapter.notifyDataSetChanged()
                 progressBarIndeterminate.visibility = View.GONE
             }
         }

    }

    private fun getCourseKey(): MutableList<String>{
        val dataInString = mutableListOf<String>()
        val dao = UserDatabase.getInstance(requireContext()).dao
        CoroutineScope(Dispatchers.IO).launch {
            val deferredData = async { dao.getCourseList() }
            val data = deferredData.await()
            for (i in data){
                val courseName = i.courseName
                val courseSection = i.section
                val courseString = "$courseName $courseSection"
                dataInString.add(courseString)
            }

        }
        return dataInString

    }

    private fun getClassSlot(item: String, classTime: String, classDay:String): MutableMap<String, MutableList<Int>>{
        println("$classTime $classDay   getclassslot")
        val slot = mutableMapOf<String, MutableList<Int>>()
        val row = mutableListOf<Int>()
        when (classTime) {
            "08:00 AM - 09:20 AM" -> row.add(0)
            "09:30 AM - 10:50 AM" -> row.add(1)
            "11:00 AM - 12:20 PM" -> row.add(2)
            "12:30 PM - 01:50 PM" -> row.add(3)
            "02:00 PM - 03:20 PM" -> row.add(4)
            "03:30 PM - 04:50 PM" -> row.add(5)
            "05:00 PM - 06:20 PM" -> row.add(6)
            "06:30 PM - 08:00 PM" -> row.add(7)
        }
        slot["row"] =  row
        val column = mutableListOf<Int>()
        val days = classDay.split(" ").toMutableList()
        for(i in 0 until days.size){
            days[i] = days[i].trim()
        }
        for (day in days){
            when (day) {
                "Sa" -> column.add(0)
                "Su" -> column.add(1)
                "Mo" -> column.add(2)
                "Tu" -> column.add(3)
                "We" -> column.add(4)
                "Th" -> column.add(5)
                "Fr" -> column.add(6)
            }
        }
        slot["column"] = column
        addedCourseMap[item]?.set("row", row)
        addedCourseMap[item]?.set("column", column)
        return slot
    }

    private fun getLabSlot(item:String, labTime: String, labDay:String): MutableMap<String, MutableList<Int>>{
        val slot = mutableMapOf<String, MutableList<Int>>()
        val row = mutableListOf<Int>()

        when (labTime) {
            "08:00 AM - 09:20 AM" -> row.add(0)
            "09:30 AM - 10:50 AM" -> row.add(1)
            "11:00 AM - 12:20 PM" -> row.add(2)
            "12:30 PM - 01:50 PM" -> row.add(3)
            "02:00 PM - 03:20 PM" -> row.add(4)
            "03:30 PM - 04:50 PM" -> row.add(5)
            "05:00 PM - 06:20 PM" -> row.add(6)
            "06:30 PM - 08:00 PM" -> row.add(7)
            "08:00 AM - 10:50 AM" -> {row.add(0); row.add(1)}
            "11:00 AM - 01:50 PM" -> {row.add(2); row.add(3)}
            "02:00 PM - 04:50 PM" -> {row.add(4); row.add(5)}
            "05:00 PM - 08:00 PM" -> {row.add(6); row.add(7)}

        }
        slot["row"] =  row
        println(row)
        val column = mutableListOf<Int>()
        val days = labDay.split(" ").toMutableList()
        for(i in 0 until days.size){
            if (days[i] == " " || days[i] == ""){
                days.removeAt(i)
            }
            else{
                days[i] = days[i].trim()
            }

        }
        for (day in days){

            when (day) {
                "Sa" -> column.add(0)
                "Su" -> column.add(1)
                "Mo" -> column.add(2)
                "Tu" -> column.add(3)
                "We" -> column.add(4)
                "Th" -> column.add(5)
                "Fr" -> column.add(6)
            }
        }
        slot["column"] = column
        addedCourseMap[item]?.set("labRow", row)
        addedCourseMap[item]?.set("labColumn", column)
        return slot
    }



    private fun setClassSlot(item: String){
        val dao = UserDatabase.getInstance(requireContext()).dao
        val itemName = item.substring(0, 6)
        val itemSection = item.substring(7)
        CoroutineScope(Dispatchers.IO).launch {

            val deferredCourseObject = async { dao.getCourseByKey(itemName, itemSection) }
            val courseObject = deferredCourseObject.await()

            val courseName = courseObject!!.courseName
            val section = courseObject.section
            val classTime = courseObject.classTime
            val classDay = courseObject.classDay
            val labDay = courseObject.labDay
            if (labDay!= null){
                setLabSlot(item)
            }
            val slotMap = getClassSlot(item, classTime!!, classDay!!)
            val row = slotMap["row"]!![0]
            val column = slotMap["column"]
            for(c in column!!){
                val currentText = table[row][c].text.toString()
                if (currentText != "-"){
                    table[row][c].text = String.format("%s\n%s %s", currentText, courseName, section)
                    table[row][c].setBackgroundResource(R.drawable.table_box_red)
                }
                else{
                    table[row][c].text = String.format("%s %s", courseName, section)
                    table[row][c].setBackgroundResource(R.drawable.table_box_green)
                }
            }
        }
    }

    private fun setLabSlot(item: String){
        val dao = UserDatabase.getInstance(requireContext()).dao
        val itemName = item.substring(0, 6)
        val itemSection = item.substring(7)
        CoroutineScope(Dispatchers.IO).launch {

            val deferredCourseObject = async { dao.getCourseByKey(itemName, itemSection) }
            val courseObject = deferredCourseObject.await()

            val courseName = courseObject!!.courseName
            val section = courseObject.section
            val labTime = courseObject.labTime
            val labDay = courseObject.labDay
            val slotMap = getLabSlot(item, labTime!!, labDay!!)
            val row = slotMap["row"]
            
            val column = slotMap["column"]
            for(c in column!!){
                for (r in row!!){
                    println("Lab slots in row: $r column $c")
                    val currentText = table[r][c].text.toString()
                    if (currentText != "-"){
                        table[r][c].text = String.format("%s\n%s %s", currentText, courseName, section)
                        table[r][c].setBackgroundResource(R.drawable.table_box_red)
                    }
                    else{
                        table[r][c].text = String.format("%s %s", courseName, section)
                        table[r][c].setBackgroundResource(R.drawable.table_box_green)
                    }
                }

            }
        }


    }

    private fun removeClassSlot(item: String){
        val dao = UserDatabase.getInstance(requireContext()).dao
        val itemName = item.substring(0, 6)
        val itemSection = item.substring(7)
        CoroutineScope(Dispatchers.IO).launch {


            val deferredCourseObject = async { dao.getCourseByKey(itemName, itemSection) }
            val courseObject = deferredCourseObject.await()

            val classTime = courseObject!!.classTime
            val classDay = courseObject.classDay
            val labDay = courseObject.labDay
            if (labDay!= null){
                removeLabSlot(item)
            }
            val slotMap = getClassSlot(item, classTime!!, classDay!!)
            val row = slotMap["row"]!![0]
            val column = slotMap["column"]
            for(c in column!!){
                var currentText = table[row][c].text.toString()
                if (currentText.length > 9){
                    val newText = currentText.substring(0, currentText.length-10)
                    table[row][c].text = newText
                }
                else{
                    table[row][c].text = "-"
                }
                currentText = table[row][c].text.toString()
                if( currentText.length > 10){
                    table[row][c].setBackgroundResource(R.drawable.table_box_red)
                }
                else if(currentText.length > 1){
                    table[row][c].setBackgroundResource(R.drawable.table_box_green)
                }
                else{
                    table[row][c].setBackgroundResource(R.drawable.table_box)
                }
            }
        }
    }

    private fun removeLabSlot(item: String){
        val dao = UserDatabase.getInstance(requireContext()).dao
        val itemName = item.substring(0, 6)
        val itemSection = item.substring(7)
        CoroutineScope(Dispatchers.IO).launch {
            
            val deferredCourseObject = async { dao.getCourseByKey(itemName, itemSection) }
            val courseObject = deferredCourseObject.await()

            val labTime = courseObject!!.labTime
            val labDay = courseObject.labDay
            val slotMap = getLabSlot(item, labTime!!, labDay!!)
            val row = slotMap["row"]
            val column = slotMap["column"]
            for(c in column!!){
                for (r in row!!){
                    var currentText = table[r][c].text.toString()
                    if (currentText.length > 9){
                        val newText = currentText.substring(0, currentText.length-10)
                        table[r][c].text = newText
                    }
                    else{
                        table[r][c].text = "-"
                    }
                    currentText = table[r][c].text.toString()
                    if( currentText.length > 10){
                        table[r][c].setBackgroundResource(R.drawable.table_box_red)
                    }
                    else if(currentText.length > 1){
                        table[r][c].setBackgroundResource(R.drawable.table_box_green)
                    }
                    else{
                        table[r][c].setBackgroundResource(R.drawable.table_box)
                    }
                }
            }
        }

    }

    private suspend fun saveUserCourse(list: MutableList<String>): Boolean {
        return withContext(Dispatchers.IO) {
            val dao = UserDatabase.getInstance(requireContext()).dao

            val currentUserData = dao.getCurrentUser(1)
            if (currentUserData != null) {
                val newUserData = User(
                    id = 1,
                    studentID = currentUserData.studentID,
                    name = currentUserData.name,
                    courses = addedCourseMap,
                    friends = currentUserData.friends,
                    password = currentUserData.password
                )
                dao.upsertUser(newUserData)
                true // Return true if currentUserData is not null
            } else {
                false // Return false if currentUserData is null
            }
        }
    }


}