/*package org.backend.database;

import net.dv8tion.jda.api.entities.User;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
//@Table(name = "blacklistUsers")
public class UserEntity {
    @Id
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
}
*/