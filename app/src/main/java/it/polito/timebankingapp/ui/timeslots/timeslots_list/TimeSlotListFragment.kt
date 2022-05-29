package it.polito.timebankingapp.ui.timeslots.timeslots_list

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import it.polito.timebankingapp.MainActivity
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.User
import it.polito.timebankingapp.ui.chats.chat.ChatViewModel
import it.polito.timebankingapp.ui.chats.chatslist.ChatListViewModel
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel


/**
 * A fragment representing a list of Items.
 */
class TimeSlotListFragment : Fragment(R.layout.fragment_timeslots_list) {

    private var filterParameter = "Title"
    private var filterKeywords = ""
    private var orderingDirection = false //false == ascending, true = descending
    private var openFilterSortMenu = false

    private val vm: TimeSlotsViewModel by activityViewModels()
    private val userVm: ProfileViewModel by activityViewModels()
    private val chatVm: ChatViewModel by activityViewModels()
    private val chatListVm: ChatListViewModel by activityViewModels()

    private lateinit var type: String

    private lateinit var rv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        type = arguments?.getString("point_of_origin").toString()



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var mainTitle = ""
        if (type == "skill_specific") {
            setHasOptionsMenu(true)
            mainTitle = "Offers for ${vm.selectedSkill.value.toString()}"
        } else if (type == "personal")
            mainTitle = "Your advertisements"
        else if (type == "interesting")
            mainTitle = "Your interesting Offers"

        (activity as MainActivity?)?.setActionBarTitle(mainTitle)


        rv = view.findViewById(R.id.time_slot_list)
        rv.layoutManager = LinearLayoutManager(context)

        val addTimeSlotButton = view.findViewById<FloatingActionButton>(R.id.addTimeSlotButton)

        if (type == "skill_specific" || type == "interesting") {
            addTimeSlotButton.visibility = View.GONE
        } else { //personal
            addTimeSlotButton.setOnClickListener {
                findNavController().navigate(R.id.action_nav_personalTimeSlotList_to_nav_timeSlotEdit)
            }
            setFragmentResultListener("timeSlot") { _, bundle ->
                val result = bundle.getInt("timeSlotConfirm")

                if (result == 1) {
                    val snackBar = Snackbar.make(
                        view,
                        "New time slot successfully added.",
                        Snackbar.LENGTH_LONG
                    )
                    snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
                } else if (result == 2) {
                    val snackBar =
                        Snackbar.make(view, "Time slot successfully edited.", Snackbar.LENGTH_LONG)
                    snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
                }
            }
        }


/*
        val adTmp = TimeSlotAdapter(
            vm.timeSlots.value?.toMutableList() ?: mutableListOf(),
            ::selectTimeSlot,
            ::requestTimeSlot,
            ::showRequests,
            type
        )
        rv.adapter = adTmp
*/


        //var skill = arguments?.getString("skill")

        vm.timeSlots.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                setVoidMessage(view, false)


                val adTmp = TimeSlotAdapter(
                    it.toMutableList(),
                    ::selectTimeSlot,
                    ::showTimeSlotRequest,
                    ::showRequests,
                    type
                )

//                adTmp.data = it.toMutableList()
                rv.adapter = adTmp

                if (type == "skill_specific") {
                    setFilteringOptions(view, adTmp)
                    adTmp.setFilter(filterKeywords, filterParameter)
                    adTmp.setOrder(filterParameter, orderingDirection)
                }
            } else {
                setVoidMessage(view, true)
            }
        }

    }

    fun showTimeSlotRequest(timeSlot: TimeSlot) {
        chatVm.selectChatFromTimeSlot(timeSlot)
        //chatVm.updateUserInfo(timeSlot.userId)
    }

    private fun setFilteringOptions(view: View, adapter: TimeSlotAdapter) {
        val spinner: Spinner = view.findViewById(R.id.filter_spinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.filter_parameters_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        //combobox
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                filterParameter =
                    resources.getStringArray(R.array.filter_parameters_array)[position]
                adapter.setFilter(filterKeywords, filterParameter)
                adapter.setOrder(filterParameter, orderingDirection)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val searchView: SearchView = view.findViewById(R.id.filter_bar)
        //barra di ricerca
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                filterKeywords = s
                adapter.setFilter(filterKeywords, filterParameter)
                adapter.setOrder(filterParameter, orderingDirection)
                return false
            }
        })

        //image buttons (sorting)
        val ascendingButton = view.findViewById(R.id.ascend_button) as ImageButton
        val descendingButton = view.findViewById(R.id.descend_button) as ImageButton
        val layoutManager = rv.layoutManager as LinearLayoutManager?

        ascendingButton.setOnClickListener {
            orderingDirection = false
            adapter.setFilter(filterKeywords, filterParameter)
            adapter.setOrder(filterParameter, orderingDirection)
            layoutManager!!.scrollToPositionWithOffset(0, 0)
        }

        descendingButton.setOnClickListener {
            orderingDirection = true
            adapter.setFilter(filterKeywords, filterParameter)
            adapter.setOrder(filterParameter, orderingDirection)
            layoutManager!!.scrollToPositionWithOffset(0, 0)
        }

    }

    private fun setVoidMessage(v: View, b: Boolean) {
        val voidMessageImage = v.findViewById<ImageView>(R.id.time_slot_icon)
        val voidMessageText = v.findViewById<TextView>(R.id.emptyListMessage)
        val voidMessageSubText = v.findViewById<TextView>(R.id.empty_list_second_message)

        val visibility = if(b) View.VISIBLE else View.GONE

        voidMessageText.visibility = visibility
        voidMessageImage.visibility = visibility
        voidMessageSubText.visibility = visibility
    }



    private fun showRequests(ts: TimeSlot) {
        chatListVm.downloadTimeSlotChats(ts.id)
    }

    private fun selectTimeSlot(ts: TimeSlot) {
        vm.setSelectedTimeSlot(ts)
    }

    override fun onDetach() {
        vm.clearTimeSlots()
        super.onDetach()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_filter_and_sort, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter_and_sort_option -> {
                /* filter by something*/
                val linearLayout = view?.findViewById(R.id.filter_and_sort_layout) as LinearLayout
                if (!openFilterSortMenu) {
                    linearLayout.visibility = View.VISIBLE
                    openFilterSortMenu = true
                } else {
                    linearLayout.visibility = View.GONE
                    openFilterSortMenu = false
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    /* Impossibile da importare... */
    fun DocumentSnapshot.toUser(): User? {

        return try {
            val pic = get("pic") as String
            val fullName = get("fullName") as String
            val nick = get("nick") as String
            val email = get("email") as String
            val location = get("location") as String
            val desc = get("description") as String
            val balance = get("balance") as Long
            val skills = get("skills") as MutableList<String>

            User(id, pic, fullName, nick, email, location, desc, balance.toInt(), skills)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}