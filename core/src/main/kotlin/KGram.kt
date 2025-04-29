package ru.danl.kgram

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import org.telegram.telegrambots.meta.TelegramUrl
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPhoto
import org.telegram.telegrambots.meta.api.methods.send.*
import org.telegram.telegrambots.meta.api.methods.stickers.*
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest
import org.telegram.telegrambots.meta.api.objects.File
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.boost.ChatBoostRemoved
import org.telegram.telegrambots.meta.api.objects.boost.ChatBoostUpdated
import org.telegram.telegrambots.meta.api.objects.business.BusinessConnection
import org.telegram.telegrambots.meta.api.objects.business.BusinessMessagesDeleted
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberUpdated
import org.telegram.telegrambots.meta.api.objects.inlinequery.ChosenInlineQuery
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.payments.PaidMediaPurchased
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery
import org.telegram.telegrambots.meta.api.objects.payments.ShippingQuery
import org.telegram.telegrambots.meta.api.objects.polls.Poll
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionCountUpdated
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionUpdated
import ru.danl.kgram.handler.UpdateHandler
import ru.danl.kgram.handler.UpdatePropertyHandler
import java.io.Serializable
import kotlin.reflect.KFunction2

/**
 * Creates a new instance of [KGram] with the provided token and applies the configuration block.
 *
 * @param token The Telegram Bot API token.
 * @param configure A lambda to configure the [KGram] instance.
 * @return A configured [KGram] instance.
 */
fun kGram(token: String, configure: KGram.() -> Unit) =
    KGram(token = token).apply(configure)

/**
 * A class that provides a Kotlin-based interface for interacting with the Telegram Bot API using long polling.
 *
 * @param token The Telegram Bot API token used for authentication.
 */
