package com.boot.dao.service.impl;

import com.boot.common.model.Pager;
import com.boot.dao.mapper.UserMapper;
import com.boot.dao.mapper.custom.UserCustomMapper;
import com.boot.dao.model.User;
import com.boot.dao.model.UserExample;
import com.boot.dao.service.UserService;
import com.boot.dao.service.model.UserBO;
import com.boot.dao.service.model.UserQueryBO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserCustomMapper userCustomMapper;

    @Override
    public void saveOrUpdate(User user) {
        long now = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(now);
        if (user.getId() == null) {
            user.setCreatedAt(timestamp);
            user.setUpdatedAt(timestamp);
            userMapper.insert(user);
        } else {
            user.setUpdatedAt(timestamp);
            userMapper.updateByPrimaryKeySelective(user);
        }
    }

    @Override
    public Pager<User> getBy(UserQueryBO condition) {
        Assert.notNull(condition, "condition 不能为 null");

        Pager<User> pager = new Pager<>();
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        if (condition.getName() != null) {
            criteria.andNameLike(condition.getName() + "%");
        }

        if (condition.getEmail() != null) {
            criteria.andEmailLike(condition.getEmail() + "%");
        }

        if (condition.getPageNo() != null && condition.getPageSize() != null) {
            int offset = (condition.getPageNo() - 1) * condition.getPageSize();
            userExample.setOffset(offset);
            userExample.setLimit(condition.getPageSize());
        }

        userExample.setOrderByClause("id desc");

        List<User> users = userMapper.selectByExample(userExample);
        int total = userMapper.countByExample(userExample);

        pager.setData(users);
        pager.setPageNo(condition.getPageNo());
        pager.setPageSize(condition.getPageSize());
        pager.setTotalCount(total);

        return pager;
    }

    @Override
    public boolean saveAll(List<UserBO> users) {
        if (CollectionUtils.isEmpty(users)) {
            return false;
        }

        List<User> userList = new ArrayList<>(users.size());
        Date now = new Date();

        for (UserBO item : users) {
            User user = new User();
            user.setName(item.getName());
            user.setEmail(item.getEmail());
            user.setPhone(item.getPhone());
            user.setStatus(item.getStatus());
            user.setCreatedAt(now);
            user.setUpdatedAt(now);

            userList.add(user);
        }
        return userCustomMapper.saveAll(userList) > 0;
    }

    @Override
    public int insertAll(List<UserBO> users) {
        if (CollectionUtils.isEmpty(users)) {
            return 0;
        }

        List<User> userList = new ArrayList<>(users.size());
        Date now = new Date();

        for (UserBO item : users) {
            User user = new User();
            user.setName(item.getName());
            user.setEmail(item.getEmail());
            user.setPhone(item.getPhone());
            user.setStatus(item.getStatus());
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            user.setType(item.getType());

            userList.add(user);
        }

        // return userMapper.batchInsert(userList);
        return userMapper.batchInsertSelective(userList,
                User.COLUMNS.NAME.getColumn(), User.COLUMNS.EMAIL.getColumn(),
                User.COLUMNS.EMAIL.getColumn(), User.COLUMNS.STATUS.getColumn());
        // return userCustomMapper.insertAll(userList);
    }

    @Override
    public int insertOrUpdateOnDuplicatePhone(User user) {
        if (user == null) {
            return 0;
        }

        return userMapper.insertOrUpdateSelectiveOnDuplicateKey(user,
                User.COLUMNS.NAME.getColumn(), User.COLUMNS.EMAIL.getColumn(), User.COLUMNS.STATUS.getColumn());
    }

    @Override
    public int batchInsertOrUpdateSelectedColumnsOnDupKey(List<User> users, List<String> insertionColumns, List<String> updateColumns) {
        if (CollectionUtils.isEmpty(users)) {
            return 0;
        }

        Date now = new Date();
        users.forEach(u -> u.setUpdatedAt(now));
        return userMapper.batchInsertOrUpdateSelectedColumnsOnDuplicateKey(users, insertionColumns, updateColumns);
    }

}
