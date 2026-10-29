package com.example.tappi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tappi.data.model.*
import com.example.tappi.data.service.GeminiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val geminiService: GeminiService = GeminiService()
) : ViewModel() {

    private val defaultWelcome = Message(
        id = "welcome",
        role = MessageRole.ASSISTANT,
        content = "Oh, look who decided to show up! I'm Tappi, Kunal's much smarter and sassier wife. What do you need now, honey? Try not to make it too boring.",
        timestamp = System.currentTimeMillis()
    )

    private val _messages = MutableStateFlow<List<Message>>(listOf(defaultWelcome))
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _savedMessages = MutableStateFlow<List<SavedMessage>>(emptyList())
    val savedMessages: StateFlow<List<SavedMessage>> = _savedMessages.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _searchTerm = MutableStateFlow("")
    val searchTerm: StateFlow<String> = _searchTerm.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastUserQuery = MutableStateFlow("")
    val lastUserQuery: StateFlow<String> = _lastUserQuery.asStateFlow()

    private val _regenCount = MutableStateFlow(0)
    val regenCount: StateFlow<Int> = _regenCount.asStateFlow()

    val filteredMessages: StateFlow<List<Message>> = combine(_messages, _searchTerm) { msgs, query ->
        if (query.isBlank()) {
            msgs
        } else {
            msgs.filter { it.content.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(defaultWelcome))

    fun updateInput(text: String) {
        _inputText.value = text
    }

    fun updateSearchTerm(term: String) {
        _searchTerm.value = term
    }

    fun sendMessage() {
        val query = _inputText.value.trim()
        if (query.isBlank() || _isLoading.value) return

        _lastUserQuery.value = query
        _regenCount.value = 0
        _inputText.value = ""
        _searchTerm.value = ""

        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            role = MessageRole.USER,
            content = query
        )

        _messages.update { it + userMessage }
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val (reply, sources) = geminiService.query(
                    prompt = query,
                    settings = _settings.value,
                    regenCount = 0
                )
                val assistantMessage = Message(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.ASSISTANT,
                    content = reply,
                    sources = sources
                )
                _messages.update { it + assistantMessage }
            } catch (e: Exception) {
                val errorMessage = Message(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.ASSISTANT,
                    content = "Ugh, Kunal, something went wrong. Try again, dear."
                )
                _messages.update { it + errorMessage }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun regenerate() {
        val query = _lastUserQuery.value
        if (query.isBlank() || _isLoading.value) return

        val newCount = _regenCount.value + 1
        _regenCount.value = newCount
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val (reply, sources) = geminiService.query(
                    prompt = query,
                    settings = _settings.value,
                    regenCount = newCount
                )
                val assistantMessage = Message(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.ASSISTANT,
                    content = reply,
                    sources = sources
                )
                _messages.update { it + assistantMessage }
            } catch (e: Exception) {
                val errorMessage = Message(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.ASSISTANT,
                    content = "Kunal, stop it! You've broken me with your constant nagging."
                )
                _messages.update { it + errorMessage }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSaveWisdom(message: Message) {
        _savedMessages.update { list ->
            if (list.any { it.id == message.id }) {
                list.filter { it.id != message.id }
            } else {
                listOf(SavedMessage(id = message.id, content = message.content)) + list
            }
        }
    }

    fun removeSavedMessage(id: String) {
        _savedMessages.update { list -> list.filter { it.id != id } }
    }

    fun toggleDarkMode() {
        _settings.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun toggleSearchGrounding() {
        _settings.update { it.copy(useSearch = !it.useSearch) }
    }

    fun clearChatHistory() {
        _messages.value = listOf(defaultWelcome)
        _searchTerm.value = ""
        _lastUserQuery.value = ""
        _regenCount.value = 0
    }
}
