package bridge.mapper;

import bridge.model.WebLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeblogMapper {

    @Insert("insert into weblog(id,host,time,ip,path,header,method,params,data,logid,version) values(#{id},#{host},#{time},#{ip},#{path},#{header},#{method},#{params},#{data},#{logid},#{version})")
    void insert(WebLog webLog);


    @Select("select * from weblog where host like concat('%',#{0},'%') order by time desc")
    List<WebLog> getAll(String host);

    @Delete("delete from weblog where id=#{id}")
    void deleteOneByID(String id);

    @Delete("delete from weblog where logid=#{logid}")
    void deleteAllBylogID(int logid);

    @Select("select * from weblog where id = #{id} limit 1")
    WebLog selectWeblogByID(String id);

    @Select("select * from weblog where host = #{host}")
    List<WebLog> selectWebLogByHost(String host);
}