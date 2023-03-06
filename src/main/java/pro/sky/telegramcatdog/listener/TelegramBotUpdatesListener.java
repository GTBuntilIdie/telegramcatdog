package pro.sky.telegramcatdog.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegramcatdog.model.Volunteer;
import pro.sky.telegramcatdog.repository.VolunteerRepository;

import java.util.List;

import static pro.sky.telegramcatdog.constants.Constants.*;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private TelegramBot telegramBot;
    private final VolunteerRepository volunteerRepository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, VolunteerRepository volunteerRepository) {
        this.telegramBot = telegramBot;
        this.volunteerRepository = volunteerRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {

            logger.info("Processing update: {}", update);

            // Process welcome message (stage 0)
            if (update.message() != null) {
                String incomeMsgText = update.message().text();
                // For stickers incomeMsgText is null
                if (incomeMsgText == null) {
                    return;
                }
                long chatId = update.message().chat().id();
                if (incomeMsgText.equals("/start")) {
                    SendMessage message = new SendMessage(chatId, WELCOME_MSG_TEXT);
                    // Adding buttons
                    message.replyMarkup(createButtonsStage0());
                    sendMessage(message);
                }
            }
            // Process buttons clicks
            else {
                processButtonClick(update);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendMessage(SendMessage message) {
        SendResponse response = telegramBot.execute(message);
        if (!response.isOk()) {
            logger.warn("Message was not sent: {}, error code: {}", message, response.errorCode());
        }
    }

    /**
     * Creates the buttons for the welcome message (reply to the /start command) at Stage 0
     * @return InlineKeyboardMarkup
     */
    private InlineKeyboardMarkup createButtonsStage0() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton buttonStage1 = new InlineKeyboardButton(BUTTON_STAGE1_TEXT).callbackData(BUTTON_STAGE1_CALLBACK_TEXT);
        InlineKeyboardButton buttonStage2 = new InlineKeyboardButton(BUTTON_STAGE2_TEXT).callbackData(BUTTON_STAGE2_CALLBACK_TEXT);
        InlineKeyboardButton buttonStage3 = new InlineKeyboardButton(BUTTON_STAGE3_TEXT).callbackData(BUTTON_STAGE3_CALLBACK_TEXT);
        InlineKeyboardButton buttonCallVolunteer = new InlineKeyboardButton(BUTTON_CALL_VOLUNTEER_TEXT).callbackData(BUTTON_CALL_VOLUNTEER_CALLBACK_TEXT);
        inlineKeyboardMarkup.addRow(buttonStage1);
        inlineKeyboardMarkup.addRow(buttonStage2);
        inlineKeyboardMarkup.addRow(buttonStage3);
        inlineKeyboardMarkup.addRow(buttonCallVolunteer);
        return inlineKeyboardMarkup;
    }

    /**
     * Process button clicks from the user.
     *
     * @param update user input (can be text, button click, emoji, sticker, etc.)
     *               but process only button clicks with {@code callbackData()} defined.
     * @see InlineKeyboardButton#callbackData()
     */
    private void processButtonClick(Update update) {
        //SendMessage message = null;
        CallbackQuery callbackQuery = update.callbackQuery();
        if (callbackQuery != null) {
            long chatId = callbackQuery.message().chat().id();
            if (callbackQuery.data().equals(BUTTON_STAGE1_CALLBACK_TEXT)) {
                // General info about the shelter (stage 1)
                sendMessage(new SendMessage(chatId, BUTTON_STAGE1_CALLBACK_TEXT));
            } else if (callbackQuery.data().equals(BUTTON_STAGE2_CALLBACK_TEXT)) {
                // How to adopt a dog (stage 2)
                sendMessage(new SendMessage(chatId, BUTTON_STAGE2_CALLBACK_TEXT));
            } else if (callbackQuery.data().equals(BUTTON_STAGE3_CALLBACK_TEXT)) {
                // Send a follow-up report (stage 3)
                sendMessage(new SendMessage(chatId, BUTTON_STAGE3_CALLBACK_TEXT));
            } else if (callbackQuery.data().equals(BUTTON_CALL_VOLUNTEER_CALLBACK_TEXT)) {
                // Call a volunteer
                sendMessage(new SendMessage(chatId, BUTTON_CALL_VOLUNTEER_CALLBACK_TEXT));
                callVolunteer(update);
            }
        }
    }

    /**
     * Generates and sends message to volunteer from volunteers table.
     * If {@code @username} of the guest is defined it refers him by his {@code @username}.
     * Otherwise, it refers him by his {@code chat_id}.
     * If volunteers table is empty - sends {@code NO_VOLUNTEERS_TEXT} message.
     *
     * @param update 'Call a volunteer' button click.
     */
    private void callVolunteer(Update update) {
        String userId = "";
        long chatId = 0;
        userId += update.callbackQuery().from().id();
        logger.info("UserId = {}", userId);
        // To do: select random volunteer. Now it always selects the 1st one.
        Volunteer volunteer = volunteerRepository.findById(1L).orElse(null);
        if (volunteer == null) {
            // Guest chat_id. Send message to the guest.
            chatId = update.callbackQuery().message().chat().id();
            SendMessage message = new SendMessage(chatId, NO_VOLUNTEERS_TEXT);
            sendMessage(message);
        } else {
            // Volunteer chat_id. Send message to volunteer.
            chatId = volunteer.getChatId();
            if (update.callbackQuery().from().username() != null) {
                userId = "@" + update.callbackQuery().from().username();
                SendMessage message = new SendMessage(chatId, String.format(CONTACT_TELEGRAM_USERNAME_TEXT, userId));
                sendMessage(message);
            } else {
                SendMessage message = new SendMessage(chatId, String.format(CONTACT_TELEGRAM_ID_TEXT, userId));
                sendMessage(message);
            }
        }
    }
}
