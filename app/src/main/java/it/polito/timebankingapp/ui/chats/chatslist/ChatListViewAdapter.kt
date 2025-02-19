package it.polito.timebankingapp.ui.chats.chatslist

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import com.google.android.material.chip.Chip
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Chat
import it.polito.timebankingapp.model.Helper

class ChatListViewAdapter(
    private var data: List<Chat>,
    private var selectChat: (chat: Chat) -> Unit?,
    /*private var updateUser: (userId: String) -> Unit?,*/
    val type: String,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_CHAT_LIST = 1
    private val VIEW_TYPE_EMPTY_MESSAGE = 2

    private var displayData = data.toMutableList()
    private var isEmpty: Boolean = displayData.isEmpty()
    init {
        if(isEmpty)
            displayData.add(Chat()) //singolo elemento == messaggio di lista vuota
    }

    class EmptyItemViewHolder(private val mainView: View ) : RecyclerView.ViewHolder(mainView) {
        fun bind() {
            //empty
        }
    }


    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvTimeSlotTitle: TextView = itemView.findViewById(R.id.chat_list_item_time_slot_title)
        var tvLastMessage: TextView = itemView.findViewById(R.id.chat_list_item_last_message)
        var tvLastMessageTime: TextView = itemView.findViewById(R.id.chat_list_item_timestamp)
        var otherUserNick: TextView = itemView.findViewById(R.id.chat_list_item_nick_name)
        var pbOtherProfilePic: ProgressBar =itemView.findViewById(R.id.progressBar)

        private val civImagePic: CircleImageView = itemView.findViewById(R.id.chat_profile_pic)
        private val nUnreadMsg: TextView = itemView.findViewById(R.id.n_unread_msg)
        private val cvUnreadMsg: CardView = itemView.findViewById(R.id.unread_msg_card)
        private val chipOffreq: Chip = itemView.findViewById(R.id.chipOffReq)

        fun bind(cli: Chat, openChatAction: (v: View) -> Unit) {
//            fullNameText.text = "Nome Cognome" //necessario riferimento usr o timeslotusr
//            messageText.text = cli.chatMessages[cli.chatMessages.size-1].messageText
//            timeText.text = cli.chatMessages[cli.chatMessages.size-1].timestamp.split("-")[1] //se è di oggi mostra l'orario, altrimenti la data
//            numNotifiesText.text =  "(1)" //logica conteggio non letti da implementare in futuro

            val otherUser = Helper.getOtherUser(cli)
            tvTimeSlotTitle.text = cli.timeSlot.title
            tvLastMessage.text = cli.lastMessage.messageText
            if(cli.lastMessage.userId != Firebase.auth.uid) {
                if (cli.unreadMsgs > 0)
                    nUnreadMsg.text = cli.unreadMsgs.toString()
                else
                    cvUnreadMsg.visibility = View.INVISIBLE
            }
            else
                cvUnreadMsg.visibility = View.INVISIBLE
            tvLastMessageTime.text = Helper.dateToDisplayString(cli.lastMessage.timestamp)
            otherUserNick.text =  otherUser.nick
            // sarebbe da mettere il last message della chat dentro il documento in userRooms (per l'anteprima)
            // e anche le altre info riguardo a tempo e conteggio non letti e foto profilo altro utente

            if(cli.getType() == Chat.CHAT_TYPE_TO_REQUESTER){
                Helper.loadImageIntoView(civImagePic, pbOtherProfilePic , otherUser.profilePicUrl)

                /* I am the offerer */
                chipOffreq.text = "My Offer"
                chipOffreq.setChipBackgroundColorResource(R.color.accent)
                chipOffreq.setTextColor(Color.WHITE)
//                chipOffreq.setBackgroundColor(Color.GREEN)
            }
            else { /* The other is the requester */
                Helper.loadImageIntoView(civImagePic, pbOtherProfilePic , otherUser.profilePicUrl)
                chipOffreq.text = "Request"
                chipOffreq.setChipBackgroundColorResource(R.color.primary)
//                chipOffreq.setBackgroundColor(Color.YELLOW)
            }


            this.itemView.setOnClickListener(openChatAction)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!isEmpty) {
            VIEW_TYPE_CHAT_LIST
        } else {
            VIEW_TYPE_EMPTY_MESSAGE
        }
    }

    //inflate the item_layout-based structure inside each ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vg: View
        return if (viewType == VIEW_TYPE_CHAT_LIST) {
            vg = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.fragment_chat_list_item, parent, false)
             ItemViewHolder(vg)
        } else { // (viewType == VIEW_TYPE_EMPTY_MESSAGE) {
            vg = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.chat_list_item_empty, parent, false)
             EmptyItemViewHolder(vg)
        }

        /*val destination =  R.layout.fragment_chat_list_item

        val vg = LayoutInflater
            .from(parent.context)
            .inflate(destination, parent, false) //attachToRoot: take all you measures
        //but do not attach it immediately to the ViewHolder tree of components (could be a ghost item)

        return ItemViewHolder(vg)*/
    }

    //populate data for each inflated ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = displayData[position]
        when (holder.itemViewType) {
            VIEW_TYPE_CHAT_LIST-> (holder as ItemViewHolder).bind(item, openChatAction = {
                val destination = if(type == ChatListFragment.GLOBAL) R.id.action_nav_allChatsList_to_nav_chat
                else
                    R.id.action_nav_timeSlotChatsList_to_nav_chat
                selectChat(item)
    //            val b = bundleOf("profilePic" to item.userPic)
    //            b.putString("profileName", item.userName)
    //            b.putString("profileId", item.userId)
                //updateUser(item.userId)
                Navigation.findNavController(it).navigate(
                    destination,
                    //bundleOf("point_of_origin" to type, "userId" to item.userId)
                )
            });
            VIEW_TYPE_EMPTY_MESSAGE -> (holder as EmptyItemViewHolder).bind()
        }
    }


    //how many items?
    override fun getItemCount(): Int = displayData.size
}