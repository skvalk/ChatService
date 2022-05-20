class MessageNotFoundException(message: String) : Exception(message)
class ChatNotFoundException(message: String) : Exception(message)

class ChatService(
    private val chats: MutableList<Chat> = mutableListOf(),
    var chatNextId: Int = 1,
    var messageNextId: Int = 1,

    ) {

    fun addChat(currentRecipientId: Int, chat: Chat): Chat {
        val newChat = chat.copy(chatId = chatNextId, recipientId = currentRecipientId)
        chatNextId++
        chats.add(newChat)
        return newChat
    }

    fun addMessage(currentRecipientId: Int, message: Message): Chat {
        val newMessage = message.copy(messageId = messageNextId)
        return chats
            .find { (it.recipientId == currentRecipientId) && (it.chatId == message.chatIdForMessages) }
            ?.apply {
                messageNextId++
                this.messages.add(message)
            }
            ?: Chat(
                messages = mutableListOf(newMessage),
                recipientId = currentRecipientId,
                chatId = newMessage.chatIdForMessages
            )
    }

    fun getChats(currentUserId: Int): MutableList<Chat> {
        val userChats: MutableList<Chat> = mutableListOf()
        var userChat: Chat
        val filteredChats = chats.filter { (it.senderId == currentUserId) || (it.recipientId == currentUserId) }
        for (chat in filteredChats) {
            val lastMessage = chat.messages.lastOrNull() ?: throw MessageNotFoundException("Нет сообщений")
            userChat = chat
            userChat.messages.clear()
            userChat.messages.add(lastMessage)
            userChats.add(userChat)
        }
        return userChats
    }

    fun getUnreadChatsCount(currentUserId: Int): Int {
        val userUnreadChats: MutableList<Chat> = mutableListOf()
        val userChats = chats.filter { it.recipientId == currentUserId }
        for (chat in userChats) {
            for (message in chat.messages) {
                if (!message.isRead) {
                    userUnreadChats += chat
                }
            }
        }
        return userUnreadChats.size
    }

    fun editMessage(message: Message) {
        for (chat in chats) {
            for ((index, newMessage) in chat.messages.withIndex()) {
                if (message.messageId == newMessage.messageId) {
                    chat.messages[index] = message.copy(text = newMessage.text)
                }
            }
        }
    }

    fun deleteChat(currentChatId: Int) {
        chats.removeIf { it ->
            (it.chatId == currentChatId)
            (it.messages.removeIf { it.chatIdForMessages == currentChatId })
        }
    }

    fun deleteMessage(currentChatId: Int, currentMessageId: Int) {
        chats
            .find { it.chatId == currentChatId }
            .apply {
                this?.messages ?: throw ChatNotFoundException("Такого чата не существует")
                this.messages.removeIf {
                    it.messageId == currentMessageId
                }
                chats.removeIf { this.messages.isEmpty() }
            }
    }

    fun getMessages(currentChatId: Int, currentMessageId: Int, count: Int, currentUserId: Int): List<Message> {
        val currentChat = chats
            .find { it.chatId == currentChatId } ?: throw ChatNotFoundException("Такого чата не существует")
        val latestMessages = currentChat.messages
            .takeLastWhile { it.messageId == currentMessageId } // будем набирать с конца сообщения в новый список, пока выполняется данное условие
            .take(count)
        for (message in latestMessages) {
            if (message.recipientId == currentUserId) {
                message.isRead = true
            }
        }
        return latestMessages
    }
}