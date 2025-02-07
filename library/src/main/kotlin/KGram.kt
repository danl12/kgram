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
import ru.danl.kgram.KGram.Config
import ru.danl.kgram.handler.UpdateHandler
import ru.danl.kgram.handler.UpdatePropertyHandler
import java.io.Serializable
import kotlin.reflect.KFunction2

suspend fun startKGram(token: String, configure: Config.() -> Unit) =
    KGram(config = Config(token).apply(configure)).apply { start() }

class KGram internal constructor(
    private val config: Config
) {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))
    private val telegramClient = OkHttpTelegramClient(config.token)

    private fun KFunction2<Update, *, Unit>.getJsonPropertyValue() =
        (annotations.single { it.annotationClass == JsonProperty::class } as JsonProperty).value

    private val telegramBotsLongPollingApplication = TelegramBotsLongPollingApplication()

    suspend fun start() = suspendCancellableCoroutine<Unit> { continuation ->
        val allowedUpdates = config.allowedUpdates.map { it.getJsonPropertyValue() }
        val botSession = telegramBotsLongPollingApplication
            .registerBot(
                config.token,
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
                        config.updateHandlers.forEach {
                            it.handleUpdate(this@KGram, update)
                        }
                    }
                }
            }

        continuation.invokeOnCancellation {
            botSession.close()
        }
    }

    suspend fun <T : Serializable> execute(botApiMethod: BotApiMethod<T>) =
        telegramClient.executeAsync(botApiMethod).await()

    suspend fun execute(sendDocument: SendDocument) =
        telegramClient.executeAsync(sendDocument).await()

    suspend fun execute(sendPhoto: SendPhoto) =
        telegramClient.executeAsync(sendPhoto).await()

    suspend fun execute(setWebhook: SetWebhook) =
        telegramClient.executeAsync(setWebhook).await()

    suspend fun execute(sendVideo: SendVideo) =
        telegramClient.executeAsync(sendVideo).await()

    suspend fun execute(sendVideoNote: SendVideoNote) =
        telegramClient.executeAsync(sendVideoNote).await()

    suspend fun execute(sendSticker: SendSticker) =
        telegramClient.executeAsync(sendSticker).await()

    suspend fun execute(sendAudio: SendAudio) =
        telegramClient.executeAsync(sendAudio).await()

    suspend fun execute(sendVoice: SendVoice) =
        telegramClient.executeAsync(sendVoice).await()

    suspend fun execute(sendPaidMedia: SendPaidMedia) =
        telegramClient.executeAsync(sendPaidMedia).await()

    suspend fun execute(sendMediaGroup: SendMediaGroup) =
        telegramClient.executeAsync(sendMediaGroup).await()

    suspend fun execute(sendAnimation: SendAnimation) =
        telegramClient.executeAsync(sendAnimation).await()

    suspend fun execute(setChatPhoto: SetChatPhoto) =
        telegramClient.executeAsync(setChatPhoto).await()

    suspend fun execute(addStickerToSet: AddStickerToSet) =
        telegramClient.executeAsync(addStickerToSet).await()

    suspend fun execute(replaceStickerInSet: ReplaceStickerInSet) =
        telegramClient.executeAsync(replaceStickerInSet).await()

    suspend fun execute(setStickerSetThumbnail: SetStickerSetThumbnail) =
        telegramClient.executeAsync(setStickerSetThumbnail).await()

    suspend fun execute(createNewStickerSet: CreateNewStickerSet) =
        telegramClient.executeAsync(createNewStickerSet).await()

    suspend fun execute(uploadStickerFile: UploadStickerFile) =
        telegramClient.executeAsync(uploadStickerFile).await()

    suspend fun execute(editMessageMedia: EditMessageMedia) =
        telegramClient.executeAsync(editMessageMedia).await()

    suspend fun downloadFile(file: File) =
        telegramClient.downloadFileAsync(file).await()

    suspend fun downloadFile(filePath: String) =
        telegramClient.downloadFileAsync(filePath).await()

    suspend fun downloadFileAsStream(file: File) =
        telegramClient.downloadFileAsStreamAsync(file).await()

    suspend fun downloadFileAsStream(filePath: String) =
        telegramClient.downloadFileAsStreamAsync(filePath).await()

    class Config internal constructor(
        val token: String,
    ) {

        internal val updateHandlers = mutableListOf<UpdateHandler>()
        internal val allowedUpdates = mutableListOf<KFunction2<Update, *, Unit>>()

        fun handleMessage(
            filter: suspend (Message) -> Boolean = { true },
            handler: suspend KGram.(Message) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getMessage, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setMessage)
        }

        fun handleInlineQuery(
            filter: suspend (InlineQuery) -> Boolean = { true },
            handler: suspend KGram.(InlineQuery) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getInlineQuery, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setInlineQuery)
        }

        fun handleChosenInlineQuery(
            filter: suspend (ChosenInlineQuery) -> Boolean = { true },
            handler: suspend KGram.(ChosenInlineQuery) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getChosenInlineQuery, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setChosenInlineQuery)
        }

        fun handleCallbackQuery(
            filter: suspend (CallbackQuery) -> Boolean = { true },
            handler: suspend KGram.(CallbackQuery) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getCallbackQuery, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setCallbackQuery)
        }

        fun handleEditedMessage(
            filter: suspend (Message) -> Boolean = { true },
            handler: suspend KGram.(Message) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getEditedMessage, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setEditedMessage)
        }

        fun handleChannelPost(
            filter: suspend (Message) -> Boolean = { true },
            handler: suspend KGram.(Message) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getChannelPost, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setChannelPost)
        }

        fun handleEditedChannelPost(
            filter: suspend (Message) -> Boolean = { true },
            handler: suspend KGram.(Message) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getEditedChannelPost, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setEditedChannelPost)
        }

        fun handleShippingQuery(
            filter: suspend (ShippingQuery) -> Boolean = { true },
            handler: suspend KGram.(ShippingQuery) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getShippingQuery, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setShippingQuery)
        }

        fun handlePreCheckoutQuery(
            filter: suspend (PreCheckoutQuery) -> Boolean = { true },
            handler: suspend KGram.(PreCheckoutQuery) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getPreCheckoutQuery, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setPreCheckoutQuery)
        }

        fun handlePoll(
            filter: suspend (Poll) -> Boolean = { true },
            handler: suspend KGram.(Poll) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getPoll, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setPoll)
        }

        fun handlePollAnswer(
            filter: suspend (PollAnswer) -> Boolean = { true },
            handler: suspend KGram.(PollAnswer) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getPollAnswer, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setPollAnswer)
        }

        fun handleMyChatMember(
            filter: suspend (ChatMemberUpdated) -> Boolean = { true },
            handler: suspend KGram.(ChatMemberUpdated) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getMyChatMember, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setMyChatMember)
        }

        fun handleChatMember(
            filter: suspend (ChatMemberUpdated) -> Boolean = { true },
            handler: suspend KGram.(ChatMemberUpdated) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getChatMember, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setChatMember)
        }

        fun handleChatJoinRequest(
            filter: suspend (ChatJoinRequest) -> Boolean = { true },
            handler: suspend KGram.(ChatJoinRequest) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getChatJoinRequest, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setChatJoinRequest)
        }

        fun handleMessageReaction(
            filter: suspend (MessageReactionUpdated) -> Boolean = { true },
            handler: suspend KGram.(MessageReactionUpdated) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getMessageReaction, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setMessageReaction)
        }

        fun handleMessageReactionCount(
            filter: suspend (MessageReactionCountUpdated) -> Boolean = { true },
            handler: suspend KGram.(MessageReactionCountUpdated) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getMessageReactionCount, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setMessageReactionCount)
        }

        fun handleChatBoost(
            filter: suspend (ChatBoostUpdated) -> Boolean = { true },
            handler: suspend KGram.(ChatBoostUpdated) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getChatBoost, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setChatBoost)
        }

        fun handleRemovedChatBoost(
            filter: suspend (ChatBoostRemoved) -> Boolean = { true },
            handler: suspend KGram.(ChatBoostRemoved) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getRemovedChatBoost, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setRemovedChatBoost)
        }

        fun handleBusinessConnection(
            filter: suspend (BusinessConnection) -> Boolean = { true },
            handler: suspend KGram.(BusinessConnection) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getBusinessConnection, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setBusinessConnection)
        }

        fun handleBusinessMessage(
            filter: suspend (Message) -> Boolean = { true },
            handler: suspend KGram.(Message) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getBusinessMessage, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setBusinessMessage)
        }

        fun handleEditedBusinessMessage(
            filter: suspend (Message) -> Boolean = { true },
            handler: suspend KGram.(Message) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getEditedBuinessMessage, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setEditedBuinessMessage)
        }

        fun handleDeletedBusinessMessages(
            filter: suspend (BusinessMessagesDeleted) -> Boolean = { true },
            handler: suspend KGram.(BusinessMessagesDeleted) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getDeletedBusinessMessages, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setDeletedBusinessMessages)
        }

        fun handlePaidMediaPurchased(
            filter: suspend (PaidMediaPurchased) -> Boolean = { true },
            handler: suspend KGram.(PaidMediaPurchased) -> Unit
        ) {
            updateHandlers.add(UpdatePropertyHandler(Update::getPaidMediaPurchased, filter = filter, handleProperty = handler))
            allowedUpdates.add(Update::setPaidMediaPurchased)
        }
    }
}