class KGram internal constructor(
    private val token: String
) {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))
    private val telegramClient = OkHttpTelegramClient(token)
    private val updateHandlers = mutableListOf<UpdateHandler>()
    private val allowedUpdates = mutableSetOf<String>()
    private val telegramBotsLongPollingApplication = TelegramBotsLongPollingApplication()

    /**
     * Retrieves the JSON property value from a function's [JsonProperty] annotation.
     *
     * @return The value of the [JsonProperty] annotation.
     */
    private fun KFunction2<Update, *, Unit>.getJsonPropertyValue() =
        (annotations.single { it.annotationClass == JsonProperty::class } as JsonProperty).value

    /**
     * Starts the bot using long polling to receive updates from Telegram.
     * The bot will process updates and invoke registered handlers.
     *
     * @throws CancellationException If the coroutine is cancelled, the bot session is closed.
     */
    suspend fun start() = suspendCancellableCoroutine<Unit> { continuation ->
        val botSession = telegramBotsLongPollingApplication
            .registerBot(
                token,
                { TelegramUrl.DEFAULT_URL },
                { lastReceivedUpdate ->
                    GetUpdates
                        .builder()
                        .limit(100)
                        .timeout(50)
                        .offset(lastReceivedUpdate + 1)
                        .allowedUpdates(allowedUpdates)
                        .build()
                }
            ) { updates ->
                updates.forEach { update ->
                    coroutineScope.launch(
                        KGramUpdateContext(this)
                    ) {
                        updateHandlers.forEach {
                            it.handleUpdate(update)
                        }
                    }
                }
            }

        continuation.invokeOnCancellation {
            botSession.close()
        }
    }

    /**
     * Executes a generic Telegram Bot API method and returns the result.
     *
     * @param T The type of the result returned by the API method.
     * @param botApiMethod The API method to execute.
     * @return The result of the API method.
     */
    suspend fun <T : Serializable> execute(botApiMethod: BotApiMethod<T>) =
        telegramClient.executeAsync(botApiMethod).await()

    /**
     * Sends a document to a Telegram chat.
     *
     * @param sendDocument The document to send.
     * @return The result of the API call.
     */
    suspend fun execute(sendDocument: SendDocument) =
        telegramClient.executeAsync(sendDocument).await()

    /**
     * Sends a photo to a Telegram chat.
     *
     * @param sendPhoto The photo to send.
     * @return The result of the API call.
     */
    suspend fun execute(sendPhoto: SendPhoto) =
        telegramClient.executeAsync(sendPhoto).await()

    /**
     * Sets a webhook for receiving updates.
     *
     * @param setWebhook The webhook configuration.
     * @return The result of the API call.
     */
    suspend fun execute(setWebhook: SetWebhook) =
        telegramClient.executeAsync(setWebhook).await()

    /**
     * Sends a video to a Telegram chat.
     *
     * @param sendVideo The video to send.
     * @return The result of the API call.
     */
    suspend fun execute(sendVideo: SendVideo) =
        telegramClient.executeAsync(sendVideo).await()

    /**
     * Sends a video note to a Telegram chat.
     *
     * @param sendVideoNote The video note to send.
     * @return The result of the API call.
     */
    suspend fun execute(sendVideoNote: SendVideoNote) =
        telegramClient.executeAsync(sendVideoNote).await()

    /**
     * Sends a sticker to a Telegram chat.
     *
     * @param sendSticker The sticker to send.
     * @return The result of the API call.
     */
    suspend fun execute(sendSticker: SendSticker) =
        telegramClient.executeAsync(sendSticker).await()

    /**
     * Sends an audio file to a Telegram chat.
     *
     * @param sendAudio The audio file to send.
     * @return The result of the API call.
     */
    suspend fun execute(sendAudio: SendAudio) =
        telegramClient.executeAsync(sendAudio).await()

    /**
     * Sends a voice message to a Telegram chat.
     *
     * @param sendVoice The voice message to send.
     * @return The result of the API call.
     */
    suspend fun execute(sendVoice: SendVoice) =
        telegramClient.executeAsync(sendVoice).await()

    /**
     * Sends paid media to a Telegram chat.
     *
     * @param sendPaidMedia The paid media to send.
     * @return The result of the API call.
     */
    suspend fun execute(sendPaidMedia: SendPaidMedia) =
        telegramClient.executeAsync(sendPaidMedia).await()

    /**
     * Sends a media group to a Telegram chat.
     *
     * @param sendMediaGroup The media group to send.
     * @return The result of the API call.
     */
    suspend fun execute(sendMediaGroup: SendMediaGroup) =
        telegramClient.executeAsync(sendMediaGroup).await()

    /**
     * Sends an animation to a Telegram chat.
     *
     * @param sendAnimation The animation to send.
     * @return The result of the API call.
     */
    suspend fun execute(sendAnimation: SendAnimation) =
        telegramClient.executeAsync(sendAnimation).await()

    /**
     * Sets a chat photo for a Telegram chat.
     *
     * @param setChatPhoto The chat photo to set.
     * @return The result of the API call.
     */
    suspend fun execute(setChatPhoto: SetChatPhoto) =
        telegramClient.executeAsync(setChatPhoto).await()

    /**
     * Adds a sticker to an existing sticker set.
     *
     * @param addStickerToSet The sticker to add.
     * @return The result of the API call.
     */
    suspend fun execute(addStickerToSet: AddStickerToSet) =
        telegramClient.executeAsync(addStickerToSet).await()

    /**
     * Replaces a sticker in an existing sticker set.
     *
     * @param replaceStickerInSet The sticker replacement details.
     * @return The result of the API call.
     */
    suspend fun execute(replaceStickerInSet: ReplaceStickerInSet) =
        telegramClient.executeAsync(replaceStickerInSet).await()

    /**
     * Sets a thumbnail for a sticker set.
     *
     * @param setStickerSetThumbnail The thumbnail to set.
     * @return The result of the API call.
     */
    suspend fun execute(setStickerSetThumbnail: SetStickerSetThumbnail) =
        telegramClient.executeAsync(setStickerSetThumbnail).await()

    /**
     * Creates a new sticker set.
     *
     * @param createNewStickerSet The sticker set to create.
     * @return The result of the API call.
     */
    suspend fun execute(createNewStickerSet: CreateNewStickerSet) =
        telegramClient.executeAsync(createNewStickerSet).await()

    /**
     * Uploads a sticker file to Telegram.
     *
     * @param uploadStickerFile The sticker file to upload.
     * @return The result of the API call.
     */
    suspend fun execute(uploadStickerFile: UploadStickerFile) =
        telegramClient.executeAsync(uploadStickerFile).await()

    /**
     * Edits the media of an existing message.
     *
     * @param editMessageMedia The media edit details.
     * @return The result of the API call.
     */
    suspend fun execute(editMessageMedia: EditMessageMedia) =
        telegramClient.executeAsync(editMessageMedia).await()

    /**
     * Downloads a file from Telegram by its file object.
     *
     * @param file The file to download.
     * @return The downloaded file content.
     */
    suspend fun downloadFile(file: File) =
        telegramClient.downloadFileAsync(file).await()

    /**
     * Downloads a file from Telegram by its file path.
     *
     * @param filePath The path of the file to download.
     * @return The downloaded file content.
     */
    suspend fun downloadFile(filePath: String) =
        telegramClient.downloadFileAsync(filePath).await()

    /**
     * Downloads a file from Telegram as a stream by its file object.
     *
     * @param file The file to download.
     * @return The streamed file content.
     */
    suspend fun downloadFileAsStream(file: File) =
        telegramClient.downloadFileAsStreamAsync(file).await()

    /**
     * Downloads a file from Telegram as a stream by its file path.
     *
     * @param filePath The path of the file to download.
     * @return The streamed file content.
     */
    suspend fun downloadFileAsStream(filePath: String) =
        telegramClient.downloadFileAsStreamAsync(filePath).await()

    /**
     * Registers a handler for incoming messages.
     *
     * @param filter A suspend function to filter messages. Defaults to accepting all messages.
     * @param handler A suspend function to handle the message.
     */
    fun handleMessage(
        filter: suspend (Message) -> Boolean = { true },
        handler: suspend KGram.(Message) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getMessage, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setMessage.getJsonPropertyValue())
    }

    /**
     * Registers a handler for inline queries.
     *
     * @param filter A suspend function to filter inline queries. Defaults to accepting all queries.
     * @param handler A suspend function to handle the inline query.
     */
    fun handleInlineQuery(
        filter: suspend (InlineQuery) -> Boolean = { true },
        handler: suspend KGram.(InlineQuery) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getInlineQuery, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setInlineQuery.getJsonPropertyValue())
    }

    /**
     * Registers a handler for chosen inline query results.
     *
     * @param filter A suspend function to filter chosen inline queries. Defaults to accepting all.
     * @param handler A suspend function to handle the chosen inline query.
     */
    fun handleChosenInlineQuery(
        filter: suspend (ChosenInlineQuery) -> Boolean = { true },
        handler: suspend KGram.(ChosenInlineQuery) -> Unit
    ) {
        updateHandlers.add(
            UpdatePropertyHandler(
                Update::getChosenInlineQuery,
                filter = filter,
                handleProperty = handler
            )
        )
        allowedUpdates.add(Update::setChosenInlineQuery.getJsonPropertyValue())
    }

    /**
     * Registers a handler for callback queries.
     *
     * @param filter A suspend function to filter callback queries. Defaults to accepting all.
     * @param handler A suspend function to handle the callback query.
     */
    fun handleCallbackQuery(
        filter: suspend (CallbackQuery) -> Boolean = { true },
        handler: suspend KGram.(CallbackQuery) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getCallbackQuery, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setCallbackQuery.getJsonPropertyValue())
    }

    /**
     * Registers a handler for edited messages.
     *
     * @param filter A suspend function to filter edited messages. Defaults to accepting all.
     * @param handler A suspend function to handle the edited message.
     */
    fun handleEditedMessage(
        filter: suspend (Message) -> Boolean = { true },
        handler: suspend KGram.(Message) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getEditedMessage, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setEditedMessage.getJsonPropertyValue())
    }

    /**
     * Registers a handler for channel posts.
     *
     * @param filter A suspend function to filter channel posts. Defaults to accepting all.
     * @param handler A suspend function to handle the channel post.
     */
    fun handleChannelPost(
        filter: suspend (Message) -> Boolean = { true },
        handler: suspend KGram.(Message) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getChannelPost, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setChannelPost.getJsonPropertyValue())
    }

    /**
     * Registers a handler for edited channel posts.
     *
     * @param filter A suspend function to filter edited channel posts. Defaults to accepting all.
     * @param handler A suspend function to handle the edited channel post.
     */
    fun handleEditedChannelPost(
        filter: suspend (Message) -> Boolean = { true },
        handler: suspend KGram.(Message) -> Unit
    ) {
        updateHandlers.add(
            UpdatePropertyHandler(
                Update::getEditedChannelPost,
                filter = filter,
                handleProperty = handler
            )
        )
        allowedUpdates.add(Update::setEditedChannelPost.getJsonPropertyValue())
    }

    /**
     * Registers a handler for shipping queries.
     *
     * @param filter A suspend function to filter shipping queries. Defaults to accepting all.
     * @param handler A suspend function to handle the shipping query.
     */
    fun handleShippingQuery(
        filter: suspend (ShippingQuery) -> Boolean = { true },
        handler: suspend KGram.(ShippingQuery) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getShippingQuery, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setShippingQuery.getJsonPropertyValue())
    }

    /**
     * Registers a handler for pre-checkout queries.
     *
     * @param filter A suspend function to filter pre-checkout queries. Defaults to accepting all.
     * @param handler A suspend function to handle the pre-checkout query.
     */
    fun handlePreCheckoutQuery(
        filter: suspend (PreCheckoutQuery) -> Boolean = { true },
        handler: suspend KGram.(PreCheckoutQuery) -> Unit
    ) {
        updateHandlers.add(
            UpdatePropertyHandler(
                Update::getPreCheckoutQuery,
                filter = filter,
                handleProperty = handler
            )
        )
        allowedUpdates.add(Update::setPreCheckoutQuery.getJsonPropertyValue())
    }

    /**
     * Registers a handler for polls.
     *
     * @param filter A suspend function to filter polls. Defaults to accepting all.
     * @param handler A suspend function to handle the poll.
     */
    fun handlePoll(
        filter: suspend (Poll) -> Boolean = { true },
        handler: suspend KGram.(Poll) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getPoll, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setPoll.getJsonPropertyValue())
    }

    /**
     * Registers a handler for poll answers.
     *
     * @param filter A suspend function to filter poll answers. Defaults to accepting all.
     * @param handler A suspend function to handle the poll answer.
     */
    fun handlePollAnswer(
        filter: suspend (PollAnswer) -> Boolean = { true },
        handler: suspend KGram.(PollAnswer) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getPollAnswer, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setPollAnswer.getJsonPropertyValue())
    }

    /**
     * Registers a handler for updates to the bot's chat member status.
     *
     * @param filter A suspend function to filter chat member updates. Defaults to accepting all.
     * @param handler A suspend function to handle the chat member update.
     */
    fun handleMyChatMember(
        filter: suspend (ChatMemberUpdated) -> Boolean = { true },
        handler: suspend KGram.(ChatMemberUpdated) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getMyChatMember, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setMyChatMember.getJsonPropertyValue())
    }

    /**
     * Registers a handler for updates to a chat member's status.
     *
     * @param filter A suspend function to filter chat member updates. Defaults to accepting all.
     * @param handler A suspend function to handle the chat member update.
     */
    fun handleChatMember(
        filter: suspend (ChatMemberUpdated) -> Boolean = { true },
        handler: suspend KGram.(ChatMemberUpdated) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getChatMember, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setChatMember.getJsonPropertyValue())
    }

    /**
     * Registers a handler for chat join requests.
     *
     * @param filter A suspend function to filter chat join requests. Defaults to accepting all.
     * @param handler A suspend function to handle the chat join request.
     */
    fun handleChatJoinRequest(
        filter: suspend (ChatJoinRequest) -> Boolean = { true },
        handler: suspend KGram.(ChatJoinRequest) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getChatJoinRequest, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setChatJoinRequest.getJsonPropertyValue())
    }

    /**
     * Registers a handler for message reaction updates.
     *
     * @param filter A suspend function to filter message reaction updates. Defaults to accepting all.
     * @param handler A suspend function to handle the message reaction update.
     */
    fun handleMessageReaction(
        filter: suspend (MessageReactionUpdated) -> Boolean = { true },
        handler: suspend KGram.(MessageReactionUpdated) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getMessageReaction, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setMessageReaction.getJsonPropertyValue())
    }

    /**
     * Registers a handler for message reaction count updates.
     *
     * @param filter A suspend function to filter reaction count updates. Defaults to accepting all.
     * @param handler A suspend function to handle the reaction count update.
     */
    fun handleMessageReactionCount(
        filter: suspend (MessageReactionCountUpdated) -> Boolean = { true },
        handler: suspend KGram.(MessageReactionCountUpdated) -> Unit
    ) {
        updateHandlers.add(
            UpdatePropertyHandler(
                Update::getMessageReactionCount,
                filter = filter,
                handleProperty = handler
            )
        )
        allowedUpdates.add(Update::setMessageReactionCount.getJsonPropertyValue())
    }

    /**
     * Registers a handler for chat boost updates.
     *
     * @param filter A suspend function to filter chat boost updates. Defaults to accepting all.
     * @param handler A suspend function to handle the chat boost update.
     */
    fun handleChatBoost(
        filter: suspend (ChatBoostUpdated) -> Boolean = { true },
        handler: suspend KGram.(ChatBoostUpdated) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getChatBoost, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setChatBoost.getJsonPropertyValue())
    }

    /**
     * Registers a handler for removed chat boosts.
     *
     * @param filter A suspend function to filter removed chat boosts. Defaults to accepting all.
     * @param handler A suspend function to handle the removed chat boost.
     */
    fun handleRemovedChatBoost(
        filter: suspend (ChatBoostRemoved) -> Boolean = { true },
        handler: suspend KGram.(ChatBoostRemoved) -> Unit
    ) {
        updateHandlers.add(
            UpdatePropertyHandler(
                Update::getRemovedChatBoost,
                filter = filter,
                handleProperty = handler
            )
        )
        allowedUpdates.add(Update::setRemovedChatBoost.getJsonPropertyValue())
    }

    /**
     * Registers a handler for business connections.
     *
     * @param filter A suspend function to filter business connections. Defaults to accepting all.
     * @param handler A suspend function to handle the business connection.
     */
    fun handleBusinessConnection(
        filter: suspend (BusinessConnection) -> Boolean = { true },
        handler: suspend KGram.(BusinessConnection) -> Unit
    ) {
        updateHandlers.add(
            UpdatePropertyHandler(
                Update::getBusinessConnection,
                filter = filter,
                handleProperty = handler
            )
        )
        allowedUpdates.add(Update::setBusinessConnection.getJsonPropertyValue())
    }

    /**
     * Registers a handler for business messages.
     *
     * @param filter A suspend function to filter business messages. Defaults to accepting all.
     * @param handler A suspend function to handle the business message.
     */
    fun handleBusinessMessage(
        filter: suspend (Message) -> Boolean = { true },
        handler: suspend KGram.(Message) -> Unit
    ) {
        updateHandlers.add(UpdatePropertyHandler(Update::getBusinessMessage, filter = filter, handleProperty = handler))
        allowedUpdates.add(Update::setBusinessMessage.getJsonPropertyValue())
    }

    /**
     * Registers a handler for edited business messages.
     *
     * @param filter A suspend function to filter edited business messages. Defaults to accepting all.
     * @param handler A suspend function to handle the edited business message.
     */
    fun handleEditedBusinessMessage(
        filter: suspend (Message) -> Boolean = { true },
        handler: suspend KGram.(Message) -> Unit
    ) {
        updateHandlers.add(
            UpdatePropertyHandler(
                Update::getEditedBuinessMessage,
                filter = filter,
                handleProperty = handler
            )
        )
        allowedUpdates.add(Update::setEditedBuinessMessage.getJsonPropertyValue())
    }

    /**
     * Registers a handler for deleted business messages.
     *
     * @param filter A suspend function to filter deleted business messages. Defaults to accepting all.
     * @param handler A suspend function to handle the deleted business messages.
     */
    fun handleDeletedBusinessMessages(
        filter: suspend (BusinessMessagesDeleted) -> Boolean = { true },
        handler: suspend KGram.(BusinessMessagesDeleted) -> Unit
    ) {
        updateHandlers.add(
            UpdatePropertyHandler(
                Update::getDeletedBusinessMessages,
                filter = filter,
                handleProperty = handler
            )
        )
        allowedUpdates.add(Update::setDeletedBusinessMessages.getJsonPropertyValue())
    }

    /**
     * Registers a handler for purchased paid media.
     *
     * @param filter A suspend function to filter purchased paid media. Defaults to accepting all.
     * @param handler A suspend function to handle the purchased paid media.
     */
    fun handlePaidMediaPurchased(
        filter: suspend (PaidMediaPurchased) -> Boolean = { true },
        handler: suspend KGram.(PaidMediaPurchased) -> Unit
    ) {
        updateHandlers.add(
            UpdatePropertyHandler(
                Update::getPaidMediaPurchased,
                filter = filter,
                handleProperty = handler
            )
        )
        allowedUpdates.add(Update::setPaidMediaPurchased.getJsonPropertyValue())
    }
}

