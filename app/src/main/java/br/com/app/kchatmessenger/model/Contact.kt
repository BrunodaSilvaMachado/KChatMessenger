package br.com.app.kchatmessenger.model

data class Contact(val uuid: String = "", val username: String = "", val lastMessage: String = "",
    val photoUrl: String = "", val timestamp: Long = 0){
    companion object{
        fun toUser(contact: Contact): User {
            return User(contact.uuid, contact.username, contact.photoUrl)
        }
    }
}