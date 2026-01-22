package com.learn.springailearn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learn.springailearn.domain.ChatRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话记录Mapper接口
 * @author ken
 * @date 2026-01-21
 */
@Mapper
public interface ChatRecordMapper extends BaseMapper<ChatRecord> {
    // MyBatisPlus BaseMapper已封装CRUD，无需额外编写基础方法
}