/**
 * Sends a Telegram Bot API method using a builder pattern.
 *
 * @param T The type of the result returned by the API method.
 * @param C The type of the API method.
 * @param B The builder type for the API method.
 * @param getBuilder A function that provides the builder instance.
 * @param block A lambda to configure the builder.
 * @return The result of the API method.
 */
suspend inline fun <T : Serializable, C : BotApiMethod<T>, B : BotApiMethod.BotApiMethodBuilder<out T, out C, *>> KGram.send(
    getBuilder: () -> B,
    block: B.() -> Unit = {}
) = execute(getBuilder().apply(block).build())

/**
 * Sends a document using a builder pattern.
 *
 * @param block A lambda to configure the [SendDocument] builder.
 * @return The result of the API call.
 */
suspend fun KGram.sendDocument(block: SendDocument.SendDocumentBuilder<*, *> .() -> Unit) =
    execute(SendDocument.builder().apply(block).build())

/**
 * Sends a photo using a builder pattern.
 *
 * @param block A lambda to configure the [SendPhoto] builder.
 * @return The result of the API call.
 */
suspend fun KGram.sendPhoto(block: SendPhoto.SendPhotoBuilder<*, *> .() -> Unit) =
    execute(SendPhoto.builder().apply(block).build())

/**
 * Sets a webhook using a builder pattern.
 *
 * @param block A lambda to configure the [SetWebhook] builder.
 * @return The result of the API call.
 */
