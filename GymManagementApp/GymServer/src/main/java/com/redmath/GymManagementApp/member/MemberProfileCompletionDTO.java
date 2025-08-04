package com.redmath.GymManagementApp.member;
public class MemberProfileCompletionDTO {
    private String phoneNumber;
    private Long trainerid;
    private String gender;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getTrainerid() {
        return trainerid;
    }


    public void setTrainerid(Long trainerid) {
        this.trainerid = trainerid;
    }
}