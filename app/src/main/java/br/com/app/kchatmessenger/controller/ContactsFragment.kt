package br.com.app.kchatmessenger.controller

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import br.com.app.kchatmessenger.R
import br.com.app.kchatmessenger.firebaseHelper.Firestore
import br.com.app.kchatmessenger.model.User
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_contact.view.*
import kotlinx.android.synthetic.main.item_user.view.*

class ContactsFragment : Fragment() {
    companion object{
        private const val TAG: String = "KCM:ContactsActivity"
        const val USER_KEY = "user_key"
    }

    private lateinit var mAdapter: GroupAdapter<ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setupActionBar()
        val view = inflater.inflate(R.layout.fragment_contact, container, false)
        mAdapter = GroupAdapter()
        mAdapter.setOnItemClickListener { item, _ ->
            val userItem = item as UserItem
            val chatFragment = ChatFragment()
            chatFragment.arguments = bundleOf(Pair<String, Parcelable>(USER_KEY, userItem.mUser))

            val fragmentTransaction = fragmentManager?.beginTransaction()
            fragmentTransaction?.replace(R.id.main_frame, chatFragment, "ChatFragment")?.commit()
            fragmentTransaction?.addToBackStack("ContactsFragment")
        }

        view.list_contacts.adapter = mAdapter
        fetchUsers()

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
        actionBar?.setTitle(R.string.select_contact)
    }

    private inner class UserItem(internal val mUser: User): Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.item_user
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.txt_username.text = mUser.name
            Picasso.get().load(mUser.url).into(viewHolder.itemView.img_photo)
        }

    }

    private fun fetchUsers() {
        Firestore.collection("/users/")
            .addSnapshotListener{
                    snapshot: QuerySnapshot?,
                    exception: FirebaseFirestoreException? ->
                exception?.let {
                    Log.d(TAG, it.message, it)
                    return@addSnapshotListener
                }
                snapshot?.let {
                    for (doc in snapshot) { // 4
                        val user = doc.toObject(User::class.java) // 5
                        Log.i(TAG, "user ${user.uid}, ${user.name}")
                        mAdapter.add(UserItem(user))
                    }
                }
            }

    }
}