suspend fun KGram.setWebhook(block: SetWebhook.SetWebhookBuilder<*, *> .() -> Unit) =
    execute(SetWebhook.builder().apply(block).build())

/**
 * Sends a video using a builder pattern.
 *
 * @param block A lambda to configure the [SendVideo] builder.
 * @return The result of the API call.
 */
suspend fun KGram.sendVideo(block: SendVideo.SendVideoBuilder<*, *> .() -> Unit) =
    execute(SendVideo.builder().apply(block).build())

/**
 * Sends a video note using a builder pattern.
 *
 * @param block A lambda to configure the [SendVideoNote] builder.
 * @return The result of the API call.
 */
suspend fun KGram.sendVideoNote(block: SendVideoNote.SendVideoNoteBuilder<*, *> .() -> Unit) =
    execute(SendVideoNote.builder().apply(block).build())

/**
 * Sends a sticker using a builder pattern.
 *
 * @param block A lambda to configure the [SendSticker] builder.
 * @return The result of the API call.
 */
suspend fun KGram.sendSticker(block: SendSticker.SendStickerBuilder<*, *> .() -> Unit) =
    execute(SendSticker.builder().apply(block).build())

/**
 * Sends an audio file using a builder pattern.
 *
 * @param block A lambda to configure the [SendAudio] builder.
 * @return The result of the API call.
 */
