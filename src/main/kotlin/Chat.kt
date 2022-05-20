data class Chat(
    var chatId: Int = 0,
    val senderId: Int = 0,
    var recipientId: Int = 0,
    val messages: MutableList<Message> = mutableListOf()
)