suspend inline fun <T: Serializable, C : BotApiMethod<T>, B : BotApiMethod.BotApiMethodBuilder<out T, out C, *>> KGram.send(
    getBuilder: () -> B,
    block: B.() -> Unit = {}
) = execute(getBuilder().apply(block).build())

suspend fun KGram.sendDocument(block: SendDocument.SendDocumentBuilder<*, *> .() -> Unit) =
    execute(SendDocument.builder().apply(block).build())

suspend fun KGram.sendPhoto(block: SendPhoto.SendPhotoBuilder<*, *> .() -> Unit) =
    execute(SendPhoto.builder().apply(block).build())

suspend fun KGram.setWebhook(block: SetWebhook.SetWebhookBuilder<*, *> .() -> Unit) =
    execute(SetWebhook.builder().apply(block).build())

suspend fun KGram.sendVideo(block: SendVideo.SendVideoBuilder<*, *> .() -> Unit) =
    execute(SendVideo.builder().apply(block).build())

suspend fun KGram.sendVideoNote(block: SendVideoNote.SendVideoNoteBuilder<*, *> .() -> Unit) =
    execute(SendVideoNote.builder().apply(block).build())

suspend fun KGram.sendSticker(block: SendSticker.SendStickerBuilder<*, *> .() -> Unit) =
    execute(SendSticker.builder().apply(block).build())

