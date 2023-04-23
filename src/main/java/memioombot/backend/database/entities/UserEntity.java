package memioombot.backend.database.entities;

import net.dv8tion.jda.api.entities.User;

public class UserEntity {
    private Long id;
    String uName;
    String uDiscriminator;

    public UserEntity() {
    }

    public UserEntity(User user){
        uName = user.getName();
        uDiscriminator = user.getDiscriminator();
    }

    public Long getId() {
        return id;
    }

    public void setuInfo(User user) {
        uName = user.getName();
        uDiscriminator = user.getDiscriminator();
    }

    public void getuInfo(User user) {
        uName = user.getName();
        uDiscriminator = user.getDiscriminator();
    }

    public String getuName() {
        return uName;
    }

    public String getuDiscriminator() {
        return uDiscriminator;
    }
}