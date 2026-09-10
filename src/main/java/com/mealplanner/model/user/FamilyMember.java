package com.mealplanner.model.user;

/**
 * Family member extending UserProfile with additional member name field
 */
public class FamilyMember extends UserProfile {
    private String memberName;

    // Default constructor
    public FamilyMember() {
        super();
    }

    // Constructor with memberName
    public FamilyMember(String memberName) {
        super();
        this.memberName = memberName;
    }

    // Constructor with UserProfile and memberName
    public FamilyMember(UserProfile userProfile, String memberName) {
        super(userProfile.getUserId(), userProfile.getName(), userProfile.getAge(),
                userProfile.getHeightCm(), userProfile.getWeightKg(), userProfile.getGender(),
                userProfile.getPrimaryHealthGoal(), userProfile.getAllergies(),
                userProfile.getMealPreferences(), userProfile.getMedicalConditions(),
                userProfile.getActivityLevel());
        this.memberName = memberName;
    }

    // Getters and Setters
    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    @Override
    public String toString() {
        return "FamilyMember{" +
                "memberName='" + memberName + '\'' +
                ", " + super.toString() +
                '}';
    }
}
