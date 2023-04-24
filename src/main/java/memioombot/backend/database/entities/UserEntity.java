package memioombot.backend.database.entities;

import net.dv8tion.jda.api.entities.User;

public class UserEntity {
    private Long uId;
    String uName;
    Integer uDiscriminator;

    public UserEntity() {
    }

    public UserEntity(User user) {
        uId = user.getIdLong();
        uName = user.getName();
        uDiscriminator = Integer.parseInt(user.getDiscriminator());
    }

    public Long getUId() {
        return uId;
    }

    public void setuInfo(User user) {
        uName = user.getName();
        uDiscriminator = Integer.parseInt(user.getDiscriminator());
    }

    public String getuName() {
        return uName;
    }

    public Integer getuDiscriminator() {
        return uDiscriminator;
    }
}