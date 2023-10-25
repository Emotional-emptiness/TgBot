package com.example.TgBot.servise;

import com.example.TgBot.config.BotConfig;
import com.example.TgBot.model.User;
import com.example.TgBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot{
    final BotConfig config;
    @Autowired
    private UserRepository userRepository;
    static final String HELP_TEXT = "This is created to demonstrate Spring capabilities.\n\n" +
            "U can execute commands from the main menu on the left or by typing command:\n\n" +
            "Type /start to see u name message\n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again\n\n" +
            "Type /settings to see settings bot";
    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start","get a u name"));
        listOfCommands.add(new BotCommand("/mydata","get my data stored"));
        listOfCommands.add(new BotCommand("/deletedata","delete my data"));
        listOfCommands.add(new BotCommand("/help","info how to use this bot"));
        listOfCommands.add(new BotCommand("/settings","set your preferences"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error settings bot's command list: " + e.getMessage());
        }
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start": {
                    registerUser(update.getMessage());
                    StartCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                } break;
                case "/help":{
                    SendMessageMethod(chatId,HELP_TEXT);
                } break;
                default: {
                    SendMessageMethod(chatId, "this command is not supported");
                }
            }
        }
    }

    private void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();
            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("User saved " + user);
        }
    }

    private void StartCommandReceived(long chatID, String name) {
        String answer = "u name -> : " + name;
        SendMessageMethod(chatID, answer);
        log.info("Replied to user " + name);
    }
    private void SendMessageMethod(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage() );
        }
    }
}