suspend fun KGram.sendAudio(block: SendAudio.SendAudioBuilder<*, *> .() -> Unit) =
    execute(SendAudio.builder().apply(block).build())

/**
 * Sends a voice message using a builder pattern.
 *
 * @param block A lambda to configure the [SendVoice] builder.
 * @return The result of the API call.
 */
suspend fun KGram.sendVoice(block: SendVoice.SendVoiceBuilder<*, *> .() -> Unit) =
    execute(SendVoice.builder().apply(block).build())

/**
 * Sends paid media using a builder pattern.
 *
 * @param block A lambda to configure the [SendPaidMedia] builder.
 * @return The result of the API call.
 */
suspend fun KGram.sendPaidMedia(block: SendPaidMedia.SendPaidMediaBuilder<*, *> .() -> Unit) =
    execute(SendPaidMedia.builder().apply(block).build())

/**
 * Sends a media group using a builder pattern.
 *
 * @param block A lambda to configure the [SendMediaGroup] builder.
 * @return The result of the API call.
 */
suspend fun KGram.sendMediaGroup(block: SendMediaGroup.SendMediaGroupBuilder<*, *> .() -> Unit) =
    execute(SendMediaGroup.builder().apply(block).build())

/**
 * Sends an animation using a builder pattern.
 *
 * @param block A lambda to configure the [SendAnimation] builder.
 * @return The result of the API call.
 */
