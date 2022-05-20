data class Message(
    val messageId: Int = 0,
    val chatIdForMessages: Int = 0,
    val senderId: Int = 0,
    val recipientId: Int = 0,
    var text: String = "",
    var isRead: Boolean = false
)