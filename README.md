[![](https://jitpack.io/v/danl12/kgram.svg)](https://jitpack.io/#danl12/kgram)
# KGram

KGram is a Kotlin library for building Telegram bots using the Telegram Bot API. It is built on top of the Java library [TelegramBots](https://github.com/rubenlagus/TelegramBots), providing a Kotlin-idiomatic, type-safe, and coroutine-based interface for handling updates, sending messages, and managing user states. The library is designed to simplify bot development while leveraging the robust foundation of `TelegramBots`.

KGram is split into two modules:
- **core**: Provides essential functionality for interacting with the Telegram Bot API, including update handling, message sending, and inline keyboard creation.
- **states**: Extends the core module with state management capabilities for complex user interactions.

## Features
- **Core Module**:
    - Long-polling support for receiving Telegram updates, powered by `TelegramBots`.
    - Type-safe API methods for sending messages, media, stickers, and more.
    - Inline keyboard builder for creating interactive buttons.
    - Coroutine-based architecture for asynchronous, non-blocking operations.
- **States Module**:
    - State management for tracking user interactions with persistent states.
    - Customizable state stores for flexible state persistence.
    - Type-safe state handling with context-aware state transitions.
- **Built on TelegramBots**:
    - Leverages the battle-tested `TelegramBots` Java library for reliable communication with the Telegram API.
    - Enhances `TelegramBots` with Kotlin-specific features like coroutines and DSLs.

## Getting Started

### Prerequisites
- Gradle or Maven for dependency management
- A Telegram Bot token obtained from [BotFather](https://t.me/BotFather)

### Installation
KGram is available on JitPack. To use it, first add the JitPack repository to your project. For Gradle, include the following in your build.gradle.kts:

```kotlin
repositories {
    maven("https://jitpack.io")
}
```
Then, add the KGram modules and the optional TelegramBots meta dependency:

```kotlin
// Core module (required)
implementation("com.github.danl12.kgram:core:<version>")

// States module (optional, for state management)
implementation("com.github.danl12.kgram:states:<version>")

// Optional, needed if KGram uses old API
implementation("org.telegram:telegrambots-meta:<telegrambots-version>")
```

Replace `<version>` with the latest version of the library. The `core` module is required, while the `states` module is optional and only needed if you plan to use state management.

### Example 1: Simple Bot Using Core Module
This example demonstrates a basic Telegram bot using only the `core` module. The bot responds to text messages with a greeting and an inline keyboard.

```kotlin
fun main(): Unit = runBlocking {
    kGram(token = "YOUR_BOT_TOKEN") {
        // Handle incoming messages
        handleMessage { message: Message ->
            sendPhoto {
                chatId(message.chatId.toString())
                photo(InputFile("https://example.com/image.jpg"))
                caption("Hello, ${message.from.firstName}!")
                replyMarkup(inlineKeyboardMarkup {
                    row {
                        button("Visit Website", url = "https://example.com")
                        button("Say Hi", callbackData = "hi")
                    }
                })
            }
        }

        // Handle callback queries from inline buttons
        handleCallbackQuery { callbackQuery ->
            if (callbackQuery.data == "hi") {
                sendPhoto {
                    chatId(callbackQuery.message.chatId.toString())
                    photo(InputFile("https://example.com/hi.jpg"))
                    caption("You said hi!")
                }
            }
        }
    }.start() // Start the bot
}
```

In this example:
- The bot uses the `core` module to listen for messages and callback queries.
- It sends a photo with a greeting and an inline keyboard containing two buttons.
- Clicking the "Say Hi" button triggers a callback query, which the bot handles by sending another photo.
- The `start()` function initiates long-polling to receive updates.

### Example 2: Bot With State Management Using Core and States Modules
This example demonstrates a bot that uses both the `core` and `states` modules to guide a user through a registration process with state management.

```kotlin
// Define states
sealed class UserState {
    data object WaitingForName : UserState()
    data object WaitingForAge : UserState()
    data object Registered : UserState()
}

// Define global state
data class UserData(val name: String = "", val age: String = "")

// State handler for WaitingForName
object WaitingForNameHandler : StateHandler<UserState, UserState.WaitingForName, UserData> {
    override suspend fun StateContext<UserState, UserState.WaitingForName, UserData>.handleMessage(message: Message) {
        val name = message.text ?: return
        updateGlobalState { it.copy(name = name) }
        setCurrentState(UserState.WaitingForAge)
        kGram.sendPhoto {
            chatId(message.chatId.toString())
            photo(InputFile("https://example.com/name_received.jpg"))
            caption("Got your name! Now, please send your age.")
        }
    }

    override suspend fun StateContext<UserState, UserState.WaitingForName, UserData>.handleState() {
        kGram.sendPhoto {
            chatId(userId.toString())
            photo(InputFile("https://example.com/welcome.jpg"))
            caption("Please send your name.")
        }
    }
}

// State handler for WaitingForAge
object WaitingForAgeHandler : StateHandler<UserState, UserState.WaitingForAge, UserData> {
    override suspend fun StateContext<UserState, UserState.WaitingForAge, UserData>.handleMessage(message: Message) {
        val age = message.text ?: return
        updateGlobalState { it.copy(age = age) }
        setCurrentState(UserState.Registered)
        kGram.sendPhoto {
            chatId(message.chatId.toString())
            photo(InputFile("https://example.com/registered.jpg"))
            caption("Registration complete! Name: ${globalState.name}, Age: $age")
        }
    }

    override suspend fun StateContext<UserState, UserState.WaitingForAge, UserData>.handleState() {
        kGram.sendPhoto {
            chatId(userId.toString())
            photo(InputFile("https://example.com/age_prompt.jpg"))
            caption("Please send your age.")
        }
    }
}

// State handler for Registered
object RegisteredHandler : StateHandler<UserState, UserState.Registered, UserData> {
    override suspend fun StateContext<UserState, UserState.Registered, UserData>.handleState() {
        kGram.sendPhoto {
            chatId(userId.toString())
            photo(InputFile("https://example.com/thank_you.jpg"))
            caption("You're registered! Name: ${globalState.name}, Age: ${globalState.age}")
        }
    }
}

fun main(): Unit = runBlocking {
    kGram(token = "YOUR_BOT_TOKEN") {
        // Configure state management using the states module
        states {
            handleState<UserState.WaitingForName>(WaitingForNameHandler)
            handleState<UserState.WaitingForAge>(WaitingForAgeHandler)
            handleState<UserState.Registered>(RegisteredHandler)

            // Handle initial message to start registration
            handleMessage { message ->
                set(message.from.id, UserState.WaitingForName, UserData())
            }
        }
    }.start() // Start the bot
}
```

In this example:
- The bot uses the `core` module for sending messages and handling updates.
- The `states` module is used to manage a state machine with three states: `WaitingForName`, `WaitingForAge`, and `Registered`.
- Each state has a handler that processes messages and transitions to the next state.
- The `UserData` global state stores the user's name and age.
- The bot guides the user through a registration process, prompting for their name and age, and confirms registration upon completion.

### Running the Bot
1. Replace `"YOUR_BOT_TOKEN"` with your actual Telegram Bot token.
2. Ensure the `core` module (and `states` module for the second example) is included in your project dependencies.
3. Run the `main` function in your Kotlin application.
4. Interact with your bot on Telegram to see it in action.

## License
This project is licensed under the MIT License.