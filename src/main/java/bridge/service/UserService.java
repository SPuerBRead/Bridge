package bridge.service;

import bridge.mapper.UserMapper;
import bridge.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Repository
public class UserService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在！");
        }
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
        simpleGrantedAuthorities.add(new SimpleGrantedAuthority("USER"));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), simpleGrantedAuthorities);
    }

    public void insertNewUser(User user) {
        User existUser = userMapper.getUserByUsername(user.getUsername());
        if (existUser == null) {
            encryptPassword(user);
            creatLogID(user);
            createUserID(user);
            createapiKey(user);
            userMapper.insert(user);
        } else {
            throw new RuntimeException("用户名已存在！");
        }
    }

    private void encryptPassword(User user) {
        String password = user.getPassword();
        user.setPassword(new BCryptPasswordEncoder().encode(password));
    }

    private void creatLogID(User user) {
        Integer logID = userMapper.getLastLogID();
        if (logID == null) {
            user.setLogid(1);
        } else {
            user.setLogid(logID + 1);
        }
    }

    private void createUserID(User user) {
        String userID = UUID.randomUUID().toString();
        user.setUserid(userID);
    }

    private void createapiKey(User user) {
        String apiKey = UUID.randomUUID().toString().replace("-", "");
        user.setApiKey(apiKey);
    }

    public int getLogIdByName(String username) {
        return userMapper.getUserByUsername(username).getLogid();
    }

    public String getapiKeyByName(String username) {
        return userMapper.getUserByUsername(username).getApiKey();
    }

    public User getUserByApiKey(String apiKey){
        return userMapper.getUserByToken(apiKey);
    }
}
