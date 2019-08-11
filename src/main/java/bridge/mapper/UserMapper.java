package bridge.mapper;

import bridge.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {
    @Insert("insert into user (userid,username,password,logid,apikey) values(#{userid},#{username},#{password},#{logid},#{apiKey})")
    void insert(User user);

    @Select("select userid,username,password,logid,apiKey from user where username = #{username}")
    User getUserByUsername(String username);

    @Select("select userid,username,logid,apiKey from user where apiKey = #{token}")
    User getUserByToken(String token);

    @Select("select logid from user order by logid desc limit 1")
    Integer getLastLogID();

}
