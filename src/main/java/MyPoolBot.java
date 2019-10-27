import org.json.JSONObject;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyPoolBot extends TelegramLongPollingBot{

    long chat_id = 0;
    boolean start = false;
    int kurs = 0;
    public int yearEnd = 0;
    int yearsStady = 0;
    Calendar c = Calendar.getInstance();
    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    Pattern pKurs = Pattern.compile("([1-6])");
    Pattern pAcademicDegree = Pattern.compile("([Бб]{1}акалавр((иат)|()))|([Сс]пециали((ст)|(тет))|([Мм]агистр((атура)|())))");
    boolean degreeCheck = false;
    InfoFromJSON infoFromJSON = new InfoFromJSON();

    public MyPoolBot(DefaultBotOptions options) {
        super(options);
    }

    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();
        String messageOfUser = "";
        ArrayList<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirst = new KeyboardRow();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false); //true -скрыть клавиатуру после нажатия

        if (message != null && message.hasText()) {
            messageOfUser = message.getText();
            chat_id = update.getMessage().getChatId(); ///
            //msg_id = update.getMessage().getMessageId();
            Matcher mAcademicDegree = pAcademicDegree.matcher(messageOfUser);
            Matcher mKurs = pKurs.matcher(messageOfUser);

            if (start && !degreeCheck && mAcademicDegree.find() && !mKurs.find()) { //ожидается ввод степени
                System.out.println("0");
                if(mAcademicDegree.group().toLowerCase().contains("бакалавр")) {
                    yearsStady = 4;
                    setKeyboards(keyboard, keyboardFirst, "1", "2", "3", "4");
                    sendMsg(message, "Вау! Бакалавр? -молодец!" +
                            "\nА какой курс?");
                }
                else if(mAcademicDegree.group().toLowerCase().contains("магистр")) {
                    yearsStady = 2;
                    setKeyboards(keyboard, keyboardFirst, "1", "2");
                    sendMsg(message, "Ух, ты! Уже магистр? -это круто!" +
                            "\nА какой курс 1 или 2?");
                }
                else{
                    yearsStady = 6;
                    setKeyboards(keyboard, keyboardFirst, "1", "2", "3", "4", "5", "6");
                    sendMsg(message, "Неплохо-неплохо! 6 лет на одну специальность? -мощьно!" +
                            "\nА какой курс?");
                }
                degreeCheck = true;
            }

            else if(start && !degreeCheck && !mAcademicDegree.find()
                    && !message.getText().equals("/stop") && !message.getText().equals("/start")) {
                sendMsg(message, "Может все-таки ответишь?..");
            }

            else if(start && degreeCheck && !mAcademicDegree.find() && pKurs.matcher(messageOfUser).find()) { //ожидается ввод курса
                kurs = Integer.parseInt(messageOfUser);
                calculateYearEnd();

                System.out.println("yearEnd = " + yearEnd);

                try {
                    counterDays(message);
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }

            else { //прежде всего необходимо запустить бота, а потом с ним общаться
                switch (messageOfUser) {
                    case ("/start"): {
                        try {
                            parseJSON();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setKeyboards(keyboard, keyboardFirst, "Бакалавриат", "Магистратура", "Специалитет");
                        sendMsg(message, "Привет, " + infoFromJSON.getUserName() + "!" + infoFromJSON.getStart().toString());
                        //sendMsg(message, Long.toString(chat_id));
                        start = true;
                    }
                    break;
                    case ("/stop"): {
                        if (start) {
                            start = false;
                            degreeCheck = false;
                            sendMsg(message, "Ну и что это такое? А кому же я буду напоминать напоминать про дипломчик?" +
                                    "А чьи нервишки я буду щекотать каждый день? Диплом сам себя не напишет," +
                                    "а часики то тикают...");
                        } else {
                            sendMsg(message, infoFromJSON.getStop().toString());
                        }
                    }
                }
            }
        }
    }

    private void counterDays(Message message) throws ParseException, InterruptedException, FileNotFoundException, TelegramApiException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String dateDiplom = "30.06." + yearEnd;
        System.out.println("dateDiplom = " + dateDiplom);
        Date date2 = dateFormat.parse(dateDiplom);
        Date date1;
        int daysD = 1;
        int oneMin = 60 * 1000;
        int oneHour = 60 * oneMin;
        int oneDay  = 24 * oneHour;
        boolean check = true;
//        String time[] = infoFromJSON.getTimeSendMsg().split(":");
//        int min = Integer.parseInt(time[1]) * oneMin;
//        int hour = Integer.parseInt(time[0]) * oneHour;
        int contFoto = kurs - 1;
        int alarmClock[] = setFrequency(oneHour, oneMin);

        while(daysD != 0) {
            getCongratulation(message);
            date1 = dateFormat.parse(dateFormat.format(new Date()));
            daysD = (int) ((date2.getTime() - date1.getTime()) / oneDay);
            System.out.print(daysD + " дня до диплома");
            if(check){
                sendMsg(message, "Напоминания установлены!");
                System.out.println ( "Текущий день: " + c.get(Calendar.DAY_OF_WEEK));
                if (infoFromJSON.getAddPictures().equals("true")) {
                    if (yearsStady == 2 && kurs == 2 ) {
                        contFoto = 5;
                    }
                    if (yearsStady == 4 && kurs == 4 )
                        contFoto = 5;
                    sendImg(message, infoFromJSON.getPictures()[contFoto++]);
                }
                sendMsg(message, infoFromJSON.getTextAlarm() + Integer.toString(daysD));
                Thread.sleep(alarmClock[0]); //сколько осталось до завтра плюс то, во сколько надо "прозвонить"
                //Thread.sleep(3000);//
                check = false;
            } else{
                if(infoFromJSON.getAddPictures().equals("true") && contFoto < 6 &&
                        (c.get(Calendar.MONTH) == 8 ) && c.get(Calendar.DAY_OF_MONTH)-1 == 1) { //фото обновляется в начале года
                    if (yearsStady == 2 ) {
                        contFoto = 5;
                    }
                    if (yearsStady == 4 && c.get(Calendar.YEAR) == yearEnd )
                        contFoto = 5;
                    sendImg(message, infoFromJSON.getPictures()[contFoto++]);

                }
                sendMsg(message, infoFromJSON.getTextAlarm() + Integer.toString(daysD));
                //Thread.sleep(1000);
                Thread.sleep(alarmClock[1]); // от установленного времени отсчитывается 1 день или 1 неделя. Время установилось в предыдущей итерации
            }
        }
    }

    private void getCongratulation(Message message) throws ParseException {
        SimpleDateFormat dateFormatDM = new SimpleDateFormat("dd.MM");
        Date currentDate = new Date();
        Date curDay = dateFormatDM.parse(dateFormatDM.format( currentDate ));
        Date userBDay = dateFormatDM.parse(infoFromJSON.getUserBDay());
        System.out.println("curDay: " + curDay );
        System.out.println("userBDay: " + userBDay);
        if(curDay.getTime() == userBDay.getTime()){
            System.out.println("Happy B-Day");
            sendMsg(message, "C днем Рождения! Диплом то начал писать или все еще тянешь кота за я... " +
                    "кхым-кхым ХВОСТ? Пора бы приступить! Ну и желаю тебе то, что ты сам себе в конфиге написал!\n\n"
            + infoFromJSON.getTextCongratulation());
        }else{
            System.out.println("NO B-Day");
        }
    }

    private int getTimeAlarm(int oneHour, int oneMin) { //узнаем время звонка из конфига

        String time[] = infoFromJSON.getTimeSendMsg().split(":");
        int min = Integer.parseInt(time[1]) * oneMin;
        int hour = Integer.parseInt(time[0]) * oneHour;
        System.out.println(hour+":"+min);
        return hour + min;
    }

    private int getHowManyTomorrowAlarm() { //вычисляем, сколько осталось до начала следующего дня
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long howManyTomorrow = (c.getTimeInMillis()-System.currentTimeMillis());
        return (int) (howManyTomorrow);
    }

    private int getDistanceDays() { //ПРОВЕРИТЬ!!!! ФОРМУЛА НЕ ВЕРНА //формула уже вроде верна //вычисляем количество дней до Пн, не считая сегодня
        int oneMin = 60 * 1000;
        int oneHour = 60 * oneMin;
        int oneDay  = 24 * oneHour;
        int distanceDays = 0;
        int dayToday = (c.get(Calendar.DAY_OF_WEEK)); //сб = 1, вс = 2, ... пт = 7
        if(dayToday < 3) {
            distanceDays = 3 - 1 - dayToday;
        }else {
            distanceDays = 7 - dayToday + 2;
        }
        System.out.println("dayToday = " + dayToday);
        System.out.println("distanceDays! = " + distanceDays);
        return distanceDays * oneDay;
    }

    private int[] setFrequency(int oneHour, int oneMin) { //получае из конфига частоту напоминания, в зависимости от чего выставляем "будильник"
        int oneDay  = 24 * oneHour;
        int oneWeek  = 7 * oneDay;
        String  strFrequency = infoFromJSON.getFrequencySendMsg();
        int howManyTomorrowAlarm = getHowManyTomorrowAlarm();
        int timeAlarm = getTimeAlarm(oneHour, oneMin);
        System.out.println("timeAlarm = " + timeAlarm);
        int distanceDays = 0;
        int alarmClock[] = new int[2];
        System.out.println("howManyTomorrowAlarm = " + howManyTomorrowAlarm);
        switch (strFrequency) {
            case("every day"): {
                alarmClock[0] = howManyTomorrowAlarm + timeAlarm; //сколько осталось до завтра плюс то, во сколько надо "прозвонить"
                System.out.println("alarmClock = " + alarmClock);
                alarmClock[1] = oneDay; //слип(2) -засыпает на день => "будильник" будет звонить каждый день
                return alarmClock;
            }
            case ("every week"): {
                distanceDays = getDistanceDays();
                System.out.println("distanceDays = " + distanceDays);
                alarmClock[0] = howManyTomorrowAlarm + timeAlarm + distanceDays; //сколько осталось до пн плюс то, во сколько надо "прозвонить"
                System.out.println("alarmClock = " + alarmClock);
                alarmClock[1] = oneWeek; //слип(2) -засыпает на день => "будильник" будет звонить каждую неделю
                return alarmClock;
            }
            default:
                return alarmClock;
        }
    }

    public void parseJSON() throws IOException { //парсим заполненный конфиг
        FileInputStream fileIS = new FileInputStream(System.getProperty("user.home") + "\\"+"Desktop" + "\\forTelegaText.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileIS, "UTF8"));

        String json = "";
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line.length() + "\t" + line);
            //json += line.replace("\n", "").replace("\t", " ");
            json += line;
        }
        JSONObject obj = null;
        try {
            obj = new JSONObject(json);
        } catch (Exception e) { //в начале файла пишется кодировка (первые 3 бита), которые содержатся в сharAt(0), их необходимо удалить.
            StringBuilder sb = new StringBuilder(json);
            json = sb.deleteCharAt(0).toString();
            obj = new JSONObject(json);
        }

        JSONObject comandText = obj.getJSONObject("comandText");
        JSONObject sendMsgInfo = obj.getJSONObject("sendMsgInfo");
        JSONObject pictures = sendMsgInfo.getJSONObject("pictures");
        JSONObject bDay = obj.getJSONObject("bDay");
        JSONObject userBDayText = bDay.getJSONObject("userBDayText");

        infoFromJSON.setStart(comandText.getString("start"));
        infoFromJSON.setStop(comandText.getString("stop"));

        infoFromJSON.setFrequencySendMsg(sendMsgInfo.getString("frequencySendMsg")); //true || false
        infoFromJSON.setTimeSendMsg(sendMsgInfo.getString("timeSendMsg")); // время в 24 часовом формате
        infoFromJSON.setUserName(sendMsgInfo.getString("userName"));  //??? для чего
        infoFromJSON.setTextAlsrm(sendMsgInfo.getString("textAlarm"));
        infoFromJSON.setAddPictures(sendMsgInfo.getString("addPictures")); // true || false

        String picture = "Picture";
        String pictureN = "";
        String pictureArr[] = new String[6];
        if(infoFromJSON.getAddPictures().equals("true")){
            for (int i = 0; i < 6; i++) {
                pictureN = picture + Integer.toString(i);
                System.out.println("pictureN = " + pictureN);
                pictureArr[i] = pictures.getString(pictureN);
            }
            System.out.println("!!!! pictureArr = " + pictureArr[0]);
            infoFromJSON.setPictures(pictureArr);
            System.out.println("infoFromJSON.getPictures() = " + infoFromJSON.getPictures().length);
        }

        infoFromJSON.setUserBDay(bDay.getString("userBDay")); // true || false
        if(!infoFromJSON.getUserBDay().equals("")) {
            infoFromJSON.setTextCongratulation(userBDayText.getString("Text1"));
        }
    }

    private void calculateYearEnd() {
        int yearNow = c.get(Calendar.YEAR);
        int monthNow = c.get(Calendar.MONTH);
        if(monthNow >= 7) { //7th month corresponds to August (0-11)
            yearEnd = yearNow + (yearsStady - kurs + 1);
        } else {
            yearEnd = yearNow + (yearsStady - kurs);
        }
        System.out.println("yearEnd = " + yearEnd);
    }

    private void setKeyboards(ArrayList<KeyboardRow> keyboard, KeyboardRow keyboardFirst, String... keys) {
            keyboard.clear();
            keyboardFirst.clear();
            addKeyboard(keyboardFirst, keys);
            keyboard.add(keyboardFirst);
            replyKeyboardMarkup.setKeyboard(keyboard);
    }

    private void addKeyboard(KeyboardRow keyboardFirst, String[] keys) {
        for (String key: keys) {
            keyboardFirst.add(key);
        }
    }

    private void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(message.getChatId().toString());
        //sendMessage.setReplyToMessageId(message.getMessageId()); //пересыл введенного сообщения
        sendMessage.setText(text);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendImg(Message message, String image) throws TelegramApiException, FileNotFoundException {
        SendPhoto sendPhoto = new SendPhoto().setPhoto("SomeText", new FileInputStream(new File(image)));
        sendPhoto.setChatId(message.getChatId().toString());
        this.execute(sendPhoto);
    }

//    @Override
//    public String getBotUsername() {
//        return "Listner";
//    }
//
//    public String getBotToken() {
//        return "706260648:AAGdBI9fDMZgPnPbrVCj4O_mboxlkQmTn2k";
//    }

    @Override
    public String getBotUsername() {
        return "Remider Bot";
    }

    public String getBotToken() {
        return "727177083:AAGI45uCE_BS7FpODlxrSoEI7WrPTQnRAkQ";
    }

}
