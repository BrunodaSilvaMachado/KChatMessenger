package br.com.app.kchatmessenger.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import br.com.app.kchatmessenger.R
import br.com.app.kchatmessenger.firebaseHelper.Auth
import br.com.app.kchatmessenger.firebaseHelper.Firestore
import br.com.app.kchatmessenger.model.Contact
import br.com.app.kchatmessenger.model.Message
import br.com.app.kchatmessenger.model.User
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.item_from_message.view.*
import kotlinx.android.synthetic.main.item_to_message.view.*

class ChatFragment : Fragment() {
    companion object {private const val TAG = "KCM:ChatActivity"}

    private lateinit var mAdapter: GroupAdapter<ViewHolder>
    private lateinit var mUser: User
    private var mMe: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        mUser = arguments?.getParcelable(ContactsFragment.USER_KEY)!!
        setupActionBar()

        mAdapter = GroupAdapter()
        view.chat_recicleview.adapter = mAdapter

        view.chat_floatingActionButton.setOnClickListener { sendMessage(it) }

        Firestore.getUserInCollectionAndDocument("/users", Auth.getUID().toString())
                 .addOnSuccessListener { mMe = it.toObject(User::class.java); fetchMessages() }
        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            if (fragmentManager?.backStackEntryCount!! > 0) {
                fragmentManager?.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar(){
        val actionBar = (activity as AppCompatActivity).supportActionBar

        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = mUser.name
    }

    private fun fetchMessages(){
        mMe?.let {
            val  fromId = it.uid
            val toId = mUser.uid

            Firestore.collectionDocumentCollection("/conversations", fromId, toId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener{ querySnapshot, _ ->
                    querySnapshot?.documentChanges?.let {docs ->
                        for (doc in docs) {
                            if(doc.type == DocumentChange.Type.ADDED) {
                                val message = doc.document.toObject(Message::class.java)
                                mAdapter.add(MessageItem(message))
                            }
                        }
                    }
                }
        }
    }

    private fun sendMessage(view: View){
        val text = view.chat_editText.text.toString()
        if(text.isEmpty()) return

        view.chat_editText.text = null

        val fromId = Auth.getUID().toString()
        val toId = mUser.uid
        val timestamp = System.currentTimeMillis()

        val message = Message(text = text, timestamp = timestamp, fromId = fromId, toId = toId)
        Firestore.collectionDocumentCollection("/conversations", fromId, toId)
            .add(message)
            .addOnSuccessListener {
                Log.i(TAG, it.id)
                val contact = Contact(uuid = toId, username = mUser.name, lastMessage = text,
                    photoUrl = mUser.url, timestamp = message.timestamp)
                Firestore.collectionDocumentCollection("/last-messages", fromId, "contacts")
                    .document(toId)
                    .set(contact)
            }
            .addOnFailureListener { Log.e(TAG, it.message, it) }

        Firestore.collectionDocumentCollection("/conversations", toId, fromId)
            .add(message)
            .addOnSuccessListener {
                Log.i(TAG, it.id)
                val contact = Contact(fromId, mMe?.name!!, text, mMe?.url!!, message.timestamp)
                Firestore.collectionDocumentCollection("/last-messages", toId, "contacts")
                    .document(fromId)
                    .set(contact)
            }
            .addOnFailureListener { Log.e(TAG, it.message, it) }
    }

    private inner class MessageItem(private val mMessage: Message): Item<ViewHolder>() {
        override fun getLayout(): Int {
            return if (mMessage.fromId == Auth.getUID()) R.layout.item_from_message
                else R.layout.item_to_message
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            if(mMessage.fromId == Auth.getUID()){
                viewHolder.itemView.txt_msg_from.text = mMessage.text
                Picasso.get().load(mMe?.url).into(viewHolder.itemView.img_msg_from)
            } else {
                viewHolder.itemView.txt_msg.text = mMessage.text
                Picasso.get().load(mUser.url).into(viewHolder.itemView.img_msg)
            }
        }

    }
}