suspend fun KGram.sendAnimation(block: SendAnimation.SendAnimationBuilder<*, *> .() -> Unit) =
    execute(SendAnimation.builder().apply(block).build())

/**
 * Sets a chat photo using a builder pattern.
 *
 * @param block A lambda to configure the [SetChatPhoto] builder.
 * @return The result of the API call.
 */
suspend fun KGram.setChatPhoto(block: SetChatPhoto.SetChatPhotoBuilder<*, *> .() -> Unit) =
    execute(SetChatPhoto.builder().apply(block).build())

/**
 * Adds a sticker to a set using a builder pattern.
 *
 * @param block A lambda to configure the [AddStickerToSet] builder.
 * @return The result of the API call.
 */
suspend fun KGram.addStickerToSet(block: AddStickerToSet.AddStickerToSetBuilder<*, *> .() -> Unit) =
    execute(AddStickerToSet.builder().apply(block).build())

/**
 * Replaces a sticker in a set using a builder pattern.
 *
 * @param block A lambda to configure the [ReplaceStickerInSet] builder.
 * @return The result of the API call.
 */
suspend fun KGram.replaceStickerInSet(block: ReplaceStickerInSet.ReplaceStickerInSetBuilder<*, *> .() -> Unit) =
    execute(ReplaceStickerInSet.builder().apply(block).build())