suspend fun KGram.sendAudio(block: SendAudio.SendAudioBuilder<*, *> .() -> Unit) =
    execute(SendAudio.builder().apply(block).build())

suspend fun KGram.sendVoice(block: SendVoice.SendVoiceBuilder<*, *> .() -> Unit) =
    execute(SendVoice.builder().apply(block).build())

suspend fun KGram.sendPaidMedia(block: SendPaidMedia.SendPaidMediaBuilder<*, *> .() -> Unit) =
    execute(SendPaidMedia.builder().apply(block).build())

suspend fun KGram.sendMediaGroup(block: SendMediaGroup.SendMediaGroupBuilder<*, *> .() -> Unit) =
    execute(SendMediaGroup.builder().apply(block).build())

suspend fun KGram.sendAnimation(block: SendAnimation.SendAnimationBuilder<*, *> .() -> Unit) =
    execute(SendAnimation.builder().apply(block).build())

suspend fun KGram.setChatPhoto(block: SetChatPhoto.SetChatPhotoBuilder<*, *> .() -> Unit) =
    execute(SetChatPhoto.builder().apply(block).build())

suspend fun KGram.addStickerToSet(block: AddStickerToSet.AddStickerToSetBuilder<*, *> .() -> Unit) =
    execute(AddStickerToSet.builder().apply(block).build())

suspend fun KGram.replaceStickerInSet(block: ReplaceStickerInSet.ReplaceStickerInSetBuilder<*, *> .() -> Unit) =
    execute(ReplaceStickerInSet.builder().apply(block).build())

suspend fun KGram.setStickerSetThumbnail(block: SetStickerSetThumbnail.SetStickerSetThumbnailBuilder<*, *> .() -> Unit) =
    execute(SetStickerSetThumbnail.builder().apply(block).build())

suspend fun KGram.createNewStickerSet(block: CreateNewStickerSet.CreateNewStickerSetBuilder<*, *> .() -> Unit) =
    execute(CreateNewStickerSet.builder().apply(block).build())

suspend fun KGram.uploadStickerFile(block: UploadStickerFile.UploadStickerFileBuilder<*, *> .() -> Unit) =
    execute(UploadStickerFile.builder().apply(block).build())

suspend fun KGram.editMessageMedia(block: EditMessageMedia.EditMessageMediaBuilder<*, *> .() -> Unit) =
    execute(EditMessageMedia.builder().apply(block).build())