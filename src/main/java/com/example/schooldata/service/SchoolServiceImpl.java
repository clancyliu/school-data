package com.example.schooldata.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.schooldata.entity.School;
import com.example.schooldata.mapper.SchoolMapper;
import org.springframework.stereotype.Service;

/**
 * @author liugang
 * @date 2020/4/23 17:55
 */
@Service
public class SchoolServiceImpl extends ServiceImpl<SchoolMapper, School> implements SchoolService {
}