/**
 * Sets a sticker set thumbnail using a builder pattern.
 *
 * @param block A lambda to configure the [SetStickerSetThumbnail] builder.
 * @return The result of the API call.
 */
suspend fun KGram.setStickerSetThumbnail(block: SetStickerSetThumbnail.SetStickerSetThumbnailBuilder<*, *> .() -> Unit) =
    execute(SetStickerSetThumbnail.builder().apply(block).build())

/**
 * Creates a new sticker set using a builder pattern.
 *
 * @param block A lambda to configure the [CreateNewStickerSet] builder.
 * @return The result of the API call.
 */
suspend fun KGram.createNewStickerSet(block: CreateNewStickerSet.CreateNewStickerSetBuilder<*, *> .() -> Unit) =
    execute(CreateNewStickerSet.builder().apply(block).build())

/**
 * Uploads a sticker file using a builder pattern.
 *
 * @param block A lambda to configure the [UploadStickerFile] builder.
 * @return The result of the API call.
 */
suspend fun KGram.uploadStickerFile(block: UploadStickerFile.UploadStickerFileBuilder<*, *> .() -> Unit) =
    execute(UploadStickerFile.builder().apply(block).build())

/**
 * Edits message media using a builder pattern.
 *
 * @param block A lambda to configure the [EditMessageMedia] builder.
 * @return The result of the API call.
 */
suspend fun KGram.editMessageMedia(block: EditMessageMedia.EditMessageMediaBuilder<*, *> .() -> Unit) =
    execute(EditMessageMedia.builder().apply(block).build())