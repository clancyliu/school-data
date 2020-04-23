package com.example.schooldata.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 解析areas.json
 *
 * @author liugang
 * @date 2020/4/23 18:04
 */
@Data
public class Area implements Serializable {
    private static final long serialVersionUID = 7584046192651542027L;
    private String code;

    private String name;

    private String cityCode;

    private String provinceCode;
}
