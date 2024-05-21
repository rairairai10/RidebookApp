package models;

public class ModelRides {

    String pId, place, date, time, destination, CC, meeting_place,uid,uName,pTime,uEmail,uDp;

    public ModelRides() {
    }

    public ModelRides(String pId, String place, String date, String time, String destination, String CC, String meeting_place, String uid, String uName, String pTime, String uEmail, String uDp) {
        this.pId = pId;
        this.place = place;
        this.date = date;
        this.time = time;
        this.destination = destination;
        this.CC = CC;
        this.meeting_place = meeting_place;
        this.uid = uid;
        this.uName = uName;
        this.pTime = pTime;
        this.uEmail = uEmail;
        this.uDp = uDp;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCC() {
        return CC;
    }

    public void setCC(String CC) {
        this.CC = CC;
    }

    public String getMeeting_place() {
        return meeting_place;
    }

    public void setMeeting_place(String meeting_place) {
        this.meeting_place = meeting_place;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }
}
