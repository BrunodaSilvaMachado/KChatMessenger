package br.com.app.kchatmessenger.controller

import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import br.com.app.kchatmessenger.MainActivity
import br.com.app.kchatmessenger.R
import br.com.app.kchatmessenger.firebaseHelper.Auth
import br.com.app.kchatmessenger.firebaseHelper.Firestore
import br.com.app.kchatmessenger.model.Contact
import com.google.firebase.firestore.DocumentChange
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_messages.view.*
import kotlinx.android.synthetic.main.item_user_message.view.*

class MessagesFragment : Fragment() {

    private lateinit var mAdapter: GroupAdapter<ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setupActionBar()
        val view = inflater.inflate(R.layout.fragment_messages, container, false)

        mAdapter = GroupAdapter()
        mAdapter.setOnItemClickListener { item, _ ->
            val contactItem = item as ContactItem
            val user = Contact.toUser(contactItem.mContact)
            val chatFragment = ChatFragment()

            chatFragment.arguments = bundleOf(Pair<String, Parcelable>(ContactsFragment.USER_KEY, user))
            val fragmentTransaction = fragmentManager?.beginTransaction()
                fragmentTransaction?.replace(R.id.main_frame, chatFragment, "ChatFragment")?.commit()
            fragmentTransaction?.addToBackStack("MessagesFragment")

        }

        view.list_messages.adapter = mAdapter
        fetchLastMessage()

        return view
    }

    private fun setupActionBar(){
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setTitle(R.string.message)
    }

    private fun fetchLastMessage(){
        val uid = Auth.getUID().toString()

        Firestore.collectionDocumentCollection("/last-messages", uid, "contacts")
            .addSnapshotListener{ query, _ ->
                val changes = query?.documentChanges
                changes?.let {
                    for (doc in it) {
                        if (doc.type == DocumentChange.Type.ADDED) {
                            val contact = doc.document.toObject(Contact::class.java)
                            mAdapter.add(ContactItem(contact))
                        }
                    }
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.message_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout ->{
                Auth.signOut()
                val mainActivity = activity

                if(mainActivity is MainActivity){
                    mainActivity.verifyAuthentication()
                }
            }

            R.id.contacts ->{
                val fragmentTransaction = fragmentManager?.beginTransaction()
                fragmentTransaction?.replace(R.id.main_frame, ContactsFragment(), "ContactsFragment")?.commit()
                fragmentTransaction?.addToBackStack("MessagesFragment")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class ContactItem(internal val mContact: Contact): Item<ViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.item_user_message
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.txt_last_message.text = mContact.lastMessage
            viewHolder.itemView.txt_username.text = mContact.username
            Picasso.get()
                .load(mContact.photoUrl)
                .into(viewHolder.itemView.img_photo)
        }

    }
}
