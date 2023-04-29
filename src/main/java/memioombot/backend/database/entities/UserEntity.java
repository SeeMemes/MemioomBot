package memioombot.backend.database.entities;

import memioombot.backend.database.ydbdriver.annotations.YdbEntity;
import memioombot.backend.database.ydbdriver.annotations.YdbPrimaryKey;
import net.dv8tion.jda.api.entities.User;

@YdbEntity
public class UserEntity {
    @YdbPrimaryKey
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

    public UserEntity(Long uId, String uName, int uDiscriminator) {
        this.uId = uId;
        this.uName = uName;
        this.uDiscriminator = uDiscriminator;
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