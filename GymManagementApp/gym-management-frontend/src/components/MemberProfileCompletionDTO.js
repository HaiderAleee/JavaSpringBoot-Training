// DTO class for profile completion (for reference)
export class MemberProfileCompletionDTO {
  constructor(phoneNumber = null, trainerId = null, gender = null) {
    this.phoneNumber = phoneNumber
    this.trainerId = trainerId
    this.gender = gender
  }
}
