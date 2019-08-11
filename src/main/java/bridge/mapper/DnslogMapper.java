package bridge.mapper;

import bridge.model.DnsLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DnslogMapper {

    @Insert("insert into dnslog(id,host,time,ip,type,logid) values(#{id},#{host},#{time},#{ip},#{type},#{logid})")
    void insert(DnsLog dnslog);

    @Select("select * from dnslog where host like concat('%',#{0},'%') order by time desc")
    List<DnsLog> getAll(String host);

    @Delete("delete from dnslog where id=#{id}")
    void deleteOneByID(String id);

    @Delete("delete from dnslog where logid=#{logid}")
    void deleteAllBylogID(int logid);

    @Select("select * from dnslog where id = #{id} limit 1")
    DnsLog selectDnslogByID(String id);

    @Select("select * from dnslog where host = #{host}")
    List<DnsLog> selectDnsLogByHost(String host);
}
