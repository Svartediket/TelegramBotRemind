public class InfoFromJSON {
    private String start = "";
    private String stop = "";
    private String frequencySendMsg = "";
    private String timeSendMsg = "";
    private String userName = "";
    private String textAlarm = "";
    private String addPictures = "";
    private String pictures[];
    private String userBDay = "";
    private String textCongratulation = "";

    public String getTextCongratulation() {
        return textCongratulation;
    }

    public String getTextAlarm() {
        return textAlarm;
    }

    public void setTextAlsrm(String textAlarm) {
        this.textAlarm = textAlarm;
    }

    public void setTextCongratulation(String textCongratulation) {
        this.textCongratulation = textCongratulation;
    }

    public String getStart() {
        return start;
    }

    public String[] getPictures() {
        return pictures;
    }

    public void setPictures(String[] pictures) {
        this.pictures = pictures;
        System.out.println("111 = " + this.pictures[0]);
        System.out.println("222 = " + pictures[0]);
    }

    public String getFrequencySendMsg() {
        return frequencySendMsg;
    }

    public String getTimeSendMsg() {
        return timeSendMsg;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public void setFrequencySendMsg(String frequencySendMsg) {
        this.frequencySendMsg = frequencySendMsg;
    }

    public void setTimeSendMsg(String timeSendMsg) {
        this.timeSendMsg = timeSendMsg;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAddPictures(String addPictures) {
        this.addPictures = addPictures;
    }

    public void setUserBDay(String userBDay) {
        this.userBDay = userBDay;
    }

    public String getUserName() {

        return userName;
    }

    public String getAddPictures() {
        return addPictures;
    }

    public String getUserBDay() {
        return userBDay;
    }

    public String getStop() {
        return stop;
    }
}
