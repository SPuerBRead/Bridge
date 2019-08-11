package bridge.mapper;


import bridge.model.Response;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseMapper {

    @Insert("insert into response(id,subDomain,responseType,statusCode,responseBody,headers,redirectURL,logid,time) values(#{id},#{subDomain},#{responseType},#{statusCode},#{responseBody},#{headers},#{redirectURL},#{logid},#{time})")
    void insert(Response response);

    @Select("select * from response where subDomain = #{subDomain} limit 1")
    Response selectResponseBySubdomain(String subDomain);

    @Select("select * from response where id = #{id} limit 1")
    Response selectResponseByID(String id);

    @Select("select * from response where subDomain like concat('%',#{0},'%') order by time desc")
    List<Response> getAll(String subDomain);

    @Update("update response set subDomain=#{subDomain}, responseType=#{responseType},statusCode=#{statusCode},responseBody=#{responseBody},headers=#{headers},time=#{time}, redirectURL=#{redirectURL} where id=#{id}")
    void updateByID(Response response);

    @Delete("delete from response where id=#{id}")
    void deleteOneByID(String id);

    @Delete("delete from response where logid=#{logid}")
    void deleteAllBylogID(int logid);

}
